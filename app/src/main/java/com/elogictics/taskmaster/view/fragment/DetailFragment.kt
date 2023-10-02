package com.elogictics.taskmaster.view.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.elogictics.taskmaster.R
import com.elogictics.taskmaster.bottomsheet.BottomSheetAddFreight
import com.elogictics.taskmaster.bottomsheet.BottomSheetAddVideo
import com.elogictics.taskmaster.common.view.BaseFragment
import com.elogictics.taskmaster.common.widgets.elasticviews.ElasticAnimation
import com.elogictics.taskmaster.common.widgets.spinner.ItemViewLocation
import com.elogictics.taskmaster.common.widgets.spinner.LocationSpinner
import com.elogictics.taskmaster.common.widgets.spinner.ProvinceData
import com.elogictics.taskmaster.databinding.FragmentDetailBinding
import com.elogictics.taskmaster.model.JobEmployeeDetailResponse
import com.elogictics.taskmaster.model.RoleCode
import com.elogictics.taskmaster.model.request.CompactedAndDoneRequest
import com.elogictics.taskmaster.model.request.DataUpdateJobRequest
import com.elogictics.taskmaster.model.request.UpdateStateWeightedRequest
import com.elogictics.taskmaster.model.response.JobDetailsResponse
import com.elogictics.taskmaster.model.response.ListEmployeeResponse
import com.elogictics.taskmaster.model.response.ListMaterialResponse
import com.elogictics.taskmaster.utils.AndroidUtils
import com.elogictics.taskmaster.utils.DialogFactory
import com.elogictics.taskmaster.utils.LoadingScreen
import com.elogictics.taskmaster.utils.SharedPreferencesManager
import com.elogictics.taskmaster.utils.UiState
import com.elogictics.taskmaster.utils.observe
import com.elogictics.taskmaster.utils.observes
import com.elogictics.taskmaster.view.activity.MainActivity
import com.elogictics.taskmaster.view.activity.MainActivity.Companion.TAG_LOG
import com.elogictics.taskmaster.view.fragment.HomeFragment.Companion.BUNDLE_KEY
import com.elogictics.taskmaster.view.fragment.HomeFragment.Companion.REQUEST_KEY
import com.elogictics.taskmaster.viewmodel.AddTaskViewModel
import com.elogictics.taskmaster.viewmodel.JobsViewModel
import com.elogictics.taskmaster.viewmodel.MaterialViewModel
import com.elogictics.taskmaster.viewmodel.UploadMediaViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MultipartBody


@AndroidEntryPoint
class DetailFragment : BaseFragment<FragmentDetailBinding>() {

    private var paymentMethod: Int = -1
    private var paymentStateId: Int = 0
    private var checkSelectedRadio: Boolean = false
    private val listEmployeeJobs = mutableListOf<JobEmployeeDetailResponse>()
    private var nv1Old: Int = -1
    private var nv2Old: Int = -1
    private var nv3Old: Int = -1
    private var uuTienIdSelected: Int = -1
    private var amountPaidEmp: Double? = 0.0
    private var totalMoney: Double? = 0.0

    private var nvSelectedNew: Int = -1
    private var bottomSheetAddImage: BottomSheetAddVideo? = null
    private var bottomSheetAddVatLieu: BottomSheetAddFreight? = null

    private var dataResponse: JobDetailsResponse? = null
    private var jobsId: Int = 1
    private var empId: Int = -1
    var empUpdate: Int = -1
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
        empUpdate = SharedPreferencesManager.instance.getInt(SharedPreferencesManager.USER_ID, -1)
        var jobIdNotify = arguments?.getString(MainActivity.ID_JOB_NOTIFY)
        jobsId = if (jobIdNotify?.isNotEmpty() == true) {
            jobIdNotify.toInt()
        } else {
            arguments?.getInt(HomeFragment.ID_JOB) ?: -1
        }
        empId = arguments?.getInt(HomeFragment.ID_EMP) ?: -1

        jobsViewModel.getJobDetails(idJob = jobsId, empId = empId)
        addTaskViewModel.getListJobType()
        materialViewModel.getListMaterial()
        addTaskViewModel.getListEmployeeByJobId(jobId = jobsId)


