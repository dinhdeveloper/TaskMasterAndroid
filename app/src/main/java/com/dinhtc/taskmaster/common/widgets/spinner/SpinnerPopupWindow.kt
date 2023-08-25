package com.dinhtc.taskmaster.common.widgets.spinner

import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.PopupWindow

class SpinnerPopupWindow(contentView: View, width: Int, height: Int, focusable: Boolean)
    : PopupWindow(contentView, width, height, focusable) {

    fun dismissRightAway() {
        super.dismiss()
    }

//    override fun dismiss() {
////        val animator = mOverlayView.animate().alpha(0f)
////        mLayout.pivotY = 0f
////        mLayout.animate().scaleY(0f).duration = 150
////        runOnAnimationEnd(animator, Runnable {
////            dismissRightAway()
////        })
//    }

    private fun runOnAnimationEnd(animator: ViewPropertyAnimator, runnable: Runnable): ViewPropertyAnimator {
        animator.withEndAction(runnable)
        return animator
    }
}