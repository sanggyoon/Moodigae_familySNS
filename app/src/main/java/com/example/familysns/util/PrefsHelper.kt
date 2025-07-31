package com.example.familysns.util

import android.content.Context

object PrefsHelper {
    private const val PREF_NAME = "MyAppPrefs"
    private const val KEY_USER_ID = "userId"
    private const val KEY_FAMILY_ID = "familyId"

    fun saveUserId(context: Context, userId: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_USER_ID, userId)
            .apply()
    }

    fun getUserId(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_USER_ID, null)
    }

    fun saveFamilyId(context: Context, familyId: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FAMILY_ID, familyId)
            .apply()
    }

    fun getFamilyId(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_FAMILY_ID, null)
    }

    fun clearAll(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
