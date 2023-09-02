package com.dinhtc.taskmaster.view.fragment

import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dinhtc.taskmaster.R
import com.dinhtc.taskmaster.bottomsheet.BottomSheetAddFreight
import com.dinhtc.taskmaster.bottomsheet.BottomSheetAddVideo
import com.dinhtc.taskmaster.databinding.FragmentDetailBinding
import com.dinhtc.taskmaster.common.view.BaseFragment
import com.dinhtc.taskmaster.common.widgets.elasticviews.ElasticAnimation
import com.dinhtc.taskmaster.common.widgets.spinner.ItemViewLocation
import com.dinhtc.taskmaster.common.widgets.spinner.LocationSpinner
import com.dinhtc.taskmaster.common.widgets.spinner.ProvinceData
import com.dinhtc.taskmaster.model.RoleCode
import com.dinhtc.taskmaster.model.request.DataUpdateJobRequest
import com.dinhtc.taskmaster.model.response.JobDetailsResponse
import com.dinhtc.taskmaster.model.response.ListEmployeeResponse
import com.dinhtc.taskmaster.model.response.ListMaterialResponse
import com.dinhtc.taskmaster.model.response.UpdateJobsResponse
import com.dinhtc.taskmaster.utils.AndroidUtils
import com.dinhtc.taskmaster.utils.DialogFactory
import com.dinhtc.taskmaster.utils.LoadingScreen
import com.dinhtc.taskmaster.utils.SharedPreferencesManager
import com.dinhtc.taskmaster.utils.UiState
import com.dinhtc.taskmaster.utils.observe
import com.dinhtc.taskmaster.view.activity.MainActivity
import com.dinhtc.taskmaster.view.activity.MainActivity.Companion.TAG_LOG
import com.dinhtc.taskmaster.viewmodel.MaterialViewModel
import com.dinhtc.taskmaster.viewmodel.AddTaskViewModel
import com.dinhtc.taskmaster.viewmodel.JobsViewModel
import com.dinhtc.taskmaster.viewmodel.UploadMediaViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MultipartBody

@AndroidEntryPoint
class DetailFragment : BaseFragment<FragmentDetailBinding>() {

    private var nv1Old: Int = -1
    private var uuTienIdSelected: Int = -1
    private var advancePayment: String? = null
    private var totalMoney: String? = null

    private var nvSelectedNew: Int = -1
    private var bottomSheetAddImage: BottomSheetAddVideo? = null
    private var dataResponse: JobDetailsResponse? = null
    private var jobsId: Int = -1
    private var empId: Int = -1
    private var checkCloseVideos: Boolean = false
    private var checkCloseImage: Boolean = false
    private var imagePartLocal: MutableList<MultipartBody.Part>? = null
    private var videoPartLocal: MultipartBody.Part? = null

    private val uploadMediaViewModel: UploadMediaViewModel by viewModels()
    private val addTaskViewModel: AddTaskViewModel by viewModels()
    private val materialViewModel: MaterialViewModel by viewModels()
    private val jobsViewModel: JobsViewModel by viewModels()

    private val dataListJob = ArrayList<ItemViewLocation<ProvinceData>>()
    private val dataListEmployee = ArrayList<ItemViewLocation<ProvinceData>>()

    override val layoutResourceId: Int
        get() = R.layout.fragment_detail

    override fun onViewCreated() {
        var jobIdNoti = arguments?.getString(MainActivity.JOB_ID)
        jobsId = if (jobIdNoti?.isNotEmpty() == true){
            jobIdNoti.toInt()
        }else {
            arguments?.getInt(HomeFragment.ID_JOB)!!
        }
        empId = arguments?.getInt(HomeFragment.ID_JOB)!!

        jobsViewModel.getJobDetails(idJob = jobsId, empId = empId)
        addTaskViewModel.getListJobType()
        materialViewModel.getListMaterial()
        addTaskViewModel.getListEmployeeByJobId(jobId = jobsId)


        observe(jobsViewModel.dataJobDetail, ::dataJobDetailLive)
        observe(uploadMediaViewModel.dataUpLoadImage, ::responseUploadMultiImage)
        observe(materialViewModel.dataListMaterial, ::onGetListMaterialLive)
        observe(materialViewModel.datAddMaterial, ::addMaterialLive)
        observe(jobsViewModel.updateStateJob, ::updateStateJobLive)
        //observe(addTaskViewModel.dataEmployee, ::onGetListEmployee)
        observe(addTaskViewModel.dataEmployeeByJobId, ::dataEmployeeByJobId)
        observe(jobsViewModel.updateJobDetails, ::updateJobDetailsLive)

        onClickItem()
        viewBinding.selectNV.setOnItemSelectedListener(mOnSelectedNV1Listener)
        viewBinding.edtSelectUuTien.setData(uuTienList)
        viewBinding.edtSelectUuTien.setOnItemSelectedListener(mOnSelectedUuTienListener)
        checkRole()
    }

