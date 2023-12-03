package com.cmpt362.blissful.ui.home

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cmpt362.blissful.R
import com.cmpt362.blissful.db.post.Post
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import java.text.SimpleDateFormat
import java.util.Locale

class GratitudeAdapter(
    private var gratitudeItems: List<Post>,
    private val onHeartToggled: ((String, ToggleButton) -> Unit)? = null
) :
    RecyclerView.Adapter<GratitudeAdapter.ViewHolder>() {
    override fun onCreateViewHolder(viewgroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewgroup.context)
            .inflate(R.layout.home_gratitude_list_item, viewgroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = gratitudeItems[position]
        val dateFormat = SimpleDateFormat("MMMM d, yyyy 'at' hh:mm a", Locale.getDefault())
        val storageRef = Firebase.storage.reference

        holder.itemDescription.text = item.content
        holder.itemPostDate.text = dateFormat.format(item.postDateTime)
        holder.itemNumberOfLikes.text = item.likesCount.toString()

        // Load the image using Glide
        if (item.imageUrl != null && item.imageUrl!!.isNotEmpty()) {
            Log.d("ImageUrl", item.imageUrl!!)
            storageRef.child("file/${item.imageUrl}").downloadUrl.addOnSuccessListener {
                Glide.with(holder.itemView).load(it).into(holder.itemImage)
            }.addOnFailureListener {
                Log.e("Firebase", "Failed in downloading")
            }

            holder.itemImage.visibility = View.VISIBLE
        } else {
            // Disable imageview if no image was passed by the user
            holder.itemImage.visibility = View.GONE
        }

        holder.heartToggle.setOnClickListener {
            onHeartToggled?.invoke(item.id, holder.heartToggle)
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
        val itemPostDate: TextView = itemView.findViewById(R.id.itemPostDate)
        val itemNumberOfLikes: TextView = itemView.findViewById(R.id.itemNumberOfLikes)
        val heartToggle: ToggleButton = itemView.findViewById(R.id.heartToggle)
    }
}