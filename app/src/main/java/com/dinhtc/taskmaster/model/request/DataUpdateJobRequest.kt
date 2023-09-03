package com.dinhtc.taskmaster.model.request

data class DataUpdateJobRequest(
    private var jodId: Int?,
    private var totalMoney: String?,
    private var statusPayment: Int,
    private var amountPaidEmp: String?,
    private var priority: Int,
    private var empOldId: Int,
    private var empNewId: Int,
    private var note: String,
)
