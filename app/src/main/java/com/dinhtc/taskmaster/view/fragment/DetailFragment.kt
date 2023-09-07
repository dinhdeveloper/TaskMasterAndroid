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
import com.dinhtc.taskmaster.common.view.BaseFragment
import com.dinhtc.taskmaster.common.widgets.elasticviews.ElasticAnimation
import com.dinhtc.taskmaster.common.widgets.spinner.ItemViewLocation
import com.dinhtc.taskmaster.common.widgets.spinner.LocationSpinner
import com.dinhtc.taskmaster.common.widgets.spinner.ProvinceData
import com.dinhtc.taskmaster.databinding.FragmentDetailBinding
import com.dinhtc.taskmaster.model.JobEmployeeDetailResponse
import com.dinhtc.taskmaster.model.JobStateCode.*
import com.dinhtc.taskmaster.model.RoleCode
import com.dinhtc.taskmaster.model.request.DataUpdateJobRequest
import com.dinhtc.taskmaster.model.request.UpdateStateRequest
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
import com.dinhtc.taskmaster.viewmodel.AddTaskViewModel
import com.dinhtc.taskmaster.viewmodel.JobsViewModel
import com.dinhtc.taskmaster.viewmodel.MaterialViewModel
import com.dinhtc.taskmaster.viewmodel.UploadMediaViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MultipartBody


@AndroidEntryPoint
class DetailFragment : BaseFragment<FragmentDetailBinding>() {

    private var paymentMethod: Int = 0
    private var paymentStateStatus: Int = 0
    private var checkSelectedRadio: Boolean = false
    private val listEmployeeJobs = mutableListOf<JobEmployeeDetailResponse>()
    private var nv1Old: Int = -1
    private var nv2Old: Int = -1
    private var nv3Old: Int = -1
    private var uuTienIdSelected: Int = -1
    private var amountPaidEmp: Long? = 0
    private var totalMoney: Long? = 0

    private var nvSelectedNew: Int = -1
    private var bottomSheetAddImage: BottomSheetAddVideo? = null
    private var bottomSheetAddVatLieu: BottomSheetAddFreight? = null

