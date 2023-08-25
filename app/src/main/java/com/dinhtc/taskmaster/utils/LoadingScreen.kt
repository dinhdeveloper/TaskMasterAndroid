package com.dinhtc.taskmaster.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.TextView
import com.dinhtc.taskmaster.R

object LoadingScreen {
    var dialog: Dialog? = null
    fun displayLoadingWithText(context: Context?, text: String? = null, cancelable: Boolean) {
        dialog = Dialog(context!!)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setContentView(R.layout.loading_progress)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCancelable(cancelable)
        val textView = dialog?.findViewById<TextView>(R.id.text)
        textView?.text = text
        try {
            dialog!!.show()
        } catch (e: Exception) {
            dialog?.dismiss()
        }
    }

    fun hideLoading() {
        try {
            if (dialog != null) {
                dialog!!.dismiss()
            }
        } catch (e: Exception) {
            dialog?.dismiss()
        }
    }
}