    private fun checkRole() {
        viewBinding.apply {
            when (SharedPreferencesManager.instance.getString(
                SharedPreferencesManager.ROLE_CODE,
                ""
            ) ?: "") {
                RoleCode.MASTER.name -> {
                    btnDaLamGon.isEnabled = true
                    btnDaLamGon.alpha = 1f

                    btnDaCan.isEnabled = true
                    btnDaCan.alpha = 1f

                    btnDaXong.isEnabled = true
                    btnDaXong.alpha = 1f
                }

                RoleCode.COLLECTOR.name -> {
                    btnDaLamGon.isEnabled = true
                    btnDaLamGon.alpha = 1f

                    btnDaCan.isEnabled = true
                    btnDaCan.alpha = 1f

                    btnDaXong.isEnabled = false
                    btnDaXong.alpha = 0.8f
                }

                RoleCode.ADMIN.name, RoleCode.LEADER.name, RoleCode.EMPLOYEE.name, RoleCode.CUSTOMER.name -> {
                    btnDaLamGon.isEnabled = false
                    btnDaLamGon.alpha = 0.8f

                    btnDaCan.isEnabled = false
                    btnDaCan.alpha = 0.8f

                    btnDaXong.isEnabled = false
                    btnDaXong.alpha = 0.8f
                }

                RoleCode.DRIVER.name -> {
                    btnDaLamGon.isEnabled = false
                    btnDaLamGon.alpha = 0.8f

                    btnDaCan.isEnabled = true
                    btnDaCan.alpha = 1f

                    btnDaXong.isEnabled = true
                    btnDaXong.alpha = 1f
                }
            }
        }
    }

    private val mOnSelectedNV1Listener =
        object : LocationSpinner.OnItemSelectedListener<ProvinceData> {
            override fun onItemSelected(
                parent: LocationSpinner<ProvinceData>,
                position: Int,
                item: ItemViewLocation<ProvinceData>?
            ) {
                nvSelectedNew = item?.data?.id!!
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

    private fun onClickItem() {
        viewBinding.apply {
            edtNVUng.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (edtNVUng.money > 0) {
                        viewBinding.apply {
                            radioChuaThanhToan.isEnabled = false
                            radioChuyenKhoan.isEnabled = false
                        }
                    } else {
                        viewBinding.apply {
                            radioChuaThanhToan.isEnabled = true
                            radioChuyenKhoan.isEnabled = true
                        }
                    }
                }

            })

            btnVatLieu.setOnClickListener {
                if (dataResponse != null) {
                    if (dataResponse!!.jobMaterial.isNotEmpty()) {
                        if (activity is MainActivity) {
                            (activity as MainActivity).sharedViewModel?.setShareListJobMaterial(
                                dataResponse!!.jobMaterial
                            )
                        }
                        findNavController().navigate(R.id.action_detailFragment_to_materialDetailFragment)
                    } else {
                        if (dataListJob.isNotEmpty()) {
                            showDialogAddVatLieu()
                        }
                    }
                }
            }

            btnAnh.setOnClickListener {
                if (dataResponse != null) {
                    if (dataResponse!!.jobMedia.isNotEmpty()) {
                        if (activity is MainActivity) {
                            (activity as MainActivity).sharedViewModel?.setShareListJobMedia(
                                dataResponse!!.jobMedia
                            )
                        }
                        findNavController().navigate(R.id.action_detailFragment_to_mediaDetailFragment)
                    } else {
                        if (dataListJob.isNotEmpty()) {
                            showDialogAddImage()
                        }
                    }
                }
            }
            var dateCreate = "dateCreate"
            btnDaLamGon.setOnClickListener {
                jobsViewModel.updateStateJob(jobsId, DALAMGON, dateCreate)
            }
            btnDaCan.setOnClickListener {
                if (dataResponse != null) {
                    if (dataResponse!!.jobMaterial.isNotEmpty() && dataResponse!!.jobMedia.isNotEmpty()) {
                        jobsViewModel.updateStateJob(jobsId, DACAN, dateCreate)
                    } else {
                        DialogFactory.showDialogDefaultNotCancel(context, "Thiếu Vật liệu/ Ảnh")
                    }
                }
            }

            btnDaXong.setOnClickListener {
                jobsViewModel.updateStateJob(jobsId, XONG, dateCreate)
            }

