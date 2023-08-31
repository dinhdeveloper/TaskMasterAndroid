package com.dinhtc.taskmaster.model.request

data class DataUpdateJobRequest(
    private var totalMoney: String?,
    private var statusPayment: Int,
    private var advancePayment: String?,
    private var priority: Int,
    private var empOldId: Int,
    private var empNewId: Int,
    private var note: String,
)
