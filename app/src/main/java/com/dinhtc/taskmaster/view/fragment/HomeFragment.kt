package com.dinhtc.taskmaster.view.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dinhtc.taskmaster.R
import com.dinhtc.taskmaster.adapter.StickyHeaderItemDecoration
import com.dinhtc.taskmaster.adapter.TableViewAdapter
import com.dinhtc.taskmaster.common.view.BaseFragment
import com.dinhtc.taskmaster.databinding.FragmentHomeBinding
import com.dinhtc.taskmaster.model.LogisticInfoModel
import com.dinhtc.taskmaster.utils.DialogFactory
import com.dinhtc.taskmaster.utils.LoadingScreen
import com.dinhtc.taskmaster.utils.SharedPreferencesManager
import com.dinhtc.taskmaster.utils.UiState
import com.dinhtc.taskmaster.utils.eventbus.AppEventBus
import com.dinhtc.taskmaster.utils.eventbus.EventBusAction
import com.dinhtc.taskmaster.utils.observe
import com.dinhtc.taskmaster.view.activity.MainActivity.Companion.TAG_LOG
import com.dinhtc.taskmaster.viewmodel.SharedViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate


@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(), AppEventBus.EventBusHandler {

    private var statusUser: String = "TOI"
    private var radioPersonLocal: String = SharedPreferencesManager.instance.getString(SharedPreferencesManager.USERNAME, null)
    private var radioTaskLocal: String = "1"
    private lateinit var radioTodayDate: String
    private var showLayoutSearch = true

    override val layoutResourceId: Int
        get() = R.layout.fragment_home

    override fun onViewCreated() {
        actionView()
        setupAdapterLogistic()
    }

    private fun setupAdapterLogistic() {
        val tableViewAdapter = TableViewAdapter()
        tableViewAdapter.submitList(logisticInfoModels)
        viewBinding.recyclerViewMovieList.layoutManager = LinearLayoutManager(context)
        viewBinding.recyclerViewMovieList.setHasFixedSize(true)
        viewBinding.recyclerViewMovieList.adapter = tableViewAdapter

        val stickyHeaderDecoration = StickyHeaderItemDecoration(tableViewAdapter)
        viewBinding.recyclerViewMovieList.addItemDecoration(stickyHeaderDecoration)

        tableViewAdapter.setOnClickItem(object : TableViewAdapter.OnItemClickListener {
            override fun onClickItem(logisticsModel: LogisticInfoModel?) {
                findNavController().navigate(
                    R.id.action_homeFragment_to_detailFragment,
                            bundleOf(ID_JOB to logisticsModel?.idOrder?.toInt())
                    //bundleOf(LOGISTIC_MODEL to logisticsModel)
                )
            }
        })
    }
    @SuppressLint("ResourceType")
    private fun actionView() {
        viewBinding.apply {
//            layoutSave.btnSubmit.setOnClickListener {
//                findNavController().navigate(R.id.action_homeFragment_to_addTaskFragment)
//            }
            notifyIcon.setOnClickListener {

            }
            searchIcon.setOnClickListener {
                val dialog = Dialog(requireContext())
                dialog.setContentView(R.layout.sheet_content)

                dialog.window!!.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                findByView(dialog)
                dialog.window!!.setGravity(Gravity.TOP)
                dialog.show()
            }
        }
    }

    private fun findByView(dialog: Dialog) {
        val btnSeach = dialog.findViewById<TextView>(R.id.btnSeach)

        val radioCuaToi = dialog.findViewById<AppCompatTextView>(R.id.radioCuaToi)
        val radioNhom = dialog.findViewById<AppCompatTextView>(R.id.radioNhom)
        val radioTatCaNguoi = dialog.findViewById<AppCompatTextView>(R.id.radioTatCaNguoi)

        val radioChuaXong = dialog.findViewById<AppCompatTextView>(R.id.radioChuaXong)
        val radioDaXong = dialog.findViewById<AppCompatTextView>(R.id.radioDaXong)
        val radioTatCaTask = dialog.findViewById<AppCompatTextView>(R.id.radioTatCaTask)

        val daThanhToan = dialog.findViewById<AppCompatTextView>(R.id.daThanhToan)
        val chuaThanhToan = dialog.findViewById<AppCompatTextView>(R.id.chuaThanhToan)

        val radioHomNay = dialog.findViewById<AppCompatTextView>(R.id.radioHomNay)
        val radioHomQua = dialog.findViewById<AppCompatTextView>(R.id.radioHomQua)

        onClickRadioPerson(radioCuaToi,radioNhom,radioTatCaNguoi)
        onClickRadioTask(radioChuaXong,radioDaXong,radioTatCaTask)
        onClickRadioMoney(daThanhToan,chuaThanhToan)
        onClickRadioDate(radioHomNay,radioHomQua)

        btnSeach.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun onClickRadioMoney(daThanhToan: AppCompatTextView?, chuaThanhToan: AppCompatTextView?) {
        daThanhToan?.setOnClickListener {
            chuaThanhToan?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            daThanhToan.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_check_search) }
        }
        chuaThanhToan?.setOnClickListener {
            daThanhToan?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            chuaThanhToan.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_check_search) }
        }
    }

    private fun onClickRadioPerson(
        radioCuaToi: AppCompatTextView?,
        radioNhom: AppCompatTextView?,
        radioTatCaNguoi: AppCompatTextView?
    ) {
        radioCuaToi?.setOnClickListener {
            radioNhom?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioTatCaNguoi?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioCuaToi.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_check_search) }
        }
        radioNhom?.setOnClickListener {
            radioCuaToi?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioTatCaNguoi?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioNhom.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_check_search) }
        }
        radioTatCaNguoi?.setOnClickListener {
            radioCuaToi?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioNhom?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioTatCaNguoi.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_check_search) }
        }
    }

    private fun onClickRadioTask(
        radioChuaXong: AppCompatTextView?,
        radioDaXong: AppCompatTextView?,
        radioTatCaTask: AppCompatTextView?
    ) {
        radioChuaXong?.setOnClickListener {
            radioDaXong?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioTatCaTask?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioChuaXong.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_check_search) }
        }
        radioDaXong?.setOnClickListener {
            radioChuaXong?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioTatCaTask?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioDaXong.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_check_search) }
        }
        radioTatCaTask?.setOnClickListener {
            radioChuaXong?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioDaXong?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioTatCaTask.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_check_search) }
        }
    }

    private fun onClickRadioDate(radioHomNay: AppCompatTextView?, radioHomQua: AppCompatTextView?) {
        radioHomNay?.setOnClickListener {
            radioHomQua?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioHomNay.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_check_search) }
        }
        radioHomQua?.setOnClickListener {
            radioHomNay?.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_item_detail) }
            radioHomQua.background = context?.let { it1 -> ContextCompat.getDrawable(it1,R.drawable.bg_check_search) }
        }
    }

    private fun getYesterdayLocalDate(): LocalDate {
        radioTodayDate = LocalDate.now().minusDays(1).toString()
        return LocalDate.now().minusDays(1)
    }
    private fun getTodayLocalDate(): LocalDate {
        radioTodayDate = LocalDate.now().toString()
        return LocalDate.now()
    }

    override fun onDestroy() {
        AppEventBus.getInstance().unRegisterEvent(this)
        super.onDestroy()
    }

    override fun handleEvent(result: EventBusAction) {
        if (result.action == EventBusAction.Action.CHANGE_LOGO){
            viewBinding.notifyIcon.setImageResource(R.drawable.animation_list)
            val animationDrawable = viewBinding.notifyIcon.drawable as AnimationDrawable
            animationDrawable.start()
        }
    }

    companion object {
        val ID_JOB = "ID_JOB"
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