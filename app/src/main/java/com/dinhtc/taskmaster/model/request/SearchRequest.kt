package com.dinhtc.taskmaster.model.request

data class SearchRequest(
    var startDate: String? = "",
    var endDate: String? = "",
    var empStatus: Int? = 0,
    var empId: Int? = 0,
    var status: Int? = 0,
    var paymentStatus: Int? = 0,
    var jobId: Int? = 0,
    var collectPoint: String? = null
)
