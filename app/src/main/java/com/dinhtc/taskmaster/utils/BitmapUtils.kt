package com.dinhtc.taskmaster.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.IOException

object BitmapUtils {

    private const val DEFAULT_WIDTH = 1500
    private const val DEFAULT_HEIGHT = 1500

    fun getBitmapFromCachedFile(file: File, bitmapConfig: Bitmap.Config = Bitmap.Config.RGB_565): Bitmap {
        val opts = BitmapFactory.Options()
        opts.inPreferredConfig = bitmapConfig
        return BitmapFactory.decodeFile(file.absolutePath, opts)
    }

    fun getBitmapFromFile(file: File, bitmapConfig: Bitmap.Config): Bitmap? {
        val opts = BitmapFactory.Options()
        opts.inPreferredConfig = bitmapConfig
        val bitmap: Bitmap? = BitmapFactory.decodeFile(file.absolutePath, opts) ?: return null
        return rotationBitmap(file.absolutePath, bitmap!!)
    }

    fun copyExifData(oldFile: File, newFile: File) {
        val attributes = arrayOf(
            ExifInterface.TAG_DATETIME,
            ExifInterface.TAG_DATETIME_DIGITIZED,
            ExifInterface.TAG_EXPOSURE_TIME,
            ExifInterface.TAG_FLASH,
            ExifInterface.TAG_FOCAL_LENGTH,
            ExifInterface.TAG_GPS_ALTITUDE,
            ExifInterface.TAG_GPS_ALTITUDE_REF,
            ExifInterface.TAG_GPS_DATESTAMP,
            ExifInterface.TAG_GPS_LATITUDE,
            ExifInterface.TAG_GPS_LATITUDE_REF,
            ExifInterface.TAG_GPS_LONGITUDE,
            ExifInterface.TAG_GPS_LONGITUDE_REF,
            ExifInterface.TAG_GPS_PROCESSING_METHOD,
            ExifInterface.TAG_GPS_TIMESTAMP,
            ExifInterface.TAG_MAKE,
            ExifInterface.TAG_MODEL,
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.TAG_SUBSEC_TIME,
            ExifInterface.TAG_WHITE_BALANCE
        )

        val oldExif = ExifInterface(oldFile.absolutePath)
        val newExif = ExifInterface(newFile.absolutePath)

        if (attributes.isNotEmpty()) {
            for (i in attributes.indices) {
                val value = oldExif.getAttribute(attributes[i])
                if (value != null)
                    newExif.setAttribute(attributes[i], value)
            }
            newExif.saveAttributes()
        }
    }

    fun getBitmapFromByte(bytes: ByteArray): Bitmap {
        val orientation = Exif.getOrientation(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        return if (orientation != 0) rotationBitmap(bitmap, orientation) else bitmap
    }

    private fun rotationBitmap(filePath: String, bm: Bitmap): Bitmap {
        var exif: ExifInterface? = null
        try {
            exif = ExifInterface(filePath)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        var orientString: String? = null
        if (exif != null)
            orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION)

        val orientation = if (orientString != null) Integer.parseInt(orientString) else ExifInterface.ORIENTATION_NORMAL

        if (orientation != ExifInterface.ORIENTATION_NORMAL) {
            val rotationAngle = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }

            if (rotationAngle == 0)
                return rotationWithUnknownOrientation(bm, orientation)

            return rotationBitmap(bm, rotationAngle)
        }
        return bm
    }

    private fun rotationBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        matrix.setRotate(orientation.toFloat(), bitmap.width.toFloat() / 2, bitmap.height.toFloat() / 2)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun rotationWithUnknownOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = decodeExifOrientation(orientation)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}