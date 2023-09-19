package com.dinhtc.taskmaster.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinhtc.taskmaster.model.request.CompactedAndDoneRequest
import com.dinhtc.taskmaster.model.request.DataUpdateJobRequest
import com.dinhtc.taskmaster.model.request.UpdateStateWeightedRequest
import com.dinhtc.taskmaster.model.response.JobDetailsResponse
import com.dinhtc.taskmaster.model.response.ListCollectPointLatLng
import com.dinhtc.taskmaster.model.response.UpdateJobsResponse
import com.dinhtc.taskmaster.service.ApiHelperImpl
import com.dinhtc.taskmaster.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class JobsViewModel @Inject constructor(private val apiHelperImpl: ApiHelperImpl) : ViewModel() {

    private val _dataJobDetail = MutableLiveData<UiState<JobDetailsResponse>>()
    val dataJobDetail : LiveData<UiState<JobDetailsResponse>>
        get() = _dataJobDetail

    fun getJobDetails(idJob: Int, empId : Int){
        viewModelScope.launch {
            _dataJobDetail.value = UiState.Loading
            try {
                val response = apiHelperImpl.getJobDetails(idJob,empId)
                if (response.result_code == 0) {
                    _dataJobDetail.value = UiState.Success(response)
                } else {
                    _dataJobDetail.value = UiState.Error(response.data.toString())
                }
            } catch (e: Exception) {
                _dataJobDetail.value = UiState.Error("Error message: ${e.message}")
            }
        }
    }


    private val _updateStateJobWeighted = MutableLiveData<UiState<UpdateJobsResponse>>()
    val updateStateJobWeighted : LiveData<UiState<UpdateJobsResponse>>
        get() = _updateStateJobWeighted

    fun updateStateWeightedJob(dataUpdate: UpdateStateWeightedRequest) {
        viewModelScope.launch {
            _updateStateJobWeighted.value = UiState.Loading
            try {
                val response = apiHelperImpl.updateStateWeightedJob(dataUpdate)
                if (response.result_code == 0) {
                    _updateStateJobWeighted.value = UiState.Success(response)
                } else {
                    _updateStateJobWeighted.value = UiState.Error(response.data.toString())
                }
            } catch (e: Exception) {
                _updateStateJobWeighted.value = UiState.Error("Error message: ${e.message}")
            }
        }
    }


    private val _updateJobDetails = MutableLiveData<UiState<Any>>()
    val updateJobDetails : LiveData<UiState<Any>>
        get() = _updateJobDetails

    fun updateJobDetails(dataUpdate: DataUpdateJobRequest) {
        viewModelScope.launch {
            _updateJobDetails.value = UiState.Loading
            try {
                val response = apiHelperImpl.updateJobDetails(dataUpdate)
                if (response.result_code == 0) {
                    _updateJobDetails.value = UiState.Success(response)
                } else {
                    _updateJobDetails.value = UiState.Error(response.data.toString())
                }
            } catch (e: Exception) {
                _updateJobDetails.value = UiState.Error("Error message: ${e.message}")
            }
        }
    }

    private val _getCollectPointLatLng = MutableLiveData<UiState<ListCollectPointLatLng>>()
    val getCollectPointLatLng : LiveData<UiState<ListCollectPointLatLng>>
        get() = _getCollectPointLatLng

    fun getCollectPointLatLng() {
        viewModelScope.launch {
            _getCollectPointLatLng.value = UiState.Loading
            try {
                val response = apiHelperImpl.getCollectPointLatLng()
//               getCollectPointLatLng val gson = Gson()
//                val response2 = Gson().toJson(response.data)
//                val data = gson.fromJson(response2, ListCollectPointLatLng::class.java)
//
//                val groupedData = data.listItem.groupBy { it.latitude to it.longitude }
//                    .map { (key, value) ->
//                        val fullName = value.joinToString { it.fullName }
//                        CollectPointLatLng(
//                            jobId = value.first().jobId,
//                            latitude = key.first,
//                            longitude = key.second,
//                            cpName = value.first().cpName,  // Lấy giá trị đầu tiên của cpName trong nhóm
//                            fullName = fullName,
//                            jobStateDesc = value.first().jobStateDesc  // Lấy giá trị đầu tiên của jobStateDesc trong nhóm
//                        )
//                    }
                if (response.result_code == 0) {
                    _getCollectPointLatLng.value = UiState.Success(response)
                } else {
                    _getCollectPointLatLng.value = UiState.Error(response.data.toString())
                }
            } catch (e: Exception) {
                _getCollectPointLatLng.value = UiState.Error("Error message: ${e.message}")
            }
        }
    }


    private val _updateStateJobCompactedAndDone = MutableLiveData<UiState<UpdateJobsResponse>>()
    val updateStateJobCompactedAndDone : LiveData<UiState<UpdateJobsResponse>>
        get() = _updateStateJobCompactedAndDone

    fun updateStateJobCompactedAndDone(dataLamGonAndDaXong: CompactedAndDoneRequest) {
        viewModelScope.launch {
            _updateStateJobCompactedAndDone.value = UiState.Loading
            try {
                val response = apiHelperImpl.updateStateJobCompactedAndDone(dataLamGonAndDaXong)
                if (response.result_code == 0) {
                    _updateStateJobCompactedAndDone.value = UiState.Success(response)
                } else {
                    _updateStateJobCompactedAndDone.value = UiState.Error(response.data.toString())
                }
            } catch (e: Exception) {
                _updateStateJobCompactedAndDone.value = UiState.Error("Error message: ${e.message}")
            }
        }
    }
}