package com.dinhtc.taskmaster.view.fragment

import android.annotation.SuppressLint
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.dinhtc.taskmaster.R
import com.dinhtc.taskmaster.adapter.ImageViewAdapter
import com.dinhtc.taskmaster.adapter.MaterialAdapter
import com.dinhtc.taskmaster.bottomsheet.BottomSheetAddFreight
import com.dinhtc.taskmaster.common.view.BaseFragment
import com.dinhtc.taskmaster.common.widgets.spinner.ItemViewLocation
import com.dinhtc.taskmaster.common.widgets.spinner.ProvinceData
import com.dinhtc.taskmaster.databinding.FragmentMaterialDetailBinding
import com.dinhtc.taskmaster.model.response.JobDetailsResponse
import com.dinhtc.taskmaster.model.response.JobMaterialDetailResponse
import com.dinhtc.taskmaster.model.response.ListMaterialResponse
import com.dinhtc.taskmaster.utils.DialogFactory
import com.dinhtc.taskmaster.utils.LoadingScreen
import com.dinhtc.taskmaster.utils.UiState
import com.dinhtc.taskmaster.utils.observe
import com.dinhtc.taskmaster.view.activity.MainActivity
import com.dinhtc.taskmaster.viewmodel.JobsViewModel
import com.dinhtc.taskmaster.viewmodel.MaterialViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MaterialDetailFragment : BaseFragment<FragmentMaterialDetailBinding>(){

    private var bottomSheetAddVatLieu: BottomSheetAddFreight? = null
    private var nodataAdapter: MaterialAdapter? = null
    private var listJobMaterial : MutableList<JobMaterialDetailResponse> = mutableListOf()
    private var jobsId: Int = -1
    private val dataListJob = ArrayList<ItemViewLocation<ProvinceData>>()

    private val materialViewModel: MaterialViewModel by viewModels()
    private val jobsViewModel: JobsViewModel by viewModels()
    override val layoutResourceId: Int
        get() = R.layout.fragment_material_detail

    override fun onViewCreated() {
        if (activity is MainActivity){
           val dataMaterial = (activity as MainActivity).sharedViewModel?.getSharedListJobMaterial()?.value
            if (dataMaterial != null){
                for (values in dataMaterial!!){
                    jobsId = values.jobId
                    listJobMaterial.add(values)
                }

                nodataAdapter = context?.let { MaterialAdapter(it) }
                nodataAdapter?.submitList(listJobMaterial)
                nodataAdapter?.setOnClickListener(object : MaterialAdapter.OnClickListener {
                    override fun onItemClick(position: Int, media: JobMaterialDetailResponse) {
                        DialogFactory.createMessageDialogWithYesNo(
                            context,
                            "Bạn chắc chắn xóa vật liệu này không?",
                            "Có",
                            "Không",
                            {
                                nodataAdapter?.removeItem(position)
                            },
                            {}
                        )

                    }
                })

                viewBinding.recyclerView.apply {
                    adapter = nodataAdapter
                    val layoutManager = LinearLayoutManager(context)
                    setLayoutManager(layoutManager)
                    setHasFixedSize(true)
                }
            }
        }

        materialViewModel.getListMaterial()

        observe(materialViewModel.dataListMaterial, ::onGetListMaterialLive)
        observe(materialViewModel.datAddMaterial, ::addMaterialLive)
        observe(jobsViewModel.dataJobDetail, ::dataJobDetailLive)


        onClickItem()
    }

    private fun onClickItem() {
        viewBinding.floatingAction.setOnClickListener {
            if (dataListJob.isNotEmpty()) {
                showDialogAddVatLieu()
            }
        }
    }

    private fun showDialogAddVatLieu() {
        bottomSheetAddVatLieu = context?.let {
            BottomSheetAddFreight(it, dataListJob, jobsId) { dataAdd ->

                materialViewModel.addMaterial(dataAdd)
            }
        }
        bottomSheetAddVatLieu?.isCancelable = false
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        activity?.supportFragmentManager?.let {
            bottomSheetAddVatLieu?.show(
                it,
                bottomSheetAddVatLieu?.tag
            )
        }
    }

    private fun onGetListMaterialLive(uiState: UiState<ListMaterialResponse>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                val listMaterialLiveData = uiState.data.data.listItem
                for (data in listMaterialLiveData) {
                    dataListJob.add(
                        ItemViewLocation(
                            ProvinceData(
                                data.mate_id,
                                "${data.mate_id}",
                                "${data.name}"
                            )
                        )
                    )
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

    private fun addMaterialLive(uiState: UiState<Any>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                //DialogFactory.showDialogDefaultNotCancel(context, "${uiState.data.data}")
                Toast.makeText(context,"${uiState.data.data}",Toast.LENGTH_SHORT).show()
                jobsViewModel.getJobDetails(idJob = jobsId)
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

    @SuppressLint("NotifyDataSetChanged")
    private fun dataJobDetailLive(uiState: UiState<JobDetailsResponse>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                jobsId = uiState.data.data.jobId
                listJobMaterial = uiState.data.data.jobMaterial as MutableList<JobMaterialDetailResponse>
                nodataAdapter?.submitList(listJobMaterial)
                viewBinding.recyclerView.setHasFixedSize(true)
                bottomSheetAddVatLieu?.dismiss()
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
}