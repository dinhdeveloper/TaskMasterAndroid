package com.dinhtc.taskmaster.model.request

data class AddTaskRequest(
    val jobType: Int,
    val jobStateId: Int,
    val nv1Id: Int,
    val nv2Id: Int,
    val assignId: Int,
    val listIdPoint: List<Int>,
    val ghiChu: String
)
