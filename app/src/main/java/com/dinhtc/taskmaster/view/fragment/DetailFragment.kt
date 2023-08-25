package com.dinhtc.taskmaster.view.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dinhtc.taskmaster.R
import com.dinhtc.taskmaster.bottomsheet.BottomSheetAddFreight
import com.dinhtc.taskmaster.bottomsheet.BottomSheetAddVideo
import com.dinhtc.taskmaster.databinding.FragmentDetailBinding
import com.dinhtc.taskmaster.common.view.BaseFragment
import com.dinhtc.taskmaster.common.widgets.elasticviews.ElasticAnimation
import com.dinhtc.taskmaster.common.widgets.spinner.ItemViewLocation
import com.dinhtc.taskmaster.common.widgets.spinner.ProvinceData
import com.dinhtc.taskmaster.model.response.JobDetailsResponse
import com.dinhtc.taskmaster.model.response.JobMaterialDetailResponse
import com.dinhtc.taskmaster.model.response.ListMaterialResponse
import com.dinhtc.taskmaster.model.response.UpdateJobsResponse
import com.dinhtc.taskmaster.utils.DialogFactory
import com.dinhtc.taskmaster.utils.LoadingScreen
import com.dinhtc.taskmaster.utils.UiState
import com.dinhtc.taskmaster.utils.observe
import com.dinhtc.taskmaster.view.activity.MainActivity
import com.dinhtc.taskmaster.viewmodel.MaterialViewModel
import com.dinhtc.taskmaster.viewmodel.AddTaskViewModel
import com.dinhtc.taskmaster.viewmodel.JobsViewModel
import com.dinhtc.taskmaster.viewmodel.UploadMediaViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MultipartBody

@AndroidEntryPoint
class DetailFragment : BaseFragment<FragmentDetailBinding>() {


    private var bottomSheetAddImage: BottomSheetAddVideo? = null
    private var dataResponse: JobDetailsResponse? = null
    private var jobsId: Int = -1
    private var checkCloseVideos: Boolean = false
    private var checkCloseImage: Boolean = false
    private var imagePartLocal: MutableList<MultipartBody.Part>? = null
    private var videoPartLocal: MultipartBody.Part? = null

    private val uploadMediaViewModel: UploadMediaViewModel by viewModels()
    private val addTaskViewModel: AddTaskViewModel by viewModels()
    private val materialViewModel: MaterialViewModel by viewModels()
    private val jobsViewModel: JobsViewModel by viewModels()

    private val dataListJob = ArrayList<ItemViewLocation<ProvinceData>>()

    override val layoutResourceId: Int
        get() = R.layout.fragment_detail

    override fun onViewCreated() {

        jobsId = arguments?.getInt(HomeFragment.ID_JOB)!!

        addTaskViewModel.getListJobType()
        jobsViewModel.getJobDetails(idJob = jobsId)
        materialViewModel.getListMaterial()


        observe(uploadMediaViewModel.dataUpLoadImage, ::responseUploadMultiImage)
        observe(materialViewModel.dataListMaterial, ::onGetListMaterialLive)
        observe(materialViewModel.datAddMaterial, ::addMaterialLive)
        observe(jobsViewModel.dataJobDetail, ::dataJobDetailLive)
        observe(jobsViewModel.updateStateJob, ::updateStateJobLive)

        onClickItem()
    }

    private fun onClickItem() {
        viewBinding.apply {
            btnVatLieu.setOnClickListener {
                if (dataResponse!= null){
                    if (dataResponse!!.jobMaterial.isNotEmpty()){
                        if (activity is MainActivity){
                            (activity as MainActivity).sharedViewModel?.setShareListJobMaterial(dataResponse!!.jobMaterial)
                        }
                        findNavController().navigate(R.id.action_detailFragment_to_materialDetailFragment)
                    }else {
                        if (dataListJob.isNotEmpty()) {
                            showDialogAddVatLieu()
                        }
                    }
                }
            }

            btnAnh.setOnClickListener {
                if (dataResponse!= null){
                    if (dataResponse!!.jobMedia.isNotEmpty()){
                        if (activity is MainActivity){
                            (activity as MainActivity).sharedViewModel?.setShareListJobMedia(dataResponse!!.jobMedia)
                        }
                        findNavController().navigate(R.id.action_detailFragment_to_mediaDetailFragment)
                    }else {
                        if (dataListJob.isNotEmpty()) {
                            showDialogAddImage()
                        }
                    }
                }
            }

            btnDaLamGon.setOnClickListener {
                jobsViewModel.updateStateJob(jobsId, DALAMGON)
            }
            btnDaCan.setOnClickListener {
                if (dataResponse != null){
                    if (dataResponse!!.jobMaterial.isNotEmpty() && dataResponse!!.jobMedia.isNotEmpty()){
                        jobsViewModel.updateStateJob(jobsId, DACAN)
                    }else{
                        DialogFactory.showDialogDefaultNotCancel(context,"Thiếu Vật liệu/ Ảnh")
                    }
                }
            }

            btnSubmit.setOnClickListener {}
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
            },{})
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

    private fun responseUploadMultiImage(uiState: UiState<Any>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancelAndClick(context, "${uiState.data.data}"){
                    jobsViewModel.getJobDetails(idJob = jobsId)
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
                jobsViewModel.getJobDetails(idJob = jobsId)
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
                    tvPriority.text = "${dataResponse?.priority}"
                }
                jobsId = uiState.data.data.jobId
                if (dataResponse?.jobMedia?.isNotEmpty() == true){
                    for (data in dataResponse!!.jobMedia){
                        if (data.mediaType == 2){
                            checkCloseVideos = true
                        }
                        if (data.mediaType == 1){
                            checkCloseImage = true
                        }
                    }
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
                DialogFactory.showDialogDefaultNotCancelAndClick(context, "${uiState.data.data.description}") {
                    jobsViewModel.getJobDetails(jobsId)
                    ElasticAnimation(viewBinding.tvStateDecs).setScaleX(0.75f).setScaleY(0.75f).setDuration(
                        500
                    ).doAction()
                    viewBinding.tvStateDecs.setTypeface(viewBinding.tvStateDecs.typeface, Typeface.BOLD)
                    viewBinding.tvStateDecs.setTextColor(ContextCompat.getColor(requireContext(), R.color.custom_error_color))
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