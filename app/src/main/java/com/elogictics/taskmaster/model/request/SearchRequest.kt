package com.elogictics.taskmaster.model.request

data class SearchRequest(
    var startDate: String? = null,
    var endDate: String? = null,
    var empStatus: Int? = 1,
    var empId: Int? = 0,
    var status: Int? = 1,
    var paymentStatus: Int? = 0,
    var jobId: Int? = 0,
    var empRequest: Int? = 0,
    var collectPoint: String? = null
)
