package com.elogictics.taskmaster.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import com.elogictics.taskmaster.R

class DialogFactory {
    companion object {
        @JvmStatic
        fun showDialogDefaultNotCancel(context: Context?, message: String?) {
            if (context == null) return
            val dialog = TranslucentDialog(context).apply {
                window?.apply {
                    setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }
                setContentView(R.layout.dialog_default_without_listener)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
            }
            val tvMessage = dialog.findViewById<View>(R.id.txtContent) as TextView
            tvMessage.text = message
            val btnConfirm = dialog.findViewById<View>(R.id.btnConfirm) as TextView
            btnConfirm.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }

        @JvmStatic
        fun showDialogSubTitleDefaultNotCancel(context: Context?, strTitle: String?,subTitle : String) {
            if (context == null) return
            val dialog = TranslucentDialog(context).apply {
                window?.apply {
                    setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }
                setContentView(R.layout.dialog_default_without_listener_sub_title)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
            }
            val tvMessage = dialog.findViewById<View>(R.id.txtContent) as TextView
            tvMessage.text = strTitle
            val tvSubTitle = dialog.findViewById<View>(R.id.tvSubTitle) as TextView
            tvSubTitle.text = subTitle
            val btnConfirm = dialog.findViewById<View>(R.id.btnConfirm) as TextView
            btnConfirm.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }

        fun showDialogDefaultNotCancelAndClick(context: Context?, message: String?,listenerYes: () -> Unit) {
            if (context == null) return
            val dialog = TranslucentDialog(context).apply {
                window?.apply {
                    setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }
                setContentView(R.layout.dialog_default_without_listener)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
            }
            val tvMessage = dialog.findViewById<View>(R.id.txtContent) as TextView
            tvMessage.text = message
            val btnConfirm = dialog.findViewById<View>(R.id.btnConfirm) as TextView
            btnConfirm.setOnClickListener {
                dialog.dismiss()
                listenerYes.invoke()
            }
            dialog.show()
        }

        @JvmStatic
        @JvmOverloads
        fun createMessageDialogWithYesNo(
            context: Context?,
            message: String?,
            titleYes: String?,
            titleNo: String?,
            listenerYes: () -> Unit,
            listenerNo: () -> Unit
        ) {
            if (context == null) return
            val dialog = TranslucentDialog(context).apply {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setContentView(R.layout.dialog_yes_no)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
            }
            val tvMessage = dialog.findViewById<View>(R.id.txtContent) as TextView
            tvMessage.text = message
            val btnYes = dialog.findViewById<View>(R.id.btnYes) as TextView
            val btnNo = dialog.findViewById<View>(R.id.btnNo) as TextView

            btnYes.text = titleYes
            btnNo.text = titleNo

            btnNo.setOnClickListener {
                dialog.dismiss()
                listenerNo.invoke()
            }
            btnYes.setOnClickListener {
                dialog.dismiss()
                listenerYes.invoke()
            }
            dialog.show()
        }

        @JvmStatic
        @JvmOverloads
        fun createMessageDialogRequirePermission(
            context: Context?,
            title: String?,
            message: String?,
            titleButtonYes: String?,
            titleButtonNo: String?,
            listenerYes: () -> Unit,
            listenerNo: () -> Unit,
        ) {
            if (context == null) return
            val dialog = TranslucentDialog(context).apply {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setContentView(R.layout.dialog_require_permission)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                setOnCancelListener {
                    dismiss()
                }
            }
            val tvMessage = dialog.findViewById<View>(R.id.txtContent) as TextView
            val subtitleTextView = dialog.findViewById<View>(R.id.subtitleTextView) as TextView
            tvMessage.text = message
            subtitleTextView.text = title
            val btnYes = dialog.findViewById<View>(R.id.btnYes) as TextView
            val btnNo = dialog.findViewById<View>(R.id.btnNo) as TextView
            btnYes.text = titleButtonYes
            btnNo.text = titleButtonNo
            btnNo.setOnClickListener {
                dialog.dismiss()
                listenerNo.invoke()
            }
            btnYes.setOnClickListener {
                dialog.dismiss()
                listenerYes.invoke()
            }

            dialog.show()
        }
    }
}