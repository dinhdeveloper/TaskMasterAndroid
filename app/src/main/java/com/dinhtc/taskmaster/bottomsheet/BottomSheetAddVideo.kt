package com.dinhtc.taskmaster.bottomsheet

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.dinhtc.taskmaster.BuildConfig.AUTHOR_FILE_PROVIDER
import com.dinhtc.taskmaster.R
import com.dinhtc.taskmaster.common.view.UploadDocumentImage
import com.dinhtc.taskmaster.common.widgets.elasticviews.ElasticLayout
import com.dinhtc.taskmaster.common.widgets.image_picker.BSImagePicker
import com.dinhtc.taskmaster.common.widgets.image_picker.BSVideoPicker
import com.dinhtc.taskmaster.model.response.JobMediaDetailResponse
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textview.MaterialTextView
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BottomSheetAddVideo(
    private var textButton: String,
    private val listenerMultiPathImage: ((imageParts: MutableList<MultipartBody.Part>) -> Unit),
    private val listenerPathVideo: ((videoPart: MultipartBody.Part) -> Unit),
    private val onClickSave: () -> Unit,
    private val onCloseImages: () -> Unit,
    private val onCloseVideo: () -> Unit,
    private val onClosePopup: () -> Unit
) : BottomSheetDialogFragment(),
    BSImagePicker.OnSingleImageSelectedListener,
    BSImagePicker.OnMultiImageSelectedListener,
    BSImagePicker.ImageLoaderDelegate,
    BSImagePicker.OnCameraMultiImageListener,
    BSVideoPicker.VideoLoaderDelegate,
    BSVideoPicker.OnSingleVideoSelectedListener,
    BSImagePicker.OnSelectImageCancelledListener {

    var bottomSheetBehavior: BottomSheetBehavior<*>? = null

    private lateinit var imgClose: ImageView
    private lateinit var imgView: ImageView
    private lateinit var imgViewVideo: ImageView
    private lateinit var imgCloseImg: ImageView
    private lateinit var tvCount: TextView
    private lateinit var tvSm: TextView
    private lateinit var tvLabelAnh: TextView
    private lateinit var viewBackground: View
    private lateinit var viewBackgroundVideo: View
    private lateinit var imgFront: UploadDocumentImage
    private lateinit var imgVideo: UploadDocumentImage
    private lateinit var imgCloseVideo: ImageView
    private lateinit var btnSubmit: ElasticLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_add_image, container, false)
    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetCommon
    }

    override fun onViewCreated(modalSheetView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(modalSheetView, savedInstanceState)
        /*Show full dialog*/
        bottomSheetBehavior = BottomSheetBehavior.from(view?.parent as View)
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior?.isDraggable = false
        findViewByID(modalSheetView)
        actionView()

       // showVideoImageHaveData()
    }

    val uriListHaveData: MutableList<Uri> = mutableListOf()
    private fun showVideoImageHaveData() {

//        if (listMedia != null) {
//            for (data in listMedia!!) {
//                if (data.mediaType == 1) {
//                    uriListHaveData.add(Uri.parse(data.url))
//                    showImageMultiImage2(uriListHaveData)
//                } else if (data.mediaType == 2) {
//                    showVideoChoose(true)
//                    Glide.with(this).load(Uri.parse(data.url)).into(imgViewVideo)
//                }
//            }
//        }
    }

    private fun actionView() {
        val labelNV1 = "1. Ảnh:<font color='#FF0000'><sup>*</sup></font>"
        // Sử dụng Html.fromHtml để hiển thị văn bản HTML trong TextView
        tvLabelAnh.text = Html.fromHtml(labelNV1, Html.FROM_HTML_MODE_COMPACT)
        imgClose.setOnClickListener {
            dismiss()
            onClosePopup.invoke()
        }

        imgFront.setOnClickListener {
            openBsImagePickerMultiSelect()
        }

        imgVideo.setOnClickListener {
            openBsVideoPicker()
        }

        btnSubmit.setOnClickListener {
            onClickSave.invoke()
        }
    }

    private fun findViewByID(modalSheetView: View) {
        imgClose = modalSheetView.findViewById(R.id.imgClose)
        imgFront = modalSheetView.findViewById(R.id.imgFront)
        imgView = modalSheetView.findViewById(R.id.imgView)
        imgCloseImg = modalSheetView.findViewById(R.id.imgCloseImg)
        viewBackground = modalSheetView.findViewById(R.id.viewBackground)
        viewBackgroundVideo = modalSheetView.findViewById(R.id.viewBackgroundVideo)
        tvCount = modalSheetView.findViewById(R.id.tvCount)
        tvLabelAnh = modalSheetView.findViewById(R.id.tvLabelAnh)

        imgVideo = modalSheetView.findViewById(R.id.imgVideo)
        imgCloseVideo = modalSheetView.findViewById(R.id.imgCloseVideo)
        imgViewVideo = modalSheetView.findViewById(R.id.imgViewVideo)
        btnSubmit = modalSheetView.findViewById(R.id.btnSubmit)
        tvSm = modalSheetView.findViewById(R.id.tvSm)

        tvSm.text = textButton
    }

    companion object {
        const val TAG = "BottomSheetAddImage"
    }

    private fun openBsVideoPicker() {
        val pickerDialog: BSVideoPicker = BSVideoPicker.Builder(AUTHOR_FILE_PROVIDER)
            .build()
        pickerDialog.show(childFragmentManager, "picker")
    }

    private fun openBsImagePickerMultiSelect() {
        val pickerDialog: BSImagePicker = BSImagePicker.Builder(AUTHOR_FILE_PROVIDER)
            .setMaximumDisplayingImages(Int.MAX_VALUE)
            .isMultiSelect
            .setMinimumMultiSelectCount(1)
            .setMaximumMultiSelectCount(5)
            .build()
        pickerDialog.show(childFragmentManager, "picker")
    }

    override fun onSingleImageSelected(uri: Uri?, tag: String?) {
        showImageChoose(true)
        Glide.with(this).load(uri).into(imgView)
        imgCloseImg.setOnClickListener {
            showImageChoose(false)
            onCloseImages.invoke()
        }
    }

    private fun showImageChoose(status: Boolean) {
        if (status) {
            imgFront.visibility = View.GONE
            imgView.visibility = View.VISIBLE
            imgCloseImg.visibility = View.VISIBLE

        } else {
            imgFront.visibility = View.VISIBLE
            imgView.visibility = View.GONE
            imgCloseImg.visibility = View.GONE
        }
    }

    private fun showVideoChoose(status: Boolean) {
        if (status) {
            imgVideo.visibility = View.GONE
            imgViewVideo.visibility = View.VISIBLE
            viewBackgroundVideo.visibility = View.VISIBLE
            imgCloseVideo.visibility = View.VISIBLE
        } else {
            imgVideo.visibility = View.VISIBLE
            imgViewVideo.visibility = View.GONE
            viewBackgroundVideo.visibility = View.GONE
            imgCloseVideo.visibility = View.GONE
        }

        imgCloseVideo.setOnClickListener {
            showVideoChoose(false)
            onCloseVideo.invoke()
        }
    }


    override fun onMultiImageSelected(uriList: MutableList<Uri>?, tag: String?) {
        showImageMultiImage(uriList)
    }

    override fun onCameraMultiImageSelected(photoURIs: MutableList<Uri>?) {
        showImageMultiImage(photoURIs)
    }


    override fun loadImage(imageUri: Uri?, ivImage: ImageView?) {
        Glide.with(requireContext()).load(imageUri).into(ivImage!!)
    }

    override fun loadVideo(imageUri: Uri?, ivImage: ImageView?) {
        Glide.with(requireContext()).load(imageUri).into(ivImage!!)
    }


    override fun onCancelled(isMultiSelecting: Boolean, tag: String?) {
    }

    override fun onSingleVideoSelected(uri: Uri?) {
        showVideoChoose(true)
        Glide.with(this).load(uri).into(imgViewVideo)
        if (uri != null) {
            prepareVideoPart(uri)?.let { listenerPathVideo(it) }
        }
    }

    private fun showImageMultiImage(uriList: MutableList<Uri>?) {
        if (uriList != null) {
            val imageParts = mutableListOf<MultipartBody.Part>()
            for (imageUri in uriList) {
                val file = context?.let {
                    getRealPathFromURI(it, imageUri) }?.let {
                    File(
                        it
                    )
                }
                val requestFile = file?.let {
                    RequestBody.create("image/*".toMediaTypeOrNull(),
                        it
                    )
                }
                val imagePart =
                    requestFile?.let {
                        MultipartBody.Part.createFormData("url_image", file?.name,
                            it
                        )
                    }
                if (imagePart != null) {
                    imageParts.add(imagePart)
                }
            }
            listenerMultiPathImage(imageParts)
        }
        showImageMultiImage2(uriList)
    }

    private fun showImageMultiImage2(uriList2: MutableList<Uri>?) {
        uriList2.let {
            if (it?.isNotEmpty() == true) {
                if (it.size == 1) {
                    viewBackground.visibility = View.GONE
                    tvCount.visibility = View.GONE
                } else {
                    viewBackground.visibility = View.VISIBLE
                    tvCount.visibility = View.VISIBLE
                    tvCount.text = "+${uriList2?.size}"
                }
            }
        }
        if (uriList2?.isNotEmpty() == true) {
            showImageChoose(true)
            Glide.with(requireContext()).load(uriList2[0]).into(imgView)
            imgView.background =
                context?.let { ContextCompat.getDrawable(it, R.color.transparent_2) }
        }

        imgCloseImg.setOnClickListener {
            uriList2?.clear()
            showImageChoose(false)
            viewBackground.visibility = View.GONE
            tvCount.visibility = View.GONE
            imgCloseImg.visibility = View.GONE
            onCloseImages.invoke()
        }
    }

    private fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
        val cursor = context.contentResolver.query(contentUri, null, null, null, null)
        cursor?.use {
            it.moveToFirst()
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            return it.getString(columnIndex)
        }
        return null
    }

    fun prepareVideoPart(videoUri: Uri): MultipartBody.Part? {
        val contentResolver = context?.contentResolver
        contentResolver?.openInputStream(videoUri)?.use { inputStream ->
            val videoFile = File(context?.let { getRealPathFromURIVideo(it, videoUri) })
            val requestFile = RequestBody.create("video/*".toMediaTypeOrNull(), videoFile)
            return MultipartBody.Part.createFormData("url_video", videoFile.name, requestFile)
        }
        return null
    }

    // Get the real path from URI
    private fun getRealPathFromURIVideo(context: Context, uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.let {
            it.moveToFirst()
            val columnIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            val path = it.getString(columnIndex)
            it.close()
            return path
        }
        return uri.path ?: ""
    }
}