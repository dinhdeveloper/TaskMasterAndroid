package com.dinhtc.taskmaster.model.response

data class JobDetailsResponse(
    val jobId: Int,
    val stateDecs: String,
    val numAddress: String,
    val namePoint: String,
    val priority: Int,
    val noteJob: String,
    val jobMedia : List<JobMediaDetailResponse>,
    val jobMaterial : List<JobMaterialDetailResponse>
)