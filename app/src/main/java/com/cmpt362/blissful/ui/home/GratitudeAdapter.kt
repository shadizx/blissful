package com.cmpt362.blissful.ui.home

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.Visibility
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
        val dateFormat = SimpleDateFormat("MMMM d, yyyy 'at' hh:mm a", Locale.getDefault())
        holder.itemDescription.text = item.content
        holder.authorUsername.text = item.userId.toString() // TODO: update to display username
        holder.itemPostDate.text = dateFormat.format(item.postDateTime.time)
        holder.itemLastUpdateDate.text =
            "Updated: ${dateFormat.format(item.lastUpdateDateTime.time)}"
        holder.itemNumberOfLikes.text = item.likesCount.toString()
        // setting up the image, gone if text only, visible if text and image present
        holder.itemImage.setImageBitmap(item.image)
        holder.itemImage.visibility = View.VISIBLE
        if (item.image == null) {
            holder.itemImage.visibility = View.GONE
        }
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
        val itemImage: ImageView = itemView.findViewById(R.id.itemImage)
        val itemDescription: TextView = itemView.findViewById(R.id.itemDescription)
        val authorUsername: TextView = itemView.findViewById(R.id.authorUsername)
        val itemPostDate: TextView = itemView.findViewById(R.id.itemPostDate)
        val itemLastUpdateDate: TextView = itemView.findViewById(R.id.itemLastUpdateDate)
        val itemNumberOfLikes: TextView = itemView.findViewById(R.id.itemNumberOfLikes)
    }
}