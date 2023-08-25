package com.dinhtc.taskmaster.utils

import android.text.Editable
import android.text.TextWatcher

interface MyTextWatcher : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, before: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

    override fun afterTextChanged(s: Editable?) {}
}