package com.dinhtc.taskmaster.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dinhtc.taskmaster.R
import com.dinhtc.taskmaster.adapter.SearchViewAdapter
import com.dinhtc.taskmaster.adapter.StickyHeaderItemDecoration
import com.dinhtc.taskmaster.common.view.BaseFragment
import com.dinhtc.taskmaster.common.widgets.dialog.FullScreenDialogFragment
import com.dinhtc.taskmaster.common.widgets.spinner.ItemViewLocation
import com.dinhtc.taskmaster.common.widgets.spinner.ProvinceData
import com.dinhtc.taskmaster.databinding.FragmentHomeBinding
import com.dinhtc.taskmaster.model.request.SearchRequest
import com.dinhtc.taskmaster.model.response.ListCollectPointResponse
import com.dinhtc.taskmaster.model.response.ListJobSearchResponse
import com.dinhtc.taskmaster.model.response.SearchResponse
import com.dinhtc.taskmaster.utils.DialogFactory
import com.dinhtc.taskmaster.utils.LoadingScreen
import com.dinhtc.taskmaster.utils.UiState
import com.dinhtc.taskmaster.utils.eventbus.AppEventBus
import com.dinhtc.taskmaster.utils.observe
import com.dinhtc.taskmaster.view.activity.MainActivity
import com.dinhtc.taskmaster.viewmodel.AddTaskViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private var itemSearch: SearchRequest? = null
    private var dialogFragment: FullScreenDialogFragment? = null
    private var dataSearch: List<SearchResponse>? = null
    private val addTaskViewModel: AddTaskViewModel by viewModels()
    private val dataListCollectPoint = ArrayList<ItemViewLocation<ProvinceData>>()

    override val layoutResourceId: Int
        get() = R.layout.fragment_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parentFragmentManager.setFragmentResultListener(
            REQUEST_KEY,
            this
        ) { _, result ->
            val data = result.getString(BUNDLE_KEY) as String
            if (data.isNotEmpty()){
                itemSearch?.let {
                        it1 -> addTaskViewModel.search(it1)
                }
            }
        }
    }
    override fun onViewCreated() {
        addTaskViewModel.getListCollectPoint()
        observe(addTaskViewModel.dataListCollectPoint, ::onGetListCollectPoint)
        observe(addTaskViewModel.dataSearch, ::dataSearchLive)
        viewBinding.layoutToolBar.imgHome.setImageResource(R.drawable.icon_search_while)
        actionView()
    }

    private fun onGetListCollectPoint(uiState: UiState<ListCollectPointResponse>) {
        when (uiState) {
            is UiState.Success -> {
                val listCollectPointLiveData = uiState.data.data.listItem
                if (listCollectPointLiveData != null) {
                    dataListCollectPoint.add(ItemViewLocation(
                        ProvinceData(-1,"-1","Tất cả")
                    ))
                    for (data in listCollectPointLiveData) {
                        dataListCollectPoint.add(
                            ItemViewLocation(
                                ProvinceData(
                                    data.collectPointId,
                                    "${data.collectPointId}",
                                    data.name
                                )
                            )
                        )
                    }
                }
                if (activity is MainActivity){
                    LoadingScreen.hideLoading()
                    if(!(activity as MainActivity).checkNavFragmentDetail){
                        showDialogSearch(dataListCollectPoint)
                    }
                }
            }

            is UiState.Error -> {
                val errorMessage = uiState.message
                Log.e(MainActivity.TAG_ERROR, "onGetListCollectPoint: $errorMessage")
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
            }

            UiState.Loading -> {}
        }
    }

    private fun showDialogSearch(listCollectPoint: ArrayList<ItemViewLocation<ProvinceData>>) {
        dialogFragment = FullScreenDialogFragment(){
            itemSearch = it
            addTaskViewModel.search(it)
        }
        dialogFragment?.setData(listCollectPoint)
        activity?.supportFragmentManager?.let { dialogFragment?.show(it, "FullScreenDialog") }
    }

    private fun setupAdapterLogistic(data: List<SearchResponse>?) {
        viewBinding.tvCount.text = "Danh sách công việc: ${dataSearch?.size}"
        val tableViewAdapter = SearchViewAdapter()
        data?.let { tableViewAdapter.submitList(it) }
        viewBinding.imageEmpty.visibility = View.GONE
        viewBinding.recyclerViewMovieList.visibility = View.VISIBLE
        viewBinding.tvCount.visibility = View.VISIBLE
        viewBinding.recyclerViewMovieList.layoutManager = LinearLayoutManager(context)
        viewBinding.recyclerViewMovieList.setHasFixedSize(true)
        viewBinding.recyclerViewMovieList.adapter = tableViewAdapter

        val stickyHeaderDecoration = StickyHeaderItemDecoration(tableViewAdapter)
        viewBinding.recyclerViewMovieList.addItemDecoration(stickyHeaderDecoration)

        tableViewAdapter.setOnClickItem(object : SearchViewAdapter.OnItemClickListener {
            override fun onClickItem(logisticsModel: SearchResponse?) {
                findNavController().navigate(
                    R.id.action_homeFragment_to_detailFragment,
                    bundleOf(
                        ID_JOB to logisticsModel?.jobId,
                        ID_EMP to logisticsModel?.empId,
                    )
                )
                if (activity is MainActivity){
                    (activity as MainActivity).checkNavFragmentDetail = true
                    //(activity as MainActivity).removeStateSearch()
                }
            }
        })
    }

    private fun dataSearchLive(uiState: UiState<ListJobSearchResponse>) {
        when (uiState) {
            is UiState.Success -> {
                LoadingScreen.hideLoading()
                dataSearch = uiState.data.data.data
                if (dataSearch?.isNotEmpty() == true){
                    setupAdapterLogistic(dataSearch)
                    viewBinding.imageEmpty.visibility = View.GONE
                    viewBinding.recyclerViewMovieList.visibility = View.VISIBLE
                    viewBinding.tvCount.visibility = View.VISIBLE
                }else{
                    viewBinding.imageEmpty.visibility = View.VISIBLE
                    viewBinding.recyclerViewMovieList.visibility = View.GONE
                    viewBinding.tvCount.visibility = View.GONE
                }
            }

            is UiState.Error -> {
                val errorMessage = uiState.message
                Log.e(MainActivity.TAG_ERROR, "dataSearchLive: $errorMessage")
                LoadingScreen.hideLoading()
                DialogFactory.showDialogDefaultNotCancel(context, "$errorMessage")
            }

            UiState.Loading -> {
                LoadingScreen.displayLoadingWithText(context,"Đang tìm kiếm...",false)
            }
        }
    }
    private fun actionView() {
        viewBinding.layoutToolBar.apply {
            titleToolBar.text = "Kết quả tìm kiếm"
            imgBackParent.setOnClickListener {
                findNavController().popBackStack()
                if (activity is MainActivity){
                    (activity as MainActivity).checkNavFragmentDetail = false
                }
            }
            imgHome.setOnClickListener {
                showDialogSearch(dataListCollectPoint)
            }
        }
        viewBinding.apply {
            floatingAction.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroy() {
        AppEventBus.getInstance().unRegisterEvent(this)
        super.onDestroy()
    }


    companion object {
        val ID_JOB = "ID_JOB"
        val ID_EMP = "ID_EMP"
        val REQUEST_KEY = "REQUEST_KEY"
        val BUNDLE_KEY = "BUNDLE_KEY"
        val KEY_NUMBER = "KEY_NUMBER"
    }
}