package com.dinhtc.taskmaster.adapter

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dinhtc.taskmaster.R
import com.dinhtc.taskmaster.model.response.SearchResponse
import com.google.gson.Gson
import java.text.Collator
import java.util.Locale

class TableViewAdapter : RecyclerView.Adapter<TableViewAdapter.RowViewHolder>(),
    StickyHeaderItemDecoration.StickyHeaderInterface {

    var onItemClickListener: OnItemClickListener? = null
    private var selectedPosition = -1
    var imageList: List<SearchResponse>? = null

    // Danh sách các item
    val imageListSort: MutableList<SearchResponse> = mutableListOf()

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return TYPE_HEADER
        }
        return TYPE_LIST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
        return if (viewType == TYPE_HEADER) {
            val header =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.table_list_header, parent, false)
            RowViewHolder(header)
        } else {
            val header = LayoutInflater.from(parent.context)
                .inflate(R.layout.table_list_item, parent, false)
            RowViewHolder(header)
        }
    }

    private fun setHeaderBgAndColor(txtView: TextView) {
        txtView.setBackgroundResource(R.drawable.table_header_cell_bg)
        txtView.setTextColor(Color.WHITE)
    }

    private fun setContentBgAndColor(txtView: TextView, modal: SearchResponse?) {
        if (modal?.priority?.equals("1") == true) {
            txtView.setBackgroundResource(R.drawable.table_cell_bg_status_one)
            txtView.setTextColor(Color.WHITE)
        } else if (modal?.priority?.equals("2") == true){
            txtView.setBackgroundResource(R.drawable.table_cell_bg_status_two)
            txtView.setTextColor(Color.WHITE)
        } else if (modal?.priority?.equals("3") == true){
            txtView.setBackgroundResource(R.drawable.table_cell_bg_status_three)
            txtView.setTextColor(Color.WHITE)
        } else {
            txtView.setBackgroundResource(R.drawable.table_content_cell_bg)
            txtView.setTextColor(Color.BLACK)
        }
    }

    private fun setContentBgAndColorOnClick(txtView: TextView, status: Boolean, modal: SearchResponse?) {
        if (status) {
            txtView.setBackgroundResource(R.drawable.table_cell_bg_onclick)
            txtView.setTextColor(Color.WHITE)
        } else {
            txtView.setBackgroundResource(R.drawable.table_content_cell_bg)
            txtView.setTextColor(Color.BLACK)
        }
    }


    interface OnItemClickListener {
        fun onClickItem(logisticsModel: SearchResponse?)
    }

    fun setOnClickItem(onClickListener: OnItemClickListener) {
        this.onItemClickListener = onClickListener
    }

    private val diffCallback = object : DiffUtil.ItemCallback<SearchResponse>() {
        override fun areItemsTheSame(
            oldItem: SearchResponse,
            newItem: SearchResponse
        ): Boolean {
            return oldItem.jobId == newItem.jobId
        }

        override fun areContentsTheSame(
            oldItem: SearchResponse,
            newItem: SearchResponse
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    fun submitList(imageList: List<SearchResponse>) {
        this.imageList = imageList
        imageListSort.addAll(imageList)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(
        holder: RowViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        when (position) {
            TYPE_HEADER -> {
                // Header Cells. Main Headings appear here
                holder.apply {
                    setLayoutHeader(txtIdOrder,txtLocation,txtPerson,txtStatus,txtDate)
                }
            }

            else -> {
                val modal = imageList?.get(position - 1)
                holder.apply {
                    setContentBgAndColor(txtIdOrder, modal)
                    setContentBgAndColor(txtLocation, modal)
                    setContentBgAndColor(txtPerson, modal)
                    setContentBgAndColor(txtStatus, modal)
                    setContentBgAndColor(txtDate, modal)

                    txtIdOrder.text = modal?.jobId.toString()
                    txtLocation.text = modal?.collectPoint.toString()
                    txtPerson.text = modal?.emp.toString()
                    txtStatus.text = modal?.status.toString()
                    txtDate.text = modal?.date.toString()
                    holder.itemView.setOnClickListener {
                        selectedPosition = position
                        onItemClickListener?.onClickItem(modal)

                        notifyDataSetChanged()
                    }

                }
            }
        }
    }

    override fun getItemCount(): Int {
        return if (imageList?.isNotEmpty() == true) {
            imageList!!.size + 1
        } else {
            1
        }
    }

    inner class RowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtIdOrder: TextView = itemView.findViewById(R.id.txtIdOrder)
        val txtLocation: TextView = itemView.findViewById(R.id.txtLocation)
        val txtPerson: TextView = itemView.findViewById(R.id.txtPerson)
        val txtStatus: TextView = itemView.findViewById(R.id.txtStatus)
        val txtDate: TextView = itemView.findViewById(R.id.txtDate)
    }

    companion object {
        val TYPE_HEADER: Int = 0
        val TYPE_LIST: Int = 1
    }

    override fun getHeaderPositionForItem(itemPosition: Int): Int {
        return 0
    }

    override fun getHeaderLayout(headerPosition: Int): Int {
        return R.layout.table_list_header
    }

    override fun bindHeaderData(itemView: View, headerPosition: Int) {

        val txtIdOrder: TextView = itemView.findViewById(R.id.txtIdOrder)
        val txtLocation: TextView = itemView.findViewById(R.id.txtLocation)
        val txtPerson: TextView = itemView.findViewById(R.id.txtPerson)
        val txtStatus: TextView = itemView.findViewById(R.id.txtStatus)
        val txtDate: TextView = itemView.findViewById(R.id.txtDate)

        itemView.apply {
            setLayoutHeader(txtIdOrder,txtLocation,txtPerson,txtStatus,txtDate)
        }
    }

    var isAscending = true

    override fun isHeader(itemPosition: Int): Boolean {
        return itemPosition == 0
    }

    private fun setLayoutHeader(
        txtIdOrder: TextView,
        txtLocation: TextView,
        txtPerson: TextView,
        txtStatus: TextView,
        txtDate: TextView
    ) {
        setHeaderBgAndColor(txtIdOrder)
        setHeaderBgAndColor(txtLocation)
        setHeaderBgAndColor(txtPerson)
        setHeaderBgAndColor(txtStatus)
        setHeaderBgAndColor(txtDate)

        txtIdOrder.text = "ID"
        txtLocation.text = "Địa điểm"
        txtPerson.text = "Giao cho"
        txtStatus.text = "Tình trạng"
        txtDate.text = "Ngày"

        txtIdOrder.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.sort_svg_2)
        txtLocation.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.sort_svg_2)
        txtPerson.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.sort_svg_2)
        txtStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.sort_svg_2)
        txtDate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0,R.drawable.sort_svg_2)

    }

}