        observe(jobsViewModel.dataJobDetail, ::dataJobDetailLive)
        observe(materialViewModel.dataListMaterial, ::onGetListMaterialLive)
        observe(addTaskViewModel.dataEmployee, ::onGetListEmployee)
        observe(addTaskViewModel.dataEmployeeByJobId, ::dataEmployeeByJobId)

        observe(jobsViewModel.updateJobDetails, ::updateJobDetailsLive)
        observe(jobsViewModel.updateStateJobCompactedAndDone, ::updateStateJobCompactedAndDone)
        observe(jobsViewModel.updateStateJobWeighted, ::updateStateJobWeightedLive)

        observes(uploadMediaViewModel.dataUpLoadImage, ::addUploadMultiImage)
        observes(materialViewModel.datAddMaterial, ::addMaterialLive)

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
                            paymentStateId = 1 // da thanh toan
                            viewBinding.layoutChuyenKhoan.background =
                                context?.let { ContextCompat.getDrawable(it, R.drawable.bg_while) }

                            amountPaidEmp = 0.0
                            edtNVUng.isEnabled = false
                        }

                        R.id.radioChuaThanhToan -> {
                            checkSelectedRadio = true
                            paymentMethod = -1 // chua thanh toan
                            paymentStateId = 0 //chua thanh toan
                            viewBinding.layoutChuyenKhoan.background =
                                context?.let { ContextCompat.getDrawable(it, R.drawable.bg_while) }

                            edtNVUng.isEnabled = true
                        }
                    }
                }
            }

            when (SharedPreferencesManager.instance.getString(SharedPreferencesManager.ROLE_CODE, "")) {
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
                    btnDaXong.alpha = 0.7f
                }

                RoleCode.ADMIN.name, RoleCode.LEADER.name, RoleCode.EMPLOYEE.name, RoleCode.CUSTOMER.name -> {
                    btnDaLamGon.isEnabled = false
                    btnDaLamGon.alpha = 0.7f

                    btnDaCan.isEnabled = false
                    btnDaCan.alpha = 0.7f

                    btnDaXong.isEnabled = false
                    btnDaXong.alpha = 0.7f
                }

                RoleCode.DRIVER.name -> {
                    btnDaLamGon.isEnabled = false
                    btnDaLamGon.alpha = 0.7f

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
        viewBinding.layoutToolBar.apply {
            titleToolBar.text = "Chi tiết công việc"
            imgBackParent.setOnClickListener {
                findNavController().popBackStack()
                if (activity is MainActivity){
                    (activity as MainActivity).checkNavFragmentDetail = true
                    (activity as MainActivity).removeStateSearch()
                }

                val bundle = Bundle()
                bundle.putString(BUNDLE_KEY,UPDATE_SEARCH)
                parentFragmentManager.setFragmentResult(REQUEST_KEY, bundle)
            }
            imgHome.setOnClickListener {
                findNavController().popBackStack(R.id.mainFragment, false)
                if (activity is MainActivity){
                    (activity as MainActivity).checkNavFragmentDetail = false
                }
            }
        }

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
                ElasticAnimation(btnVatLieu).setScaleX(0.75f).setScaleY(0.75f).setDuration(
                    500
                ).doAction()
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
                val dataLamGonAndDaXong = CompactedAndDoneRequest(
                    jobsId = jobsId,
                    stateJob = DALAMGON,
                    empUpdate = empUpdate
                )
                jobsViewModel.updateStateJobCompactedAndDone(dataLamGonAndDaXong)
            }

            btnDaXong.setOnClickListener {
                getDataSelected()

                val dataLamGonAndDaXong = CompactedAndDoneRequest(
                    jobsId = jobsId,
                    stateJob = XONG,
                    empUpdate = empUpdate
                )
                jobsViewModel.updateStateJobCompactedAndDone(dataLamGonAndDaXong)
            }

            btnDaCan.setOnClickListener {
                getDataSelected()
                if (dataResponse != null) {
                    if (dataResponse!!.jobMaterial.isNotEmpty() && dataResponse!!.jobMedia.isNotEmpty()) {
                        if (checkBank()) {
                            if (viewBinding.edtNVUng.text.toString().isNotEmpty()){
                                val dataUpdate = UpdateStateWeightedRequest(
                                    empUpdate = empUpdate,
                                    stateJob = DACAN,
                                    jodId = jobsId,
                                    totalMoney = AndroidUtils.decodeMoneyStr(totalMoney.toString()),
                                    paymentMethod = 1,
                                    paymentStateId = 1,
                                    amountPaidEmp = amountPaidEmp,
                                    priority = uuTienIdSelected,
                                    empOldId = nv1Old,
                                    empNewId = nvSelectedNew,
                                    note = viewBinding.edtGhiChu.text.toString()
                                )

                                jobsViewModel.updateStateWeightedJob(dataUpdate)
                            }else{
                                val dataUpdate = UpdateStateWeightedRequest(
                                    empUpdate = empUpdate,
                                    stateJob = DACAN,
                                    jodId = jobsId,
                                    totalMoney = AndroidUtils.decodeMoneyStr(totalMoney.toString()),
                                    paymentMethod = paymentMethod,
                                    paymentStateId = paymentStateId,
                                    amountPaidEmp = amountPaidEmp,
                                    priority = uuTienIdSelected,
                                    empOldId = nv1Old,
                                    empNewId = nvSelectedNew,
                                    note = viewBinding.edtGhiChu.text.toString()
                                )

                                jobsViewModel.updateStateWeightedJob(dataUpdate)
                            }
                        } else {
                            scrollView.post {
                                scrollView.smoothScrollTo(0, layoutChuyenKhoan.top)
                            }
                            viewBinding.layoutChuyenKhoan.background =
                                context?.let {
                                    ContextCompat.getDrawable(it, R.drawable.bg_red_)
                                }
                        }
                    } else {
                        DialogFactory.showDialogDefaultNotCancel(context, "Thiếu Vật liệu/ Ảnh")
                    }
                }
            }

            btnSubmit.setOnClickListener {
                if (viewBinding.tvPrice.text.toString().trim().isNotEmpty()) {
                    totalMoney = AndroidUtils.getMoneyRealValue(viewBinding.tvPrice.text.toString().trim())
                }
                if (viewBinding.edtNVUng.money.toString().trim().isNotEmpty()) {
                    amountPaidEmp = viewBinding.edtNVUng.money.toString().trim().toDouble()
                }
                if (viewBinding.edtNVUng.text.toString().isNotEmpty()){

                    val dataUpdate = DataUpdateJobRequest(
                        jodId = jobsId,
                        totalMoney = AndroidUtils.decodeMoneyStr(totalMoney.toString()),
                        paymentMethod = 1,
                        paymentStateId = 1,
                        amountPaidEmp = amountPaidEmp,
                        priority = uuTienIdSelected,
                        empOldId = nv1Old,
                        empNewId = nvSelectedNew,
                        empAssignId = empUpdate,
                        note = viewBinding.edtGhiChu.text.toString(),
                    )
                    jobsViewModel.updateJobDetails(dataUpdate)

                }else{
                    val dataUpdate = DataUpdateJobRequest(
                        jodId = jobsId,
                        totalMoney = AndroidUtils.decodeMoneyStr(totalMoney.toString()),
                        paymentMethod = paymentMethod,
                        paymentStateId = paymentStateId,
                        amountPaidEmp = amountPaidEmp,
                        priority = uuTienIdSelected,
                        empOldId = nv1Old,
                        empNewId = nvSelectedNew,
                        empAssignId = empUpdate,
                        note = viewBinding.edtGhiChu.text.toString(),
                    )
                    jobsViewModel.updateJobDetails(dataUpdate)

                }
            }
        }
    }

    private fun checkBank(): Boolean {
        return if (!checkSelectedRadio) {
            viewBinding.edtNVUng.text.toString().trim().isNotEmpty()
        } else {
            true
        }
    }

    private fun getDataSelected() {
        viewBinding.apply {
            if (tvPrice.text.toString().trim().isNotEmpty()) {
                totalMoney = AndroidUtils.getMoneyRealValue(tvPrice.text.toString().trim())
            }
            if (edtNVUng.money.toString().trim().isNotEmpty()) {
                amountPaidEmp = edtNVUng.money.toString().trim().toDouble()
            }

            if (radioChuyenKhoan.isChecked) {
                paymentMethod = 2 //bank
                paymentStateId = 1 // da thanh toan
            }
            if (radioChuaThanhToan.isChecked) {
                paymentMethod = -1 // chua thanh toan
                paymentStateId = 0 //chua thanh toan
            }
            if (edtNVUng.text.toString().trim().isNotBlank() && !checkSelectedRadio) {
                paymentMethod = 1 //cash
                paymentStateId = 1 // da thanh toan
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
                dataListEmployee.clear()
                for (data in listEmpLiveData) {
                    if (nv1Old != -1 && nv1Old != data.empId ||
                        nv2Old != -1 && nv2Old != data.empId ||
                        nv3Old != -1 && nv3Old != data.empId
                    ) {
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
                Log.e(MainActivity.TAG_ERROR, "onGetListEmployee: $errorMessage")
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
                Log.e(MainActivity.TAG_ERROR, "dataEmployeeByJobId: $errorMessage")
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
            }

            UiState.Loading -> {}
        }
    }

    private fun addUploadMultiImage(uiState: UiState<Any>?) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                bottomSheetAddImage?.dismiss()
                DialogFactory.showDialogDefaultNotCancelAndClick(context, "${uiState.data.data}") {
                    jobsViewModel.getJobDetails(idJob = jobsId, empId = empId)
                }
            }

            is UiState.Error -> {
                val errorMessage = uiState.message
                Log.e(MainActivity.TAG_ERROR, "addUploadMultiImage: $errorMessage")
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

            else -> {}
        }
    }

    private fun onGetListMaterialLive(uiState: UiState<ListMaterialResponse>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                val listMaterialLiveData = uiState.data.data.listItem
                dataListJob.clear()
                for (data in listMaterialLiveData) {
                    dataListJob.add(
                        ItemViewLocation(
                            ProvinceData(
                                data.mate_id,
                                "${data.unitPrice}",
                                "${data.name}"
                            )
                        )
                    )
                }
            }

            is UiState.Error -> {
                val errorMessage = uiState.message
                Log.e(MainActivity.TAG_ERROR, "onGetListMaterialLive: $errorMessage")
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

    private fun addMaterialLive(uiState: UiState<Any>?) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "${uiState.data.data}")
                jobsViewModel.getJobDetails(idJob = jobsId, empId = empId)
                bottomSheetAddVatLieu?.deleteDataInsert()
            }

            is UiState.Error -> {
                val errorMessage = uiState.message
                Log.e(MainActivity.TAG_ERROR, "addMaterialLive: $errorMessage")
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
            }

            UiState.Loading -> {}
            else -> {}
        }
    }

    private fun dataJobDetailLive(uiState: UiState<JobDetailsResponse>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()

                dataResponse = uiState.data.data
                dataResponse.let {
                    updateUI(dataResponse!!)
                    //changeStatusUI(dataResponse!!)
                    uuTienIdSelected = dataResponse?.priority!!

                    if (dataResponse!!.jobStateId >=30){

                        updateStatusButton(false, 0.7f)

                        viewBinding.edtSelectUuTien.visibility = View.GONE
                        viewBinding.tvUuTien.visibility = View.VISIBLE
                        viewBinding.tvUuTien.text = "Ưu tiên ${dataResponse!!.priority}"
                        viewBinding.layoutChuyenViecToi.visibility = View.GONE
                        viewBinding.tvGhiChu.visibility = View.VISIBLE
                        viewBinding.edtGhiChu.visibility = View.GONE
                        viewBinding.tvGhiChu.text = dataResponse!!.noteJob
                        if (dataResponse!!.amountPaidEmp != 0.0) {
                            viewBinding.edtNVUng.visibility = View.GONE
                            viewBinding.tvNVUng.visibility = View.VISIBLE
                            viewBinding.tvNVUng.text = AndroidUtils.formatMoneyCard("${dataResponse!!.amountPaidEmp}","VNĐ")
                        }
                    }
                }

            }

            is UiState.Error -> {
                val errorMessage = uiState.message
                Log.e(MainActivity.TAG_ERROR, "dataJobDetailLive: $errorMessage")
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

            if (dataResponse.amountPaidEmp != 0.0) {
                edtNVUng.setText(AndroidUtils.formatMoneyCard(dataResponse.amountPaidEmp.toString()))
            }

            if (dataResponse.paymentMethod == 2 && dataResponse.paymentStateId == 1){
                // đã chuyển khoản
                viewBinding.radioChuyenKhoan.isSelected = true
                viewBinding.radioChuyenKhoan.isChecked = true
                viewBinding.radioChuaThanhToan.isSelected = false
                viewBinding.radioChuaThanhToan.isChecked = false
            }
            else if (dataResponse.paymentMethod == -1 && dataResponse.paymentStateId == 0){
                // chua thanh toan
                viewBinding.radioChuyenKhoan.isSelected = false
                viewBinding.radioChuyenKhoan.isChecked = false
                viewBinding.radioChuaThanhToan.isSelected = true
                viewBinding.radioChuaThanhToan.isChecked = true
            }
            else if (dataResponse.paymentMethod == 1 && dataResponse.paymentStateId == 1){
                viewBinding.radioChuyenKhoan.isSelected = false
                viewBinding.radioChuyenKhoan.isChecked = false
                viewBinding.radioChuaThanhToan.isSelected = false
                viewBinding.radioChuaThanhToan.isChecked = false
            }

            if (dataResponse.priority != 0 && dataResponse.jobStateId >= 30) {
                uuTienIdSelected = dataResponse.priority
                edtSelectUuTien.visibility = View.GONE
                tvUuTien.visibility = View.VISIBLE
                tvUuTien.text = "Ưu tiên ${dataResponse.priority}"
            } else {
                edtSelectUuTien.text = "Ưu tiên ${dataResponse.priority}"
                edtSelectUuTien.visibility = View.VISIBLE
                tvUuTien.visibility = View.GONE
            }

            if (dataResponse.noteJob.isNotEmpty()){
                viewBinding.edtGhiChu.setText(dataResponse.noteJob)
            }

            if (dataResponse.jobStateId >= 30) {
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

            var moneyTemp: Double = 0.0
            if (dataResponse.jobMaterial.isNotEmpty()) {
                for (data in dataResponse.jobMaterial) {
                    moneyTemp += (data.price * data.weight)
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

                    layoutChuyenViecToi.visibility = View.GONE

                    tvGhiChu.visibility = View.VISIBLE
                    edtGhiChu.visibility = View.GONE
                    tvGhiChu.text = dataResponse.noteJob
                }

                else -> {
                    updateStatusButton(true, 1f)

                    btnDaLamGon.visibility = View.VISIBLE
                    btnAnh.visibility = View.VISIBLE
                    btnVatLieu.visibility = View.VISIBLE
                    btnDaCan.visibility = View.VISIBLE
                    btnDaXong.visibility = View.VISIBLE
                    btnSubmit.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun updateStatusButton(statusButton : Boolean, alpha : Float) {
        viewBinding.apply {
            btnDaLamGon.isEnabled = statusButton
            btnDaLamGon.alpha = alpha

            btnAnh.isEnabled = statusButton
            btnAnh.alpha = alpha

            btnVatLieu.isEnabled = statusButton
            btnVatLieu.alpha = alpha

            btnDaCan.isEnabled = statusButton
            btnDaCan.alpha = alpha

            btnDaXong.isEnabled = statusButton
            btnDaXong.alpha = alpha

            btnSubmit.isEnabled = statusButton
            btnSubmit.alpha = alpha
        }
    }

    private fun updateStateJobWeightedLive(uiState: UiState<Any>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancelAndClick(
                    context,
                    "${uiState.data.data}"
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
                Log.e(MainActivity.TAG_ERROR, "updateStateJobLive: $errorMessage")
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
            else -> {}
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
                Log.e(MainActivity.TAG_ERROR, "updateJobDetailsLive: $errorMessage")
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
            }

            UiState.Loading -> {}
            else -> {}
        }
    }
    private fun updateStateJobCompactedAndDone(uiState: UiState<Any>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "${uiState.data.data}")
                jobsViewModel.getJobDetails(idJob = jobsId, empId = empId)
            }

            is UiState.Error -> {
                val errorMessage = uiState.message
                Log.e(MainActivity.TAG_ERROR, "updateJobDetailsLive: $errorMessage")
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
            }

            UiState.Loading -> {}
            else -> {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        uploadMediaViewModel.cleanup()
        materialViewModel.cleanup()
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

        val UPDATE_SEARCH = "UPDATE_SEARCH"
    }
}