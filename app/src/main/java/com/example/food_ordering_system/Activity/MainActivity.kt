package com.example.food_ordering_system.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cloudinary.android.MediaManager
import com.example.food_ordering_system.Adapter.BestFoodAdapter
import com.example.food_ordering_system.Adapter.CategoryAdapter
import com.example.food_ordering_system.Domain.Category
import com.example.food_ordering_system.Domain.Foods
import com.example.food_ordering_system.Domain.Location
import com.example.food_ordering_system.Domain.Time
import com.example.food_ordering_system.R
import com.example.food_ordering_system.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Initialize Cloudinary
        val config: HashMap<String, String> = hashMapOf(
            "cloud_name" to "dwjfvmjfh",
            "api_key" to "763756178671368",
            "api_secret" to "1fhz5923fnJSQ4seVRageDhU8QI"
        )
        MediaManager.init(this, config)

        setupBottomNavigation()
        setupSearchAndFilter()
//        loadLocationAndTimeData()
        loadCategoryList()
        setupBestFoodRecyclerView()
    }

    private fun loadCategoryList() {
        val database = FirebaseDatabase.getInstance()
        val categoryRef = database.getReference("Category")
//        val timeRef = database.getReference("Time")

        // Location data
        categoryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
//
                val categoryObjects = mutableListOf<Category>()

                for (categorySnapshot in snapshot.children) {

                    val category = categorySnapshot.getValue(Category::class.java)
                    if (category != null) {
                        categoryObjects.add(category)
                    }
                }
//                val adapter = CategoryAdapter(this@MainActivity,
//                    categoryObjects,
//                    onItemClick = { category ->
//                        val intent = Intent(this, ProductDetailActivity::class.java).apply {
//                            putExtra("category_id", category.id)
//                        }
//                        startActivity(intent)
//                    },
//                )



//
//                binding.categoryView.adapter=adapter
//                binding.categoryView.layoutManager=GridLayoutManager(this@MainActivity,2)

            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Toast.makeText(
                    this@MainActivity,
                    "Failed to load locations: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Already on home, do nothing
                    true
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, UserProfileActivity::class.java))
                    false
                }
                R.id.navigation_orders -> {
                    // TODO: Implement orders activity
                    Toast.makeText(this, "Orders coming soon!", Toast.LENGTH_SHORT).show()
                    false
                }
                else -> false
            }
        }
    }

    private fun setupSearchAndFilter() {
        // Filter button click
//        binding.filterBtn.setOnClickListener {
//            // TODO: Implement filter functionality
//            Toast.makeText(this, "Filter clicked", Toast.LENGTH_SHORT).show()
//        }

        // Cart button click
        binding.cartBtn.setOnClickListener {
            // TODO: Implement cart functionality
            Toast.makeText(this, "Cart clicked", Toast.LENGTH_SHORT).show()
        }

        // Search functionality
        binding.searchContainer.editText?.setOnEditorActionListener { _, _, _ ->
            val searchQuery = binding.searchContainer.editText?.text.toString()
            if (searchQuery.isNotEmpty()) {
                // TODO: Implement search functionality
                Toast.makeText(this, "Searching for: $searchQuery", Toast.LENGTH_SHORT).show()
            }
            true
        }

        // Logout button
        binding.logoutBtn.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, StartActivity::class.java))
            finish()
        }
    }



    private fun setupBestFoodRecyclerView() {
        val foodObjects = mutableListOf<Foods>()
        val database = FirebaseDatabase.getInstance()
        val foodRef = database.getReference("Foods")

        // Initialize adapter with click listeners
        val adapter = BestFoodAdapter(
            foodObjects,
            onItemClick = { food ->
                val intent = Intent(this, ProductDetailActivity::class.java).apply {
                    putExtra("food_id", food.Id)
                    putExtra("food_title", food.Title)
                    putExtra("food_price", food.price)
                    putExtra("food_description", food.Description)
                    putExtra("food_image", food.imagepath)
                    putExtra("food_rating", food.star)
                    putExtra("food_time", food.TimeValue)
                }
                startActivity(intent)
            },
            onAddToCartClick = { food ->
                Toast.makeText(
                    this,
                    "Added ${food.Title} to cart",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        binding.bestFoodView.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: android.graphics.Rect,
                    view: android.view.View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val spacing = resources.getDimensionPixelSize(R.dimen.item_spacing)
                    outRect.right = spacing
                    outRect.left = spacing
                }
            })
        }

        // Load food data
        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                foodObjects.clear()
                for (foodsnapshot in snapshot.children) {
                    val food = foodsnapshot.getValue(Foods::class.java)
                    if (food != null) {
                        foodObjects.add(food)
                    }
                }
                adapter.notifyDataSetChanged()
                binding.progressBar.visibility = android.view.View.GONE
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Toast.makeText(
                    this@MainActivity,
                    "Failed to load foods: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.progressBar.visibility = android.view.View.GONE
            }
        })
    }
}

//    private fun loadLocationAndTimeData() {
//        val database = FirebaseDatabase.getInstance()
//        val locationRef = database.getReference("Location")
//        val timeRef = database.getReference("Time")
//
//        // Location data
//        locationRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
//                val locationList = mutableListOf<String>()
//                val locationObjects = mutableListOf<Location>()
//
//                for (locationSnapshot in snapshot.children) {
//                    val location = locationSnapshot.getValue(Location::class.java)
//                    if (location != null) {
//                        locationObjects.add(location)
//                        locationList.add(location.location)
//                    }
//                }
//
//                val adapter = ArrayAdapter(
//                    this@MainActivity,
//                    R.layout.spinner_item,
//                    locationList
//                )
//                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//                (binding.locationSpinnerLayout.editText as? android.widget.AutoCompleteTextView)?.setAdapter(adapter)
//            }
//
//            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
//                Toast.makeText(
//                    this@MainActivity,
//                    "Failed to load locations: ${error.message}",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        })
//
//        // Time data
//        timeRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
//                val timeList = mutableListOf<String>()
//                val timeObjects = mutableListOf<Time>()
//
//                for (timeSnapshot in snapshot.children) {
//                    val time = timeSnapshot.getValue(Time::class.java)
//                    if (time != null) {
//                        timeObjects.add(time)
//                        timeList.add(time.Timevalue)
//                    }
//                }
//
//                val adapter = ArrayAdapter(
//                    this@MainActivity,
//                    R.layout.spinner_item,
//                    timeList
//                )
//                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//                (binding.timeSpinnerLayout.editText as? android.widget.AutoCompleteTextView)?.setAdapter(adapter)
//            }
//
//            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
//                Toast.makeText(
//                    this@MainActivity,
//                    "Failed to load times: ${error.message}",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        })
//    }

