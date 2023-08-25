package com.dinhtc.taskmaster.model.response

data class EmployeeResponse(
    val age: Int,
    val dist: String,
    val empId: Int,
    val gender: String,
    val name: String,
    val numAddress: String,
    val province: String,
    val role: String,
    val startDate: String,
    val state: Boolean,
    val streetAddress: String,
    val teamId: Int,
    val ward: String
)