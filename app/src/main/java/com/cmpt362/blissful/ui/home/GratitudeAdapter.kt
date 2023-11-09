package com.cmpt362.blissful.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cmpt362.blissful.R

class GratitudeAdapter(private var gratitudeItems: List<GratitudeItem>) : RecyclerView.Adapter<GratitudeAdapter.ViewHolder>() {
    override fun onCreateViewHolder(viewgroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewgroup.context).inflate(R.layout.home_gratitude_list_item, viewgroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = gratitudeItems[position]
        holder.itemImage.setImageResource(item.imageId)
        holder.itemHeader.text = item.header
        holder.itemDescription.text = item.description
    }

    override fun getItemCount(): Int {
        return gratitudeItems.size
    }

    fun setData(it: List<GratitudeItem>?) {
        if (it != null) {
            gratitudeItems = it
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.itemImage)
        val itemHeader: TextView = itemView.findViewById(R.id.itemHeader)
        val itemDescription: TextView = itemView.findViewById(R.id.itemDescription)
    }
}