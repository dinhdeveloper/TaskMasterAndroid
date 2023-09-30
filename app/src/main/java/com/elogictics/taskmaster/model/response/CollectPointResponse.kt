package com.elogictics.taskmaster.model.response

data class CollectPointResponse(
    val collectPointId: Int,
    val bankAcct: String,
    val bankAcctName: String,
    val bankAcctNumber: String,
    val contactName: String,
    val customId: Int,
    val dist: String,
    val empId: Int,
    val name: String,
    val numAddress: String,
    val phone: String,
    val posLat: String,
    val posLong: String,
    val refPlace: String,
    val state: Boolean,
    val streetAddress: String,
    val useCusBank: String,
    val ward: String,
    val province: String
)
