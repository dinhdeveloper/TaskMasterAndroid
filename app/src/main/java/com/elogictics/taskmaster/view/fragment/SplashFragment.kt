package com.elogictics.taskmaster.view.fragment

import android.os.Handler
import android.os.Looper
import androidx.navigation.fragment.findNavController
import com.elogictics.taskmaster.R
import com.elogictics.taskmaster.databinding.FragmentSplashBinding
import com.elogictics.taskmaster.common.view.BaseFragment

class SplashFragment : BaseFragment<FragmentSplashBinding>() {
    override val layoutResourceId: Int
        get() = R.layout.fragment_splash

    override fun onViewCreated() {
        Handler(Looper.getMainLooper()).postDelayed({
            findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
        }, 3000)
    }
}