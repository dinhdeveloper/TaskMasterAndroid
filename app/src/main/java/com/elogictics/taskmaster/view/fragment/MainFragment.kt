package com.elogictics.taskmaster.view.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.elogictics.taskmaster.R
import com.elogictics.taskmaster.common.view.BaseFragment
import com.elogictics.taskmaster.databinding.FragmentMainBinding
import com.elogictics.taskmaster.model.RoleCode
import com.elogictics.taskmaster.model.response.UserProfileResponse
import com.elogictics.taskmaster.utils.AndroidUtils
import com.elogictics.taskmaster.utils.DialogFactory
import com.elogictics.taskmaster.utils.LoadingScreen
import com.elogictics.taskmaster.utils.SharedPreferencesManager
import com.elogictics.taskmaster.utils.SharedPreferencesManager.Companion.FULL_NAME
import com.elogictics.taskmaster.utils.SharedPreferencesManager.Companion.USER_ID
import com.elogictics.taskmaster.utils.UiState
import com.elogictics.taskmaster.utils.observe
import com.elogictics.taskmaster.view.activity.MainActivity
import com.elogictics.taskmaster.viewmodel.SharedViewModel
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment  : BaseFragment<FragmentMainBinding>() {

    private val sharedViewModel: SharedViewModel by viewModels()
    private var data : UserProfileResponse? = null

    override val layoutResourceId: Int
        get() = R.layout.fragment_main

    override fun onViewCreated() {
        askNotificationPermission()

        observe(sharedViewModel.updateTokenFirebase, ::updateTokenFirebaseLiveData)

        onClickItem()

        sharedViewModel.getUserProfile(
            SharedPreferencesManager.instance.getString(
            SharedPreferencesManager.USERNAME,null
        ))

        observe(sharedViewModel.getUserProfile,::getUserProfileLive)
    }

    private fun onClickItem() {
        viewBinding.apply {
            btnSearch.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_homeFragment)
            }
            btnAddTask.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_addTaskFragment)
            }
            btnNotify.setOnClickListener {
                //findNavController().navigate(R.id.action_mainFragment_to_mapFragment)
            }
            btnSetting.setOnClickListener {
                try {
                    if (data != null){
                        var bundle = Bundle()
                        bundle.putSerializable(KEY_USER_PROFILE ,data)
                        findNavController().navigate(R.id.action_mainFragment_to_settingFragment,
                            bundle
                        )
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }

        }
    }
    private fun updateTokenFirebaseLiveData(uiState: UiState<Any>){
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                Log.e("updateTokenFirebaseLiveData","${uiState.data.data}")
            }

            is UiState.Error -> {
                val errorMessage = uiState.message
                Log.e(MainActivity.TAG_ERROR, "updateTokenFirebaseLiveData: $errorMessage")
                LoadingScreen.hideLoading()
                if (errorMessage == "401"){
                    DialogFactory.showDialogDefaultNotCancelAndClick(context,"Phiên đăng nhập đã hết hạn"){
                        AndroidUtils.logout()
                        findNavController().navigate(R.id.action_mainFragment_to_loginFragment)
                    }
                }else{
                    DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
                }
            }

            UiState.Loading -> {}
        }
    }

    private fun askNotificationPermission() {
        if (context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.POST_NOTIFICATIONS) } ==
            PackageManager.PERMISSION_GRANTED
        ) {
            // FCM SDK (and your app) can post notifications.
            Log.e("API_R", "GỬI LẠI TOKEN")
            getFCMToken()

        } else {
            // Directly ask for the permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            getFCMToken()
        }
    }

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                if (token != null){
                    sharedViewModel.updateTokenFirebase(
                        token,
                        activity?.applicationContext?.let { deviceId ->
                            AndroidUtils.getAndroidDeviceId(deviceId) },
                        AndroidUtils.getDeviceName()
                    )
                }
            }
        }
    }

    private fun handleFCMTokenError() {
        // Đã xảy ra lỗi khi lấy token, xử lý theo cách phù hợp
        // Ví dụ: Hiển thị thông báo hoặc hướng dẫn người dùng bật quyền thông báo

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Yêu cầu quyền thông báo")
        builder.setMessage("Ứng dụng cần quyền thông báo để hoạt động chính xác. Hãy bật quyền thông báo trong cài đặt của thiết bị.")
        builder.setPositiveButton("OK") { _, _ ->
            askNotificationPermission()
        }
        builder.setNegativeButton("Hủy") { _, _ ->
            // Xử lý khi người dùng từ chối cấp quyền
        }
        builder.show()
    }
    private fun getUserProfileLive(uiState: UiState<Any>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                data = uiState.data.data as UserProfileResponse
                viewBinding.tvName.text = data?.name
                data?.empId?.toInt()?.let {
                    SharedPreferencesManager.instance.putInt(USER_ID, it)
                }
                data?.name?.let { SharedPreferencesManager.instance.putString(FULL_NAME, it) }

                when (SharedPreferencesManager.instance.getString(
                    SharedPreferencesManager.ROLE_CODE, "") ?: "") {
                    RoleCode.ADMIN.name, RoleCode.LEADER.name, RoleCode.MASTER.name -> {
                        viewBinding.btnAddTask.isEnabled = true
                        viewBinding.btnAddTask.alpha = 1f
                    }
                    else -> {
                        viewBinding.btnAddTask.isEnabled = false
                        viewBinding.btnAddTask.alpha = 0.7f
                    }
                }
            }

            is UiState.Error -> {
                val errorMessage = uiState.message
                Log.e(MainActivity.TAG_ERROR, "getUserProfileLive: $errorMessage")
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
            }

            UiState.Loading -> {
                LoadingScreen.displayLoadingWithText(
                    requireContext(),
                    "Vui lòng chờ...",
                    false
                )
            }
        }
    }
    companion object {
        val KEY_USER_PROFILE = "KEY_USER_PROFILE"
    }
}