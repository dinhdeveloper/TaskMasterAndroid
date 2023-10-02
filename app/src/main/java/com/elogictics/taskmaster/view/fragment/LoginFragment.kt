package com.elogictics.taskmaster.view.fragment

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
import com.elogictics.taskmaster.R
import com.elogictics.taskmaster.common.view.BaseFragment
import com.elogictics.taskmaster.databinding.FragmentLoginBinding
import com.elogictics.taskmaster.utils.AndroidUtils
import com.elogictics.taskmaster.utils.DialogFactory
import com.elogictics.taskmaster.utils.LoadingScreen
import com.elogictics.taskmaster.utils.SharedPreferencesManager
import com.elogictics.taskmaster.utils.SharedPreferencesManager.*
import com.elogictics.taskmaster.utils.SharedPreferencesManager.Companion.IS_LOGGED_IN
import com.elogictics.taskmaster.utils.SharedPreferencesManager.Companion.LAST_LOGIN_TINE
import com.elogictics.taskmaster.utils.SharedPreferencesManager.Companion.PASS_W
import com.elogictics.taskmaster.utils.SharedPreferencesManager.Companion.USERNAME
import com.elogictics.taskmaster.utils.UiState
import com.elogictics.taskmaster.view.activity.MainActivity
import com.elogictics.taskmaster.viewmodel.UsersViewModel
import com.elogictics.taskmaster.BuildConfig.VERSION_NAME
import com.elogictics.taskmaster.view.activity.MainActivity.Companion.TAG_ERROR
import com.elogictics.taskmaster.view.activity.MainActivity.Companion.rememberLoginLocal
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject


@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>() {
    private var jobIdNotify: String? = null
    private var checkShowEyePass: Boolean = false
    private val viewModel: UsersViewModel by viewModels()

    override val layoutResourceId: Int
        get() = R.layout.fragment_login

    override fun onViewCreated() {
        context?.let { SharedPreferencesManager.init(it) }
        //checkAutoLogin()
        actionView()
        jobIdNotify = arguments?.getString(MainActivity.ID_JOB_NOTIFY)
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun actionView() {

        val versionName: String = VERSION_NAME
        viewBinding.tvVersion.text = "App version: $versionName"

        viewBinding.layoutMain.setOnClickListener {
            AndroidUtils.hideKeyboard(viewBinding.layoutMain)
        }

        viewBinding.btnLogin.setOnClickListener {
            if (checkValidate()) {
                viewModel.loginUser(
                    viewBinding.edtUsername.text.toString().trim(),
                    viewBinding.edtPassword.text.toString().trim(),
                    AndroidUtils.getAndroidDeviceId(requireContext()),
                    AndroidUtils.getDeviceName()
                )

                viewModel.dataLogin.observe(viewLifecycleOwner) { uiState ->
                    when (uiState) {
                        is UiState.Success -> {
                            LoadingScreen.hideLoading()
                            val loginData = uiState.data.data.toString()
                            val jsonObject = JSONObject(loginData)
                            val tokenAuth = jsonObject.getString("tokenAuth")
                            val roleCode = jsonObject.getString("role")

                            SharedPreferencesManager.instance.putString(
                                SharedPreferencesManager.TOKEN_LOGIN,
                                tokenAuth
                            )
                            SharedPreferencesManager.instance.putString(
                                SharedPreferencesManager.ROLE_CODE,
                                roleCode
                            )
                            rememberLogin(
                                viewBinding.edtUsername.text.toString().trim(),
                                viewBinding.edtPassword.text.toString().trim()
                            )
                            rememberLoginLocal = true
                        }

                        is UiState.Error -> {
                            val errorMessage = uiState.message
                            Log.e(TAG_ERROR, "Login: +$errorMessage")
                            LoadingScreen.hideLoading()
                            DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
                        }

                        UiState.Loading -> {
                            LoadingScreen.displayLoadingWithText(
                                context,
                                "Đang đăng nhập...",
                                false
                            )
                        }
                    }
                }
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
        SharedPreferencesManager.instance.putString(PASS_W, edtPassword)
        SharedPreferencesManager.instance.putBoolean(IS_LOGGED_IN, true)
        SharedPreferencesManager.instance.putLong(LAST_LOGIN_TINE, System.currentTimeMillis())
        try {
            if (jobIdNotify != null){
                findNavController().navigate(R.id.action_loginFragment_to_detailFragment,
                    bundleOf(MainActivity.ID_JOB_NOTIFY to jobIdNotify)
                )
                LoadingScreen.hideLoading()
            }else{
                findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
                LoadingScreen.hideLoading()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
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
                    R.id.action_loginFragment_to_mainFragment
                )
            } else {
                // Đăng xuất người dùng
                AndroidUtils.logout()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        LoadingScreen.hideLoading()
    }
}