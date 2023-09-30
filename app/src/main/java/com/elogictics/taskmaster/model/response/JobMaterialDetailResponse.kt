package com.elogictics.taskmaster.model.response

import java.io.Serializable

data class JobMaterialDetailResponse(
    val jobId: Int,
    val mateId: Int,
    val price: Long,
    val weight: Int,
    val weightToCus: Int,
    val name: String
):Serializable