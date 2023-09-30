package com.elogictics.taskmaster.view.fragment

import android.annotation.SuppressLint
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.elogictics.taskmaster.R
import com.elogictics.taskmaster.adapter.MaterialAdapter
import com.elogictics.taskmaster.bottomsheet.BottomSheetAddFreight
import com.elogictics.taskmaster.common.view.BaseFragment
import com.elogictics.taskmaster.common.widgets.spinner.ItemViewLocation
import com.elogictics.taskmaster.common.widgets.spinner.ProvinceData
import com.elogictics.taskmaster.databinding.FragmentMaterialDetailBinding
import com.elogictics.taskmaster.model.response.JobDetailsResponse
import com.elogictics.taskmaster.model.response.JobMaterialDetailResponse
import com.elogictics.taskmaster.model.response.ListMaterialResponse
import com.elogictics.taskmaster.utils.DialogFactory
import com.elogictics.taskmaster.utils.LoadingScreen
import com.elogictics.taskmaster.utils.UiState
import com.elogictics.taskmaster.utils.observe
import com.elogictics.taskmaster.utils.observes
import com.elogictics.taskmaster.view.activity.MainActivity
import com.elogictics.taskmaster.viewmodel.JobsViewModel
import com.elogictics.taskmaster.viewmodel.MaterialViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MaterialDetailFragment : BaseFragment<FragmentMaterialDetailBinding>(){

    private var bottomSheetAddVatLieu: BottomSheetAddFreight? = null
    private var noDataAdapter: MaterialAdapter? = null
    private var listJobMaterial : MutableList<JobMaterialDetailResponse> = mutableListOf()
    private val dataListJob = ArrayList<ItemViewLocation<ProvinceData>>()

    private var jobsId: Int = -1
    private var empId: Int = -1
    private var positionDelete: Int = -1

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

                noDataAdapter = context?.let { MaterialAdapter(it) }
                noDataAdapter?.submitList(listJobMaterial)
                noDataAdapter?.setOnClickListener(object : MaterialAdapter.OnClickListener {
                    override fun onItemClick(position: Int, material: JobMaterialDetailResponse) {
                        DialogFactory.createMessageDialogWithYesNo(
                            context,
                            "Bạn chắc chắn xóa vật liệu này không?",
                            "Có",
                            "Không",
                            {
                                positionDelete = position
                                materialViewModel.deleteMaterial(material)
                            },
                            {}
                        )

                    }
                })

                viewBinding.recyclerView.apply {
                    adapter = noDataAdapter
                    val layoutManager = LinearLayoutManager(context)
                    setLayoutManager(layoutManager)
                    setHasFixedSize(true)
                }
            }
        }

        materialViewModel.getListMaterial()

        observe(materialViewModel.dataListMaterial, ::onGetListMaterialLive)
        observes(materialViewModel.datAddMaterial, ::addMaterialLive)
        observe(jobsViewModel.dataJobDetail, ::dataJobDetailLive)
        observe(materialViewModel.dataDeleteMaterial, ::dataDeleteMaterialLive)


        onClickItem()
    }

    private fun onClickItem() {
        viewBinding.layoutToolBar.apply {
            titleToolBar.text = "Danh sách vật liệu"
            imgBackParent.setOnClickListener {
                findNavController().popBackStack()
            }
            imgHome.setOnClickListener {
                findNavController().popBackStack(R.id.mainFragment,false)
            }
        }
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
                Log.e(MainActivity.TAG_ERROR, "onGetListMaterialLive: $errorMessage")
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
            }

            UiState.Loading -> {
                LoadingScreen.displayLoadingWithText(
                    requireContext(),
                    "Vui lòng chờ...",
                    false
                )
            }
        }
    }

    private fun addMaterialLive(uiState: UiState<Any>?) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                //DialogFactory.showDialogDefaultNotCancel(context, "${uiState.data.data}")
                Toast.makeText(context,"${uiState.data.data}",Toast.LENGTH_SHORT).show()
                jobsViewModel.getJobDetails(idJob = jobsId, empId = empId )
            }

            is UiState.Error -> {
                val errorMessage = uiState.message
                Log.e(MainActivity.TAG_ERROR, "addMaterialLive: $errorMessage")
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
            }

            UiState.Loading -> {}
            else -> {}
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun dataJobDetailLive(uiState: UiState<JobDetailsResponse>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                jobsId = uiState.data.data.jobId
                listJobMaterial = uiState.data.data.jobMaterial as MutableList<JobMaterialDetailResponse>
                noDataAdapter?.submitList(listJobMaterial)
                viewBinding.recyclerView.setHasFixedSize(true)
                bottomSheetAddVatLieu?.dismiss()
            }

            is UiState.Error -> {
                val errorMessage = uiState.message
                Log.e(MainActivity.TAG_ERROR, "dataJobDetailLive: $errorMessage")
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
            }

            UiState.Loading -> {}
        }
    }
    private fun dataDeleteMaterialLive(uiState: UiState<Any>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancelAndClick(context, "${uiState.data.data}"){
                    jobsViewModel.getJobDetails(idJob = jobsId, empId = empId)
                    bottomSheetAddVatLieu?.dismiss()
                    noDataAdapter?.removeItem(positionDelete)
                }
            }

            is UiState.Error -> {
                val errorMessage = uiState.message
                Log.e(MainActivity.TAG_ERROR, "dataDeleteMaterialLive: $errorMessage")
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
            }

            UiState.Loading -> {}
        }
    }
}