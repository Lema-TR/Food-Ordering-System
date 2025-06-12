package com.example.food_ordering_system.Domain

data class CartItem(
    val cartId: Int = 0,
    val foodId: Int = 0,
    var quantity: Int = 0,
    val price: Double = 0.0,
    var totalPrice: Double = 0.0,
    val title: String = "",
    var cartItemId: String = "" // Optional, will be set by Firebase
)