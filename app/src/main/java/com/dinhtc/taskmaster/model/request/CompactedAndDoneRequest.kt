package com.dinhtc.taskmaster.model.request

data class CompactedAndDoneRequest(
    val jobsId: Int,
    val stateJob: Int,
    val empUpdate: Int,
)
