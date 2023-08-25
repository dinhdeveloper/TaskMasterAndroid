package com.dinhtc.taskmaster.model.request

data class DeleteMediaRequest(val jobId: Int, val url: String, val mediaType: Int)