    private var dataResponse: JobDetailsResponse? = null
    private var jobsId: Int = -1
    private var empId: Int = -1
    val empAssignId = SharedPreferencesManager.instance.getInt(SharedPreferencesManager.USER_ID,-1)
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
        var jobIdNotify = arguments?.getString(MainActivity.ID_JOB_NOTIFY)
        jobsId = if (jobIdNotify?.isNotEmpty() == true) {
            jobIdNotify.toInt()
        } else {
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
        observe(addTaskViewModel.dataEmployee, ::onGetListEmployee)
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

            layoutChuyenKhoan.setOnCheckedChangeListener { group, checkedId ->
                run {
                    when (checkedId) {
                        R.id.radioChuyenKhoan -> {
                            checkSelectedRadio = true
                            paymentMethod = 2 //bank
                            paymentStateStatus = 1 // da thanh toan
                            viewBinding.layoutChuyenKhoan.background = context?.let { ContextCompat.getDrawable(it,R.drawable.bg_while) }
                        }
                        R.id.radioChuaThanhToan -> {
                            checkSelectedRadio = true
                            paymentMethod = -1 // chua thanh toan
                            paymentStateStatus = 0 //chua thanh toan
                            viewBinding.layoutChuyenKhoan.background = context?.let { ContextCompat.getDrawable(it,R.drawable.bg_while) }
                        }
                    }
                }
            }

            when (SharedPreferencesManager.instance.getString(
                SharedPreferencesManager.ROLE_CODE,
                ""
            )) {
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
        var dateCreate = "dateCreate"

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
                            checkSelectedRadio = false
                            radioChuyenKhoan.isSelected = false
                            radioChuaThanhToan.isSelected = false

                            radioChuaThanhToan.isEnabled = false
                            radioChuyenKhoan.isEnabled = false
                            amountPaidEmp
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
                        showDialogAddImage()
                    }
                }
            }

            btnDaLamGon.setOnClickListener {
                val dataUpdate = UpdateStateRequest(
                    jobsId = jobsId,
                    stateJob = DALAMGON,
                    paymentMethod = paymentMethod,
                    paymentStateStatus = paymentStateStatus,
                    amountPaidEmp = 0,
                    amountTotal = 0,
                    dateCreate = dateCreate
                )
                jobsViewModel.updateStateJob(dataUpdate)
            }

            btnDaCan.setOnClickListener {
                getDataSelected()
                if (dataResponse != null) {
                    if (dataResponse!!.jobMaterial.isNotEmpty() && dataResponse!!.jobMedia.isNotEmpty()) {
                        if (checkBank()) {
                            val dataUpdate = UpdateStateRequest(
                                jobsId = jobsId,
                                stateJob = DACAN,
                                paymentMethod = paymentMethod,
                                paymentStateStatus = paymentStateStatus,
                                amountPaidEmp = amountPaidEmp!!,
                                amountTotal = totalMoney!!,
                                dateCreate = dateCreate
                            )
                            jobsViewModel.updateStateJob(dataUpdate)
                        } else {
                            viewBinding.layoutChuyenKhoan.background = context?.let { ContextCompat.getDrawable(it,R.drawable.bg_red_) }
                        }
                    } else {
                        DialogFactory.showDialogDefaultNotCancel(context, "Thiếu Vật liệu/ Ảnh")
                    }
                }
            }

            btnDaXong.setOnClickListener {
                getDataSelected()

                val dataUpdate = UpdateStateRequest(
                    jobsId = jobsId,
                    stateJob = XONG,
                    paymentMethod = paymentMethod,
                    paymentStateStatus = paymentStateStatus,
                    amountPaidEmp = amountPaidEmp!!,
                    amountTotal = totalMoney!!,
                    dateCreate = dateCreate
                )
                jobsViewModel.updateStateJob(dataUpdate)
            }

            btnSubmit.setOnClickListener {
                if (viewBinding.tvPrice.text.toString().trim().isNotEmpty()) {
                    totalMoney = AndroidUtils.getMoneyRealValue(viewBinding.tvPrice.text.toString().trim())
                }
                if (viewBinding.edtNVUng.money.toString().trim().isNotEmpty()) {
                    amountPaidEmp = viewBinding.edtNVUng.money.toString().trim().toLong()
                }
                val dataUpdate = DataUpdateJobRequest(
                    jodId = jobsId,
                    totalMoney = AndroidUtils.decodeMoneyStr(totalMoney.toString()),
                    statusPayment = 1, //chuyển khoản là 0, chưa thanh toán là 1
                    amountPaidEmp = amountPaidEmp,
                    priority = uuTienIdSelected,
                    empOldId = nv1Old,
                    empNewId = nvSelectedNew,
                    empAssignId = empAssignId,
                    note = viewBinding.edtGhiChu.text.toString(),
                )
                jobsViewModel.updateJobDetails(dataUpdate)
            }
        }
    }

    private fun checkBank(): Boolean {
        return if (!checkSelectedRadio) {
            viewBinding.edtNVUng.text.toString().trim().isNotEmpty()
        }else {
            true
        }
    }

    private fun getDataSelected() {
        viewBinding.apply {
            if (tvPrice.text.toString().trim().isNotEmpty()) {
                totalMoney = AndroidUtils.getMoneyRealValue(tvPrice.text.toString().trim())
            }
            if (edtNVUng.money.toString().trim().isNotEmpty()) {
                amountPaidEmp = edtNVUng.money.toString().trim().toLong()
            }

            if (radioChuyenKhoan.isChecked){
                paymentMethod = 2 //bank
                paymentStateStatus = 1 // da thanh toan
            }
            if (radioChuaThanhToan.isChecked){
                paymentMethod = -1 // chua thanh toan
                paymentStateStatus = 0 //chua thanh toan
            }
            if (edtNVUng.text.toString().trim().isNotBlank() && !checkSelectedRadio){
                paymentMethod = 1 //cash
                paymentStateStatus = 1 // da thanh toan
            }
        }
    }

    private fun checkValidateEmp(): Boolean {
        for (data in listEmployeeJobs) {
            return data.empId != nvSelectedNew
        }
        return false
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
        bottomSheetAddVatLieu = context?.let {
            BottomSheetAddFreight(it, dataListJob, jobsId) { dataAdd ->

                materialViewModel.addMaterial(dataAdd)
            }
        }
        bottomSheetAddVatLieu?.isCancelable = false
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        activity?.supportFragmentManager?.let {
            bottomSheetAddVatLieu?.show(
                it,
                bottomSheetAddVatLieu?.tag
            )
        }
    }

    private fun onGetListEmployee(uiState: UiState<ListEmployeeResponse>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                val listEmpLiveData = uiState.data.data.listItem
                for (data in listEmpLiveData) {
                    if (nv1Old != -1 && nv1Old != data.empId ||
                        nv2Old != -1 && nv2Old != data.empId ||
                        nv3Old != -1 && nv3Old != data.empId ){
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
                    "Vui lòng chờ...",
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
                    "Vui lòng chờ...",
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
                bottomSheetAddVatLieu?.deleteDataInsert()
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
                dataResponse.let {
                    updateUI(dataResponse!!)
                    changeStatusUI(dataResponse!!)
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

    private fun updateUI(dataResponse: JobDetailsResponse) {
        viewBinding.apply {
            tvIdJob.text = "${dataResponse.jobId}"
            tvStateDecs.text = dataResponse.stateDecs
            tvNamePoint.text = dataResponse.namePoint
            tvNameAddress.text = dataResponse.numAddress

            if (dataResponse.amountPaidEmp != 0L){
                edtNVUng.visibility = View.GONE
                tvNVUng.visibility = View.VISIBLE
                tvNVUng.text = AndroidUtils.formatMoneyCard(dataResponse.amountPaidEmp.toString())
            }else{
                edtNVUng.visibility = View.VISIBLE
                tvNVUng.visibility = View.GONE
            }

            if (dataResponse.priority != 0 && dataResponse.jobStateId >= 30){
                uuTienIdSelected = dataResponse.priority
                edtSelectUuTien.visibility = View.GONE
                tvUuTien.visibility = View.VISIBLE
                tvUuTien.text = "Ưu tiên ${dataResponse.priority}"
            }else{
                edtSelectUuTien.text = "Ưu tiên ${dataResponse.priority}"
                edtSelectUuTien.visibility = View.VISIBLE
                tvUuTien.visibility = View.GONE
            }

            if (dataResponse.jobStateId >= 30){
                if (dataResponse.paymentMethod == 2 && dataResponse.paymentStateId == 1) {
                    layoutChuyenKhoan.visibility = View.GONE
                    layoutStatusPayment.visibility = View.VISIBLE
                    tvStatusPayment.text = "Chuyển khoản"
                } else if (dataResponse.paymentMethod == -1 && dataResponse.paymentStateId == 0) {
                    layoutChuyenKhoan.visibility = View.GONE
                    layoutStatusPayment.visibility = View.VISIBLE
                    tvStatusPayment.text = "Chưa thanh toán"
                } else if (dataResponse.paymentMethod == 1 && dataResponse.paymentStateId == 1) {
                    layoutChuyenKhoan.visibility = View.GONE
                    layoutStatusPayment.visibility = View.VISIBLE
                    tvStatusPayment.text = "Nhân viên ứng"
                }
            }


            if (dataResponse.employeeJobs.isNotEmpty()) {
                for (data in dataResponse.employeeJobs) {
                    listEmployeeJobs.add(data)
                }
                if (dataResponse.employeeJobs.size == 1) {
                    tvNV1.text = dataResponse.employeeJobs[0].name
                    nv1Old = dataResponse.employeeJobs[0].empId
                }
                if (dataResponse.employeeJobs.size == 2) {
                    tvNV1.text = dataResponse.employeeJobs[0].name
                    tvNV2.text = dataResponse.employeeJobs[1].name

                    nv1Old = dataResponse.employeeJobs[0].empId
                    nv2Old = dataResponse.employeeJobs[1].empId
                }
                if (dataResponse.employeeJobs.size == 3) {
                    tvNV1.text = dataResponse.employeeJobs[0].name
                    tvNV2.text = dataResponse.employeeJobs[1].name
                    tvNV3.text = dataResponse.employeeJobs[2].name

                    nv1Old = dataResponse.employeeJobs[0].empId
                    nv2Old = dataResponse.employeeJobs[1].empId
                    nv3Old = dataResponse.employeeJobs[2].empId
                }
            }

            var moneyTemp: Long = 0
            if (dataResponse.jobMaterial.isNotEmpty()) {
                for (data in dataResponse.jobMaterial) {
                    moneyTemp += data.price
                }
                Log.e(TAG_LOG, "$moneyTemp")
                tvPrice.text = AndroidUtils.formatMoneyCard("$moneyTemp")
            } else {
                tvPrice.text = AndroidUtils.formatMoneyCard("0")
            }
        }
        jobsId = dataResponse.jobId

        if (dataResponse.jobMedia.isNotEmpty()) {
            for (data in dataResponse.jobMedia) {
                if (data.mediaType == 2) {
                    checkCloseVideos = true
                }
                if (data.mediaType == 1) {
                    checkCloseImage = true
                }
            }
        }
    }

    private fun changeStatusUI(dataResponse: JobDetailsResponse) {
        viewBinding.apply {
            when (dataResponse.jobStateId) {
                1, 5, 10 -> {
                    btnDaLamGon.isEnabled = true
                    btnDaLamGon.alpha = 1f

                    btnDaXong.isEnabled = false
                    btnDaXong.alpha = 0.7f

                    btnDaCan.isEnabled = false
                    btnDaCan.alpha = 0.7f
                }

                15 -> {
                    btnDaLamGon.isEnabled = false
                    btnDaLamGon.alpha = 0.7f

                    btnDaXong.isEnabled = true
                    btnDaXong.alpha = 1f

                    btnDaCan.isEnabled = true
                    btnDaCan.alpha = 1f
                }
                20, 25 -> {
                    btnDaLamGon.isEnabled = false
                    btnDaLamGon.alpha = 0.7f

                    btnAnh.isEnabled = false
                    btnAnh.alpha = 0.7f

                    btnVatLieu.isEnabled = false
                    btnVatLieu.alpha = 0.7f

                    btnDaCan.isEnabled = false
                    btnDaCan.alpha = 0.7f

                    btnDaXong.isEnabled = true
                    btnDaXong.alpha = 1f
                }
                30 -> {
                    btnDaLamGon.isEnabled = false
                    btnDaLamGon.alpha = 0.7f

                    btnAnh.isEnabled = false
                    btnAnh.alpha = 0.7f

                    btnVatLieu.isEnabled = false
                    btnVatLieu.alpha = 0.7f

                    btnDaCan.isEnabled = false
                    btnDaCan.alpha = 0.7f

                    btnDaXong.isEnabled = false
                    btnDaXong.alpha = 0.7f

                    btnDaLamGon.visibility = View.GONE
                    btnAnh.visibility = View.GONE
                    btnVatLieu.visibility = View.GONE
                    btnDaCan.visibility = View.GONE
                    btnDaXong.visibility = View.GONE
                    btnSubmit.visibility = View.GONE

                    //read only
                    edtSelectUuTien.visibility = View.GONE
                    tvUuTien.visibility = View.VISIBLE
                    tvUuTien.text = "Ưu tiên ${dataResponse.priority}"

//                    edtNVUng.visibility = View.GONE
//                    tvNVUng.visibility = View.VISIBLE
//                    if (dataResponse.amountPaidEmp != 0L) {
//                        tvNVUng.text = AndroidUtils.formatMoneyCard("${dataResponse.amountPaidEmp} VNĐ")
//                    }

                    layoutChuyenViecToi.visibility = View.GONE

                    tvGhiChu.visibility = View.VISIBLE
                    edtGhiChu.visibility = View.GONE
                    tvGhiChu.text = dataResponse.noteJob
                }
                else ->{
                    btnDaLamGon.isEnabled = true
                    btnDaLamGon.alpha = 1f

                    btnAnh.isEnabled = true
                    btnAnh.alpha = 1f

                    btnVatLieu.isEnabled = true
                    btnVatLieu.alpha = 1f

                    btnDaCan.isEnabled = true
                    btnDaCan.alpha = 1f

                    btnDaXong.isEnabled = true
                    btnDaXong.alpha = 1f

                    btnSubmit.isEnabled = true
                    btnSubmit.alpha = 1f

                    btnDaLamGon.visibility = View.VISIBLE
                    btnAnh.visibility = View.VISIBLE
                    btnVatLieu.visibility = View.VISIBLE
                    btnDaCan.visibility = View.VISIBLE
                    btnDaXong.visibility = View.VISIBLE
                    btnSubmit.visibility = View.VISIBLE
                }
            }
        }
        checkRole()
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

            UiState.Loading -> {
                LoadingScreen.displayLoadingWithText(
                    requireContext(),
                    "Cập nhật trạng thái.",
                    false
                )
            }
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