package com.dinhtc.taskmaster.view.fragment

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dinhtc.taskmaster.R
import com.dinhtc.taskmaster.databinding.FragmentLoginBinding
import com.dinhtc.taskmaster.common.view.BaseFragment
import com.dinhtc.taskmaster.model.response.LoginResponse
import com.dinhtc.taskmaster.utils.DialogFactory
import com.dinhtc.taskmaster.utils.LoadingScreen
import com.dinhtc.taskmaster.utils.SharedPreferencesManager
import com.dinhtc.taskmaster.utils.SharedPreferencesManager.*
import com.dinhtc.taskmaster.utils.SharedPreferencesManager.Companion.IS_LOGGED_IN
import com.dinhtc.taskmaster.utils.SharedPreferencesManager.Companion.LAST_LOGIN_TINE
import com.dinhtc.taskmaster.utils.SharedPreferencesManager.Companion.PASS_W
import com.dinhtc.taskmaster.utils.SharedPreferencesManager.Companion.USERNAME
import com.dinhtc.taskmaster.utils.UiState
import com.dinhtc.taskmaster.viewmodel.UsersViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>() {
    private var checkShowEyePass: Boolean = false
    private val viewModel: UsersViewModel by viewModels()

    override val layoutResourceId: Int
        get() = R.layout.fragment_login

    override fun onViewCreated() {
        context?.let { SharedPreferencesManager.init(it) }
        checkAutoLogin()
        actionView()
    }

    private fun checkAutoLogin() {
        val isLoggedIn = SharedPreferencesManager.instance.getBoolean(IS_LOGGED_IN, false)
        val lastLoginTime = SharedPreferencesManager.instance.getLong(LAST_LOGIN_TINE, 0)
        if (isLoggedIn) {
            // Kiểm tra thời gian đăng nhập gần nhất
            val currentTime = System.currentTimeMillis()
            val elapsedTime = currentTime - lastLoginTime
            //val maxLoginDuration = (7 * 24 * 60 * 60 * 1000).toLong() // 7 ngày
            val maxLoginDuration = (1 * 24 * 60 * 60 * 1000).toLong() // 7 ngày
            if (elapsedTime <= maxLoginDuration) {
                // Đăng nhập tự động
                // Chuyển đến màn hình chính
                findNavController().navigate(
                    R.id.action_loginFragment_to_homeFragment
                )
            } else {
                // Đăng xuất người dùng
                logout()
            }
        }
    }

    // Đăng xuất người dùng
    private fun logout() {
        // Xóa thông tin đăng nhập từ SharedPreferences
        SharedPreferencesManager.instance.remove(USERNAME)
        SharedPreferencesManager.instance.remove(IS_LOGGED_IN)
        SharedPreferencesManager.instance.remove(LAST_LOGIN_TINE)
        // Chuyển đến màn hình đăng nhập
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun actionView() {
        viewBinding.btnLogin.setOnClickListener {
            if (checkValidate()) {
                viewModel.loginUser(
                    viewBinding.edtUsername.text.toString().trim(),
                    viewBinding.edtPassword.text.toString().trim()
                )

                viewModel.dataLogin.observe(viewLifecycleOwner) { uiState ->
                    when (uiState) {
                        is UiState.Success -> {
                            LoadingScreen.hideLoading()
                            val loginData = uiState.data.data as LoginResponse
                            SharedPreferencesManager.instance.putString(
                                SharedPreferencesManager.TOKEN_LOGIN,
                                loginData.tokenAuth
                            )
                            rememberLogin(
                                viewBinding.edtUsername.text.toString().trim(),
                                viewBinding.edtPassword.text.toString().trim()
                            )
                        }

                        is UiState.Error -> {
                            val errorMessage = uiState.message
                            Log.e("SSSSSSSSSSS", errorMessage)
                            LoadingScreen.hideLoading()
                            DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
                        }

                        UiState.Loading -> {
                            LoadingScreen.displayLoadingWithText(
                                context,
                                "Please wait...",
                                false
                            )
                        }
                    }
                }

//                rememberLogin(
//                    viewBinding.edtUsername.text.toString().trim(),
//                    viewBinding.edtPassword.text.toString().trim()
//                )
            }
        }

        viewBinding.edtUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().trim().isEmpty()) {
                    viewBinding.edtUsername.background = context?.let { it1 ->
                        ContextCompat.getDrawable(
                            it1, R.drawable.bg_button_strock
                        )
                    }
                }
            }

        })

        viewBinding.edtPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().trim().isEmpty()) {
                    viewBinding.edtPassword.background = context?.let { it1 ->
                        ContextCompat.getDrawable(
                            it1, R.drawable.bg_button_strock
                        )
                    }
                }
            }

        })

        viewBinding.edtPassword.setOnTouchListener(View.OnTouchListener { v, event ->
            val DRAWABLE_RIGHT = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= viewBinding.edtPassword.right - viewBinding.edtPassword.compoundDrawables.get(
                        DRAWABLE_RIGHT
                    )?.bounds?.width()!!
                ) {
                    if (checkShowEyePass) {
                        viewBinding.edtPassword.transformationMethod =
                            PasswordTransformationMethod.getInstance()
                        viewBinding.edtPassword.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            null,
                            context?.let {
                                ContextCompat.getDrawable(it, R.drawable.eye_off)
                            }, null
                        )
                    } else {
                        viewBinding.edtPassword.transformationMethod =
                            HideReturnsTransformationMethod.getInstance()
                        viewBinding.edtPassword.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            null,
                            context?.let {
                                ContextCompat.getDrawable(it, R.drawable.eye_open)
                            }, null
                        )
                    }
                    checkShowEyePass = !checkShowEyePass
                    return@OnTouchListener true
                }
            }
            false
        })
    }

    private fun checkValidate(): Boolean {
        if (!viewBinding.edtUsername.text.toString().trim()
                .isNullOrEmpty() && !viewBinding.edtPassword.text.toString().trim().isNullOrEmpty()
        ) {
            viewBinding.edtUsername.background = context?.let { it1 ->
                ContextCompat.getDrawable(
                    it1, R.drawable.bg_button_strock
                )
            }
            viewBinding.edtPassword.background = context?.let { it1 ->
                ContextCompat.getDrawable(
                    it1, R.drawable.bg_button_strock
                )
            }
            return true
        } else {
            viewBinding.edtUsername.background = context?.let { it1 ->
                ContextCompat.getDrawable(
                    it1, R.drawable.bg_button_strock_red
                )
            }
            viewBinding.edtPassword.background = context?.let { it1 ->
                ContextCompat.getDrawable(
                    it1, R.drawable.bg_button_strock_red
                )
            }
            return false
        }
    }

    private fun rememberLogin(edtUsername: String, edtPassword: String) {
        SharedPreferencesManager.instance.putString(USERNAME, edtUsername)
        SharedPreferencesManager.instance.putString(PASS_W, edtUsername)
        SharedPreferencesManager.instance.putBoolean(IS_LOGGED_IN, true)
        SharedPreferencesManager.instance.putLong(LAST_LOGIN_TINE, System.currentTimeMillis())
        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        LoadingScreen.hideLoading()
    }
}