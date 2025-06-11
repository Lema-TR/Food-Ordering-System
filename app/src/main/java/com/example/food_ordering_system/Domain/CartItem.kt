package com.example.food_ordering_system.Domain

data class CartItem(
    val cartItemId: String = "",
    val cartId: Int=0,
    val foodId: Int=0,
    val quantity: Int=0,
    val totalPrice: Double=0.0,
    val price: Double=0.0,
    val title: String=""
)