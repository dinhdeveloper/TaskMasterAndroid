package com.elogictics.taskmaster.service

import com.elogictics.taskmaster.utils.SharedPreferencesManager
import okhttp3.Interceptor
import okhttp3.Response

class GeneralHeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val newBuilder = originalRequest.newBuilder()

        val authToken = SharedPreferencesManager.instance.getString(SharedPreferencesManager.TOKEN_LOGIN, "") ?: ""
        val deviceId = SharedPreferencesManager.instance.getString(SharedPreferencesManager.DEVICE_ID, "") ?: ""

        authToken.let { token ->
            newBuilder.addHeader("Authorization", "Bearer $token")
        }

        deviceId.let { id ->
            newBuilder.addHeader("Device-ID", id)
        }

        val newRequest = newBuilder.build()

        return chain.proceed(newRequest)
    }
}
