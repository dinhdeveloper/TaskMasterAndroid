package com.dinhtc.taskmaster.view.fragment

import android.os.Bundle
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
import java.io.Serializable


@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(), AppEventBus.EventBusHandler {

    override val layoutResourceId: Int
        get() = R.layout.fragment_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parentFragmentManager.setFragmentResultListener(
            REQUEST_KEY,
            this
        ) { _, result ->
            val data = result.getSerializable(BUNDLE_KEY) as ListJobSearchResponse
            setupAdapterLogistic(data)
        }
    }

    override fun onViewCreated() {
        actionView()
    }

    private fun setupAdapterLogistic(data: ListJobSearchResponse?) {
        val tableViewAdapter = TableViewAdapter()
        data?.data?.let { tableViewAdapter.submitList(it) }
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


private val logisticInfoModels = ArrayList<LogisticInfoModel>().apply {
    add(
        LogisticInfoModel(
            1,
            "1",
            "860/29 Xô Viết Nghệ Tĩnh, phường 25, quận Bình Thạnh, TPHCM",
            "Trần Tinh Hoa",
            "Mới",
            "20-03-2023",false
        )
    )
    add(
        LogisticInfoModel(
            2,
            "10",
            "860/29 Xô Viết Nghệ Tĩnh, phường 25, quận Bình Thạnh, TPHCM",
            "Cảnh Vinh Xin",
            "Đã Giao",
            "20-03-2023",false
        )
    )
    add(
        LogisticInfoModel(
            3,
            "11",
            "860/29 Xô Viết Nghệ Tĩnh, phường 25, quận Bình Thạnh, TPHCM",
            "Dinh Hoa Mỹ",
            "Chấp nhận",
            "20-03-2023",false
        )
    )
    add(
        LogisticInfoModel(
            4,
            "12",
            "860/29 Xô Viết Nghệ Tĩnh, phường 25, quận Bình Thạnh, TPHCM",
            "Cảnh Dinh Khùng",
            "Đã làm gọn",
            "20-03-2023",false
        )
    )
    add(
        LogisticInfoModel(
            5,
            "13",
            "860/29 Xô Viết Nghệ Tĩnh, phường 25, quận Bình Thạnh, TPHCM",
            "Đinh",
            "Đã cân",
            "20-03-2023",false
        )
    )
    add(
        LogisticInfoModel(
            6,
            "14",
            "860/29 Xô Viết Nghệ Tĩnh, phường 25, quận Bình Thạnh, TPHCM",
            "Xinh đã Cân",
            "Đã cân",
            "20-03-2023",false
        )
    )
    add(
        LogisticInfoModel(
            7,
            "7",
            "860/29 Xô Viết Nghệ Tĩnh, phường 25, quận Bình Thạnh, TPHCM",
            "Minh",
            "Đã lên xe",
            "7",false
        )
    )
    add(
        LogisticInfoModel(
            8,
            "8",
            "860/29 Xô Viết Nghệ Tĩnh, phường 25, quận Bình Thạnh, TPHCM",
            "QƯERT",
            "Xong",
            "8",false
        )
    )
    add(
        LogisticInfoModel(
            9,
            "9",
            "860/29 Xô Viết Nghệ Tĩnh, phường 25, quận Bình Thạnh, TPHCM",
            "SDGSDG",
            "Từ chối",
            "9",false
        )
    )
    add(
        LogisticInfoModel(
            10,
            "10",
            "860/29 Xô Viết Nghệ Tĩnh, phường 25, quận Bình Thạnh, TPHCM",
            "Cinh",
            "Hủy",
            "10",false
        )
    )
    add(
        LogisticInfoModel(
            11,
            "11",
            "860/29 Xô Viết Nghệ Tĩnh, phường 25, quận Bình Thạnh, TPHCM",
            "Pinh",
            "Đã cân",
            "11",false
        )
    )
    add(
        LogisticInfoModel(
            12,
            "12",
            "860/29 Xô Viết Nghệ Tĩnh, phường 25, quận Bình Thạnh, TPHCM",
            "Minh",
            "Đã cân",
            "12",false
        )
    )
    add(
        LogisticInfoModel(
            13,
            "13",
            "860/29 Xô Viết Nghệ Tĩnh, phường 25, quận Bình Thạnh, TPHCM",
            "Ynh",
            "Đã cân",
            "13",false
        )
    )
    add(
        LogisticInfoModel(
            14,
            "14",
            "860/29 Xô Viết Nghệ Tĩnh, phường 25, quận Bình Thạnh, TPHCM",
            "Binh",
            "Đã cân",
            "14",false
        )
    )
    add(
        LogisticInfoModel(
            15,
            "15",
            "860/29 Xô Viết Nghệ Tĩnh, phường 25, quận Bình Thạnh, TPHCM",
            "Einh",
            "Đã cân",
            "15",false
        )
    )
    add(
        LogisticInfoModel(
            16,
            "16",
            "860/29 Xô Viết Nghệ Tĩnh, phường 25, quận Bình Thạnh, TPHCM",
            "Linh",
            "Đã cân",
            "16",false
        )
    )
    add(
        LogisticInfoModel(
            17,
            "17",
            "860/29 Xô Viết Nghệ Tĩnh, phường 25, quận Bình Thạnh, TPHCM",
            "Winh",
            "Đã cân",
            "17",false
        )
    )
    add(
        LogisticInfoModel(
            18,
            "18",
            "860/29 Xô Viết Nghệ Tĩnh, phường 25, quận Bình Thạnh, TPHCM",
            "Ninh",
            "Đã cân",
            "18",false
        )
    )
    add(
        LogisticInfoModel(
            19,
            "19",
            "860/29 Xô Viết Nghệ Tĩnh, phường 25, quận Bình Thạnh, TPHCM",
            "Qinh",
            "Đã cân",
            "19",false
        )
    )
    add(
        LogisticInfoModel(
            20,
            "20",
            "860/29 Xô Viết Nghệ Tĩnh, phường 25, quận Bình Thạnh, TPHCM",
            "Zinh",
            "Đã cân",
            "20",false
        )
    )

}