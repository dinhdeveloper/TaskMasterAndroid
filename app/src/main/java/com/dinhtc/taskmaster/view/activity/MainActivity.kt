package com.dinhtc.taskmaster.view.activity

import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.dinhtc.taskmaster.R
import com.dinhtc.taskmaster.common.view.BaseActivity
import com.dinhtc.taskmaster.databinding.ActivityMainBinding
import com.dinhtc.taskmaster.utils.SharedPreferencesManager
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
        val rememberLogin = SharedPreferencesManager.instance.getBoolean(SharedPreferencesManager.IS_LOGGED_IN,false)
        if (jobIdNoti != null) {
            if (rememberLogin){
                navController.navigate(R.id.action_loginFragment_to_detailFragment,
                    bundleOf(ID_JOB_NOTIFY to jobIdNoti )
                )
            } else{
                navController.navigate(R.id.loginFragment,
                    bundleOf(ID_JOB_NOTIFY to jobIdNoti )
                )
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.navhost)
        val prevDestinationId = navController.previousBackStackEntry?.destination?.id ?: -1

       return when{
           navController.currentDestination?.id == R.id.detailFragment &&
                   prevDestinationId == R.id.loginFragment -> {

               navController.navigate(R.id.mainFragment)
               //clear back stack
                       true
           }
            else -> navController.navigateUp()
        }


        //return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        onSupportNavigateUp()
    }


    companion object {
        val TAG_LOG = "API_NOTE: "
        val ID_JOB_NOTIFY = "ID_JOB_NOTIFY"
    }
}