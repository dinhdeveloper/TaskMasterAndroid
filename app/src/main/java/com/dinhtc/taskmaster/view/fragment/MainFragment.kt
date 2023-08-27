package com.dinhtc.taskmaster.view.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dinhtc.taskmaster.R
import com.dinhtc.taskmaster.common.view.BaseFragment
import com.dinhtc.taskmaster.databinding.FragmentMainBinding
import com.dinhtc.taskmaster.utils.DialogFactory
import com.dinhtc.taskmaster.utils.LoadingScreen
import com.dinhtc.taskmaster.utils.SharedPreferencesManager
import com.dinhtc.taskmaster.utils.UiState
import com.dinhtc.taskmaster.utils.eventbus.AppEventBus
import com.dinhtc.taskmaster.utils.eventbus.EventBusAction
import com.dinhtc.taskmaster.utils.observe
import com.dinhtc.taskmaster.viewmodel.SharedViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment  : BaseFragment<FragmentMainBinding>(), AppEventBus.EventBusHandler  {

    private val sharedViewModel: SharedViewModel by viewModels()

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

            },
        )

        onClickItem()
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
                findNavController().navigate(R.id.action_mainFragment_to_settingFragment)
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
                        SharedPreferencesManager.TOKEN_FIREBASE, null))
                count ++
            }

        }
    }
}