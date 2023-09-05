package com.dinhtc.taskmaster.bottomsheet

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.dinhtc.taskmaster.R
import com.dinhtc.taskmaster.common.widgets.elasticviews.ElasticLayout
import com.dinhtc.taskmaster.model.request.CollectPointRequest
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textview.MaterialTextView
import okhttp3.MultipartBody

class BottomSheetAddCollectPoint (
    private val mContext: Context,
    private val listenerAddPoint: ((dataPoint: CollectPointRequest) -> Unit)
) : BottomSheetDialogFragment()  {
    var bottomSheetBehavior: BottomSheetBehavior<*>? = null

    private lateinit var imgClose: ImageView
    private lateinit var edtTenDiaDiem: EditText
    private lateinit var edtDiaChi: EditText
    private lateinit var edtLienHe: EditText
    private lateinit var edtPhone: EditText
    private lateinit var btnSubmit: ElasticLayout
    private lateinit var tvLabelDC: MaterialTextView
    private lateinit var tvLabelDD: MaterialTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_add_collect_point, container, false)
    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetCommon
    }

    override fun onViewCreated(
        modalSheetView: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(modalSheetView, savedInstanceState)
        /*Show full dialog*/
        bottomSheetBehavior = BottomSheetBehavior.from(view?.parent as View)
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior?.isDraggable = false

        findViewByID(modalSheetView)
        actionView()
    }

    private fun actionView() {
        val labelDC = "Địa chỉ:<font color='#FF0000'><sup>*</sup></font>"
        val labelDD = "Tên địa điểm:<font color='#FF0000'><sup>*</sup></font>"

        // Sử dụng Html.fromHtml để hiển thị văn bản HTML trong TextView
        tvLabelDC.text = Html.fromHtml(labelDC, Html.FROM_HTML_MODE_COMPACT)
        tvLabelDD.text = Html.fromHtml(labelDD, Html.FROM_HTML_MODE_COMPACT)


        imgClose.setOnClickListener {
            dismiss()
        }

        btnSubmit.setOnClickListener {
            if (checkValidate()){
                var dataSubmit = CollectPointRequest(
                    edtTenDiaDiem.text.toString(),
                    edtDiaChi.text.toString(),
                    edtLienHe.text.toString(),
                    edtPhone.text.toString()
                )
                listenerAddPoint(dataSubmit)
            }
        }
    }

    private fun checkValidate(): Boolean {
        return if (edtTenDiaDiem.text.toString().trim().isEmpty()){
            edtTenDiaDiem.background = context?.let { ContextCompat.getDrawable(it,R.drawable.bg_red_) }
            false
        } else{
            edtTenDiaDiem.background = context?.let { ContextCompat.getDrawable(it,R.drawable.bg_item_detail_black) }
            return if (edtDiaChi.text.toString().trim().isEmpty()){
                edtDiaChi.background = context?.let { ContextCompat.getDrawable(it,R.drawable.bg_red_) }
                false
            } else{
                edtDiaChi.background = context?.let { ContextCompat.getDrawable(it,R.drawable.bg_item_detail_black) }
                true
            }
        }
    }

    private fun findViewByID(modalSheetView: View) {
        imgClose = modalSheetView.findViewById(R.id.imgClose)
        edtTenDiaDiem = modalSheetView.findViewById(R.id.edtTenDiaDiem)
        edtDiaChi = modalSheetView.findViewById(R.id.edtDiaChi)
        edtLienHe = modalSheetView.findViewById(R.id.edtLienHe)
        edtPhone = modalSheetView.findViewById(R.id.edtPhone)
        btnSubmit = modalSheetView.findViewById(R.id.btnSubmit)
        tvLabelDC = modalSheetView.findViewById(R.id.tvLabelDC)
        tvLabelDD = modalSheetView.findViewById(R.id.tvLabelDD)
    }
    companion object {
        const val TAG = "BottomSheetAddCollectPoint"
    }
}