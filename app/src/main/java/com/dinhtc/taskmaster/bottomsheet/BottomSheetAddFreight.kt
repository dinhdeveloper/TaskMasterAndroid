package com.dinhtc.taskmaster.bottomsheet

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.dinhtc.taskmaster.R
import com.dinhtc.taskmaster.common.widgets.edittext.MoneyEditText
import com.dinhtc.taskmaster.common.widgets.elasticviews.ElasticLayout
import com.dinhtc.taskmaster.common.widgets.spinner.ItemViewLocation
import com.dinhtc.taskmaster.common.widgets.spinner.LocationSpinner
import com.dinhtc.taskmaster.common.widgets.spinner.ProvinceData
import com.dinhtc.taskmaster.common.widgets.spinner.ProvinceSpinner
import com.dinhtc.taskmaster.model.request.AddMaterialRequest
import com.dinhtc.taskmaster.utils.AndroidUtils.getMoneyRealValue
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView

class BottomSheetAddFreight(
    private val mContext: Context,
    private val dataListJob: ArrayList<ItemViewLocation<ProvinceData>>,
    private val jobsId : Int,
    private val listenerAddMaterial: ((AddMaterialRequest) -> Unit)
    ) : BottomSheetDialogFragment() {

    var bottomSheetBehavior: BottomSheetBehavior<*>? = null

    private lateinit var imgClose: ImageView
    private lateinit var itemSelectTask: ProvinceSpinner
    private lateinit var edtKhoiLuong: TextInputEditText
    private lateinit var edtKhoiLuongKH: TextInputEditText
    private lateinit var edtDonGia: MoneyEditText
    private lateinit var btnSubmit: ElasticLayout
    private lateinit var tvLabelVL: MaterialTextView

    private var jobTypeIdSelected = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_add_freight, container, false)
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
        itemSelectTask.setData(dataListJob)
        itemSelectTask.setOnItemSelectedListener(mOnSelectedTaskListener)
    }

    private val mOnSelectedTaskListener =
        object : LocationSpinner.OnItemSelectedListener<ProvinceData> {
            override fun onItemSelected(
                parent: LocationSpinner<ProvinceData>,
                position: Int,
                item: ItemViewLocation<ProvinceData>?
            ) {
                jobTypeIdSelected = item?.data?.id!!
            }
        }

    private fun actionView() {
        imgClose.setOnClickListener {
            dismiss()
        }

        btnSubmit.setOnClickListener {
            if (jobTypeIdSelected != -1){
                val addMaterialRequest = AddMaterialRequest(
                    mateId = jobTypeIdSelected,
                    jobId = jobsId,
                    weight = (if (edtKhoiLuong.text.toString().isNotEmpty()) edtKhoiLuong.text.toString().toLong() else null)!!,
                    weightToCus = (if (edtKhoiLuongKH.text.toString().isNotEmpty()) edtKhoiLuongKH.text.toString().toLong() else null)!!,
                    price = getMoneyRealValue(edtDonGia.text.toString())
                )
                listenerAddMaterial(addMaterialRequest)
            }
        }
        val labelNV1 = "Vật liệu:<font color='#FF0000'><sup>*</sup></font>"
        // Sử dụng Html.fromHtml để hiển thị văn bản HTML trong TextView
        tvLabelVL.text = Html.fromHtml(labelNV1, Html.FROM_HTML_MODE_COMPACT)
    }

    private fun findViewByID(modalSheetView: View) {
        imgClose = modalSheetView.findViewById(R.id.imgClose)
        itemSelectTask = modalSheetView.findViewById(R.id.itemSelectTask)
        edtKhoiLuong = modalSheetView.findViewById(R.id.edtKhoiLuong)
        edtKhoiLuongKH = modalSheetView.findViewById(R.id.edtKhoiLuongKH)
        edtDonGia = modalSheetView.findViewById(R.id.edtDonGia)
        btnSubmit = modalSheetView.findViewById(R.id.btnSubmit)
        tvLabelVL = modalSheetView.findViewById(R.id.tvLabelVL)
    }

    fun deleteDataInsert() {
        itemSelectTask.text = null
        edtKhoiLuong.text = null
        edtKhoiLuongKH.text = null
        edtDonGia.text = null
    }

    companion object {
        const val TAG = "BottomSheetAddFreight"
    }
}