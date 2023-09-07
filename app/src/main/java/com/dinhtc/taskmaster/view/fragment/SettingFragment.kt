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
import com.dinhtc.taskmaster.utils.DialogFactory
import com.dinhtc.taskmaster.utils.LoadingScreen
import com.dinhtc.taskmaster.utils.SharedPreferencesManager
import com.dinhtc.taskmaster.utils.UiState
import com.dinhtc.taskmaster.utils.observe
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
                tvName.text = dataUserProfile.name
                tvAge.text = "Tuổi: ${dataUserProfile.age.toInt()}"
                tvGender.text = "Giới tính: ${dataUserProfile.gender}"
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
                logout()
                findNavController().navigate(R.id.action_settingFragment_to_loginFragment)
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
                    "Đăng xuất...",
                    false
                )
            }
        }
    }

    // Đăng xuất người dùng
    private fun logout() {
        // Xóa thông tin đăng nhập từ SharedPreferences
        SharedPreferencesManager.instance.remove(SharedPreferencesManager.USERNAME)
        SharedPreferencesManager.instance.remove(SharedPreferencesManager.IS_LOGGED_IN)
        SharedPreferencesManager.instance.remove(SharedPreferencesManager.LAST_LOGIN_TINE)
        SharedPreferencesManager.instance.remove(SharedPreferencesManager.TOKEN_LOGIN)
        SharedPreferencesManager.instance.remove(SharedPreferencesManager.TOKEN_FIREBASE)
        SharedPreferencesManager.instance.remove(SharedPreferencesManager.ROLE_CODE)
        SharedPreferencesManager.instance.remove(SharedPreferencesManager.USER_ID)
        SharedPreferencesManager.instance.remove(SharedPreferencesManager.PASS_W)
        // Chuyển đến màn hình đăng nhập
    }
}