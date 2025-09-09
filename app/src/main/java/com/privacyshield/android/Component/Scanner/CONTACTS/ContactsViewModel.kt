package com.privacyshield.android.Component.Scanner.CONTACTS

import android.content.ContentProviderOperation
import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts

    private val _duplicates = MutableStateFlow<Map<String, List<Contact>>>(emptyMap())
    val duplicates: StateFlow<Map<String, List<Contact>>> = _duplicates

    private val _selected = MutableStateFlow<Set<String>>(emptySet()) // contactId set
    val selected: StateFlow<Set<String>> = _selected

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _sortOrder = MutableStateFlow(SortOrder.ASCENDING)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    init {
        // load after UI grants permission
    }

    fun refreshData() {
        loadContactsAfterPermissionGranted()
    }

    fun loadContactsAfterPermissionGranted() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val list = fetchContacts(context)
            _contacts.value = applyFilters(list)
            _duplicates.value = findDuplicateContacts(list)
            _isLoading.value = false
        }
    }

    // ---------------- SELECTION ----------------
    fun toggleSelect(contactId: String) {
        val current = _selected.value.toMutableSet()
        if (current.contains(contactId)) current.remove(contactId) else current.add(contactId)
        _selected.value = current
    }

    fun selectAllInGroup(phone: String) {
        val group = _duplicates.value[phone] ?: return
        val ids = group.map { it.id }.toSet()
        _selected.value = _selected.value + ids
    }

    fun clearSelection() {
        _selected.value = emptySet()
    }

    // ---------------- DELETE ----------------
    suspend fun deleteSelected(): Pair<Int, Int> = withContext(Dispatchers.IO) {
        var deleted = 0
        var failed = 0
        val resolver = context.contentResolver

        _selected.value.forEach { contactId ->
            try {
                val ops = ArrayList<ContentProviderOperation>()
                val where = "${ContactsContract.RawContacts.CONTACT_ID} = ?"
                val args = arrayOf(contactId)
                ops.add(
                    ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                        .withSelection(where, args)
                        .build()
                )
                resolver.applyBatch(ContactsContract.AUTHORITY, ops)
                deleted++
            } catch (e: Exception) {
                Log.e("ContactsViewModel", "Failed to delete contact $contactId", e)
                failed++
            }
        }

        loadContactsAfterPermissionGranted()
        clearSelection()
        deleted to failed
    }

    // ---------------- SEARCH ----------------
    fun updateSearch(query: String) {
        _searchQuery.value = query
        _contacts.value = applyFilters(fetchContacts(context))
    }

    // ---------------- SORT ----------------
    fun toggleSortOrder() {
        _sortOrder.value = if (_sortOrder.value == SortOrder.ASCENDING) SortOrder.DESCENDING else SortOrder.ASCENDING
        _contacts.value = applyFilters(fetchContacts(context))
    }

    private fun applyFilters(list: List<Contact>): List<Contact> {
        var filtered = list
        // search
        if (_searchQuery.value.isNotBlank()) {
            val q = _searchQuery.value.lowercase()
            filtered = filtered.filter {
                (it.name?.lowercase()?.contains(q) == true) ||
                        it.numbers.any { num -> num.contains(q) }
            }
        }
        // sort
        filtered = if (_sortOrder.value == SortOrder.ASCENDING) {
            filtered.sortedBy { it.name?.lowercase() ?: "" } // 👈 safe fallback
        } else {
            filtered.sortedByDescending { it.name?.lowercase() ?: "" }
        }
        return filtered
    }


    // ---------------- EXPORT CSV ----------------
    suspend fun exportContactsToCSV(file: File): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val writer = file.bufferedWriter()
            writer.write("ID,Name,Numbers\n")
            _contacts.value.forEach { contact ->
                val numbers = contact.numbers.joinToString("|")
                writer.write("${contact.id},${contact.name},$numbers\n")
            }
            writer.close()
            true
        } catch (e: Exception) {
            Log.e("ContactsViewModel", "Export failed", e)
            false
        }
    }

    // ---------------- MERGE DUPLICATES ----------------
    suspend fun mergeDuplicates(): Int = withContext(Dispatchers.IO) {
        var mergedCount = 0
        val resolver = context.contentResolver

        _duplicates.value.forEach { (phone, group) ->
            if (group.size > 1) {
                val primary = group.first()
                val others = group.drop(1)

                others.forEach { dup ->
                    try {
                        val ops = ArrayList<ContentProviderOperation>()
                        val where = "${ContactsContract.RawContacts.CONTACT_ID} = ?"
                        val args = arrayOf(dup.id)
                        ops.add(
                            ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                                .withSelection(where, args)
                                .build()
                        )
                        resolver.applyBatch(ContactsContract.AUTHORITY, ops)
                        mergedCount++
                    } catch (e: Exception) {
                        Log.e("ContactsViewModel", "Failed to merge ${dup.id}", e)
                    }
                }
            }
        }
        loadContactsAfterPermissionGranted()
        mergedCount
    }

    // ---------------- HELPERS ----------------
    private fun fetchContacts(context: Context): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val resolver = context.contentResolver
        val cursor = resolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER),
            null,
            null,
            "${ContactsContract.Contacts.DISPLAY_NAME} ASC"
        )
        cursor?.use { c ->
            while (c.moveToNext()) {
                val id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                val name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                val hasPhone = c.getInt(c.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                val numbers = mutableListOf<String>()
                if (hasPhone > 0) {
                    val phoneCursor = resolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(id),
                        null
                    )
                    phoneCursor?.use { pc ->
                        while (pc.moveToNext()) {
                            val raw = pc.getString(pc.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            numbers.add(normalizePhone(raw))
                        }
                    }
                }
                contacts.add(Contact(id = id, name = name, numbers = numbers))
            }
        }
        return contacts
    }

    private fun normalizePhone(number: String?): String {
        if (number.isNullOrBlank()) return ""
        return number.filter { it.isDigit() || it == '+' }
            .replace("\\s+".toRegex(), "")
    }

    private fun findDuplicateContacts(list: List<Contact>): Map<String, List<Contact>> {
        val byPhone = list.flatMap { contact ->
            contact.numbers.mapNotNull { num -> num.takeIf { it.isNotEmpty() }?.let { num to contact } }
        }.groupBy({ it.first }, { it.second })

        return byPhone.filter { it.value.size > 1 }
    }
}

// ----------- EXTRA ENUM -----------
enum class SortOrder {
    ASCENDING,
    DESCENDING
}
