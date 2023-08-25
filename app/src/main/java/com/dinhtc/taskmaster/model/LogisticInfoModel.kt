package com.dinhtc.taskmaster.model

import java.io.Serializable

data class LogisticInfoModel(
    var id: Int,
    var idOrder: String?,
    var locationOrder: String?,
    var personOrder: String?,
    var statusOrder : String?,
    var prioritizeOrder : String?,
    var checkedOrder : Boolean
) : Serializable