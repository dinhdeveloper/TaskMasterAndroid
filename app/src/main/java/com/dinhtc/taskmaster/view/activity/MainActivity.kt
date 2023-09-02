package com.dinhtc.taskmaster.view.activity

import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.dinhtc.taskmaster.R
import com.dinhtc.taskmaster.common.view.BaseActivity
import com.dinhtc.taskmaster.databinding.ActivityMainBinding
import com.dinhtc.taskmaster.viewmodel.SharedViewModel
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

        val jobIdNoti = intent.getStringExtra("OPEN_FRAGMENT")
        if (jobIdNoti != null) {
            navController.navigate(R.id.action_loginFragment_to_detailFragment,
                bundleOf(JOB_ID to jobIdNoti )
            )
//            when (fragmentToOpen) {
//                "DETAIL" -> {
//                    navController.navigate(R.id.action_homeFragment_to_detailFragment)
//                }
//                // Xử lý các fragment khác tương tự ở đây
//            }
        }
    }
    companion object {
        val TAG_LOG = "API_NOTE: "
        val JOB_ID = "JOB_ID"
    }
}