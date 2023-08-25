package com.dinhtc.taskmaster.view.fragment

import android.os.Handler
import android.os.Looper
import androidx.navigation.fragment.findNavController
import com.dinhtc.taskmaster.R
import com.dinhtc.taskmaster.databinding.FragmentSplashBinding
import com.dinhtc.taskmaster.common.view.BaseFragment

class SplashFragment : BaseFragment<FragmentSplashBinding>() {
    override val layoutResourceId: Int
        get() = R.layout.fragment_splash

    override fun onViewCreated() {
        Handler(Looper.getMainLooper()).postDelayed({
            findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
        }, 3000)
    }
}