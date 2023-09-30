package com.elogictics.taskmaster.service

import okhttp3.Interceptor
import okhttp3.Response

class AuthTokenInterceptor(private val authToken: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Thêm authToken vào header nếu nó có giá trị
        val modifiedRequest = if (authToken.isNotEmpty()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $authToken")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(modifiedRequest)
    }
}
