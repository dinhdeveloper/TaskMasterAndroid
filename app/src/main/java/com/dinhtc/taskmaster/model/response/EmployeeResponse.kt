package com.dinhtc.taskmaster.model.response

data class EmployeeResponse(
    val age: Int = 0,
    val dist: String = "",
    val empId: Int = 0,
    val gender: String = "",
    val name: String = "",
    val numAddress: String = "",
    val province: String = "",
    val role: String = "",
    val startDate: String = "",
    val state: Boolean = false,
    val streetAddress: String = "",
    val teamId: Int = 0,
    val ward: String = "",
    val phone: String = "",
    val posId: Int = 0,
    val roleId: Int = 0
)