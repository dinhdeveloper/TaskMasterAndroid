package com.elogictics.taskmaster.model.request

data class LoginRequest(
    val username: String,
    val password: String,
    val deviceId: String,
    val deviceName: String,
)