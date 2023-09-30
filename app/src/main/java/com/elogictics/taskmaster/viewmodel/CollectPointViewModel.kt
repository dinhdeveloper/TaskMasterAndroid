package com.elogictics.taskmaster.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elogictics.taskmaster.model.SuggestionNoteModel
import com.elogictics.taskmaster.model.request.AddTaskRequest
import com.elogictics.taskmaster.model.request.CollectPointRequest
import com.elogictics.taskmaster.model.response.ListCollectPointResponse
import com.elogictics.taskmaster.model.response.ListEmployeeResponse
import com.elogictics.taskmaster.model.response.ListJobTypeResponse
import com.elogictics.taskmaster.service.ApiHelperImpl
import com.elogictics.taskmaster.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectPointViewModel @Inject constructor(private val apiHelperImpl: ApiHelperImpl) : ViewModel() {

    private var suggestionIdCounter = 1

    private val _dataEmployee = MutableLiveData<UiState<ListEmployeeResponse>>()
    val dataEmployee: LiveData<UiState<ListEmployeeResponse>>
        get() = _dataEmployee

    private val _dataListCollectPoint = MutableLiveData<UiState<ListCollectPointResponse>>()
    val dataListCollectPoint: LiveData<UiState<ListCollectPointResponse>>
        get() = _dataListCollectPoint

    private val _combinedData = MediatorLiveData<List<SuggestionNoteModel>>()
    val combinedData: LiveData<List<SuggestionNoteModel>>
        get() = _combinedData

    init {
        _combinedData.addSource(dataEmployee) { employeeState ->
            val collectPointState = dataListCollectPoint.value
            val combinedState = combineAndCreateList(employeeState, collectPointState)
            _combinedData.value = combinedState
        }

        _combinedData.addSource(dataListCollectPoint) { collectPointState ->
            val employeeState = dataEmployee.value
            val combinedState = combineAndCreateList(employeeState, collectPointState)
            _combinedData.value = combinedState
        }

        viewModelScope.launch {
            getListEmployee()
            getListCollectPoint()
        }
    }

    fun getListEmployee() {
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

    private fun combineAndCreateList(
        employeeState: UiState<ListEmployeeResponse>?,
        collectPointState: UiState<ListCollectPointResponse>?
    ): List<SuggestionNoteModel> {
        // Xử lý kết quả từ cả hai LiveData ở đây và tạo MutableList<SuggestionModel>
        val combinedList = mutableListOf<SuggestionNoteModel>()

        if (employeeState is UiState.Success) {
            if (employeeState.data.data != null) {
                val employeeData = employeeState.data.data
                for (data in employeeData.listItem) {
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

        if (collectPointState is UiState.Success) {
            val collectPointData = collectPointState.data
            if (collectPointData.data != null) {
                for (data in collectPointData.data.listItem) {
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

        return sortedList
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


    fun fetchEmployeeAndCollectPoint() {
        viewModelScope.launch {
            val deferredEmployee = async { apiHelperImpl.getListEmployee() }
            val deferredCollectPoint = async { apiHelperImpl.getListCollectPoint() }

            try {
                val employeeResponse = deferredEmployee.await()
                val collectPointResponse = deferredCollectPoint.await()

                if (employeeResponse.result_code == 0 && collectPointResponse.result_code == 0) {
                    // Cả hai API đều thành công
                    _dataEmployee.value = UiState.Success(employeeResponse)
                    _dataListCollectPoint.value = UiState.Success(collectPointResponse)
                } else {
                    // Một trong hai hoặc cả hai API thất bại
                    if (employeeResponse.result_code != 0) {
                        _dataEmployee.value = UiState.Error(employeeResponse.data.toString())
                    }
                    if (collectPointResponse.result_code != 0) {
                        _dataListCollectPoint.value = UiState.Error(collectPointResponse.data.toString())
                    }
                }
            } catch (e: Exception) {
                // Xử lý lỗi nếu có
                _dataEmployee.value = UiState.Error("Error message: ${e.message}")
                _dataListCollectPoint.value = UiState.Error("Error message: ${e.message}")
            }
        }
    }
}