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
            // Set title with ellipsis
            tvTitle.text = item.Title
            
            // Format price with currency
            tvPrice.text = formatPrice(item.price)
            
            // Set rating with count (placeholder for now)
            tvRating.text = String.format("%.1f", item.star)
            tvRatingCount.text = "(200)" // TODO: Add rating count to Foods model
            
            // Set time
            tvTime.text = "${item.TimeValue} mins"
            
            // Load image with rounded corners
            Glide.with(itemImage.context)
                .load(item.imagepath)
                .apply(
                    RequestOptions()
                        .centerCrop()
                        .transform(RoundedCorners(12))
                )
                .into(itemImage)

            // Set click listeners
            root.setOnClickListener { onItemClick(item) }
            btnAddToCart.setOnClickListener { onAddToCartClick(item) }
        }
    }

    override fun getItemCount(): Int = items.size

    private fun formatPrice(price: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        return format.format(price)
    }

    fun updateItems(newItems: List<Foods>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
