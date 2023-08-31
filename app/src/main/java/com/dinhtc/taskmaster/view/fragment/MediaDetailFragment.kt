package com.dinhtc.taskmaster.view.fragment

import android.annotation.SuppressLint
import android.util.Log
import android.view.WindowManager
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.dinhtc.taskmaster.R
import com.dinhtc.taskmaster.adapter.ImageViewAdapter
import com.dinhtc.taskmaster.bottomsheet.BottomSheetAddVideo
import com.dinhtc.taskmaster.common.view.BaseFragment
import com.dinhtc.taskmaster.databinding.FragmentMediaDetailBinding
import com.dinhtc.taskmaster.model.response.JobDetailsResponse
import com.dinhtc.taskmaster.model.response.JobMediaDetailResponse
import com.dinhtc.taskmaster.utils.DialogFactory
import com.dinhtc.taskmaster.utils.LoadingScreen
import com.dinhtc.taskmaster.utils.UiState
import com.dinhtc.taskmaster.utils.observe
import com.dinhtc.taskmaster.view.activity.MainActivity
import com.dinhtc.taskmaster.viewmodel.JobsViewModel
import com.dinhtc.taskmaster.viewmodel.UploadMediaViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MultipartBody


@AndroidEntryPoint
class MediaDetailFragment : BaseFragment<FragmentMediaDetailBinding>(){

    private var countMediaImages: Int = 0
    private var countMediaVideo: Int = 0

    private var countMediaImageHave : Int = 0
    private var countMediaVideoHave : Int = 0

    private var positionDelete: Int = -1
    private var noDataAdapter: ImageViewAdapter? = null
    private var bottomSheetAddImage: BottomSheetAddVideo? = null
    private var jobsId: Int = -1
    private var empId: Int = -1
    private var checkCloseVideos: Boolean = false
    private var checkCloseImage: Boolean = false
    private var imagePartLocal: MutableList<MultipartBody.Part>? = null
    private var videoPartLocal: MultipartBody.Part? = null

    private val uploadMediaViewModel: UploadMediaViewModel by viewModels()
    private val jobsViewModel: JobsViewModel by viewModels()
    var listJobMedia : MutableList<JobMediaDetailResponse> = mutableListOf()

    override val layoutResourceId: Int
        get() = R.layout.fragment_media_detail

    override fun onViewCreated() {
        if(activity is MainActivity){
            val data = (activity as MainActivity).sharedViewModel?.getSharedListJobMedia()?.value

            for (values in data!!){
                jobsId = values.jobId
                listJobMedia.add(values)
                if (values.mediaType == 1) {
                    countMediaImageHave++
                } else if (values.mediaType == 2) {
                    countMediaVideoHave++
                }
            }
            noDataAdapter = context?.let { ImageViewAdapter(it) }
            noDataAdapter?.submitList(listJobMedia)
            noDataAdapter?.setOnClickListener(object : ImageViewAdapter.OnClickListener {
                override fun onItemClick(position: Int, media: JobMediaDetailResponse) {
                    DialogFactory.createMessageDialogWithYesNo(
                        context,
                        "Bạn chắc chắn xóa hình ảnh/video này không?",
                        "Có",
                        "Không",
                        {
                            positionDelete = position
                            uploadMediaViewModel.deleteMedia(media)
                        },
                        {}
                    )

                }
            })

            viewBinding.recyclerView.apply {
                adapter = noDataAdapter
                val layoutManager = GridLayoutManager(context, 2)
                setLayoutManager(layoutManager)
                setHasFixedSize(true)
            }
        }

        onClickItem()
        observe(uploadMediaViewModel.dataUpLoadImage, ::responseUploadMultiImage)
        observe(uploadMediaViewModel.dataDeleteMedia, ::dataDeleteMediaLive)
        observe(jobsViewModel.dataJobDetail, ::dataJobDetailLive)
    }

    private fun onClickItem() {
        viewBinding.apply {
            floatingAction.setOnClickListener {
                showDialogAddImage()
            }
        }
    }

    private fun showDialogAddImage() {
        bottomSheetAddImage = BottomSheetAddVideo(
            "Thêm",
            { imageParts ->
                imagePartLocal = imageParts
                countMediaImages = imageParts.size
                checkCloseImage = true
            },
            { videoPart ->
                videoPartLocal = videoPart
                countMediaVideo = 1
                checkCloseVideos = true
            },
            {
                var totalImages = countMediaImages + countMediaImageHave
                var totalVideo = countMediaVideo + countMediaVideoHave
                if (totalImages <= 5 && totalVideo <= 1){
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
                }else{
                    DialogFactory.showDialogSubTitleDefaultNotCancel(
                        context,
                        "Quá số lượng quy định",
                    "Vui lòng kiểm tra lại số ảnh và video hiện có. Bạn chỉ phép upload tối đa 5 hình và 1 video dưới 10s.")
                }
            }, { //close image
                imagePartLocal?.clear()
                checkCloseImage = false
                countMediaImages = 0
            }, { //close videos
                checkCloseVideos = false
                countMediaVideo = 0
            },{
                countMediaImages = 0
                countMediaVideo = 0
            })
        bottomSheetAddImage?.isCancelable = false
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        activity?.supportFragmentManager?.let {
            bottomSheetAddImage?.show(
                it,
                bottomSheetAddImage?.tag
            )
        }
    }

    private fun responseUploadMultiImage(uiState: UiState<Any>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancelAndClick(context, "${uiState.data.data}"){
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

    @SuppressLint("NotifyDataSetChanged")
    private fun dataJobDetailLive(uiState: UiState<JobDetailsResponse>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                jobsId = uiState.data.data.jobId
                listJobMedia = uiState.data.data.jobMedia as MutableList<JobMediaDetailResponse>
                noDataAdapter?.submitList(listJobMedia)
                viewBinding.recyclerView.setHasFixedSize(true)
                if (listJobMedia.isNotEmpty()){
                    countMediaImageHave = 0
                    countMediaVideoHave = 0
                    for (data in listJobMedia){
                        if (data.mediaType == 1) {
                            countMediaImageHave++
                        } else if (data.mediaType == 2) {
                            countMediaVideoHave++
                        }
                    }
                }
                Log.e("API_R","IM_$countMediaImageHave")
                Log.e("API_R","VD_$countMediaVideoHave")
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

    @SuppressLint("NotifyDataSetChanged")
    private fun dataDeleteMediaLive(uiState: UiState<Any>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancelAndClick(context, "${uiState.data.data}"){
                    jobsViewModel.getJobDetails(idJob = jobsId, empId = empId)
                    bottomSheetAddImage?.dismiss()
                    noDataAdapter?.removeItem(positionDelete)
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
}