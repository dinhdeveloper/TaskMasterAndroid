package com.elogictics.taskmaster.common.view

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.elogictics.taskmaster.R
import com.elogictics.taskmaster.databinding.UploadDocumentCellViewImageBinding

class UploadDocumentImage @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var viewBinding: UploadDocumentCellViewImageBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.upload_document_cell_view_image, this, true)

    private var iconResId: Int = iconResIdDefault
    private var actionResId: Int = actionResIdDefault
    var hintResId: Int = 0
    private var isAllCaps: Boolean = false

    init {
        setup(attrs)
    }

    private fun setup(attrs: AttributeSet?) {

        attrs?.let {
            val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.UploadDocumentCell)
            iconResId = a.getResourceId(R.styleable.UploadDocumentCell_iconResId, iconResIdDefault)
            actionResId = a.getResourceId(R.styleable.UploadDocumentCell_actionResId, actionResIdDefault)
            isAllCaps = attrs.getAttributeBooleanValue(XML_NAMESPACE_ANDROID, "textAllCaps", false)
            hintResId = a.getResourceId(R.styleable.UploadDocumentCell_hintResId, 0)
            a.recycle()
        }
        viewBinding.ivUpload1.setImageResource(iconResId)
        viewBinding.tvUpload1.text = context.getString(actionResId)
        //viewBinding.tvUpload1.isAllCaps = isAllCaps
    }

    fun setUploadType(useCameraIcon: Boolean) {
        iconResId = if (useCameraIcon) R.drawable.ic_camera else R.drawable.ic_upload
        viewBinding.ivUpload1.setImageResource(iconResId)
    }

    companion object {
        private const val XML_NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android"
        private val iconResIdDefault = R.drawable.ic_upload
        private val actionResIdDefault = R.string.upload_document
    }
}