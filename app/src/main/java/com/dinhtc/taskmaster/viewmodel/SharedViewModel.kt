package com.dinhtc.taskmaster.viewmodel

import androidx.lifecycle.LiveData

import androidx.lifecycle.MutableLiveData

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinhtc.taskmaster.model.response.JobMaterialDetailResponse
import com.dinhtc.taskmaster.model.response.JobMediaDetailResponse
import com.dinhtc.taskmaster.model.response.UserProfileResponse
import com.dinhtc.taskmaster.service.ApiHelperImpl
import com.dinhtc.taskmaster.utils.UiState
import com.dinhtc.taskmaster.utils.convertJsonToObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject


@HiltViewModel
class SharedViewModel @Inject constructor(private val apiHelperImpl: ApiHelperImpl) : ViewModel() {

    private val _updateTokenFirebase = MutableLiveData<UiState<Any>>()
    val updateTokenFirebase : LiveData<UiState<Any>>
        get() = _updateTokenFirebase

    fun updateTokenFirebase(newToken: String, androidDeviceId: String?, deviceName: String?) {
        viewModelScope.launch {
            _updateTokenFirebase.value = UiState.Loading
            try {
                val response = apiHelperImpl.saveFirebaseToken(newToken,androidDeviceId, deviceName)
                if (response.result_code == 0) {
                    _updateTokenFirebase.value = UiState.Success(response)
                } else {
                    _updateTokenFirebase.value = UiState.Error(response.data.toString())
                }
            } catch (e: Exception) {
                _updateTokenFirebase.value = UiState.Error("${(e as HttpException).response()?.code()}")
            }
        }
    }

    private val sharedDataNotify = MutableLiveData<String>()
    fun setSharedNotifyData(data: String) {
        sharedDataNotify.value = data
    }

    fun getSharedNotifyData(): LiveData<String> {
        return sharedDataNotify
    }

    private val sharedDataListJobMedia = MutableLiveData<List<JobMediaDetailResponse>?>()
    fun setShareListJobMedia(jobMedia: List<JobMediaDetailResponse>?) {
        sharedDataListJobMedia.postValue(jobMedia)
    }

    fun getSharedListJobMedia(): LiveData<List<JobMediaDetailResponse>?> {
        return sharedDataListJobMedia
    }

    private val sharedDataListJobMaterial = MutableLiveData<List<JobMaterialDetailResponse>?>()
    fun setShareListJobMaterial(jobMedia: List<JobMaterialDetailResponse>?) {
        sharedDataListJobMaterial.postValue(jobMedia)
    }

    fun getSharedListJobMaterial(): LiveData<List<JobMaterialDetailResponse>?> {
        return sharedDataListJobMaterial
    }

    private val _getUserProfile = MutableLiveData<UiState<Any>>()
    val getUserProfile : LiveData<UiState<Any>>
        get() = _getUserProfile
    fun getUserProfile(username: String) {
        viewModelScope.launch {
            _getUserProfile.value = UiState.Loading
            try {
                val responseJson = apiHelperImpl.getUserProfile(username)
                val response = convertJsonToObject(responseJson, UserProfileResponse::class.java)
                if (response.result_code == 0) {
                    _getUserProfile.value = UiState.Success(response)
                } else {
                    _getUserProfile.value = UiState.Error(response.data.toString())
                }
            } catch (e: Exception) {
                _getUserProfile.value = UiState.Error("Error message: ${e.message}")
            }
        }
    }

    private val sharedDataUserProfile = MutableLiveData<UserProfileResponse>()
    fun setDataUserProfile(data: UserProfileResponse) {
        sharedDataUserProfile.value = data
    }

    fun getSharedDataUserProfile(): LiveData<UserProfileResponse> {
        return sharedDataUserProfile
    }


}