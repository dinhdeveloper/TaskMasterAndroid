package com.dinhtc.taskmaster.model.request

data class UpdateStateRequest (
    var jobsId : Int,
    var stateJob : Int,
    var paymentMethod : Int,
    var paymentStateStatus : Int,
    var amountPaidEmp : Long,
    var amountTotal : Long,
    var dateCreate : String
)