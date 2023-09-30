package com.elogictics.taskmaster.bottomsheet

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Html
import android.util.Config
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.elogictics.taskmaster.BuildConfig
import com.elogictics.taskmaster.R
import com.elogictics.taskmaster.common.view.UploadDocumentImage
import com.elogictics.taskmaster.common.widgets.elasticviews.ElasticLayout
import com.elogictics.taskmaster.common.widgets.image_picker.BSImagePicker
import com.elogictics.taskmaster.common.widgets.image_picker.BSVideoPicker
import com.elogictics.taskmaster.utils.DialogFactory
import com.elogictics.taskmaster.utils.Utils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

    private var currentPhotoUri: Uri? = null
    private var currentVideoUri: Uri? = null

    private val PERMISSION_WRITE_STORAGE = 2003
    private val PERMISSION_CAMERA = 2002
    private val REQUEST_TAKE_PHOTO = 3001
    private val REQUEST_TAKE_VIDEO = 3004

    private val MAX_PHOTOS = 5
    private var photosTakenCount = 0
    private val photoURIs: ArrayList<Uri> = ArrayList()

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
            openCamera()
        }

        imgVideo.setOnClickListener {
            openVideo()
        }

        btnSubmit.setOnClickListener {
            onClickSave.invoke()
        }
    }

    private fun openVideo() {
        if (Utils.isCameraGranted(context)) {
            launchCameraForVideo()
        } else {
            Utils.checkPermission(this@BottomSheetAddVideo, Manifest.permission.CAMERA, PERMISSION_CAMERA)
        }
    }

    private fun openCamera() {
        if (Utils.isCameraGranted(context)) {
            launchCamera()
        } else {
            Utils.checkPermission(this@BottomSheetAddVideo, Manifest.permission.CAMERA, PERMISSION_CAMERA)
        }
    }

    private fun launchCamera() {
        if (context == null) return
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePhotoIntent.resolveActivity(requireContext().packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (e: IOException) {
                if (Config.DEBUG) e.printStackTrace()
            }
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(
                    requireContext(),
                    BuildConfig.APPLICATION_ID + ".provider",
                    photoFile
                )
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                if (true) {
                    //Below does not always work, just a hack.
                    //Reference: https://stackoverflow.com/a/40175503/7870874
                    takePhotoIntent.putExtra(
                        "android.intent.extras.CAMERA_FACING",
                        Camera.CameraInfo.CAMERA_FACING_FRONT
                    )
                    takePhotoIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
                    takePhotoIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
                }
                val resolvedIntentActivities = requireContext().packageManager.queryIntentActivities(
                    takePhotoIntent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                for (resolvedIntentInfo in resolvedIntentActivities) {
                    val packageName = resolvedIntentInfo.activityInfo.packageName
                    requireContext().grantUriPermission(
                        packageName,
                        photoURI,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO)
            }
        }
    }

    private fun launchCamera2() {
        if (context == null) return
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePhotoIntent.resolveActivity(requireContext().packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (e: IOException) {
                if (Config.DEBUG) e.printStackTrace()
            }
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(
                    requireContext(),
                    BuildConfig.APPLICATION_ID + ".provider",
                    photoFile
                )
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

                val resolvedIntentActivities = requireContext().packageManager.queryIntentActivities(
                    takePhotoIntent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                for (resolvedIntentInfo in resolvedIntentActivities) {
                    val packageName = resolvedIntentInfo.activityInfo.packageName
                    requireContext().grantUriPermission(
                        packageName,
                        photoURI,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().time)
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DCIM)
        //        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoUri = Uri.fromFile(image)
        return image
    }

    private fun launchCameraForVideo() {
        if (context == null) return
        val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if (takeVideoIntent.resolveActivity(requireContext().packageManager) != null) {
            var videoFile: File? = null
            try {
                videoFile = createVideoFile()
            } catch (e: IOException) {
                if (Config.DEBUG) e.printStackTrace()
            }
            if (videoFile != null) {
                val videoURI = FileProvider.getUriForFile(
                    requireContext(),
                    BuildConfig.APPLICATION_ID + ".provider",
                    videoFile
                )
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoURI)
                val resolvedIntentActivities = requireContext().packageManager.queryIntentActivities(
                    takeVideoIntent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                for (resolvedIntentInfo in resolvedIntentActivities) {
                    val packageName = resolvedIntentInfo.activityInfo.packageName
                    requireContext().grantUriPermission(
                        packageName,
                        videoURI,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                startActivityForResult(takeVideoIntent, REQUEST_TAKE_VIDEO)
            }
        }
    }

    @Throws(IOException::class)
    private fun createVideoFile(): File? {
        // Thư mục lưu trữ video (đường dẫn tùy chỉnh)
        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
            ), "YourVideoFolder"
        )

        // Tạo thư mục nếu nó chưa tồn tại
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        // Tạo tên tệp video duy nhất, có thể sử dụng timestamp để tạo tên duy nhất
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val videoFileName = "VIDEO_$timeStamp.mp4"

        // Tạo tệp video mới
        val videoFile = File(storageDir, videoFileName)
        currentVideoUri = Uri.fromFile(videoFile)
        // Trả về tệp để camera có thể lưu video vào đó
        return videoFile
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
        val contentResolver = context.contentResolver
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(contentUri, projection, null, null, null)
        if (cursor != null){
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    return it.getString(columnIndex)
                }
            }
        }else{
            return Uri.parse(contentUri.toString()).path
        }
        return null
    }



    fun prepareVideoPart(videoUri: Uri): MultipartBody.Part? {
        val contentResolver = context?.contentResolver
        try {
            contentResolver?.openInputStream(videoUri)?.use { inputStream ->
                val videoFile = File(videoUri.path)
                val requestFile = RequestBody.create("video/*".toMediaTypeOrNull(), videoFile)
                return MultipartBody.Part.createFormData("url_video", videoFile.name, requestFile)
            }
        } catch (e: IOException) {
            // Xử lý lỗi khi mở inputStream
            e.printStackTrace()
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_TAKE_PHOTO -> if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
                // Increment the number of photos taken and add the photo URI to the list
                photosTakenCount++
                currentPhotoUri?.let { photoURIs.add(it) }

                // Check if the desired number of photos has been reached
                if (photosTakenCount >= MAX_PHOTOS) {
                    // Display the photos in a fragment or any other desired action
                    displayPhotosInFragment(photoURIs)

                    // Reset the counters and the photo URI list for future captures
                    photosTakenCount = 0
                    photoURIs.clear()
                } else {
                    // Continue taking photos until the desired number is reached
                    launchCamera2()
                }
            } else {
                displayPhotosInFragment(photoURIs)
                photosTakenCount = 0
                photoURIs.clear()
            }

            REQUEST_TAKE_VIDEO -> if (requestCode == REQUEST_TAKE_VIDEO && resultCode == Activity.RESULT_OK){

                // Khi quay video thành công và trả về kết quả
                notifyGalleryVideo()
                if (currentVideoUri?.let { isVideoLengthValid(it) } == true){
                    onSingleVideoSelected(currentVideoUri)
                }else{
                    DialogFactory.showDialogSubTitleDefaultNotCancel(
                        context,
                        "Quá số lượng quy định",
                        "Vui lòng kiểm tra lại số ảnh và video hiện có. Bạn chỉ phép upload tối đa 1 video dưới 10s.")
                }
            }

            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun notifyGalleryVideo() {
        if (context == null) return
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = currentVideoUri
        requireContext().sendBroadcast(mediaScanIntent)
    }

    private fun displayPhotosInFragment(photoURIs: MutableList<Uri>) {
        if (photoURIs.size <= 5){
            showImageMultiImage(photoURIs)
        }else{
            DialogFactory.showDialogSubTitleDefaultNotCancel(
                context,
                "Quá số lượng quy định",
                "Vui lòng kiểm tra lại số ảnh và video hiện có. Bạn chỉ phép upload tối đa 5 hình.")
        }
    }

    @Throws(IOException::class)
    private fun isVideoLengthValid(videoUri: Uri): Boolean {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, videoUri)
        val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        retriever.release()
        val duration = (durationString ?: "0").toLong()
        val maxDuration: Long = 10000 // 10 giây trong milliseconds
        return duration <= maxDuration
    }
}