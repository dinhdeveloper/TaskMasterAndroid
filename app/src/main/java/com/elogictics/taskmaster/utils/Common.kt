package com.elogictics.taskmaster.utils

import android.graphics.Matrix
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun <T> convertJsonToObject(responseJson: ApiResponse<Any>, clazz: Class<T>): ApiResponse<T> {
    val gson = Gson()
    val dataJson = gson.toJson(responseJson)
    val type = TypeToken.getParameterized(ApiResponse::class.java, clazz).type
    val response: ApiResponse<T> = gson.fromJson(dataJson, type)
    return response
}
fun decodeExifOrientation(exifOrientation: Int): Matrix {
    val matrix = Matrix()

    // Apply transformation corresponding to declared EXIF orientation
    when (exifOrientation) {
        ExifInterface.ORIENTATION_NORMAL -> Unit
        ExifInterface.ORIENTATION_UNDEFINED -> Unit
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90F)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180F)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270F)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1F, 1F)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1F, -1F)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.postScale(-1F, 1F)
            matrix.postRotate(270F)
        }
        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.postScale(-1F, 1F)
            matrix.postRotate(90F)
        }

        // Error out if the EXIF orientation is invalid
        else -> Log.e("TAG", "Invalid orientation: $exifOrientation")
    }

    // Return the resulting matrix
    return matrix
}