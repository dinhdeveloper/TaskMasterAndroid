package com.dinhtc.taskmaster.common.widgets.spinner

import com.google.gson.annotations.SerializedName
import java.io.Serializable

open class LocationData(
    val id: Int,
    @SerializedName("code")
    private val code_: String?,
    @SerializedName("name")
    private val name_: String?
) : Serializable {
    val code: String
        get() = code_ ?: ""

    val name: String
        get() = name_ ?: ""
}