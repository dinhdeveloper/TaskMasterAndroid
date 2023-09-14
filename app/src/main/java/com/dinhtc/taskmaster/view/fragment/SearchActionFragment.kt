package com.dinhtc.taskmaster.view.fragment

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dinhtc.taskmaster.R
import com.dinhtc.taskmaster.common.date_picker.CalendarPicker
import com.dinhtc.taskmaster.common.date_picker.model.CalendarEvent
import com.dinhtc.taskmaster.common.view.BaseFragment
import com.dinhtc.taskmaster.common.widgets.spinner.ItemViewLocation
import com.dinhtc.taskmaster.common.widgets.spinner.LocationSpinner
import com.dinhtc.taskmaster.common.widgets.spinner.ProvinceData
import com.dinhtc.taskmaster.databinding.FragmentSearchActionBinding
import com.dinhtc.taskmaster.model.request.SearchRequest
import com.dinhtc.taskmaster.model.response.ListCollectPointResponse
import com.dinhtc.taskmaster.model.response.ListJobSearchResponse
import com.dinhtc.taskmaster.utils.DialogFactory
import com.dinhtc.taskmaster.utils.LoadingScreen
import com.dinhtc.taskmaster.utils.SharedPreferencesManager
import com.dinhtc.taskmaster.utils.SharedPreferencesManager.Companion.USER_ID
import com.dinhtc.taskmaster.utils.UiState
import com.dinhtc.taskmaster.utils.observe
import com.dinhtc.taskmaster.view.activity.MainActivity
import com.dinhtc.taskmaster.view.fragment.HomeFragment.Companion.BUNDLE_KEY
import com.dinhtc.taskmaster.view.fragment.HomeFragment.Companion.REQUEST_KEY
import com.dinhtc.taskmaster.viewmodel.AddTaskViewModel
import com.dinhtc.taskmaster.viewmodel.JobsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@AndroidEntryPoint
class SearchActionFragment : BaseFragment<FragmentSearchActionBinding>() {

    private var listDataSearchLiveData: ListJobSearchResponse? = null
    private var paymentStatus: Int = 0
    private var empStatus: Int = 1
    private var statusStatus: Int = 1

    private val addTaskViewModel: AddTaskViewModel by viewModels()
    private val dataListCollectPoint = ArrayList<ItemViewLocation<ProvinceData>>()

    private var secondDateFormat: String? = null
    private var firstDateFormat: String? = null
    private var collectPointSelected: Int = -1

    var empId = SharedPreferencesManager.instance.getInt(USER_ID, 0).toString().toInt()
    override val layoutResourceId: Int
        get() = R.layout.fragment_search_action

    override fun onViewCreated() {
        actionView()
        viewBinding.apply {
            if (firstDateFormat?.isNotEmpty() == true && secondDateFormat?.isNotEmpty() == true){
                btnSelectDate.text = "$firstDateFormat - $secondDateFormat"
            }else{
                val (year, month, day) = getCurrentDateComponents()
                btnSelectDate.text = "$day/$month/$year - $day/$month/$year"
                firstDateFormat = "$day/$month/$year"
                secondDateFormat = "$day/$month/$year"
            }

            onClickRadioPerson(radioCuaToi,radioNhom,radioTatCaNguoi)
            onClickRadioTask(radioChuaXong,radioDaXong,radioTatCaTask)
            onClickRadioMoney(daThanhToan,chuaThanhToan)
        }

        viewBinding.btnSelectCollectPoint.setOnItemSelectedListener(mOnSelectedCollectPointListener)

        addTaskViewModel.getListCollectPoint()
        observe(addTaskViewModel.dataListCollectPoint, ::onGetListCollectPoint)
        observe(addTaskViewModel.dataSearch, ::dataSearchLive)
    }

    private val mOnSelectedCollectPointListener =
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

    private fun onGetListCollectPoint(uiState: UiState<ListCollectPointResponse>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                val listCollectPointLiveData = uiState.data.data.listItem
                if (listCollectPointLiveData != null) {
                    for (data in listCollectPointLiveData) {
                        dataListCollectPoint.add(
                            ItemViewLocation(
                                ProvinceData(
                                    data.empId,
                                    "${data.empId}",
                                    data.name
                                )
                            )
                        )
                    }
                }
                viewBinding.btnSelectCollectPoint.setData(dataListCollectPoint)
            }

            is UiState.Error -> {
                val errorMessage = uiState.message
                Log.e(MainActivity.TAG_ERROR, "onGetListCollectPoint: $errorMessage")
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
            }

