package com.myblogs.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.myblogs.DataBase.NewBlogData
import com.myblogs.R
import com.squareup.picasso.Picasso

class BlogAdapter(
    private val context: Context,
    private val blogList: List<NewBlogData>
) : RecyclerView.Adapter<BlogAdapter.BlogViewHolder>() {

    inner class BlogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUserName: TextView = view.findViewById(R.id.rlusername)
        val ivBlogImage: ImageView = view.findViewById(R.id.ivblogimage)
        val tvDescription: TextView = view.findViewById(R.id.rldescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_user_blog, parent, false)
        return BlogViewHolder(view)
    }

    override fun getItemCount(): Int = blogList.size

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        val blog = blogList[position]

        holder.tvUserName.text = blog.userName  // or fetch username if available
        holder.tvDescription.text = blog.description

        if (blog.imageUrl.isNotEmpty()) {
            try {
                val decodedBytes = android.util.Base64.decode(blog.imageUrl, android.util.Base64.DEFAULT)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                holder.ivBlogImage.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                holder.ivBlogImage.setImageResource(R.drawable.ic_objects)
            }
        } else {
            holder.ivBlogImage.setImageResource(R.drawable.ic_objects)
        }

    }
}
