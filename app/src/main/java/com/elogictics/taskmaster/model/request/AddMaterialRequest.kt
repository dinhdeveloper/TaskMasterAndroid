package com.elogictics.taskmaster.model.request

data class AddMaterialRequest(
    private val mateId: Int,
    private val jobId: Int,
    private val weight: Double?,
    private val weightToCus: Double?,
    private val price: Double?,
)
