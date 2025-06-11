package com.example.food_ordering_system.Activity



import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cloudinary.android.MediaManager
import com.example.food_ordering_system.Adapter.BestFoodAdapter
import com.example.food_ordering_system.Adapter.CategoryAdapter
import com.example.food_ordering_system.Domain.CartItem
import com.example.food_ordering_system.Domain.Category
import com.example.food_ordering_system.Domain.Foods
import com.example.food_ordering_system.R
import com.example.food_ordering_system.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
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

        setupBestFoodRecyclerView()
        setupCategoryRecyclerView()
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
                    putExtra("food_price", food.price.toString())
                    putExtra("food_description", food.Description)
                    putExtra("food_image", food.imagepath)
                    putExtra("food_rating", food.star.toString())
                    putExtra("food_time", food.Timevalue)
                }
                startActivity(intent)
            },
            onAddToCartClick = { food ->
//                val database = FirebaseDatabase.getInstance()
//                val cartRef = database.getReference("Cart") // Reference to "Cart" node
//                val cartItem = CartItem(food.Title, food.price, 1, food.price)
//
//                cartRef.push().setValue(cartItem)
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@BestFoodAdapter
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
            }
        )

        binding.bestFoodView.apply {
            this.adapter = adapter
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
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

    private fun updateCartItemsByCartId(cartId: Int, food: Foods) {
        val cartItemRef = FirebaseDatabase.getInstance().getReference("CartItem")

        cartItemRef.orderByChild("cartId").equalTo(cartId.toDouble()) // match Firebase's internal double representation
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var nodeKey = ""
                    var cartItem : CartItem? = null
                    for (cartItemSnapshot in snapshot.children) {
                        cartItem = cartItemSnapshot.getValue(CartItem::class.java)
                        if (cartItem?.foodId == food.Id) {
                            nodeKey = cartItemSnapshot.key.toString()
                            break
                        }
                    }
                    if (cartItem?.foodId == food.Id) {
                        Toast.makeText(this@MainActivity, "Food is already in the cart", Toast.LENGTH_LONG).show()
                    }
                    else {
                        // initialize the new cart item
//                        val safeNodeKey = nodeKey.toIntOrNull() ?: 0
                        val newCartItem = CartItem(nodeKey, cartId, food.Id, 1, food.price, food.price, food.Title)
                        cartItemRef.push().setValue(newCartItem)
                        Toast.makeText(this@MainActivity, "Food added to cart", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CartItemQuery", "Error: ${error.message}")
                }
            })
    }


    private fun setupCategoryRecyclerView() {
        val categoryObjects = mutableListOf<Category>()
        val database = FirebaseDatabase.getInstance()
        val categoryRef = database.getReference("Category")


        // Initialize adapter first with empty list
        val adapter = CategoryAdapter(categoryObjects, onItemClick = { category ->
            val intent = Intent(this, FoodListActivity::class.java).apply {
                putExtra("id", category.id)
                putExtra("CATEGORY_NAME",category.name)
            }
            startActivity(intent)
        })

        binding.categoryRecyclerView.apply {
            this.adapter = adapter
            layoutManager = GridLayoutManager(this@MainActivity, 3)
        }

        categoryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                categoryObjects.clear()

                for (categorysnapshot in snapshot.children) {

                    val category = categorysnapshot.getValue(Category::class.java)
                    if (category != null) {
                        categoryObjects.add(category)
                    }
                }

                adapter.notifyDataSetChanged() // Notify adapter that data changed
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
//
//    private fun setupCategoryRecyclerView() {
//        val adapter = CategoryAdapter(mutableListOf()) { category ->
//            // Handle category click
//            val intent = Intent(this, FoodListActivity::class.java).apply {
//                putExtra("CATEGORY_ID", category.id)
//            }
//            startActivity(intent)
//        }
//
//        binding.categoryRecyclerView.apply {
//            this.adapter = adapter
//            layoutManager = GridLayoutManager(this@MainActivity, 3)
//        }
//
//        // Load categories from Firebase
//        binding.categoryProgressBar.visibility = View.VISIBLE
//        val database = FirebaseDatabase.getInstance()
//        val categoryRef = database.getReference("Categories")
//
//        categoryRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
//                val categoryList = mutableListOf<Category>()
//                for (categorySnapshot in snapshot.children) {
//                    val category = categorySnapshot.getValue(Category::class.java)
//                    if (category != null) {
//                        categoryList.add(category)
//                    }
//                }
//                adapter.updateItems(categoryList)
//                binding.categoryProgressBar.visibility = View.GONE
//            }
//
//            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
//                binding.categoryProgressBar.visibility = View.GONE
//                Toast.makeText(
//                    this@MainActivity,
//                    "Failed to load categories: ${error.message}",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        })
//    }
//}

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

