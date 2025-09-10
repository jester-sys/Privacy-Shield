package com.privacyshield.android.Component.Scanner.CONTACTS

data class Contact(
    val id: String,
    val name: String?,
    val numbers: List<String>,
    val emails: List<String> = emptyList(),
    val lastContactedAt: Long? = null // epoch millis if available
)