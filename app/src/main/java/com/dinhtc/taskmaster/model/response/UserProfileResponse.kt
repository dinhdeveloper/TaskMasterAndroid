package com.dinhtc.taskmaster.model.response

import java.io.Serializable

data class UserProfileResponse(
    val age: Double,
    val dist: String? = "",
    val empId: Double,
    val gender: String? = "",
    val leaderId: Double,
    val name: String? = "",
    val numAddress: String? = "",
    val phone: String? = "",
    val province: String? = "",
    val roleCode: String? = "",
    val roleId: Double,
    val roleName: String? = "",
    val streetAddress: String? = "",
    val teamId: Double,
    val teamName: String? = "",
    val territory: String? = "",
    val ward: String? = ""
): Serializable