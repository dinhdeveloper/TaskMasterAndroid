package com.elogictics.taskmaster.model.response

import java.io.Serializable

data class JobMaterialDetailResponse(
    val jobId: Int,
    val mateId: Int,
    val price: Double,
    val weight: Double,
    val weightToCus: Double,
    val name: String
):Serializable