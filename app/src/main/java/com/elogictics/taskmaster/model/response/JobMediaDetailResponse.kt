package com.elogictics.taskmaster.model.response

import java.io.Serializable

data class JobMediaDetailResponse(
    val jobId: Int,
    val mediaId: Int,
    val mediaType: Int,
    val url: String,
    val urlHard: String
) : Serializable