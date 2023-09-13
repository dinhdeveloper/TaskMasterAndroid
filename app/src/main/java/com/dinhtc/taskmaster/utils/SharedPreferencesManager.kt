package com.dinhtc.taskmaster.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SharedPreferencesManager(context: Context) {

    private var sharedPreferences: SharedPreferences

    init {
        // Although you can define your own key generation parameter specification, it's
        // recommended that you use the value specified here.
        val masterKeyAlias = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        sharedPreferences = EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }



    fun putString(key: String, value: String?) = sharedPreferences.edit().putString(key, value).apply()

    fun getString(key: String, defaultValue: String?): String = sharedPreferences.getString(key, defaultValue ?: "") ?: ""

    fun putBoolean(key: String, value: Boolean) = sharedPreferences.edit().putBoolean(key, value).apply()

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean = sharedPreferences.getBoolean(key, defaultValue)

    fun putFloat(key: String, value: Float) = sharedPreferences.edit().putFloat(key, value).apply()

    fun getFloat(key: String, defaultValue: Float = 0f): Float = sharedPreferences.getFloat(key, defaultValue)

    fun putInt(key: String, value: Int) = sharedPreferences.edit().putInt(key, value).apply()

    fun getInt(key: String, defaultValue: Int = 0): Int = sharedPreferences.getInt(key, defaultValue)

    fun putLong(key: String, value: Long) = sharedPreferences.edit().putLong(key, value).apply()

    fun getLong(key: String, defaultValue: Long = 0L): Long = sharedPreferences.getLong(key, defaultValue)

    fun remove(key: String) = sharedPreferences.edit().remove(key).apply()


    companion object {
        const val DEVICE_ID = "DEVICE_ID"
        const val TOKEN_LOGIN = "TOKEN_LOGIN"
        const val ROLE_CODE = "ROLE_CODE"
        const val TOKEN_FIREBASE = "TOKEN_FIREBASE"
        const val USERNAME = "USERNAME"
        const val USER_ID = "USER_ID"
        const val FULL_NAME = "FULL_NAME"

        private const val FILE_NAME = "advsarg"
        const val PASS_W = "PASS_W"
        const val IS_LOGGED_IN = "IS_LOGGED_IN"
        const val LAST_LOGIN_TINE = "LAST_LOGIN_TINE"

        private var _instance: SharedPreferencesManager? = null

        fun init(context: Context) {
            _instance = SharedPreferencesManager(context)
        }

        @JvmStatic val instance: SharedPreferencesManager
            get() {
                if (_instance == null) throw RuntimeException("Stub! You must call SecurePreferenceManager.init(applicationContext) ")
                return _instance!!
            }
    }
}