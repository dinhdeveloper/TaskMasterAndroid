package com.dinhtc.taskmaster.model.request

data class CollectPointRequest(
    val namePoint: String,
    val nameAddress: String,
    val nameContact: String = "",
    val phoneContact: String = "",
)
