package com.elogictics.taskmaster.model.response

import com.elogictics.taskmaster.model.JobEmployeeDetailResponse

data class JobDetailsResponse(
    val jobId: Int,
    val jobStateId: Int,
    val stateDecs: String,
    val numAddress: String,
    val namePoint: String,
    val priority: Int,
    val noteJob: String,
    val jobStateCode: String,
    val amountPaidEmp: Long,
    val paymentMethod: Int,
    val paymentStateId: Int,
    val jobMedia : List<JobMediaDetailResponse>,
    val jobMaterial : List<JobMaterialDetailResponse>,
    val employeeJobs : List<JobEmployeeDetailResponse>
)