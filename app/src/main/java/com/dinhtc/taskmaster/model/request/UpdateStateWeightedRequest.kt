package com.dinhtc.taskmaster.model.request

data class UpdateStateWeightedRequest (
    var empUpdate: Int,
    var jodId: Int?,
    var stateJob: Int?,
    var totalMoney: String?,
    var paymentMethod: Int,
    var paymentStateId: Int,
    var amountPaidEmp: Long?,
    var priority: Int,
    var empOldId: Int,
    var empNewId: Int,
    var note: String,
)