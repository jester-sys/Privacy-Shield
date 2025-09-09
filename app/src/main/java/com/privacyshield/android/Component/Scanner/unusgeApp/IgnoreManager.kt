package com.privacyshield.android.Component.Scanner.unusgeApp

import android.content.Context

object IgnoreManager {
    private const val PREFS_NAME = "ignored_apps_prefs"
    private const val KEY_IGNORED = "ignored_apps"

    fun addIgnoredApp(context: Context, packageName: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(KEY_IGNORED, mutableSetOf()) ?: mutableSetOf()
        set.add(packageName)
        prefs.edit().putStringSet(KEY_IGNORED, set).apply()
    }

    fun removeIgnoredApp(context: Context, packageName: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(KEY_IGNORED, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        set.remove(packageName)
        prefs.edit().putStringSet(KEY_IGNORED, set).apply()
    }

    fun getIgnoredApps(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_IGNORED, emptySet()) ?: emptySet()
    }
}
