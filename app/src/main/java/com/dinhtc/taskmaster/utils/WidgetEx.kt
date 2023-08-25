package com.dinhtc.taskmaster.utils

import android.text.InputFilter
import android.widget.EditText

fun EditText.setCursorPosition(position: Int, requestFocus: Boolean = true) {
    try {
        setSelection(position)
        if (requestFocus) post { requestFocus(position) }
    } catch (ex: Exception) {
        setSelection(0)
        if (requestFocus) post { requestFocus(0) }
    }
}

fun EditText.setMaxLength(maxLength: Int) {
    filters = arrayOf(InputFilter.LengthFilter(if (maxLength <= 0) Int.MAX_VALUE else maxLength))
}

fun EditText.showKeyboard(delay: Long = 0L) = postDelayed( { AndroidUtils.showKeyboard(this) }, delay)

fun EditText.hideKeyboard() = post { AndroidUtils.hideKeyboard(this) }