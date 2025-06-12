package com.example.food_ordering_system.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.food_ordering_system.Domain.Foods
import com.example.food_ordering_system.databinding.ViewholderBestDealBinding
import java.text.NumberFormat
import java.util.Locale

class BestFoodAdapter(
    private val items: MutableList<Foods>,
    private val onItemClick: (Foods) -> Unit,
    private val onAddToCartClick: (Foods) -> Unit
) : RecyclerView.Adapter<BestFoodAdapter.ViewHolder>() {

    class ViewHolder(val binding: ViewholderBestDealBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderBestDealBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {

            tvTitle.text = item.Title
            tvPrice.text = item.price.toString()
            tvRating.text = String.format("%.1f", item.star)
            tvRatingCount.text = "(200)" // I will implement the logic here
            

            tvTime.text = "${item.Timevalue} "
            

            Glide.with(foodImage.context)
                .load(item.imagepath)
                .apply(
                    RequestOptions()
                        .centerCrop()
                        .transform(RoundedCorners(12))
                )
                .into(foodImage)

            // Set click listeners
            root.setOnClickListener { onItemClick(item) }
            btnAddToCart.setOnClickListener { onAddToCartClick(item) }
        }
    }

    override fun getItemCount(): Int = items.size



    fun updateItems(newItems: List<Foods>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
