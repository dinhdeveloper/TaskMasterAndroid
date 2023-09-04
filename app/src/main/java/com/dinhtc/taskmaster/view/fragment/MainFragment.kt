package com.dinhtc.taskmaster.view.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dinhtc.taskmaster.R
import com.dinhtc.taskmaster.common.view.BaseFragment
import com.dinhtc.taskmaster.databinding.FragmentMainBinding
import com.dinhtc.taskmaster.model.RoleCode
import com.dinhtc.taskmaster.model.response.UserProfileResponse
import com.dinhtc.taskmaster.utils.AndroidUtils
import com.dinhtc.taskmaster.utils.DialogFactory
import com.dinhtc.taskmaster.utils.LoadingScreen
import com.dinhtc.taskmaster.utils.SharedPreferencesManager
import com.dinhtc.taskmaster.utils.SharedPreferencesManager.Companion.USER_ID
import com.dinhtc.taskmaster.utils.UiState
import com.dinhtc.taskmaster.utils.eventbus.AppEventBus
import com.dinhtc.taskmaster.utils.eventbus.EventBusAction
import com.dinhtc.taskmaster.utils.observe
import com.dinhtc.taskmaster.view.activity.MainActivity
import com.dinhtc.taskmaster.viewmodel.SharedViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment  : BaseFragment<FragmentMainBinding>(), AppEventBus.EventBusHandler  {

    private val sharedViewModel: SharedViewModel by viewModels()
    private var data : UserProfileResponse? = null

    override val layoutResourceId: Int
        get() = R.layout.fragment_main

    override fun onViewCreated() {
        AppEventBus.getInstance().registerEvent(this, EventBusAction.Action.CHANGE_LOGO, this)
        AppEventBus.getInstance().registerEvent(this, EventBusAction.Action.REFRESH_TOKEN_FB, this)
        askNotificationPermission()

        observe(sharedViewModel.updateTokenFirebase, ::updateTokenFirebaseLiveData)

        Firebase.messaging.token.addOnCompleteListener(
            OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("MyFirebaseMsgService", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                Log.e("TOKEN_FCM: ","$token")
                SharedPreferencesManager.instance.putString(SharedPreferencesManager.TOKEN_FIREBASE, token)
                sharedViewModel.updateTokenFirebase(
                    SharedPreferencesManager.instance.getString(
                        SharedPreferencesManager.TOKEN_FIREBASE, null),
                    activity?.applicationContext?.let { deviceId ->
                        AndroidUtils.getAndroidDeviceId(deviceId) },
                    AndroidUtils.getDeviceName()
                )
            },
        )

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
                findNavController().navigate(R.id.action_mainFragment_to_notifyListFragment)
            }
            btnSetting.setOnClickListener {
                var bundle = Bundle()
                bundle.putSerializable(KEY_USER_PROFILE ,data)
                findNavController().navigate(R.id.action_mainFragment_to_settingFragment,
                    bundle
                )
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
                Log.e("SSSSSSSSSSS", errorMessage)
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
            }

            UiState.Loading -> {}
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API Level > 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.POST_NOTIFICATIONS) } ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
                Log.e("API_R", "GỬI LẠI TOKEN")

            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            AppEventBus.getInstance().publishEvent(EventBusAction.Action.REFRESH_TOKEN_FB)
        }
    }

    private var count = 1
    override fun handleEvent(result: EventBusAction) {
        if (result.action == EventBusAction.Action.REFRESH_TOKEN_FB){
            if (count == 1){
                sharedViewModel.updateTokenFirebase(
                    SharedPreferencesManager.instance.getString(
                        SharedPreferencesManager.TOKEN_FIREBASE, null),
                    activity?.applicationContext?.let { deviceId ->
                        AndroidUtils.getAndroidDeviceId(deviceId) },
                    AndroidUtils.getDeviceName()
                    )
                count ++
            }

        }
    }

    private fun getUserProfileLive(uiState: UiState<Any>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                data = uiState.data.data as UserProfileResponse
                viewBinding.tvName.text = data?.name
                data?.empId?.toInt()?.let { SharedPreferencesManager.instance.putInt(USER_ID, it) }

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
                Log.e("SSSSSSSSSSS", errorMessage)
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
            }

            UiState.Loading -> {
                LoadingScreen.displayLoadingWithText(
                    requireContext(),
                    "Please wait...",
                    false
                )
            }
        }
    }
    companion object {
        val KEY_USER_PROFILE = "KEY_USER_PROFILE"
    }
}