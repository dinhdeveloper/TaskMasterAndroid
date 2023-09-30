package com.elogictics.taskmaster.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.provider.Settings
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.util.DisplayMetrics
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.elogictics.taskmaster.common.widgets.dialog.FullScreenDialogFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import org.apache.commons.lang3.StringEscapeUtils
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import kotlin.math.roundToInt

object AndroidUtils {

    fun bitmapDescriptorFromVector(context: Context?, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = context?.let { ContextCompat.getDrawable(it, vectorResId) }
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap =
            Bitmap.createBitmap(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    fun logout() {
        // Xóa thông tin đăng nhập từ SharedPreferences
        SharedPreferencesManager.instance.remove(SharedPreferencesManager.USERNAME)
        SharedPreferencesManager.instance.remove(SharedPreferencesManager.IS_LOGGED_IN)
        SharedPreferencesManager.instance.remove(SharedPreferencesManager.LAST_LOGIN_TINE)
        SharedPreferencesManager.instance.remove(SharedPreferencesManager.TOKEN_LOGIN)
        SharedPreferencesManager.instance.remove(SharedPreferencesManager.TOKEN_FIREBASE)
        SharedPreferencesManager.instance.remove(SharedPreferencesManager.ROLE_CODE)
        SharedPreferencesManager.instance.remove(SharedPreferencesManager.USER_ID)
        SharedPreferencesManager.instance.remove(SharedPreferencesManager.PASS_W)
        SharedPreferencesManager.instance.remove(SharedPreferencesManager.DEVICE_ID)
        SharedPreferencesManager.instance.remove(SharedPreferencesManager.FULL_NAME)
        //REMOVE SEARCH
        SharedPreferencesManager.instance.remove(FullScreenDialogFragment.RADIO_PERSON)
        SharedPreferencesManager.instance.remove(FullScreenDialogFragment.RADIO_TASK)
        SharedPreferencesManager.instance.remove(FullScreenDialogFragment.RADIO_PAYMENT)
        SharedPreferencesManager.instance.remove(FullScreenDialogFragment.FIRST_DATE)
        SharedPreferencesManager.instance.remove(FullScreenDialogFragment.SECOND_DATE)
        // Chuyển đến màn hình đăng nhập
    }

    fun getAndroidDeviceId(context: Context): String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else {
            capitalize(manufacturer) + " " + model
        }
    }

    private fun capitalize(s: String): String {
        if (s.isEmpty()) return s
        val first = s[0]
        return if (Character.isUpperCase(first)) {
            s
        } else {
            Character.toUpperCase(first) + s.substring(1)
        }
    }

    @JvmStatic
    fun fromHtml(str: String?): Spanned {
        return when {
            str == null -> {
                SpannableString("")
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                Html.fromHtml(StringEscapeUtils.unescapeHtml4(str), Html.FROM_HTML_MODE_LEGACY)
            }

            else -> {
                Html.fromHtml(StringEscapeUtils.unescapeHtml4(str))
            }
        }
    }

    @JvmStatic
    fun convertDpToPx(context: Context?, dp: Float): Int {
        if (context == null) return 0
        //val r = context.resources
        //return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dp, r.displayMetrics).roundToInt()
        return (dp * (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }

    @JvmStatic
    fun formatMoneyCard(money: String?, unit: String? = "VND"): String {
        val un = unit ?: "VND"
        val symbols = DecimalFormatSymbols()
        symbols.decimalSeparator = if (un == "VND" || un == "VNĐ") ',' else ','
        symbols.groupingSeparator = if (un == "VND" || un == "VNĐ") ',' else ','
        val myFormatter = DecimalFormat("#,###", symbols)

        var s = ""
        try {
            s = myFormatter.format(money?.toDouble() ?: 0)
        } catch (ignore: java.lang.Exception) {
        }

        return "$s"
    }

    @JvmStatic
    fun getMoneyRealValue(str: String?): Long {
        if (str == null || str == "") return 0L
        return try {
            decodeMoneyStr(str).toLong()
        } catch (ignore: java.lang.Exception) {
            0L
        }
    }

    @JvmStatic
    fun decodeMoneyStr(str: String?): String {
        return str?.trim()?.replace("VNĐ", "")?.replace("VND", "")
            ?.replace(".", "")?.replace(",", "")?.replace(" ", "") ?: ""
    }

    fun showKeyboard(view: View? = null) {
        try {
            view?.let {
                val inputMethodManager =
                    it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(it, InputMethodManager.SHOW_FORCED)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun hideKeyboard(view: View?) {
        view?.let {
            val imm =
                it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}