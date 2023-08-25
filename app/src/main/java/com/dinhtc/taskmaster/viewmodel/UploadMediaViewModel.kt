package com.dinhtc.taskmaster.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinhtc.taskmaster.model.request.DeleteMediaRequest
import com.dinhtc.taskmaster.model.response.JobMediaDetailResponse
import com.dinhtc.taskmaster.service.ApiHelperImpl
import com.dinhtc.taskmaster.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class UploadMediaViewModel @Inject constructor(private val apiHelperImpl: ApiHelperImpl) :
    ViewModel() {

    private val _dataUpLoadImage = MutableLiveData<UiState<Any>>()
    val dataUpLoadImage: LiveData<UiState<Any>>
        get() = _dataUpLoadImage

    fun uploadMultiImageVideo(
        jobId: Int,
        imageParts: MutableList<MultipartBody.Part>?,
        videoPart: MultipartBody.Part?,
        mediaType: Int
    ) {
        viewModelScope.launch {
            _dataUpLoadImage.value = UiState.Loading
            try {
                val response =
                    apiHelperImpl.uploadMultiImageVideo(jobId, imageParts, videoPart, mediaType)
                if (response.result_code == 0) {
                    _dataUpLoadImage.value = UiState.Success(response)
                } else {
                    _dataUpLoadImage.value = UiState.Error(response.data.toString())
                }
            } catch (e: Exception) {
                _dataUpLoadImage.value = UiState.Error("Error message: ${e.message}")
            }
        }
    }
    fun uploadMultiImage(
        jobId: Int,
        imageParts: MutableList<MultipartBody.Part>?,
        mediaType: Int
    ) {
        viewModelScope.launch {
            _dataUpLoadImage.value = UiState.Loading
            try {
                val response =
                    apiHelperImpl.uploadMultiImage(
                        jobId,
                        imageParts,
                        mediaType
                    )
                if (response.result_code == 0) {
                    _dataUpLoadImage.value = UiState.Success(response)
                } else {
                    _dataUpLoadImage.value = UiState.Error(response.data.toString())
                }
            } catch (e: Exception) {
                _dataUpLoadImage.value = UiState.Error("Error message: ${e.message}")
            }
        }
    }
    fun uploadVideo(
        jobId: Int,
        videoPart: MultipartBody.Part?,
        mediaType: Int
    ) {
        viewModelScope.launch {
            _dataUpLoadImage.value = UiState.Loading
            try {
                val response =
                    apiHelperImpl.uploadVideo(
                        jobId,
                        videoPart,
                        mediaType
                    )
                if (response.result_code == 0) {
                    _dataUpLoadImage.value = UiState.Success(response)
                } else {
                    _dataUpLoadImage.value = UiState.Error(response.data.toString())
                }
            } catch (e: Exception) {
                _dataUpLoadImage.value = UiState.Error("Error message: ${e.message}")
            }
        }
    }

    private val _dataDeleteMedia = MutableLiveData<UiState<Any>>()
    val dataDeleteMedia: LiveData<UiState<Any>>
        get() = _dataDeleteMedia

    fun deleteMedia(media: JobMediaDetailResponse) {
        viewModelScope.launch {
            _dataUpLoadImage.value = UiState.Loading
            try {
                val deleteMediaRequest = DeleteMediaRequest(
                    media.jobId,
                    media.url,
                    media.mediaType
                )
                val response =
                    apiHelperImpl.deleteMedia(
                        deleteMediaRequest
                    )
                if (response.result_code == 0) {
                    _dataUpLoadImage.value = UiState.Success(response)
                } else {
                    _dataUpLoadImage.value = UiState.Error(response.data.toString())
                }
            } catch (e: Exception) {
                _dataUpLoadImage.value = UiState.Error("Error message: ${e.message}")
            }
        }
    }

}