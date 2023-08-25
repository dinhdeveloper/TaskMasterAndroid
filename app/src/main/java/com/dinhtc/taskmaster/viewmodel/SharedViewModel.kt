package com.dinhtc.taskmaster.viewmodel

import androidx.lifecycle.LiveData

import androidx.lifecycle.MutableLiveData

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinhtc.taskmaster.model.response.JobMaterialDetailResponse
import com.dinhtc.taskmaster.model.response.JobMediaDetailResponse
import com.dinhtc.taskmaster.model.response.LoginResponse
import com.dinhtc.taskmaster.service.ApiHelperImpl
import com.dinhtc.taskmaster.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SharedViewModel @Inject constructor(private val apiHelperImpl: ApiHelperImpl) : ViewModel() {

    private val _updateTokenFirebase = MutableLiveData<UiState<Any>>()
    val updateTokenFirebase : LiveData<UiState<Any>>
        get() = _updateTokenFirebase

    fun updateTokenFirebase(newToken: String) {
        viewModelScope.launch {
            _updateTokenFirebase.value = UiState.Loading
            try {
                val response = apiHelperImpl.saveFirebaseToken(newToken)
                if (response.result_code == 0) {
                    _updateTokenFirebase.value = UiState.Success(response)
                } else {
                    _updateTokenFirebase.value = UiState.Error(response.data.toString())
                }
            } catch (e: Exception) {
                _updateTokenFirebase.value = UiState.Error("Error message: ${e.message}")
            }
        }
    }

    private val sharedData = MutableLiveData<String>()
    fun setSharedData(data: String) {
        sharedData.value = data
    }

    fun getSharedData(): LiveData<String> {
        return sharedData
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
}