package com.elogictics.taskmaster.view.fragment

import android.text.Html
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.elogictics.taskmaster.R
import com.elogictics.taskmaster.adapter.SuggestionAdapter
import com.elogictics.taskmaster.adapter.SuggestionNoteAdapter
import com.elogictics.taskmaster.bottomsheet.BottomSheetAddCollectPoint
import com.elogictics.taskmaster.databinding.FragmentAddTaskBinding
import com.elogictics.taskmaster.common.view.BaseFragment
import com.elogictics.taskmaster.common.widgets.spinner.ItemViewLocation
import com.elogictics.taskmaster.common.widgets.spinner.LocationSpinner
import com.elogictics.taskmaster.common.widgets.spinner.ProvinceData
import com.elogictics.taskmaster.model.RoleCode
import com.elogictics.taskmaster.model.SuggestionModel
import com.elogictics.taskmaster.model.SuggestionNoteModel
import com.elogictics.taskmaster.model.request.AddTaskRequest
import com.elogictics.taskmaster.model.response.ListCollectPointResponse
import com.elogictics.taskmaster.model.response.ListEmployeeResponse
import com.elogictics.taskmaster.model.response.ListJobTypeResponse
import com.elogictics.taskmaster.utils.DialogFactory
import com.elogictics.taskmaster.utils.LoadingScreen
import com.elogictics.taskmaster.utils.SharedPreferencesManager
import com.elogictics.taskmaster.utils.UiState
import com.elogictics.taskmaster.utils.observe
import com.elogictics.taskmaster.view.activity.MainActivity.Companion.TAG_ERROR
import com.elogictics.taskmaster.viewmodel.AddTaskViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddTaskFragment : BaseFragment<FragmentAddTaskBinding>() {

    private var bottomSheetAdd: BottomSheetAddCollectPoint? = null
    val mTagList: MutableList<SuggestionModel> = mutableListOf()
    val mTagListNote: MutableList<SuggestionModel> = mutableListOf()
    private val addTaskViewModel: AddTaskViewModel by viewModels()
    private val dataListJob = ArrayList<ItemViewLocation<ProvinceData>>()
    private val dataListEmployeeNV1 = ArrayList<ItemViewLocation<ProvinceData>>()
    private val dataListEmployeeNV2 = ArrayList<ItemViewLocation<ProvinceData>>()

    private var nv1Selected = -1
    private var nv2Selected = -1
    private var jobTypeIdSelected = 1
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

        observe(addTaskViewModel.combinedData, ::combinedDataLive)

        viewBinding.apply {
            val labelNV1 = "Nhân Viên 1:<font color='#FF0000'><sup>*</sup></font>"
            val labelDD = "Địa điểm<font color='#FF0000'><sup>*</sup></font>"

            // Sử dụng Html.fromHtml để hiển thị văn bản HTML trong TextView
            tvLabelNV1.text = Html.fromHtml(labelNV1, Html.FROM_HTML_MODE_COMPACT)
            tvDiaDiem.text = Html.fromHtml(labelDD, Html.FROM_HTML_MODE_COMPACT)
        }
    }

    private fun onClickItem() {
        viewBinding.layoutToolBar.apply {
            titleToolBar.text = "Giao việc"
            imgBackParent.setOnClickListener {
                findNavController().popBackStack()
            }
            imgHome.setOnClickListener {
                findNavController().popBackStack(R.id.mainFragment,false)
            }
        }
        viewBinding.apply {
            when (SharedPreferencesManager.instance.getString(SharedPreferencesManager.ROLE_CODE, "") ?: "") {
                RoleCode.ADMIN.name, RoleCode.LEADER.name, RoleCode.MASTER.name -> {
                    btnAddDiaDiem.visibility = View.VISIBLE
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
                }else ->{
                    btnAddDiaDiem.visibility = View.GONE
                }
            }

            btnSubmit.setOnClickListener {
                if (checkValidate()) {
                    val addTaskRequest = AddTaskRequest(
                        jobTypeIdSelected,
                        1,
                        nv1Selected,
                        nv2Selected,
                        SharedPreferencesManager.instance.getInt(
                            SharedPreferencesManager.USER_ID,0),
                        viewBinding.edtDiaDiem.ids,
                        viewBinding.edtGhiChu.inputText
                    )
                    addTaskViewModel.addTask(addTaskRequest)
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
                Log.e(TAG_ERROR, "onGetListJobType: $errorMessage")
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
            }

            UiState.Loading -> {
                LoadingScreen.displayLoadingWithText(
                    requireContext(),
                    "Vui lòng chờ...",
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

                    val model = SuggestionModel(data.empId, data.name, (data.phone.lowercase()))
                    mTagListNote.add(model)
                }

                viewBinding.edtSelectNV1.setData(dataListEmployeeNV1)
            }

            is UiState.Error -> {
                val errorMessage = uiState.message
                Log.e(TAG_ERROR, "onGetListEmployee: $errorMessage")
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
                Log.e(TAG_ERROR, "onGetListEmployeeByNotId: $errorMessage")
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
                val listCollectPointLiveData = uiState.data.data.listItem.sortedBy { it.name }
                mTagList.clear()
                for (data in listCollectPointLiveData) {
                    val model =
                        SuggestionModel(data.collectPointId, data.name, (data.numAddress.lowercase()))
                    mTagList.add(model)
                }
                if (mTagList.isNotEmpty()) {
                    viewBinding.edtDiaDiem.setTags(mTagList.toList())
                    //viewBinding.edtGhiChu.setTags(mTagList.toList())

                    var tagViewAdapter =
                        context?.let {
                            SuggestionAdapter(
                                it,
                                R.layout.item_user_suggestion,
                                mTagList.toList()
                            )
                        }
                    viewBinding.edtDiaDiem.setAdapter(tagViewAdapter)
                    //viewBinding.edtGhiChu.setAdapter(tagViewAdapter)
                }
            }

            is UiState.Error -> {
                val errorMessage = uiState.message
                Log.e(TAG_ERROR, "onGetListCollectPoint: $errorMessage")
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
            }

            UiState.Loading -> {}
        }
    }

    private fun combinedDataLive(uiState: MutableList<SuggestionNoteModel>){
        viewBinding.edtGhiChu.setTags(uiState.toList())

        var suggetdapter =
            context?.let {
                SuggestionNoteAdapter(
                    it,
                    R.layout.item_user_suggestion,
                    uiState.toList()
                )
            }
        viewBinding.edtGhiChu.setAdapter(suggetdapter)
        suggetdapter?.notifyDataSetChanged()
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
                Log.e(TAG_ERROR, "onAddCollectPoint: $errorMessage")
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
            }

            UiState.Loading -> {}
        }
    }

    private fun onAddTask(uiState: UiState<Any>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                clearDataAddTask()
                DialogFactory.showDialogDefaultNotCancel(context, "${uiState.data.data}")
                bottomSheetAdd?.dismiss()
            }

            is UiState.Error -> {
                val errorMessage = uiState.message
                Log.e(TAG_ERROR, "onAddTask: $errorMessage")
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
            }

            UiState.Loading -> {
                LoadingScreen.displayLoadingWithText(context,"Đang giao việc.",false)
            }
        }
    }

    private fun checkValidate(): Boolean {
        viewBinding.apply {
           return if (edtSelectNV1.text.toString().trim().isEmpty()) {
                edtSelectNV1.background =
                    context?.let { ContextCompat.getDrawable(it, R.drawable.bg_red_) }
                false
            } else {
                edtSelectNV1.background = context?.let {
                    ContextCompat.getDrawable(
                        it,
                        R.drawable.bg_item_detail_black
                    )
                }
                return if (edtDiaDiem.text.toString().trim().isEmpty()) {
                    edtDiaDiem.background =
                        context?.let { ContextCompat.getDrawable(it, R.drawable.bg_red_) }
                    false
                } else {
                    edtDiaDiem.background = context?.let {
                        ContextCompat.getDrawable(
                            it,
                            R.drawable.bg_item_detail_black
                        )
                    }
                    return true
                }
            }
        }
    }

    private fun clearDataAddTask() {
        nv1Selected = -1
        nv2Selected = -1
        jobTypeIdSelected = 1
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

val uuTienList = ArrayList<ItemViewLocation<ProvinceData>>().apply {
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