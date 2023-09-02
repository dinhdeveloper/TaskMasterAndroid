package com.dinhtc.taskmaster.model

import java.io.Serializable

data class ReceiverNotiData(
    var notificationType: String,
    var notificationData: String
) : Serializable
