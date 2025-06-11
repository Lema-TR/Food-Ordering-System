package com.example.food_ordering_system.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.food_ordering_system.Domain.Foods
import com.example.food_ordering_system.databinding.ViewholderFoodItemBinding
import java.text.NumberFormat
import java.util.Locale

class FoodListAdapter(
    private val items: MutableList<Foods>,
    private val onItemClick: (Foods) -> Unit,
    private val onAddToCartClick: (Foods) -> Unit  // New lambda for button clicks
) : RecyclerView.Adapter<FoodListAdapter.ViewHolder>() {

    class ViewHolder(val binding: ViewholderFoodItemBinding) : RecyclerView.ViewHolder(binding.root) {
        // Add reference to the button
        val btnAddToCart = binding.btnAddToCart
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderFoodItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvTitle.text = item.Title
            tvPrice.text = formatPrice(item.price)
            tvRating.text = item.star.toString()

            Glide.with(itemImage.context)
                .load(item.imagepath)
                .apply(
                    RequestOptions()
                        .centerCrop()
                        .transform(RoundedCorners(8))
                )
                .into(itemImage)

            // Set click listener for entire card
            root.setOnClickListener { onItemClick(item) }

            // Set click listener specifically for the add-to-cart button
            holder.btnAddToCart.setOnClickListener {
                onAddToCartClick(item)  // Use the new lambda
            }
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