            btnSubmit.setOnClickListener {
                if (viewBinding.tvPrice.text.toString().trim().isNotEmpty()) {
                    totalMoney = viewBinding.tvPrice.text.toString().trim()
                }
                if (viewBinding.edtNVUng.money.toString().trim().isNotEmpty()) {
                    advancePayment = viewBinding.edtNVUng.money.toString().trim()
                }

                val dataUpdate = DataUpdateJobRequest(
                    totalMoney = totalMoney,
                    statusPayment = 1, //chuyển khoản là 0, chưa thanh toán là 1
                    advancePayment = advancePayment,
                    priority = uuTienIdSelected,
                    empOldId = nv1Old,
                    empNewId = nvSelectedNew,
                    note = viewBinding.edtGhiChu.text.toString(),
                )
                jobsViewModel.updateJobDetails(dataUpdate)
            }
        }
    }

    private fun showDialogAddImage() {
        bottomSheetAddImage = BottomSheetAddVideo(
            "Thêm",
            { imageParts ->
                imagePartLocal = imageParts
                checkCloseImage = true
            },
            { videoPart ->
                videoPartLocal = videoPart
                checkCloseVideos = true
            },
            {
                if (checkCloseVideos && checkCloseImage) {
                    uploadMediaViewModel.uploadMultiImageVideo(
                        jobsId,
                        imagePartLocal,
                        videoPartLocal,
                        1
                    )
                } else if (checkCloseImage && !checkCloseVideos) {
                    uploadMediaViewModel.uploadMultiImage(
                        jobsId,
                        imagePartLocal,
                        1
                    )
                } else if (!checkCloseImage && checkCloseVideos) {
                    uploadMediaViewModel.uploadVideo(
                        jobsId,
                        videoPartLocal,
                        2
                    )
                }
            }, { //close image
                imagePartLocal?.clear()
                checkCloseImage = false
            }, { //close videos
                checkCloseVideos = false
            }, {})
        bottomSheetAddImage?.isCancelable = false
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        activity?.supportFragmentManager?.let {
            bottomSheetAddImage?.show(
                it,
                bottomSheetAddImage?.tag
            )
        }
    }

    private fun showDialogAddVatLieu() {
        val bottomSheetAddVatLieu = context?.let {
            BottomSheetAddFreight(it, dataListJob, jobsId) { dataAdd ->

                materialViewModel.addMaterial(dataAdd)
            }
        }
        bottomSheetAddVatLieu?.isCancelable = false
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        activity?.supportFragmentManager?.let {
            bottomSheetAddVatLieu?.show(
                it,
                bottomSheetAddVatLieu.tag
            )
        }
    }

    private fun onGetListEmployee(uiState: UiState<ListEmployeeResponse>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                val listEmpLiveData = uiState.data.data.listItem
                for (data in listEmpLiveData) {
                    dataListEmployee.add(
                        ItemViewLocation(
                            ProvinceData(
                                data.empId,
                                "${data.empId}",
                                "${data.name}"
                            )
                        )
                    )
                }
                viewBinding.selectNV.setData(dataListEmployee)
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

    private fun dataEmployeeByJobId(uiState: UiState<ListEmployeeResponse>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                val listEmpLiveData = uiState.data.data.listItem
                for (data in listEmpLiveData) {
                    dataListEmployee.add(
                        ItemViewLocation(
                            ProvinceData(
                                data.empId,
                                "${data.empId}",
                                "${data.name}"
                            )
                        )
                    )
                }
                viewBinding.selectNV.setData(dataListEmployee)
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

    private fun responseUploadMultiImage(uiState: UiState<Any>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancelAndClick(context, "${uiState.data.data}") {
                    jobsViewModel.getJobDetails(idJob = jobsId, empId = empId)
                    bottomSheetAddImage?.dismiss()
                }
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

    private fun onGetListMaterialLive(uiState: UiState<ListMaterialResponse>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                val listMaterialLiveData = uiState.data.data.listItem
                for (data in listMaterialLiveData) {
                    dataListJob.add(
                        ItemViewLocation(
                            ProvinceData(
                                data.mate_id,
                                "${data.mate_id}",
                                "${data.name}"
                            )
                        )
                    )
                }
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

    private fun addMaterialLive(uiState: UiState<Any>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "${uiState.data.data}")
                jobsViewModel.getJobDetails(idJob = jobsId, empId = empId)
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

    private fun dataJobDetailLive(uiState: UiState<JobDetailsResponse>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                dataResponse = uiState.data.data
                viewBinding.apply {
                    tvIdJob.text = "${dataResponse?.jobId}"
                    tvStateDecs.text = dataResponse?.stateDecs
                    tvNamePoint.text = dataResponse?.namePoint
                    tvNameAddress.text = dataResponse?.numAddress
                    edtSelectUuTien.text = "Ưu tiên ${dataResponse?.priority}"
                    if (dataResponse?.jobStateId == 30) {
                        edtNVUng.isEnabled = false
                        edtNVUng.visibility = View.GONE
                        tvNVUng.visibility = View.VISIBLE

                        selectNV.isEnabled = false
                        selectNV.visibility = View.GONE
                        tvSelectNV.visibility = View.VISIBLE

                        edtGhiChu.isEnabled = false
                        radioChuyenKhoan.isEnabled = false
                        radioChuaThanhToan.isEnabled = false

                        edtSelectUuTien.isEnabled = false
                        edtSelectUuTien.visibility = View.GONE
                        tvUuTien.visibility = View.VISIBLE
                    } else {
                        edtNVUng.isEnabled = true
                        edtNVUng.visibility = View.VISIBLE
                        tvNVUng.visibility = View.GONE

                        selectNV.isEnabled = true
                        selectNV.visibility = View.VISIBLE
                        tvSelectNV.visibility = View.GONE

                        edtGhiChu.isEnabled = true
                        radioChuyenKhoan.isEnabled = true
                        radioChuaThanhToan.isEnabled = true

                        edtSelectUuTien.isEnabled = true
                        edtSelectUuTien.visibility = View.VISIBLE
                        tvUuTien.visibility = View.GONE
                    }
                }
                if (dataResponse?.employeeJobs?.isNotEmpty() == true) {
                    if (dataResponse!!.employeeJobs.size == 1) {
                        viewBinding.tvNV1.text = dataResponse!!.employeeJobs[0].name
                        nv1Old = dataResponse!!.employeeJobs[0].empId
                    }
                    if (dataResponse!!.employeeJobs.size == 2) {
                        viewBinding.tvNV1.text = dataResponse!!.employeeJobs[0].name
                        viewBinding.tvNV2.text = dataResponse!!.employeeJobs[1].name
                    }
                    if (dataResponse!!.employeeJobs.size == 3) {
                        viewBinding.tvNV1.text = dataResponse!!.employeeJobs[0].name
                        viewBinding.tvNV2.text = dataResponse!!.employeeJobs[1].name
                        viewBinding.tvNV3.text = dataResponse!!.employeeJobs[2].name
                    }
                }
                jobsId = uiState.data.data.jobId

                if (dataResponse?.jobMedia?.isNotEmpty() == true) {
                    for (data in dataResponse!!.jobMedia) {
                        if (data.mediaType == 2) {
                            checkCloseVideos = true
                        }
                        if (data.mediaType == 1) {
                            checkCloseImage = true
                        }
                    }
                }
                var moneyTemp: Long = 0
                if (dataResponse?.jobMaterial?.isNotEmpty() == true) {
                    for (data in dataResponse!!.jobMaterial) {
                        moneyTemp += data.price
                    }
                    Log.e(TAG_LOG, "$moneyTemp")
                    viewBinding.tvPrice.text = AndroidUtils.formatMoneyCard("$moneyTemp")
                } else {
                    viewBinding.tvPrice.text = AndroidUtils.formatMoneyCard("0")
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

    private fun updateStateJobLive(uiState: UiState<UpdateJobsResponse>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancelAndClick(
                    context,
                    "${uiState.data.data.description}"
                ) {
                    jobsViewModel.getJobDetails(jobsId, empId = empId)
                    ElasticAnimation(viewBinding.tvStateDecs).setScaleX(0.75f).setScaleY(0.75f)
                        .setDuration(
                            500
                        ).doAction()
                    viewBinding.tvStateDecs.setTypeface(
                        viewBinding.tvStateDecs.typeface,
                        Typeface.BOLD
                    )
                    viewBinding.tvStateDecs.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.custom_error_color
                        )
                    )
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

    private fun updateJobDetailsLive(uiState: UiState<Any>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "${uiState.data.data}")
                jobsViewModel.getJobDetails(idJob = jobsId, empId = empId)
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

    companion object {
        val MOI = 1
        val DAGIAO = 5
        val CHAPNHAN = 10
        val DALAMGON = 15
        val DACAN = 20
        val DALENXE = 25
        val XONG = 30
        val TUCHOI = 35
        val HUY = 37

        val BUNDLE_MEDIA = "BUNDLE_MEDIA"
        val BUNDLE_MATERIAL = "BUNDLE_MATERIAL"
    }
}