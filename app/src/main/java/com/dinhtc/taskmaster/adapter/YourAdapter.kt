package com.dinhtc.taskmaster.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dinhtc.taskmaster.R
import com.dinhtc.taskmaster.model.response.CollectPointLatLng

class YourAdapter(private val dataResponse: List<CollectPointLatLng>) : RecyclerView.Adapter<YourAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        val tvStatusJob: TextView = itemView.findViewById(R.id.tvStatusJob)
        val tvEmpJob: TextView = itemView.findViewById(R.id.tvEmpJob)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.custom_layout_map, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataResponse[position]
        holder.tvLocation.text = data.cpName
        holder.tvStatusJob.text = data.jobStateDesc
        holder.tvEmpJob.text = data.fullName
    }

    override fun getItemCount(): Int {
        return dataResponse.size
    }
}
