package com.dinhtc.taskmaster.view.fragment

import android.util.Log
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.dinhtc.taskmaster.R
import com.dinhtc.taskmaster.adapter.SuggestionAdapter
import com.dinhtc.taskmaster.bottomsheet.BottomSheetAddCollectPoint
import com.dinhtc.taskmaster.databinding.FragmentAddTaskBinding
import com.dinhtc.taskmaster.common.view.BaseFragment
import com.dinhtc.taskmaster.common.widgets.spinner.ItemViewLocation
import com.dinhtc.taskmaster.common.widgets.spinner.LocationSpinner
import com.dinhtc.taskmaster.common.widgets.spinner.ProvinceData
import com.dinhtc.taskmaster.model.SuggestionModel
import com.dinhtc.taskmaster.model.request.AddTaskRequest
import com.dinhtc.taskmaster.model.response.ListCollectPointResponse
import com.dinhtc.taskmaster.model.response.ListEmployeeResponse
import com.dinhtc.taskmaster.model.response.ListJobTypeResponse
import com.dinhtc.taskmaster.utils.DialogFactory
import com.dinhtc.taskmaster.utils.LoadingScreen
import com.dinhtc.taskmaster.utils.UiState
import com.dinhtc.taskmaster.utils.observe
import com.dinhtc.taskmaster.viewmodel.AddTaskViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddTaskFragment : BaseFragment<FragmentAddTaskBinding>() {

    private var bottomSheetAdd: BottomSheetAddCollectPoint? = null
    val mTagList: MutableList<SuggestionModel> = mutableListOf()
    private val addTaskViewModel: AddTaskViewModel by viewModels()
    private val dataListJob = ArrayList<ItemViewLocation<ProvinceData>>()
    private val dataListEmployeeNV1 = ArrayList<ItemViewLocation<ProvinceData>>()
    private val dataListEmployeeNV2 = ArrayList<ItemViewLocation<ProvinceData>>()

    private var nv1Selected = -1
    private var nv2Selected = -1
    private var jobTypeIdSelected = -1
    private var uuTienIdSelected = 2
    override val layoutResourceId: Int
        get() = R.layout.fragment_add_task

    override fun onViewCreated() {
        addTaskViewModel.getListJobType()
        addTaskViewModel.getListEmployee()
        addTaskViewModel.getListCollectPoint()

        observe(addTaskViewModel.dataJobType, ::onGetListJobType)
        observe(addTaskViewModel.dataEmployee, ::onGetListEmployee)
        observe(addTaskViewModel.dataEmployeeListNotById, ::onGetListEmployeeByNotId)
        observe(addTaskViewModel.dataListCollectPoint, ::onGetListCollectPoint)
        observe(addTaskViewModel.dataAddCollectPoint, ::onAddCollectPoint)
        observe(addTaskViewModel.dataAddTask, ::onAddTask)

        viewBinding.edtSelectNV1.setOnItemSelectedListener(mOnSelectedNV1Listener)
        viewBinding.edtSelectNV2.setOnItemSelectedListener(mOnSelectedNV2Listener)
        viewBinding.edtSelectTask.setOnItemSelectedListener(mOnSelectedTaskListener)

        viewBinding.edtSelectUuTien.text = uuTienList[1].data?.name
        viewBinding.edtSelectUuTien.setData(uuTienList)
        viewBinding.edtSelectUuTien.setOnItemSelectedListener(mOnSelectedUuTienListener)

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        onClickItem()
    }

    private fun onClickItem() {
        viewBinding.apply {
            btnAddDiaDiem.setOnClickListener {
                bottomSheetAdd = context?.let {
                    BottomSheetAddCollectPoint(it) { data ->
                        addTaskViewModel.addCollectPoint(data)
                    }
                }
                bottomSheetAdd?.isCancelable = false
                activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                activity?.supportFragmentManager?.let {
                    bottomSheetAdd?.show(
                        it,
                        bottomSheetAdd?.tag
                    )
                }
            }

            btnSubmit.setOnClickListener {
                Log.d("SSSSSSSSSS_TEXT", "${viewBinding.edtGhiChu.inputText}")
                if (checkValidate()){
                    if (nv2Selected != -1) {
                        val addTaskRequest = AddTaskRequest(
                            jobTypeIdSelected,
                            nv1Selected,
                            nv2Selected,
                            viewBinding.edtDiaDiem.ids,
                            viewBinding.edtGhiChu.inputText
                        )
                        addTaskViewModel.addTask(addTaskRequest)
                    }
                }
            }
        }
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

    private val mOnSelectedNV1Listener =
        object : LocationSpinner.OnItemSelectedListener<ProvinceData> {
            override fun onItemSelected(
                parent: LocationSpinner<ProvinceData>,
                position: Int,
                item: ItemViewLocation<ProvinceData>?
            ) {
                viewBinding.edtSelectNV2.text = null
                nv2Selected = -1
                nv1Selected = item?.data?.id!!
                addTaskViewModel.getListEmployeeNotById(nv1Selected)
            }
        }

    private val mOnSelectedNV2Listener =
        object : LocationSpinner.OnItemSelectedListener<ProvinceData> {
            override fun onItemSelected(
                parent: LocationSpinner<ProvinceData>,
                position: Int,
                item: ItemViewLocation<ProvinceData>?
            ) {
                nv2Selected = item?.data?.id!!
            }
        }

    private val mOnSelectedUuTienListener =
        object : LocationSpinner.OnItemSelectedListener<ProvinceData> {
            override fun onItemSelected(
                parent: LocationSpinner<ProvinceData>,
                position: Int,
                item: ItemViewLocation<ProvinceData>?
            ) {
                uuTienIdSelected = item?.data?.id!!
            }
        }


    private fun onGetListJobType(uiState: UiState<ListJobTypeResponse>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                val listJobLiveData = uiState.data.data.listItem
                //val jsonArray = JSONArray(listJobLiveData)

                for (data in listJobLiveData) {
                    dataListJob.add(
                        ItemViewLocation(
                            ProvinceData(
                                data.jobTypeId,
                                "${data.jobTypeId}",
                                "${data.jobTypeName}"
                            )
                        )
                    )
                }
                viewBinding.edtSelectTask.setData(dataListJob)
            }

            is UiState.Error -> {
                val errorMessage = uiState.message
                Log.e("SSSSSSSSSSS", errorMessage)
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
            }

            UiState.Loading -> {
                LoadingScreen.displayLoadingWithText(
                    requireContext(),
                    "Please wait...",
                    false
                )
            }
        }
    }
    private fun onGetListEmployee(uiState: UiState<ListEmployeeResponse>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                val listEmpLiveData = uiState.data.data.listItem
                for (data in listEmpLiveData) {
                    dataListEmployeeNV1.add(
                        ItemViewLocation(
                            ProvinceData(
                                data.empId,
                                "${data.empId}",
                                "${data.name}"
                            )
                        )
                    )
                }
                viewBinding.edtSelectNV1.setData(dataListEmployeeNV1)
            }

            is UiState.Error -> {
                val errorMessage = uiState.message
                Log.e("SSSSSSSSSSS", errorMessage)
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
            }

            UiState.Loading -> {}
        }
    }

    private fun onGetListEmployeeByNotId(uiState: UiState<ListEmployeeResponse>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                val listEmpLiveData = uiState.data.data.listItem
                dataListEmployeeNV2.clear()
                if (listEmpLiveData != null) {
                    for (data in listEmpLiveData) {
                        dataListEmployeeNV2.add(
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
                viewBinding.edtSelectNV2.setData(dataListEmployeeNV2)
            }

            is UiState.Error -> {
                val errorMessage = uiState.message
                Log.e("SSSSSSSSSSS", errorMessage)
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
            }

            UiState.Loading -> {}
        }
    }

    private fun onGetListCollectPoint(uiState: UiState<ListCollectPointResponse>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                val listCollectPointLiveData = uiState.data.data.listItem
                mTagList.clear()
                for (data in listCollectPointLiveData) {
                    val model =
                        SuggestionModel(data.empId, data.name, (data.numAddress.lowercase()))
                    mTagList.add(model)
                }
                if (mTagList.isNotEmpty()) {
                    viewBinding.edtDiaDiem.setTags(mTagList.toList())
                    viewBinding.edtGhiChu.setTags(mTagList.toList())

                    var tagViewAdapter =
                        context?.let {
                            SuggestionAdapter(
                                it,
                                R.layout.item_user_suggestion,
                                mTagList.toList()
                            )
                        }
                    viewBinding.edtDiaDiem.setAdapter(tagViewAdapter)
                    viewBinding.edtGhiChu.setAdapter(tagViewAdapter)
                }

            }

            is UiState.Error -> {
                val errorMessage = uiState.message
                Log.e("SSSSSSSSSSS", errorMessage)
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
            }

            UiState.Loading -> {}
        }
    }

    private fun onAddCollectPoint(uiState: UiState<Any>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "${uiState.data.data}")
                bottomSheetAdd?.dismiss()
            }

            is UiState.Error -> {
                val errorMessage = uiState.message
                Log.e("SSSSSSSSSSS", errorMessage)
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
            }

            UiState.Loading -> {}
        }
    }
    private fun  onAddTask(uiState: UiState<Any>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                clearDataAddTask()
                DialogFactory.showDialogDefaultNotCancel(context, "${uiState.data.data}")
                bottomSheetAdd?.dismiss()
            }

            is UiState.Error -> {
                val errorMessage = uiState.message
                Log.e("SSSSSSSSSSS", errorMessage)
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
            }

            UiState.Loading -> {}
        }
    }

    private fun checkValidate(): Boolean {
        viewBinding.apply {
            return if (edtSelectTask.text.toString().trim().isEmpty()){
                edtSelectTask.background = context?.let { ContextCompat.getDrawable(it,R.drawable.bg_red_) }
                false
            } else{
                edtSelectTask.background = context?.let { ContextCompat.getDrawable(it,R.drawable.bg_item_detail_black) }
                return if (edtSelectNV1.text.toString().trim().isEmpty()){
                    edtSelectNV1.background = context?.let { ContextCompat.getDrawable(it,R.drawable.bg_red_) }
                    false
                } else{
                    edtSelectNV1.background = context?.let { ContextCompat.getDrawable(it,R.drawable.bg_item_detail_black) }
                    return if (edtSelectNV2.text.toString().trim().isEmpty()){
                        edtSelectNV2.background = context?.let { ContextCompat.getDrawable(it,R.drawable.bg_red_) }
                        false
                    } else{
                        edtSelectNV2.background = context?.let { ContextCompat.getDrawable(it,R.drawable.bg_item_detail_black) }
                        return if (edtDiaDiem.text.toString().trim().isEmpty()){
                            edtDiaDiem.background = context?.let { ContextCompat.getDrawable(it,R.drawable.bg_red_) }
                            false
                        } else{
                            edtDiaDiem.background = context?.let { ContextCompat.getDrawable(it,R.drawable.bg_item_detail_black) }
                            return if (edtGhiChu.text.toString().trim().isEmpty()){
                                edtGhiChu.background = context?.let { ContextCompat.getDrawable(it,R.drawable.bg_red_) }
                                false
                            } else{
                                edtGhiChu.background = context?.let { ContextCompat.getDrawable(it,R.drawable.bg_item_detail_black) }
                                return true
                            }
                        }
                    }
                }
            }
        }
    }

    private fun clearDataAddTask() {
        nv1Selected = -1
        nv2Selected = -1
        jobTypeIdSelected = -1
        uuTienIdSelected = 2
        viewBinding.apply {
            edtSelectTask.text = null
            edtSelectNV1.text = null
            edtSelectNV2.text = null
            edtDiaDiem.text.clear()
            edtGhiChu.text.clear()
        }
    }

}

private val uuTienList = ArrayList<ItemViewLocation<ProvinceData>>().apply {
    add(
        ItemViewLocation(
            ProvinceData(
                1,
                "1",
                "Ưu tiên 1"
            )
        )
    )
    add(
        ItemViewLocation(
            ProvinceData(
                2,
                "2",
                "Ưu tiên 2"
            )
        )
    )
    add(
        ItemViewLocation(
            ProvinceData(
                3,
                "3",
                "Ưu tiên 3"
            )
        )
    )

}