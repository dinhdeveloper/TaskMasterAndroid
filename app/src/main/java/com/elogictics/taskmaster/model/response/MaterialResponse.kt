package com.elogictics.taskmaster.model.response

data class MaterialResponse(
    val mate_id: Int,
    val name: String,
    val note: String,
    val state: Boolean,
    val unitPrice: Int
)