package com.elogictics.taskmaster.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elogictics.taskmaster.model.SuggestionNoteModel
import com.elogictics.taskmaster.model.request.AddTaskRequest
import com.elogictics.taskmaster.model.request.CollectPointRequest
import com.elogictics.taskmaster.model.request.SearchRequest
import com.elogictics.taskmaster.model.response.ListCollectPointResponse
import com.elogictics.taskmaster.model.response.ListEmployeeResponse
import com.elogictics.taskmaster.model.response.ListJobSearchResponse
import com.elogictics.taskmaster.model.response.ListJobTypeResponse
import com.elogictics.taskmaster.service.ApiHelperImpl
import com.elogictics.taskmaster.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTaskViewModel @Inject constructor(private val apiHelperImpl: ApiHelperImpl) : ViewModel() {

    private var suggestionIdCounter = 1

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

    fun getListEmployee(): Flow<UiState<ListEmployeeResponse>> = flow {
        emit(UiState.Loading)
        try {
            val response = apiHelperImpl.getListEmployee()
            if (response.result_code == 0) {
                emit(UiState.Success(response))
            } else {
                emit(UiState.Error(response.data.toString()))
            }
        } catch (e: Exception) {
            emit(UiState.Error("Error message: ${e.message}"))
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

    fun getListCollectPoint(): Flow<UiState<ListCollectPointResponse>> = flow {
        emit(UiState.Loading)
        try {
            val response = apiHelperImpl.getListCollectPoint()
            if (response.result_code == 0) {
                emit(UiState.Success(response))
            } else {
                emit(UiState.Error(response.data.toString()))
            }
        } catch (e: Exception) {
//            emit(UiState.Error("Error message: ${e.message}"))
        }
    }


    private val _combinedData = MutableLiveData<MutableList<SuggestionNoteModel>>()
    val combinedData: LiveData<MutableList<SuggestionNoteModel>>
        get() = _combinedData

    init {
        viewModelScope.launch {
            combineAndCreateList().collect { combinedList ->
                _combinedData.value = combinedList
            }
        }

        viewModelScope.launch {
            getListCollectPoint().collect { combinedList ->
                _dataListCollectPoint.value = combinedList
            }
        }

        viewModelScope.launch {
            getListEmployee().collect { combinedList ->
                _dataEmployee.value = combinedList
            }
        }

    }
    private fun combineAndCreateList(): Flow<MutableList<SuggestionNoteModel>> = combine(
        getListEmployee(),
        getListCollectPoint()
    ) { employeeResult, collectPointResult ->
        // Xử lý kết quả từ cả hai Flow ở đây và tạo MutableList<SuggestionModel>
        val combinedList = mutableListOf<SuggestionNoteModel>()

        if (employeeResult is UiState.Success) {
            if (employeeResult.data.data != null){
                val employeeData = employeeResult.data
                for (data in employeeData.data.listItem){
                    val model = SuggestionNoteModel(
                        suggestionIdCounter++,
                        data.empId,
                        data.name,
                        data.numAddress.lowercase()
                    )
                    combinedList.add(model)
                }
            }
        }

        if (collectPointResult is UiState.Success) {
            if (collectPointResult.data.data != null){
                val collectPointData = collectPointResult.data
                for (data in collectPointData.data.listItem){
                    val model = SuggestionNoteModel(
                        suggestionIdCounter++,
                        data.empId,
                        data.name,
                        data.numAddress.lowercase()
                    )
                    combinedList.add(model)
                }
            }
        }

        val sortedList = combinedList.sortedBy { it.name }

        return@combine sortedList.toMutableList()
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
                    _dataAddCollectPoint.value = UiState.Success(response)
                    getListCollectPoint().collect { combinedList ->
                        _dataListCollectPoint.value = combinedList
                    }
                    combineAndCreateList().collect { combinedList ->
                        _combinedData.value = combinedList
                    }
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

    private val _dataEmployeeByJobId = MutableLiveData<UiState<ListEmployeeResponse>>()
    val dataEmployeeByJobId : LiveData<UiState<ListEmployeeResponse>>
        get() = _dataEmployeeByJobId

    fun getListEmployeeByJobId(jobId: Int) {
        viewModelScope.launch {
            _dataEmployeeByJobId.value = UiState.Loading
            try {
                val response = apiHelperImpl.getListEmployeeByJobId(jobId)
                if (response.result_code == 0) {
                    _dataEmployeeByJobId.value = UiState.Success(response)
                } else {
                    _dataEmployeeByJobId.value = UiState.Error(response.data.toString())
                }
            } catch (e: Exception) {
                _dataEmployeeByJobId.value = UiState.Error("Error message: ${e.message}")
            }
        }
    }

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
}