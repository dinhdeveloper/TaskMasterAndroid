package com.dinhtc.taskmaster.common.widgets.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.dinhtc.taskmaster.R
import com.dinhtc.taskmaster.common.date_picker.CalendarPicker
import com.dinhtc.taskmaster.common.date_picker.model.CalendarEvent
import com.dinhtc.taskmaster.common.widgets.spinner.ItemViewLocation
import com.dinhtc.taskmaster.common.widgets.spinner.LocationSpinner
import com.dinhtc.taskmaster.common.widgets.spinner.ProvinceData
import com.dinhtc.taskmaster.databinding.CustomLayoutSearchBinding
import com.dinhtc.taskmaster.model.request.SearchRequest
import com.dinhtc.taskmaster.utils.AndroidUtils
import com.dinhtc.taskmaster.utils.SharedPreferencesManager
import com.dinhtc.taskmaster.view.fragment.uuTienList
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Locale


class FullScreenDialogFragment(
    private val searchListener: ((dataSearch: SearchRequest) -> Unit)
) : DialogFragment() {

    private var dataListCollectPoint: ArrayList<ItemViewLocation<ProvinceData>>? = null
    private var viewBinding: CustomLayoutSearchBinding? = null

    private var paymentStatus: Int = 0
    private var empStatus: Int = 1
    private var statusStatus: Int = 1

    private var collectPointSelected: Int = -1

    var empId = SharedPreferencesManager.instance.getInt(SharedPreferencesManager.USER_ID, 0).toString().toInt()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = DataBindingUtil.inflate(inflater, R.layout.custom_layout_search, container, false)
        updateUI()
        searchAction()
        return viewBinding?.root
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        showUIBySaveState()
        viewBinding?.apply {
            titleToolBar.text = "Tìm kiếm"
            titleToolBar.setPadding(20,0,0,0)
            imgBackParent.visibility = View.GONE
            imgHome.setImageResource(R.drawable.icon_close_while)
            imgHome.setOnClickListener {
                dialog?.dismiss()
            }
        }

        viewBinding?.apply {
            if (SharedPreferencesManager.instance.getString(FIRST_DATE,null).isNotEmpty()
                && SharedPreferencesManager.instance.getString(SECOND_DATE,null).isNotEmpty()){
                btnSelectDate.text = "${SharedPreferencesManager.instance.getString(FIRST_DATE, null)} " +
                        "- ${SharedPreferencesManager.instance.getString(SECOND_DATE,null)}"
            }else{
                val (year, month, day) = getCurrentDateComponents()
                btnSelectDate.text = "$day/$month/$year - $day/$month/$year"

                SharedPreferencesManager.instance.putString(FIRST_DATE,"$day/$month/$year")
                SharedPreferencesManager.instance.putString(SECOND_DATE,"$day/$month/$year")

//                SharedPreferencesManager.instance.putInt(DAY_FIRST_SEARCH,day)
//                SharedPreferencesManager.instance.putInt(MONTH_FIRST_SEARCH,month)
//                SharedPreferencesManager.instance.putInt(YEAR_FIRST_SEARCH,year)

            }

            onClickRadioPerson(radioCuaToi,radioNhom,radioTatCaNguoi)
            onClickRadioTask(radioChuaXong,radioDaXong,radioTatCaTask)
            onClickRadioMoney(chuaThanhToan,daThanhToan)
        }

        if (dataListCollectPoint?.isNotEmpty() == true){
            viewBinding?.btnSelectCollectPoint?.setData(dataListCollectPoint!!)
            viewBinding?.btnSelectCollectPoint?.text = dataListCollectPoint!![0].data?.name
            viewBinding?.btnSelectCollectPoint?.setOnItemSelectedListener(mOnSelectedListener)
        }
    }

    private fun searchAction() {
        viewBinding?.apply {
            layoutMain.setOnClickListener {
                AndroidUtils.hideKeyboard(layoutMain)
            }
            btnSelectDate.setOnClickListener {
                val dialog = Dialog(requireContext())
                dialog.setContentView(R.layout.custom_dialog_calendar)

                dialog.window!!.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                findByView(dialog)
                dialog.window!!.setGravity(Gravity.CENTER)
                dialog.show()
            }
            var jobId = 0
            var collectPoint : String? = null
            btnSearch.setOnClickListener {
                viewBinding?.apply {
                    if (edtMaNV.text.toString().trim().isNotEmpty() || edtMaNV.text.toString().trim().isNotBlank()){
                        empId = edtMaNV.text.toString().trim().toInt()
                    }
                    if (edtMaCV.text.toString().trim().isNotEmpty() || edtMaCV.text.toString().trim().isNotBlank()){
                        jobId = edtMaCV.text.toString().trim().toInt()
                    }
                }
                if (btnSelectCollectPoint.text.toString().isNotEmpty()){
                    collectPoint = btnSelectCollectPoint.text.toString()
                }

                val searchRequest = SearchRequest(
                    startDate = SharedPreferencesManager.instance.getString(FIRST_DATE,null),
                    endDate = SharedPreferencesManager.instance.getString(SECOND_DATE,null),
                    empStatus = empStatus,
                    empId =  if (empId == 0) null else empId,
                    status = statusStatus,
                    paymentStatus = paymentStatus,
                    jobId = if (jobId == 0) null else jobId,
                    collectPoint = if (collectPoint.equals("Tất cả")) null else collectPoint
                )

                searchListener(searchRequest)
                dialog?.dismiss()
            }
        }
    }

    private val mOnSelectedListener =
        object : LocationSpinner.OnItemSelectedListener<ProvinceData> {
            override fun onItemSelected(
                parent: LocationSpinner<ProvinceData>,
                position: Int,
                item: ItemViewLocation<ProvinceData>?
            ) {
                collectPointSelected = item?.data?.id!!
            }
        }

    private fun getCurrentDateComponents(): Triple<Int, Int, Int> {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1  // Months are 0-based
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return Triple(year, month, day)
    }

    private fun onClickRadioMoney(chuaThanhToan: AppCompatTextView?, daThanhToan: AppCompatTextView?) {
        chuaThanhToan?.setOnClickListener {
            changeLayoutRadioChuaThanhToan()
        }
        daThanhToan?.setOnClickListener {
            changeLayoutRadioDaThanhToan()
        }
    }

    private fun onClickRadioPerson(
        radioCuaToi: AppCompatTextView?,
        radioNhom: AppCompatTextView?,
        radioTatCaNguoi: AppCompatTextView?
    ) {
        radioCuaToi?.setOnClickListener {
            changeLayoutRadioCuaToiSelected()
        }
        radioNhom?.setOnClickListener {
            changeLayoutRadioNhomSelected()
        }
        radioTatCaNguoi?.setOnClickListener {
            changeLayoutRadioPersonAllSelected()
        }
    }

    private fun onClickRadioTask(
        radioChuaXong: AppCompatTextView?,
        radioDaXong: AppCompatTextView?,
        radioTatCaTask: AppCompatTextView?
    ) {
        radioChuaXong?.setOnClickListener {
            changeLayoutRadioChuaXong()
        }
        radioDaXong?.setOnClickListener {
            changeLayoutRadioDaXong()
        }
        radioTatCaTask?.setOnClickListener {
            changeLayoutRadioTaskAll()
        }
    }

    private fun findByView(dialog: Dialog) {

        val (year, month, day) = getCurrentDateComponents()

        val calendarPicker = dialog.findViewById<CalendarPicker>(R.id.calendarPicker)
        val eventsList = listOf(
            CalendarEvent(
                eventName = "Hôm nay",
                eventDescription = "Hôm nay",
                date = Calendar.getInstance().time
            )
        )
        calendarPicker.addEvents(eventsList)

        calendarPicker.eventDotColor = Color.BLUE
        calendarPicker.eventDotColorWhenSelected = Color.RED
        calendarPicker.eventDotColorWhenHighlighted = Color.GREEN
        calendarPicker.setFirstSelectedDate(year = year, month = month - 1, day = day)
        calendarPicker.setSecondSelectedDate(year = year, month = month - 1 , day = day)

        calendarPicker.initCalendar()

        calendarPicker.setCalendarPickerListener(object : CalendarPicker.CalendarPickerListener{
            @SuppressLint("SetTextI18n")
            override fun onDateSelected(firstDate: Long, secondDate: Long) {
                dialog.dismiss()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                SharedPreferencesManager.instance.putString(FIRST_DATE,dateFormat.format(firstDate))
                SharedPreferencesManager.instance.putString(SECOND_DATE,dateFormat.format(secondDate))

                if (SharedPreferencesManager.instance.getString(FIRST_DATE,null).isNotEmpty()
                    && SharedPreferencesManager.instance.getString(SECOND_DATE,null).isNotEmpty()){
                    viewBinding?.btnSelectDate?.text = "${SharedPreferencesManager.instance.getString(FIRST_DATE, null)} " +
                            "- ${SharedPreferencesManager.instance.getString(SECOND_DATE,null)}"
                }
            }
        })

    }

    private fun showUIBySaveState() {
        viewBinding?.apply {
            when(SharedPreferencesManager.instance.getInt(RADIO_PERSON,1)){
                1 -> {
                    changeLayoutRadioCuaToiSelected()
                }

                2 -> {
                    changeLayoutRadioNhomSelected()
                }

                3 -> {
                    changeLayoutRadioPersonAllSelected()
                }
            }
            when(SharedPreferencesManager.instance.getInt(RADIO_TASK,1)){
                1 -> {
                    changeLayoutRadioChuaXong()
                }

                2 -> {
                    changeLayoutRadioDaXong()
                }

                3 -> {
                    changeLayoutRadioTaskAll()
                }
            }
            when(SharedPreferencesManager.instance.getInt(RADIO_PAYMENT,0)){
                0 -> {
                    changeLayoutRadioChuaThanhToan()
                }

                1 -> {
                    changeLayoutRadioDaThanhToan()
                }
            }
        }
    }

    private fun changeLayoutRadioCuaToiSelected() {
        viewBinding?.apply {
            empStatus = 1
            SharedPreferencesManager.instance.putInt(RADIO_PERSON,1)
            viewBinding?.edtMaNV?.isEnabled = false
            viewBinding?.edtMaNV?.text = null

            radioNhom.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioTatCaNguoi.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioCuaToi.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_check_search) }
        }
    }

    private fun changeLayoutRadioNhomSelected(){
        viewBinding?.apply {
            empStatus = 2
            SharedPreferencesManager.instance.putInt(RADIO_PERSON,2)
            viewBinding?.edtMaNV?.isEnabled = false
            viewBinding?.edtMaNV?.text = null

            radioCuaToi.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioTatCaNguoi.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioNhom.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_check_search) }
        }
    }
    private fun changeLayoutRadioPersonAllSelected(){
        viewBinding?.apply {
            empStatus = 3
            SharedPreferencesManager.instance.putInt(RADIO_PERSON,3)
            viewBinding?.edtMaNV?.isEnabled = true

            radioCuaToi.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioNhom.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioTatCaNguoi.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_check_search) }
        }
    }

    private fun changeLayoutRadioChuaXong(){
        viewBinding?.apply {
            statusStatus = 1
            SharedPreferencesManager.instance.putInt(RADIO_TASK,1)
            radioDaXong.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioTatCaTask.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioChuaXong.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_check_search) }
        }
    }

    private fun changeLayoutRadioDaXong(){
        viewBinding?.apply {
            statusStatus = 2
            SharedPreferencesManager.instance.putInt(RADIO_TASK,2)
            radioChuaXong.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioTatCaTask.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioDaXong.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_check_search) }
        }
    }
    private fun changeLayoutRadioTaskAll(){
        viewBinding?.apply {
            statusStatus = 3
            SharedPreferencesManager.instance.putInt(RADIO_TASK,3)
            radioChuaXong.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioDaXong.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioTatCaTask.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_check_search) }
        }
    }

    private fun changeLayoutRadioChuaThanhToan(){
        viewBinding?.apply {
            paymentStatus = 0
            SharedPreferencesManager.instance.putInt(RADIO_PAYMENT,0)
            daThanhToan.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            chuaThanhToan.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_check_search) }
        }
    }

    private fun changeLayoutRadioDaThanhToan(){
        viewBinding?.apply {
            paymentStatus = 1
            SharedPreferencesManager.instance.putInt(RADIO_PAYMENT,1)
            chuaThanhToan.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            daThanhToan.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_check_search) }
        }
    }

    override fun getTheme(): Int {
        return R.style.FullScreenDialogTheme
    }

    fun setData(dataListCollectPoint: ArrayList<ItemViewLocation<ProvinceData>>) {
        this.dataListCollectPoint = dataListCollectPoint
    }

    companion object{
        val RADIO_PERSON = "RADIO_PERSON"
        val RADIO_TASK = "RADIO_TASK"
        val RADIO_PAYMENT = "RADIO_PAYMENT"
        val FIRST_DATE = "FIRST_DATE"
        val SECOND_DATE = "SECOND_DATE"

        val DAY_FIRST_SEARCH = "DAY_FIRST_SEARCH"
        val MONTH_FIRST_SEARCH = "MONTH_FIRST_SEARCH"
        val YEAR_FIRST_SEARCH = "YEAR_FIRST_SEARCH"
    }
}
