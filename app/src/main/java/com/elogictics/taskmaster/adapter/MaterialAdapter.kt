package com.elogictics.taskmaster.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.elogictics.taskmaster.databinding.CustomItemMaterialDetailBinding
import com.elogictics.taskmaster.model.response.JobMaterialDetailResponse
import com.elogictics.taskmaster.utils.AndroidUtils

class MaterialAdapter(private val mContext: Context) : RecyclerView.Adapter<MaterialAdapter.ViewHolder>() {

    private var listUrl: MutableList<JobMaterialDetailResponse>? = null
    private var clickListener: OnClickListener? = null

    interface OnClickListener {
        fun onItemClick(position: Int, media: JobMaterialDetailResponse)
    }
    fun setOnClickListener(listener: OnClickListener) {
        clickListener = listener
    }

    private val diffCallbackMaterial = object : DiffUtil.ItemCallback<JobMaterialDetailResponse>() {
        override fun areItemsTheSame(oldItem: JobMaterialDetailResponse, newItem: JobMaterialDetailResponse): Boolean {
            return oldItem.mateId == newItem.mateId
        }

        override fun areContentsTheSame(
            oldItem: JobMaterialDetailResponse,
            newItem: JobMaterialDetailResponse
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differMaterial = AsyncListDiffer(this, diffCallbackMaterial)

    fun submitList(list: MutableList<JobMaterialDetailResponse>){
        this.listUrl = list
        return differMaterial.submitList(list)
    }

    inner class ViewHolder(val binding: CustomItemMaterialDetailBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CustomItemMaterialDetailBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = differMaterial.currentList[position]

        holder.binding.apply {
            tvName.text = item.name
            tvKL.text = "${item.weight}"
            tvKLBK.text = "${item.weightToCus}"
            tvPrice.text =  AndroidUtils.formatMoneyCard("${item.price}")
        }

        holder.binding.closeItem.setOnClickListener {
            clickListener?.onItemClick(position,item)
        }

    }

    override fun getItemCount(): Int {
        return listUrl?.size!!
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < listUrl?.size!!) {
            listUrl?.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount - position)
        }
    }
}