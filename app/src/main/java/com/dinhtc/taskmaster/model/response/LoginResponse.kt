package com.dinhtc.taskmaster.model.response

data class LoginResponse(
    val tokenAuth: String,
    val tokenFirebase: String? = "",
    val roleCode: String? = ""
)