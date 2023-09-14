package com.dinhtc.taskmaster.view.fragment

import android.util.Log
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dinhtc.taskmaster.R
import com.dinhtc.taskmaster.common.view.BaseFragment
import com.dinhtc.taskmaster.common.widgets.spinner.ItemViewLocation
import com.dinhtc.taskmaster.common.widgets.spinner.ProvinceData
import com.dinhtc.taskmaster.databinding.FragmentSettingBinding
import com.dinhtc.taskmaster.model.response.ListJobTypeResponse
import com.dinhtc.taskmaster.model.response.UserProfileResponse
import com.dinhtc.taskmaster.utils.AndroidUtils
import com.dinhtc.taskmaster.utils.DialogFactory
import com.dinhtc.taskmaster.utils.LoadingScreen
import com.dinhtc.taskmaster.utils.SharedPreferencesManager
import com.dinhtc.taskmaster.utils.UiState
import com.dinhtc.taskmaster.utils.observe
import com.dinhtc.taskmaster.view.activity.MainActivity
import com.dinhtc.taskmaster.viewmodel.UsersViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SettingFragment : BaseFragment<FragmentSettingBinding>() {

    private val viewModel: UsersViewModel by viewModels()
    override val layoutResourceId: Int
        get() = R.layout.fragment_setting

    override fun onViewCreated() {
        val dataUserProfile = arguments?.getSerializable(MainFragment.KEY_USER_PROFILE)
        if (dataUserProfile != null){
            dataUserProfile as UserProfileResponse
            var address = "${dataUserProfile.numAddress}, ${dataUserProfile.streetAddress}, ${dataUserProfile.ward}, ${dataUserProfile.dist}, ${dataUserProfile.province}"
            viewBinding.apply {
                tvName.text = if (dataUserProfile.name?.isNotEmpty() == true) dataUserProfile.name else ""
                tvAge.text = if (dataUserProfile.age != null) "Tuổi: ${dataUserProfile.age.toInt()}" else "Tuổi"
                tvGender.text = if (dataUserProfile.gender?.isNotEmpty() == true) "Giới tính: ${dataUserProfile.gender}" else "Giới tính"
                tvNameAddress.text = address
                tvTeamName.text = "Nhóm: ${dataUserProfile.teamName}"
            }
        }

        viewBinding.btnLogout.setOnClickListener {
            viewModel.logoutUser()
//            logout()
//            findNavController().navigate(R.id.action_settingFragment_to_loginFragment)
        }

        observe(viewModel.dataLogout, ::dataLogoutLive)
    }
    private fun dataLogoutLive(uiState: UiState<Any>){
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                AndroidUtils.logout()
                findNavController().navigate(R.id.action_settingFragment_to_loginFragment)
            }

            is UiState.Error -> {
                LoadingScreen.hideLoading()
                val errorMessage = uiState.message
                if (errorMessage == "401"){
                    AndroidUtils.logout()
                    findNavController().navigate(R.id.action_settingFragment_to_loginFragment)
                }else{
                    DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
                }
                Log.e(MainActivity.TAG_ERROR, "dataLogoutLive: $errorMessage")
            }

            UiState.Loading -> {
                LoadingScreen.displayLoadingWithText(
                    requireContext(),
                    "Đăng xuất...",
                    false
                )
            }
        }
    }
}