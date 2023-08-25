package com.dinhtc.taskmaster.model.request

data class AddTaskRequest(
    val jobType: Int,
    val nv1Id: Int,
    val nv2Id: Int,
    val listIdPoint: List<Int>,
    val ghiChu: String
)
