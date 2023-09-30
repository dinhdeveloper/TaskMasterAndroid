package com.elogictics.taskmaster.model.response

import java.io.Serializable

data class SearchResponse(
    val jobId: Int,
    val collectPoint: String,
    val emp: String,
    val status: String,
    val priority: Int,
    val date: String,
    val temp: String,
    val empId: Int
): Serializable
