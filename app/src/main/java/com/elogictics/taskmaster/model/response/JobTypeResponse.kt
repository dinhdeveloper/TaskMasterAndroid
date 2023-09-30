package com.elogictics.taskmaster.model.response

data class JobTypeResponse(
    val jobTypeDesc: String,
    val jobTypeId: Int,
    val jobTypeName: String,
    val state: Boolean
)