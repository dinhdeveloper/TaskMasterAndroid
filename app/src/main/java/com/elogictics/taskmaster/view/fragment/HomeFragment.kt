package com.elogictics.taskmaster.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.elogictics.taskmaster.R
import com.elogictics.taskmaster.adapter.SearchViewAdapter
import com.elogictics.taskmaster.adapter.StickyHeaderItemDecoration
import com.elogictics.taskmaster.common.view.BaseFragment
import com.elogictics.taskmaster.common.widgets.dialog.FullScreenDialogFragment
import com.elogictics.taskmaster.common.widgets.spinner.ItemViewLocation
import com.elogictics.taskmaster.common.widgets.spinner.ProvinceData
import com.elogictics.taskmaster.databinding.FragmentHomeBinding
import com.elogictics.taskmaster.model.request.SearchRequest
import com.elogictics.taskmaster.model.response.ListCollectPointResponse
import com.elogictics.taskmaster.model.response.ListJobSearchResponse
import com.elogictics.taskmaster.model.response.SearchResponse
import com.elogictics.taskmaster.utils.DialogFactory
import com.elogictics.taskmaster.utils.LoadingScreen
import com.elogictics.taskmaster.utils.UiState
import com.elogictics.taskmaster.utils.eventbus.AppEventBus
import com.elogictics.taskmaster.utils.observe
import com.elogictics.taskmaster.view.activity.MainActivity
import com.elogictics.taskmaster.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private var itemSearch: SearchRequest? = null
    private var dialogFragment: FullScreenDialogFragment? = null
    private var dataSearch: List<SearchResponse>? = null
    private val searchViewModel: SearchViewModel by viewModels()
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
                        it1 -> searchViewModel.search(it1)
                }
            }
        }
    }
    override fun onViewCreated() {
        searchViewModel.getListCollectPoint()
        observe(searchViewModel.dataListCollectPoint, ::onGetListCollectPoint)
        observe(searchViewModel.dataSearch, ::dataSearchLive)
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
            searchViewModel.search(it)
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
        tableViewAdapter.notifyDataSetChanged()

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