package com.example.food_ordering_system.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.food_ordering_system.Domain.CartItem
import com.example.food_ordering_system.databinding.ViewholderAddtocartBinding

class CartAdapter(
    private val cartItemItems: MutableList<CartItem>,
    private val placeOrderClickListener: (CartItem) -> Unit,
    private val deleteOrderClickListener: (CartItem) -> Unit,
    private val quantityChangeListener: (CartItem, Int) -> Unit // New listener for quantity changes
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(val binding: ViewholderAddtocartBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ViewholderAddtocartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItemItems[position]

        with(holder.binding) {
            orderName.text = item.title
            orderPrice.text = "${item.price} ETB"
            orderQuantity.text = item.quantity.toString()
            totalPrice.text = "${item.price * item.quantity} ETB"

            // Quantity change listeners
            btnDecrease.setOnClickListener {
                if (item.quantity > 1) {
                    val newQuantity = item.quantity - 1
                    quantityChangeListener(item, newQuantity)
                }
            }

            btnIncrease.setOnClickListener {
                val newQuantity = item.quantity + 1
                quantityChangeListener(item, newQuantity)
            }

            // Existing buttons
            btnPlaceOrder.setOnClickListener { placeOrderClickListener(item) }
            btnDeleteOrder.setOnClickListener { deleteOrderClickListener(item) }
        }
    }

    override fun getItemCount() = cartItemItems.size

    fun updateItem(position: Int, updatedItem: CartItem) {
        cartItemItems[position] = updatedItem
        notifyItemChanged(position)
    }

    fun removeItem(position: Int) {
        cartItemItems.removeAt(position)
        notifyItemRemoved(position)
    }

    fun updateData(newItems: List<CartItem>) {
        cartItemItems.clear()
        cartItemItems.addAll(newItems)
        notifyDataSetChanged()
    }
}