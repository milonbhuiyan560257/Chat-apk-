package com.example.chatapp.data

import android.content.Context

class UserPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun saveLogoUri(uri: String) {
        prefs.edit().putString("logo_uri", uri).apply()
    }

    fun getLogoUri(): String? {
        return prefs.getString("logo_uri", null)
    }
}
