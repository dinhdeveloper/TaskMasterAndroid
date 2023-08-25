package com.dinhtc.taskmaster.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dinhtc.taskmaster.databinding.CustomItemImageViewBinding
import com.dinhtc.taskmaster.model.response.JobMediaDetailResponse
import java.io.File

class ImageViewAdapter(private val mContext: Context) : RecyclerView.Adapter<ImageViewAdapter.ViewHolder>() {

    private var listUrl: MutableList<JobMediaDetailResponse>? = null
    private var clickListener: OnClickListener? = null

    interface OnClickListener {
        fun onItemClick(position: Int, media: JobMediaDetailResponse)
    }
    fun setOnClickListener(listener: OnClickListener) {
        clickListener = listener
    }

    private val diffCallback = object : DiffUtil.ItemCallback<JobMediaDetailResponse>() {
        override fun areItemsTheSame(oldItem: JobMediaDetailResponse, newItem: JobMediaDetailResponse): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }

        override fun areContentsTheSame(
            oldItem: JobMediaDetailResponse,
            newItem: JobMediaDetailResponse
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: MutableList<JobMediaDetailResponse>){
        this.listUrl = list
        return differ.submitList(list)
    }

    inner class ViewHolder(val binding: CustomItemImageViewBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CustomItemImageViewBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        val screenWidth = mContext.resources.displayMetrics.widthPixels
        val imageWidth = screenWidth / 2 // Chia cho 2 để có chiều rộng của ImageView
        val layoutParams = holder.binding.imvView.layoutParams
        layoutParams.width = imageWidth
        layoutParams.height = imageWidth // Đảm bảo ImageView là hình vuông
        holder.binding.imvView.layoutParams = layoutParams

        val item = differ.currentList[position]
        val file = File(item.url)

        if (item.url.endsWith(".jpg", true) || item.url.endsWith(".png", true) || item.url.endsWith(".jpeg", true)) {
            Glide.with(mContext)
                .load(file)
                .into(holder.binding.imvView)
        }
//        else if (mediaPath.endsWith(".mp4", true)) {
//            // Hiển thị video bằng VideoView
//            holder.binding.imvView.setVideoPath(file.absolutePath)
//            holder.binding.videoView.start()
//        }

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