package com.elogictics.taskmaster.model.response

data class CollectPointLatLng(
    val cpName: String,
    val fullName: String,
    val jobId: Int,
    val jobStateDesc: String,
    val latitude: String,
    val longitude: String
)