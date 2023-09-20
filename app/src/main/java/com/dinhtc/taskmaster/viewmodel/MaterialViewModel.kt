package com.dinhtc.taskmaster.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinhtc.taskmaster.model.request.AddMaterialRequest
import com.dinhtc.taskmaster.model.request.DeleteMaterialRequest
import com.dinhtc.taskmaster.model.request.DeleteMediaRequest
import com.dinhtc.taskmaster.model.response.JobMaterialDetailResponse
import com.dinhtc.taskmaster.model.response.ListMaterialResponse
import com.dinhtc.taskmaster.service.ApiHelperImpl
import com.dinhtc.taskmaster.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MaterialViewModel @Inject constructor(private val apiHelperImpl: ApiHelperImpl) : ViewModel() {

    private var _dataListMaterial = MutableLiveData<UiState<ListMaterialResponse>>()
    val dataListMaterial : LiveData<UiState<ListMaterialResponse>>
        get() = _dataListMaterial

    private var materialJob: Job? = null

    fun getListMaterial(){
        viewModelScope.launch {
            _dataListMaterial.value = UiState.Loading
            try {
                val response = apiHelperImpl.getListMaterial()
                if (response.result_code == 0) {
                    _dataListMaterial.value = UiState.Success(response)
                } else {
                    _dataListMaterial.value = UiState.Error(response.data.toString())
                }
            } catch (e: Exception) {
                _dataListMaterial.value = UiState.Error("Error message: ${e.message}")
            }
        }
    }

    private val _datAddMaterial = MutableLiveData<UiState<Any>?>()
    val datAddMaterial : LiveData<UiState<Any>?>
        get() = _datAddMaterial

    fun addMaterial(dataMaterial: AddMaterialRequest){
       materialJob = viewModelScope.launch {
            _datAddMaterial.value = UiState.Loading
            try {
                val response = apiHelperImpl.addMaterial(dataMaterial)
                if (response.result_code == 0) {
                    _datAddMaterial.value = UiState.Success(response)
                } else {
                    _datAddMaterial.value = UiState.Error(response.data.toString())
                }
            } catch (e: Exception) {
                _datAddMaterial.value = UiState.Error("Error message: ${e.message}")
            }
        }
    }

    private val _dataDeleteMaterial = MutableLiveData<UiState<Any>>()
    val dataDeleteMaterial: LiveData<UiState<Any>>
        get() = _dataDeleteMaterial

    fun deleteMaterial(material: JobMaterialDetailResponse) {
        viewModelScope.launch {
            _dataDeleteMaterial.value = UiState.Loading
            try {
                val deleteMediaRequest = DeleteMaterialRequest(
                    material.jobId,
                    material.mateId
                )
                val response =
                    apiHelperImpl.deleteMaterial(
                        deleteMediaRequest
                    )
                if (response.result_code == 0) {
                    _dataDeleteMaterial.value = UiState.Success(response)
                } else {
                    _dataDeleteMaterial.value = UiState.Error(response.data.toString())
                }
            } catch (e: Exception) {
                _dataDeleteMaterial.value = UiState.Error("Error message: ${e.message}")
            }
        }
    }

    fun cleanup() {
        materialJob?.cancel()
        _datAddMaterial.value = null
    }
}