            UiState.Loading -> {}
        }
    }

    private fun dataSearchLive(uiState: UiState<ListJobSearchResponse>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                listDataSearchLiveData = uiState.data.data
                val bundle = Bundle()
                bundle.putSerializable(BUNDLE_KEY,listDataSearchLiveData)
                parentFragmentManager.setFragmentResult(REQUEST_KEY, bundle)
                findNavController().popBackStack()
            }

            is UiState.Error -> {
                val errorMessage = uiState.message
                Log.e(MainActivity.TAG_ERROR, "dataSearchLive: $errorMessage")
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
            }

            UiState.Loading -> {
                LoadingScreen.displayLoadingWithText(context,"Đang tìm kiếm...",false)
            }
        }
    }

    private fun actionView() {
        viewBinding.layoutToolBar.apply {
            titleToolBar.text = "Tìm kiếm"
            titleToolBar.setPadding(20,0,0,0)
            imgBackParent.visibility = View.GONE
            imgHome.setImageResource(R.drawable.icon_close_while)
            imgHome.setOnClickListener {
               // findNavController().popBackStack()
                findNavController().navigate(R.id.action_searchActionFragment_to_homeFragment)
            }
        }
        viewBinding.apply {
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

            if (viewBinding.edtMaNV.text.toString().trim().isNotEmpty() || viewBinding.edtMaNV.text.toString().trim().isNotBlank()){
                empId = viewBinding.edtMaNV.text.toString().trim().toInt()
            }
            var jobId = 0
            if (viewBinding.edtMaNV.text.toString().trim().isNotEmpty() || viewBinding.edtMaNV.text.toString().trim().isNotBlank()){
                jobId = viewBinding.edtMaNV.text.toString().trim().toInt()
            }
            var collectPoint : String? = null
            if (btnSelectCollectPoint.text.toString().isNotEmpty()){
                collectPoint = btnSelectCollectPoint.text.toString()
            }
            btnSearch.setOnClickListener {
                var searchRequest = SearchRequest(
                    startDate = firstDateFormat,
                    endDate = secondDateFormat,
                    empStatus = empStatus,
                    empId =  if (empId == 0) null else empId,
                    status = statusStatus,
                    paymentStatus = paymentStatus,
                    jobId = if (jobId == 0) null else jobId,
                    collectPoint = collectPoint
                )

                addTaskViewModel.search(searchRequest)
            }
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

        calendarPicker.setCalendarPickerListener(object :CalendarPicker.CalendarPickerListener{
            override fun onDateSelected(firstDate: Long, secondDate: Long) {
                dialog.dismiss()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                firstDateFormat = dateFormat.format(firstDate)
                secondDateFormat = dateFormat.format(secondDate)

                if (firstDateFormat?.isNotEmpty() == true && secondDateFormat?.isNotEmpty() == true){
                    viewBinding.btnSelectDate.text = "$firstDateFormat - $secondDateFormat"
                }
            }
        })

    }

    private fun onClickRadioMoney(daThanhToan: AppCompatTextView?, chuaThanhToan: AppCompatTextView?) {
        daThanhToan?.setOnClickListener {
            paymentStatus = 0
            chuaThanhToan?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            daThanhToan.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_check_search) }
        }
        chuaThanhToan?.setOnClickListener {
            paymentStatus = 1
            daThanhToan?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            chuaThanhToan.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_check_search) }
        }
    }

    private fun onClickRadioPerson(
        radioCuaToi: AppCompatTextView?,
        radioNhom: AppCompatTextView?,
        radioTatCaNguoi: AppCompatTextView?
    ) {
        radioCuaToi?.setOnClickListener {
            empStatus = 1
            viewBinding.edtMaNV.isEnabled = false
            viewBinding.edtMaNV.text = null

            radioNhom?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioTatCaNguoi?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioCuaToi.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_check_search) }
        }
        radioNhom?.setOnClickListener {
            empStatus = 2
            viewBinding.edtMaNV.isEnabled = false
            viewBinding.edtMaNV.text = null

            radioCuaToi?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioTatCaNguoi?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioNhom.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_check_search) }
        }
        radioTatCaNguoi?.setOnClickListener {
            empStatus = 3
            viewBinding.edtMaNV.isEnabled = true

            radioCuaToi?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioNhom?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioTatCaNguoi.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_check_search) }
        }
    }

    private fun onClickRadioTask(
        radioChuaXong: AppCompatTextView?,
        radioDaXong: AppCompatTextView?,
        radioTatCaTask: AppCompatTextView?
    ) {
        radioChuaXong?.setOnClickListener {
            statusStatus = 1
            radioDaXong?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioTatCaTask?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioChuaXong.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_check_search) }
        }
        radioDaXong?.setOnClickListener {
            statusStatus = 2
            radioChuaXong?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioTatCaTask?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioDaXong.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_check_search) }
        }
        radioTatCaTask?.setOnClickListener {
            statusStatus = 3
            radioChuaXong?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioDaXong?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioTatCaTask.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_check_search) }
        }
    }
}