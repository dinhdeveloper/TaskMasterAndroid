package com.dinhtc.taskmaster.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.util.DisplayMetrics
import android.view.View
import android.view.inputmethod.InputMethodManager
import org.apache.commons.lang3.StringEscapeUtils
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import kotlin.math.roundToInt

object AndroidUtils {

    fun getAndroidDeviceId(context: Context): String = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

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
        } catch (ignore: java.lang.Exception) {}

        return "$s VNĐ"
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
            ?.replace(".", "")?.replace(",", "")?.replace(" ","") ?: ""
    }

    fun showKeyboard(view: View? = null) {
        try {
            view?.let {
                val inputMethodManager = it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(it, InputMethodManager.SHOW_FORCED)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun hideKeyboard(view: View?) {
        view?.let {
            val imm = it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}