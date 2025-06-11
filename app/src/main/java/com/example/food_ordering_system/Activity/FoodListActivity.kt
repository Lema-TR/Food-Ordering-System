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

        Log.d("foodListLog", "before adapter")
        // Initialize adapter early with empty list
        adapter = FoodListAdapter(foodObjects, onItemClick = { food ->
            val intent = Intent(this@FoodListActivity, FoodDetailActivity::class.java).apply {
                putExtra("FOOD_TITLE", food.Title)
                putExtra("FOOD_DESCRIPTION", food.Description)
                putExtra("FOOD_PRICE", food.price)
                putExtra("FOOD_IMAGE", food.imagepath)
                putExtra("FOOD_RATING", food.star.toString())
                putExtra("FOOD_TIME",food.Timevalue)

            }
            startActivity(intent)
        },
            onAddToCartClick= { food ->
//                val database = FirebaseDatabase.getInstance()
//                val cartRef = database.getReference("Cart") // Reference to "Cart" node
//                val cartItem = CartItem(food.Title, food.price, 1, food.price)
//
//                cartRef.push().setValue(cartItem)

        })

        binding.foodRecyclerView.adapter = adapter
        binding.foodRecyclerView.layoutManager = GridLayoutManager(this, 2)

        // Get category ID from intent
        categoryId = intent.getIntExtra("id", 0)
        binding.progressBar.visibility = View.VISIBLE
        setupToolbar()
        Log.d("foodListLog", "before loadFoodData()")

        loadFoodData()
    }

    private fun loadFoodData() {
        val database = FirebaseDatabase.getInstance()
        val foodRef = database.getReference("Foods")

        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                foodObjects.clear() // Clear existing data
                Log.d("foodListLog","before loop")
                for (foodsnapshot in snapshot.children) {
                    Log.d("foodListLog","inside loop")
                    val food = foodsnapshot.getValue(Foods::class.java)
                    if (food != null && food.CategoryID == categoryId) {
                        foodObjects.add(food)
                        Log.d("foodListLog", "Food: ${food?.Title}, Category: ${food?.CategoryID}")
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
}