package com.elogictics.taskmaster.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elogictics.taskmaster.model.request.SearchRequest
import com.elogictics.taskmaster.model.response.ListCollectPointResponse
import com.elogictics.taskmaster.model.response.ListJobSearchResponse
import com.elogictics.taskmaster.service.ApiHelperImpl
import com.elogictics.taskmaster.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val apiHelperImpl: ApiHelperImpl) : ViewModel() {

    private val _dataSearch = MutableLiveData<UiState<ListJobSearchResponse>>()
    val dataSearch : LiveData<UiState<ListJobSearchResponse>>
        get() = _dataSearch
    fun search(searchRequest: SearchRequest) {
        viewModelScope.launch {
            _dataSearch.value = UiState.Loading
            try {
                val response = apiHelperImpl.search(searchRequest)
                if (response.result_code == 0) {
                    _dataSearch.value = UiState.Success(response)
                } else {
                    _dataSearch.value = UiState.Error(response.data.toString())
                }
            } catch (e: Exception) {
                _dataSearch.value = UiState.Error("Error message: ${e.message}")
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
            } catch (e: IllegalStateException) {
                _dataListCollectPoint.value = UiState.Error("Error message: ${e.message}")
            }
        }
    }
}