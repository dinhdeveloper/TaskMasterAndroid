package com.elogictics.taskmaster.view.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.wifi.WifiManager
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.elogictics.taskmaster.R
import com.elogictics.taskmaster.common.view.BaseActivity
import com.elogictics.taskmaster.common.widgets.dialog.FullScreenDialogFragment
import com.elogictics.taskmaster.databinding.ActivityMainBinding
import com.elogictics.taskmaster.utils.LoadingScreen
import com.elogictics.taskmaster.utils.SharedPreferencesManager
import com.elogictics.taskmaster.viewmodel.SharedViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(){
    var sharedViewModel: SharedViewModel? = null
    var checkNavFragmentDetail: Boolean = false

    override val layoutResourceId: Int
        get() = R.layout.activity_main

    override fun onCreateActivity() {
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        findNavController(R.id.navhost)

        val navController = findNavController(R.id.navhost)

        val jobIdNotify = intent.getStringExtra("OPEN_FRAGMENT")
        val jobTypeNotify = intent.getStringExtra("INFO_FRAGMENT")
        //val rememberLogin = SharedPreferencesManager.instance.getBoolean(SharedPreferencesManager.IS_LOGGED_IN,false)
        if (jobIdNotify != null) {
            if (rememberLoginLocal){
                navController.navigate(R.id.mainFragment)
            } else{
                navController.navigate(R.id.loginFragment)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.navhost)
        val prevDestinationId = navController.previousBackStackEntry?.destination?.id ?: -1

       return when{
           navController.currentDestination?.id == R.id.mainFragment
                   && prevDestinationId == R.id.loginFragment -> {
               navController.navigate(R.id.mainFragment)
               true
           }
           navController.currentDestination?.id == R.id.detailFragment &&
                   prevDestinationId == R.id.homeFragment -> {
               checkNavFragmentDetail = true
               navController.navigateUp()
           }

           navController.currentDestination?.id == R.id.homeFragment &&
                   prevDestinationId == R.id.mainFragment -> {
               checkNavFragmentDetail = false
               removeStateSearch()
               navController.navigateUp()
           }

            else -> navController.navigateUp()
        }


        //return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun removeStateSearch() {
        SharedPreferencesManager.instance.remove(FullScreenDialogFragment.RADIO_PERSON)
        SharedPreferencesManager.instance.remove(FullScreenDialogFragment.RADIO_TASK)
        SharedPreferencesManager.instance.remove(FullScreenDialogFragment.RADIO_PAYMENT)
        SharedPreferencesManager.instance.remove(FullScreenDialogFragment.FIRST_DATE)
        SharedPreferencesManager.instance.remove(FullScreenDialogFragment.SECOND_DATE)
    }

    private fun checkConnection() {
        LoadingScreen.hideLoading()
        if (isOnline(this)) {
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }, 1000L)
        } else {
            val parentLayout = findViewById<View>(android.R.id.content)
            val snackbar =
                Snackbar.make(parentLayout, "Không có internet.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY") {
                        val  timer = object : android.os.CountDownTimer(5000, 1000) {
                            override fun onTick(millisUntilFinished: Long) {
                                LoadingScreen.displayLoadingWithText(
                                    this@MainActivity,
                                    "Vui lòng chờ...",
                                    false
                                )
                                val wifiManager =
                                    applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                                wifiManager.isWifiEnabled = true
                            }

                            override fun onFinish() {
                                LoadingScreen.hideLoading()
                                checkConnection()
                            }
                        }
                        (timer as CountDownTimer).start()
                    }
                    .setActionTextColor(Color.parseColor("#4AA5FE"))
                    .setTextColor(Color.parseColor("#F40707"))
            snackbar.show()
        }
    }

    override fun onBackPressed() {
        onSupportNavigateUp()
    }


    companion object {
        var rememberLoginLocal = false
        val TAG_LOG = "API_NOTE: "
        val ID_JOB_NOTIFY = "ID_JOB_NOTIFY"
        val TAG_ERROR = "TAG_ERROR_"
    }
}