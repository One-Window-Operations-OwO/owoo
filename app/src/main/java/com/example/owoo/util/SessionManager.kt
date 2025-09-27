package com.example.owoo.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private var prefs: SharedPreferences = context.getSharedPreferences("OwooAppPrefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_COOKIE = "key_cookie"
        const val KEY_USERNAME = "key_username"
        const val KEY_PASSWORD = "key_password"
    }

    fun saveAuthData(cookie: String, username: String, password: String) {
        val editor = prefs.edit()
        editor.putString(KEY_COOKIE, cookie)
        editor.putString(KEY_USERNAME, username)
        editor.putString(KEY_PASSWORD, password)
        editor.apply()
    }

    fun getCookie(): String? {
        return prefs.getString(KEY_COOKIE, null)
    }

    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    fun getPassword(): String? {
        return prefs.getString(KEY_PASSWORD, null)
    }

    fun clearData() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}
