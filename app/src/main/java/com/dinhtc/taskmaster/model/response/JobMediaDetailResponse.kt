package com.dinhtc.taskmaster.model.response

import java.io.Serializable

data class JobMediaDetailResponse(
    val jobId: Int,
    val mediaId: Int,
    val mediaType: Int,
    val url: String
) : Serializable