package com.cmpt362.blissful.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cmpt362.blissful.R
import com.cmpt362.blissful.db.post.Post
import java.text.SimpleDateFormat
import java.util.Locale

class GratitudeAdapter(private var gratitudeItems: List<Post>) :
    RecyclerView.Adapter<GratitudeAdapter.ViewHolder>() {
    override fun onCreateViewHolder(viewgroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewgroup.context)
            .inflate(R.layout.home_gratitude_list_item, viewgroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = gratitudeItems[position]
        val dateFormat = SimpleDateFormat("MMMM d, yyyy - hh:mm a", Locale.getDefault())
        holder.itemHeader.text = item.title ?: "Post ${item.postId}"
        holder.itemDescription.text = item.content
        holder.itemLocation.text = "Location: ${item.location ?: "Unknown"}"
        holder.itemPostDate.text = "Posted: ${dateFormat.format(item.postDateTime.time)}"
        holder.itemLastUpdateDate.text =
            "Last Update: ${dateFormat.format(item.lastUpdateDateTime.time)}"
        holder.itemNumberOfLikes.text = item.likesCount.toString()
    }

    override fun getItemCount(): Int {
        return gratitudeItems.size
    }

    fun setData(it: List<Post>?) {
        if (it != null) {
            gratitudeItems = it
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemHeader: TextView = itemView.findViewById(R.id.itemHeader)
        val itemDescription: TextView = itemView.findViewById(R.id.itemDescription)
        val itemLocation: TextView = itemView.findViewById(R.id.itemLocation)
        val itemPostDate: TextView = itemView.findViewById(R.id.itemPostDate)
        val itemLastUpdateDate: TextView = itemView.findViewById(R.id.itemLastUpdateDate)
        val itemNumberOfLikes: TextView = itemView.findViewById(R.id.itemNumberOfLikes)
    }
}