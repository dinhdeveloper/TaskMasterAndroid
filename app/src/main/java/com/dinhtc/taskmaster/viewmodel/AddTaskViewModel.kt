package com.dinhtc.taskmaster.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinhtc.taskmaster.model.request.AddTaskRequest
import com.dinhtc.taskmaster.model.request.CollectPointRequest
import com.dinhtc.taskmaster.model.response.ListCollectPointResponse
import com.dinhtc.taskmaster.model.response.ListEmployeeResponse
import com.dinhtc.taskmaster.model.response.ListJobTypeResponse
import com.dinhtc.taskmaster.service.ApiHelperImpl
import com.dinhtc.taskmaster.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTaskViewModel @Inject constructor(private val apiHelperImpl: ApiHelperImpl) : ViewModel() {

    private val _dataJobType = MutableLiveData<UiState<ListJobTypeResponse>>()
    val dataJobType : LiveData<UiState<ListJobTypeResponse>>
        get() = _dataJobType

    fun getListJobType(){
        viewModelScope.launch {
            _dataJobType.value = UiState.Loading
            try {
                val response = apiHelperImpl.getListJobType()
                if (response.result_code == 0) {
                    _dataJobType.value = UiState.Success(response)
                } else {
                    _dataJobType.value = UiState.Error(response.data.toString())
                }
            } catch (e: Exception) {
                _dataJobType.value = UiState.Error("Error message: ${e.message}")
            }
        }
    }

    private val _dataEmployee = MutableLiveData<UiState<ListEmployeeResponse>>()
    val dataEmployee: LiveData<UiState<ListEmployeeResponse>>
        get() = _dataEmployee

    fun getListEmployee(){
        viewModelScope.launch {
            _dataEmployee.value = UiState.Loading
            try {
                val response = apiHelperImpl.getListEmployee()
                if (response.result_code == 0) {
                    _dataEmployee.value = UiState.Success(response)
                } else {
                    _dataEmployee.value = UiState.Error(response.data.toString())
                }
            } catch (e: Exception) {
                _dataEmployee.value = UiState.Error("Error message: ${e.message}")
            }
        }
    }

    private val _dataEmployeeNotById = MutableLiveData<UiState<ListEmployeeResponse>>()
    val dataEmployeeListNotById : LiveData<UiState<ListEmployeeResponse>>
        get() = _dataEmployeeNotById

    fun getListEmployeeNotById(id : Int){
        viewModelScope.launch {
            _dataEmployeeNotById.value = UiState.Loading
            try {
                val response = apiHelperImpl.getListEmployeeNotById(id)
                if (response.result_code == 0) {
                    _dataEmployeeNotById.value = UiState.Success(response)
                } else {
                    _dataEmployeeNotById.value = UiState.Error(response.data.toString())
                }
            } catch (e: Exception) {
                _dataEmployeeNotById.value = UiState.Error("Error message: ${e.message}")
            }
        }
    }
    private val _dataListCollectPoint = MutableLiveData<UiState<ListCollectPointResponse>>()
    val dataListCollectPoint: LiveData<UiState<ListCollectPointResponse>>
        get() = _dataListCollectPoint

    fun getListCollectPoint() {
        viewModelScope.launch {
            _dataListCollectPoint.value = UiState.Loading
            try {
                val response = apiHelperImpl.getListCollectPoint()
                if (response.result_code == 0) {
                    _dataListCollectPoint.value = UiState.Success(response)
                } else {
                    _dataListCollectPoint.value = UiState.Error(response.data.toString())
                }
            } catch (e: Exception) {
                _dataListCollectPoint.value = UiState.Error("Error message: ${e.message}")
            }
        }
    }


    private val _dataAddCollectPoint = MutableLiveData<UiState<Any>>()
    val dataAddCollectPoint: LiveData<UiState<Any>>
        get() = _dataAddCollectPoint

    fun addCollectPoint(data: CollectPointRequest) {
        viewModelScope.launch {
            _dataAddCollectPoint.value = UiState.Loading
            try {
                val response = apiHelperImpl.addCollectPoint(data)
                if (response.result_code == 0) {
                    getListCollectPoint()
                    _dataAddCollectPoint.value = UiState.Success(response)
                } else {
                    _dataAddCollectPoint.value = UiState.Error(response.data.toString())
                }
            } catch (e: Exception) {
                _dataAddCollectPoint.value = UiState.Error("Error message: ${e.message}")
            }
        }
    }

    private val _dataAddTask = MutableLiveData<UiState<Any>>()
    val dataAddTask: LiveData<UiState<Any>>
        get() = _dataAddTask
    fun addTask(addTaskRequest: AddTaskRequest) {
        viewModelScope.launch {
            _dataAddTask.value = UiState.Loading
            try {
                val response = apiHelperImpl.addTask(addTaskRequest)
                if (response.result_code == 0) {
                    _dataAddTask.value = UiState.Success(response)
                } else {
                    _dataAddTask.value = UiState.Error(response.data.toString())
                }
            } catch (e: Exception) {
                _dataAddTask.value = UiState.Error("Error message: ${e.message}")
            }
        }
    }
}