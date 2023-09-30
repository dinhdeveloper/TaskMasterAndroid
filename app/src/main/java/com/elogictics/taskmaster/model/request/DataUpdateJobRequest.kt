package com.elogictics.taskmaster.model.request

data class DataUpdateJobRequest(
    private var jodId: Int?,
    private var totalMoney: String?,
    private var paymentMethod: Int,
    private var paymentStateId: Int,
    private var amountPaidEmp: Long?,
    private var priority: Int,
    private var empOldId: Int,
    private var empNewId: Int,
    private var empAssignId: Int,
    private var note: String,
)
