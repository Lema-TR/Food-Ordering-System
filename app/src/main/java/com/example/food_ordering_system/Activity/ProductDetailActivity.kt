package com.example.food_ordering_system.Activity


import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.food_ordering_system.databinding.ActivityProductDetailBinding
import com.example.food_ordering_system.Domain.CartItem
import com.example.food_ordering_system.Domain.Foods
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailBinding
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
    private lateinit var productTitle: String
    private lateinit var productImage: String
    private var food_id: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeViews()
        loadProductData() // Load data BEFORE setting up listeners
        setupClickListeners()
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
            // gets user specific cart
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val cartRef = FirebaseDatabase.getInstance().getReference("Cart")

            cartRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (cartSnapshot in snapshot.children) {
                            val cartId = cartSnapshot.child("cartId").getValue(Int::class.java) ?: continue
                            Log.d("itemLog", "cartId retrieved")

                            food_id?.let {
                                updateCartItemsByCartId(cartId, it)
                            }

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("CartQuery", "Error: ${error.message}")
                    }
                })

        }

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
    private fun loadProductData() {
        // Get data from intent
        productTitle = intent.getStringExtra("food_title") ?: "Unknown Product"
        val priceString = intent.getStringExtra("food_price") ?: "0.0"
        basePrice = priceString.toDoubleOrNull() ?: 0.0
        val cookTime = intent.getStringExtra("food_time") ?: ""
        val description = intent.getStringExtra("food_description") ?: ""
        food_id = intent.getIntExtra("food_id", -1) // Changed to getIntExtra with default value
        val rating = intent.getStringExtra("food_rating")?.toFloatOrNull() ?: 0f
        productImage = intent.getStringExtra("food_image") ?: ""

        // Validate food_id
        if (food_id == -1) {
            Toast.makeText(this, "Error: Invalid food ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Set views
        tv_productTitle.text = productTitle
        tv_productPrice.text = formatPrice(basePrice)
        tv_cookingTime.text = cookTime
        rb_ratingBar.rating = rating
        tv_productDescription.text = description

        // Load image
        Glide.with(this)
            .load(productImage)
            .into(iv_productImage)

        // Initialize price display
        updateQuantityAndPrice()
    }


    private fun updateCartItemsByCartId(cartId: Int, food_id: Int) {
        val cartItemRef = FirebaseDatabase.getInstance().getReference("CartItem")

        cartItemRef.orderByChild("cartId").equalTo(cartId.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var existingItem: CartItem? = null
                    var existingItemKey: String? = null
                    
                    for (cartItemSnapshot in snapshot.children) {
                        val cartItem = cartItemSnapshot.getValue(CartItem::class.java)
                        if (cartItem?.foodId == food_id) {
                            existingItem = cartItem
                            existingItemKey = cartItemSnapshot.key
                            break
                        }
                    }

                    if (existingItem != null) {
                        // Update quantity and total price if item exists
                        val newQuantity = existingItem.quantity + quantity
                        val updates = mapOf(
                            "quantity" to newQuantity,
                            "totalPrice" to (basePrice * newQuantity)
                        )
                        existingItemKey?.let { key ->
                            cartItemRef.child(key).updateChildren(updates)
                            Toast.makeText(this@ProductDetailActivity, "Item quantity updated in cart", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Create new cart item
                        val newCartItem = CartItem(
                            cartId = cartId,
                            foodId = food_id,
                            quantity = quantity,
                            price = basePrice,
                            totalPrice = basePrice * quantity,
                            title = productTitle
                        )
                        cartItemRef.push().setValue(newCartItem)
                            .addOnSuccessListener {
                                Toast.makeText(this@ProductDetailActivity, "Item added to cart", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this@ProductDetailActivity, "Failed to add item: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ProductDetailActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }




}