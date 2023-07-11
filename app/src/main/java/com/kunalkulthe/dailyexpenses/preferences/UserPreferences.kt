package com.kunalkulthe.dailyexpenses.preferences

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    companion object {
        private const val PREF_USER_ID = "user_id"
        private const val PREF_USER_NAME = "user_name"
        private const val PREF_USER_EMAIL = "user_email"
        private const val PREF_NAME = "MyAppPreferences"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun storeUserId(userId: String) {
        sharedPreferences.edit().putString(PREF_USER_ID, userId).apply()
    }

    fun storeUserName(name: String) {
        sharedPreferences.edit().putString(PREF_USER_NAME, name).apply()
    }

    fun storeUserEmail(email: String) {
        sharedPreferences.edit().putString(PREF_USER_EMAIL, email).apply()
    }

    fun getUserId(): String? {
        return sharedPreferences.getString(PREF_USER_ID, null)
    }

    fun getUserName(): String? {
        return sharedPreferences.getString(PREF_USER_NAME, null)
    }

    fun getUserEmail(): String? {
        return sharedPreferences.getString(PREF_USER_EMAIL, null)
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.contains(PREF_USER_ID)
    }

    fun logout() {
        sharedPreferences.edit().clear().apply()
    }
}
