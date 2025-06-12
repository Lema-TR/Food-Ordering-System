package com.example.food_ordering_system.Activity


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.food_ordering_system.Adapter.FoodListAdapter
import com.example.food_ordering_system.Domain.CartItem
import com.example.food_ordering_system.Domain.Foods
import com.example.food_ordering_system.databinding.ActivityFoodListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FoodListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFoodListBinding
    private lateinit var adapter: FoodListAdapter
    private var categoryId: Int = 0
    private val foodObjects = mutableListOf<Foods>() // Moved to class level

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFoodListBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Initialize adapter early with empty list
        adapter = FoodListAdapter(foodObjects, onItemClick = { food ->
            val intent = Intent(this@FoodListActivity, FoodDetailActivity::class.java).apply {
                putExtra("FOOD_TITLE", food.Title)
                putExtra("FOOD_DESCRIPTION", food.Description)
                putExtra("FOOD_PRICE", food.price)
                putExtra("FOOD_IMAGE", food.imagepath)
                putExtra("FOOD_RATING", food.star.toString())
                putExtra("FOOD_TIME", food.Timevalue)
                putExtra("FOOD_ID", food.Id)
            }
            startActivity(intent)
        },
            onAddToCartClick= { food ->
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@FoodListAdapter
                val cartRef = FirebaseDatabase.getInstance().getReference("Cart")

                cartRef.orderByChild("userId").equalTo(userId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (cartSnapshot in snapshot.children) {
                                val cartId = cartSnapshot.child("cartId").getValue(Int::class.java) ?: continue
                                Log.d("itemLog", "cartId retrieved")
                                // Now we can use this cartId to query the CartItem node
                                updateCartItemsByCartId(cartId, food)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("CartQuery", "Error: ${error.message}")
                        }
                    })





        })

        binding.foodRecyclerView.adapter = adapter
        binding.foodRecyclerView.layoutManager = GridLayoutManager(this, 2)

        // Get category ID from intent
        categoryId = intent.getIntExtra("id", 0)
        binding.progressBar.visibility = View.VISIBLE
        setupToolbar()


        loadFoodData()
    }

    private fun loadFoodData() {
        val database = FirebaseDatabase.getInstance()
        val foodRef = database.getReference("Foods")

        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                foodObjects.clear() // Clear existing data

                for (foodsnapshot in snapshot.children) {
                    val food = foodsnapshot.getValue(Foods::class.java)
                    if (food != null && food.CategoryID == categoryId) {
                        foodObjects.add(food)

                    }
                }

                // Notify adapter that data has changed
                adapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.GONE


            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Toast.makeText(
                    this@FoodListActivity,
                    "Failed to load foods: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.progressBar.visibility = View.GONE

            }
        })
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Category Foods"
        supportActionBar?.title = categoryName
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateCartItemsByCartId(cartId: Int, food: Foods) {
        val cartItemRef = FirebaseDatabase.getInstance().getReference("CartItem")

        cartItemRef.orderByChild("cartId").equalTo(cartId.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var existingItem: CartItem? = null
                    var existingItemKey: String? = null
                    
                    for (cartItemSnapshot in snapshot.children) {
                        val cartItem = cartItemSnapshot.getValue(CartItem::class.java)
                        if (cartItem?.foodId == food.Id) {
                            existingItem = cartItem
                            existingItemKey = cartItemSnapshot.key
                            break
                        }
                    }

                    if (existingItem != null) {
                        // Update quantity and total price if item exists
                        val newQuantity = existingItem.quantity + 1 // Since we're adding from list, increment by 1
                        val updates = mapOf(
                            "quantity" to newQuantity,
                            "totalPrice" to (food.price * newQuantity)
                        )
                        existingItemKey?.let { key ->
                            cartItemRef.child(key).updateChildren(updates)
                            Toast.makeText(this@FoodListActivity, "Item quantity updated in cart", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Create new cart item
                        val newCartItem = CartItem(
                            cartId = cartId,
                            foodId = food.Id,
                            quantity = 1,
                            price = food.price,
                            totalPrice = food.price,
                            title = food.Title
                        )
                        cartItemRef.push().setValue(newCartItem)
                            .addOnSuccessListener {
                                Toast.makeText(this@FoodListActivity, "Item added to cart", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this@FoodListActivity, "Failed to add item: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@FoodListActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


}