package com.dinhtc.taskmaster.view.activity

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.dinhtc.taskmaster.R
import com.dinhtc.taskmaster.common.view.BaseActivity
import com.dinhtc.taskmaster.databinding.ActivityMainBinding
import com.dinhtc.taskmaster.utils.AndroidUtils
import com.dinhtc.taskmaster.utils.AndroidUtils.getAndroidDeviceId
import com.dinhtc.taskmaster.utils.SharedPreferencesManager
import com.dinhtc.taskmaster.utils.eventbus.AppEventBus
import com.dinhtc.taskmaster.utils.eventbus.EventBusAction
import com.dinhtc.taskmaster.viewmodel.SharedViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(){
    var sharedViewModel: SharedViewModel? = null
    override val layoutResourceId: Int
        get() = R.layout.activity_main

    override fun onCreateActivity() {
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        findNavController(R.id.navhost)

        val navController = findNavController(R.id.navhost)

        val fragmentToOpen = intent.getStringExtra("fragmentToOpen")
        if (fragmentToOpen != null) {
            when (fragmentToOpen) {
                "DETAIL" -> {
                    navController.navigate(R.id.action_homeFragment_to_detailFragment)
                }
                // Xử lý các fragment khác tương tự ở đây
            }
        }
    }
    companion object {
        val TAG_LOG = "API_NOTE: "
    }
}