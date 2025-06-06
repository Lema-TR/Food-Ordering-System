package com.example.food_ordering_system.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.food_ordering_system.Domain.Category
import com.example.food_ordering_system.Domain.Foods
import com.example.food_ordering_system.databinding.ItemCategoryCardBinding
import com.example.food_ordering_system.databinding.ViewholderBestDealBinding
import java.text.NumberFormat
import java.util.Locale

class CategoryAdapter(
    private val items: MutableList<Category>,
    private val onItemClick: (Category) -> Unit

) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(val binding:ItemCategoryCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {

           categoryName.text = item.name

            // Load image with rounded corners
            Glide.with(categoryIcon.context)
                .load(item.imagepath)
                .apply(
                    RequestOptions()
                        .centerCrop()
                        .transform(RoundedCorners(12))
                )
                .into(categoryIcon)

            // Set click listeners
            root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemCount(): Int = items.size


//    fun updateItems(newItems: List<Category>) {
//        items.clear()
//        items.addAll(newItems)
//        notifyDataSetChanged()
//    }
}
