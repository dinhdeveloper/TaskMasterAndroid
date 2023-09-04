package com.dinhtc.taskmaster.view.fragment

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dinhtc.taskmaster.R
import com.dinhtc.taskmaster.adapter.StickyHeaderItemDecoration
import com.dinhtc.taskmaster.adapter.TableViewAdapter
import com.dinhtc.taskmaster.common.view.BaseFragment
import com.dinhtc.taskmaster.databinding.FragmentHomeBinding
import com.dinhtc.taskmaster.model.LogisticInfoModel
import com.dinhtc.taskmaster.model.response.ListJobSearchResponse
import com.dinhtc.taskmaster.model.response.SearchResponse
import com.dinhtc.taskmaster.utils.eventbus.AppEventBus
import com.dinhtc.taskmaster.utils.eventbus.EventBusAction
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(), AppEventBus.EventBusHandler {

    private var dataSearch: List<SearchResponse>? = null
    override val layoutResourceId: Int
        get() = R.layout.fragment_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parentFragmentManager.setFragmentResultListener(
            REQUEST_KEY,
            this
        ) { _, result ->
            val data = result.getSerializable(BUNDLE_KEY) as ListJobSearchResponse
            dataSearch = data.data
            setupAdapterLogistic(dataSearch)
        }
    }

    override fun onViewCreated() {
        actionView()
        if (dataSearch?.isNotEmpty() == true){
            setupAdapterLogistic(dataSearch)
            viewBinding.imageEmpty.visibility = View.GONE
        }else{
            viewBinding.imageEmpty.visibility = View.VISIBLE
        }
    }

    private fun setupAdapterLogistic(data: List<SearchResponse>?) {
        viewBinding.tvCount.text = "Danh sách công việc: ${dataSearch?.size}"
        val tableViewAdapter = TableViewAdapter()
        data?.let { tableViewAdapter.submitList(it) }
        viewBinding.imageEmpty.visibility = View.GONE
        viewBinding.recyclerViewMovieList.layoutManager = LinearLayoutManager(context)
        viewBinding.recyclerViewMovieList.setHasFixedSize(true)
        viewBinding.recyclerViewMovieList.adapter = tableViewAdapter

        val stickyHeaderDecoration = StickyHeaderItemDecoration(tableViewAdapter)
        viewBinding.recyclerViewMovieList.addItemDecoration(stickyHeaderDecoration)

        tableViewAdapter.setOnClickItem(object : TableViewAdapter.OnItemClickListener {
            override fun onClickItem(logisticsModel: SearchResponse?) {
                findNavController().navigate(
                    R.id.action_homeFragment_to_detailFragment,
                    bundleOf(
                        ID_JOB to logisticsModel?.jobId,
                        ID_EMP to logisticsModel?.empId,
                    )
                    //bundleOf(LOGISTIC_MODEL to logisticsModel)
                )
            }
        })
    }
    private fun actionView() {
        viewBinding.apply {
            floatingAction.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_searchActionFragment)
            }
        }
    }


    override fun onDestroy() {
        AppEventBus.getInstance().unRegisterEvent(this)
        super.onDestroy()
    }

    override fun handleEvent(result: EventBusAction) {
//        if (result.action == EventBusAction.Action.CHANGE_LOGO){
//            viewBinding.notifyIcon.setImageResource(R.drawable.animation_list)
//            val animationDrawable = viewBinding.notifyIcon.drawable as AnimationDrawable
//            animationDrawable.start()
//        }
    }

    companion object {
        val ID_JOB = "ID_JOB"
        val ID_EMP = "ID_EMP"
        val REQUEST_KEY = "REQUEST_KEY"
        val BUNDLE_KEY = "BUNDLE_KEY"
        val KEY_NUMBER = "KEY_NUMBER"
    }
}