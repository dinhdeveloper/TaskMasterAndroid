package com.elogictics.taskmaster.service

import okhttp3.Interceptor
import okhttp3.Response

class SpecialHeaderInterceptor(private val specialHeader: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val newRequest = originalRequest.newBuilder()
            .addHeader("Special-Header", specialHeader)
            .build()

        return chain.proceed(newRequest)
    }
}
