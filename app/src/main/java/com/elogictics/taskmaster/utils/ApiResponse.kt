package com.elogictics.taskmaster.utils
data class ApiResponse<T>(
    val code_status: Int,
    val data: T,
    val result_code: Int,
    val result_description: String,
    val timestamp: String
)





