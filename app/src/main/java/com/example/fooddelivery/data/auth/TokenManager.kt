package com.example.fooddelivery.data.auth

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context?
) {
    private val prefs: SharedPreferences? = try {
        context?.getSharedPreferences("freshbite_secure_prefs", Context.MODE_PRIVATE)
    } catch (_: Exception) {
        null
    }

    private var inMemoryToken: String? = null

    open fun saveToken(token: String) {
        if (prefs != null) {
            prefs.edit().putString(KEY_JWT_TOKEN, token).apply()
        } else {
            inMemoryToken = token
        }
    }

    open fun getToken(): String? {
        return prefs?.getString(KEY_JWT_TOKEN, null) ?: inMemoryToken
    }

    open fun clearToken() {
        if (prefs != null) {
            prefs.edit().remove(KEY_JWT_TOKEN).apply()
        } else {
            inMemoryToken = null
        }
    }

    companion object {
        private const val KEY_JWT_TOKEN = "jwt_token"
    }
}
