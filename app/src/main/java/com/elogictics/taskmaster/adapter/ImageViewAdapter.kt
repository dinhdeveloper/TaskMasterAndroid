package com.elogictics.taskmaster.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.elogictics.taskmaster.R
import com.elogictics.taskmaster.databinding.CustomItemImageViewBinding
import com.elogictics.taskmaster.model.response.JobMediaDetailResponse
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem



class ImageViewAdapter(private val mContext: Context) : RecyclerView.Adapter<ImageViewAdapter.ViewHolder>() {

    private var listUrl: MutableList<JobMediaDetailResponse>? = null
    private var clickListener: OnClickListener? = null
    private lateinit var player: ExoPlayer
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

    @SuppressLint("NotifyDataSetChanged", "Range")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = differ.currentList[position]

        val options: RequestOptions = RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.multi_color_progress)
            .error(R.drawable.close_red)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)
            .dontAnimate()
            .dontTransform()

        if (item.urlHard.endsWith(".jpg", true) || item.urlHard.endsWith(".png", true) || item.urlHard.endsWith(".jpeg", true)) {

            holder.binding.playerView.visibility = View.GONE
            holder.binding.imvView.visibility = View.VISIBLE

            Glide.with(mContext)
                .load(item.urlHard)
                .apply(options)
                .into(holder.binding.imvView)
        }
        else if (item.urlHard.endsWith(".mp4", true)) {
            // Hiển thị video bằng VideoView
            holder.binding.playerView.visibility = View.VISIBLE
            holder.binding.imvView.visibility = View.GONE

            player = ExoPlayer.Builder(mContext).build()
            holder.binding.playerView.player = player

            val mediaItem = MediaItem.fromUri(Uri.parse(item.urlHard))
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
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