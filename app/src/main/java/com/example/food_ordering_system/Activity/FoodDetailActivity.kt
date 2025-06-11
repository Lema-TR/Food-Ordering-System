package com.example.food_ordering_system.Activity


import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.food_ordering_system.Domain.CartItem
import com.example.food_ordering_system.databinding.ActivityFoodDetailBinding
import com.google.firebase.database.FirebaseDatabase
import java.text.NumberFormat
import java.util.Locale

class FoodDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFoodDetailBinding
    private lateinit var iv_productImage: ImageView
    private lateinit var tv_productTitle: TextView
    private lateinit var tv_productPrice: TextView
    private lateinit var tv_cookingTime: TextView
    private lateinit var rb_ratingBar: RatingBar
    private lateinit var tv_productDescription: TextView
    private lateinit var tv_quantityText: TextView
    private lateinit var tv_totalPrice: TextView
    private lateinit var decreaseButton: Button
    private lateinit var increaseButton: Button
    private lateinit var addToCartButton: Button

    private var quantity = 1
    private var basePrice = 0.0
    private lateinit var foodTitle: String
    private lateinit var foodImage: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeViews()
        setupClickListeners()
        loadProductData()
    }

    private fun initializeViews() {
        iv_productImage = binding.productImage
        tv_productTitle = binding.productTitle
        tv_productPrice = binding.productPrice
        tv_cookingTime = binding.cookingTime
        rb_ratingBar = binding.ratingBar
        tv_productDescription = binding.productDescription
        tv_quantityText = binding.quantityText
        tv_totalPrice = binding.totalPrice
        decreaseButton = binding.decreaseButton
        increaseButton = binding.increaseButton
        addToCartButton = binding.addToCartButton
    }

    private fun setupClickListeners() {
        decreaseButton.setOnClickListener {
            if (quantity > 1) {
                quantity--
                updateQuantityAndPrice()
            }
        }

        increaseButton.setOnClickListener {
            quantity++
            updateQuantityAndPrice()
        }

        addToCartButton.setOnClickListener {
            // Create Cart object with all required data
//            val cartItem = CartItem(
//               foodTitle,
//                basePrice,
//              quantity,
//                totalPrice = basePrice * quantity,
//            )

            // Add to Firebase
//            val database = FirebaseDatabase.getInstance()
//            val cartRef = database.getReference("Cart")
//
//            cartRef.push().setValue(cartItem)
//                .addOnSuccessListener {
//                    Toast.makeText(this, "Added to cart!", Toast.LENGTH_SHORT).show()
//                }
//                .addOnFailureListener { e ->
//                    Toast.makeText(this, "Failed to add: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
        }
    }

    private fun loadProductData() {
        // Get data from intent
        foodTitle = intent.getStringExtra("FOOD_TITLE") ?: ""
        val priceString = intent.getStringExtra("FOOD_PRICE") ?: "0.0"
        basePrice = priceString.toDouble()
        val cookTime = intent.getStringExtra("FOOD_TIME") ?: ""
        val description = intent.getStringExtra("FOOD_DESCRIPTION") ?: ""
        val rating = intent.getStringExtra("FOOD_RATING")?.toFloat() ?: 0f
        foodImage = intent.getStringExtra("FOOD_IMAGE") ?: ""

        // Set views
        tv_productTitle.text = foodTitle
        tv_productPrice.text = formatPrice(basePrice)
        tv_cookingTime.text = cookTime
        rb_ratingBar.rating = rating
        tv_productDescription.text = description

        Glide.with(this)
            .load(foodImage)
            .into(iv_productImage)

        updateQuantityAndPrice()
    }

    private fun updateQuantityAndPrice() {
        tv_quantityText.text = quantity.toString()
        val total = basePrice * quantity
        tv_totalPrice.text = formatPrice(total)
    }

    private fun formatPrice(price: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        return format.format(price)
    }
}