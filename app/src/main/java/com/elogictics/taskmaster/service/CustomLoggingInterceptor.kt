package com.elogictics.taskmaster.service

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer

class CustomLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBody = request.body

        val buffer = Buffer()
        requestBody?.writeTo(buffer)
        val requestContent = buffer.readUtf8()

        val authToken = request.header("Authorization") ?: "No Authorization Header"

        val response = chain.proceed(request)
        val responseBodyString = response.body?.string() ?: ""

        Log.e(
            "API_REQUEST",
            "URL: ${request.url}\n" +
                    "            Method: ${request.method}\n" +
                    "            Headers: ${request.headers}\n" +
                    "            Authorization: $authToken\n" + // Hiển thị Authorization header
                    "            Body: $requestContent"
        )

        Log.d(
            "API_RESPONSE",
            "URL: ${request.url}\n" +
                    "            Method: ${request.method}\n" +
                    "            Headers: ${response.headers}\n" +
                    "            Response Code: ${response.code}\n" +
                    "            Response Body: $responseBodyString"
        )

        // Tạo lại body từ responseBodyString để đảm bảo có thể truy cập nội dung khi cần
        val newResponseBody = responseBodyString.toResponseBody(response.body?.contentType())
        return response.newBuilder().body(newResponseBody).build()
    }
}


