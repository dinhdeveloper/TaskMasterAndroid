package com.dinhtc.taskmaster.model.request

data class AddMaterialRequest(
    private val mateId: Int,
    private val jobId: Int,
    private val weight: Long,
    private val weightToCus: Long,
    private val price: Long,
)
