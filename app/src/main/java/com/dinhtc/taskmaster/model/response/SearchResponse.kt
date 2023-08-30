package com.dinhtc.taskmaster.model.response

data class SearchResponse(
    val jobId: Int,
    val collectPoint: String,
    val emp: String,
    val status: String,
    val priority: Int,
    val date: String,
)
