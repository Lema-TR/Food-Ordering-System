package com.example.food_ordering_system

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.util.Locale

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var productImage: ImageView
    private lateinit var productTitle: TextView
    private lateinit var productPrice: TextView
    private lateinit var cookingTime: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var productDescription: TextView
    private lateinit var quantityText: TextView
    private lateinit var totalPrice: TextView
    private lateinit var decreaseButton: Button
    private lateinit var increaseButton: Button
    private lateinit var addToCartButton: Button

    private var quantity = 1
    private var basePrice = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        initializeViews()
        setupClickListeners()
        loadProductData()
    }

    private fun initializeViews() {
        productImage = findViewById(R.id.productImage)
        productTitle = findViewById(R.id.productTitle)
        productPrice = findViewById(R.id.productPrice)
        cookingTime = findViewById(R.id.cookingTime)
        ratingBar = findViewById(R.id.ratingBar)
        productDescription = findViewById(R.id.productDescription)
        quantityText = findViewById(R.id.quantityText)
        totalPrice = findViewById(R.id.totalPrice)
        decreaseButton = findViewById(R.id.decreaseButton)
        increaseButton = findViewById(R.id.increaseButton)
        addToCartButton = findViewById(R.id.addToCartButton)
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
            // TODO: Implement actual cart functionality
            Toast.makeText(
                this,
                "Added $quantity item(s) to cart",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadProductData() {
        // TODO: Replace with actual data from your data source
        // This is sample data for demonstration
        productTitle.text = "Delicious Pizza"
        basePrice = 12.99
        productPrice.text = formatPrice(basePrice)
        cookingTime.text = "30 mins"
        ratingBar.rating = 4.5f
        productDescription.text = "A delicious pizza made with fresh ingredients and our special sauce. " +
                "Topped with premium cheese and your favorite toppings."

        // Load product image (you would typically use an image loading library like Glide or Coil)
        // For now, we'll use a placeholder
        productImage.setImageResource(R.drawable.rounded_corner)

        updateQuantityAndPrice()
    }

    private fun updateQuantityAndPrice() {
        quantityText.text = quantity.toString()
        val total = basePrice * quantity
        totalPrice.text = formatPrice(total)
    }

    private fun formatPrice(price: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        return format.format(price)
    }
} 