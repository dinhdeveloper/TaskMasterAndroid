package com.dinhtc.taskmaster.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinhtc.taskmaster.model.response.LoginResponse
import com.dinhtc.taskmaster.service.ApiHelperImpl
import com.dinhtc.taskmaster.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(private val apiHelperImpl: ApiHelperImpl) : ViewModel() {

    private val _dataLogin = MutableLiveData<UiState<Any>>()
    val dataLogin : LiveData<UiState<Any>>
    get() = _dataLogin

    fun loginUser(userName: String, passWord: String) {
        viewModelScope.launch {
            _dataLogin.value = UiState.Loading
            val response = apiHelperImpl.loginUser(userName,passWord)
            if (response.result_code == 0) {
                _dataLogin.value = UiState.Success(response)
            } else {
                _dataLogin.value = UiState.Error(response.data.toString())
            }
        }
    }
}