package com.dinhtc.taskmaster.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinhtc.taskmaster.model.request.AddMaterialRequest
import com.dinhtc.taskmaster.model.response.ListMaterialResponse
import com.dinhtc.taskmaster.service.ApiHelperImpl
import com.dinhtc.taskmaster.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MaterialViewModel @Inject constructor(private val apiHelperImpl: ApiHelperImpl) : ViewModel() {

    private val _dataListMaterial = MutableLiveData<UiState<ListMaterialResponse>>()
    val dataListMaterial : LiveData<UiState<ListMaterialResponse>>
        get() = _dataListMaterial

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

    private val _datAddMaterial = MutableLiveData<UiState<Any>>()
    val datAddMaterial : LiveData<UiState<Any>>
        get() = _datAddMaterial

    fun addMaterial(dataMaterial: AddMaterialRequest){
        viewModelScope.launch {
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
}