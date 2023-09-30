package com.elogictics.taskmaster.utils

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Pattern

class EnglishInputFilter : InputFilter {
    private val pattern = Pattern.compile("[a-zA-Z ]*") // Chỉ cho phép ký tự tiếng Anh và dấu cách

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val inputText = source?.subSequence(start, end).toString()
        if (!pattern.matcher(inputText).matches()) {
            return "" // Nếu ký tự không phải là tiếng Anh, thì không cho phép nhập
        }
        return null // Cho phép nhập
    }
}
