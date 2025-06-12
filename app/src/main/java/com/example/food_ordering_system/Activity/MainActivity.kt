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


        setupBestFoodRecyclerView()
        setupCategoryRecyclerView()
    }


  // function for navigation
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
                   // navigate to orders
                    Toast.makeText(this, "Orders coming soon!", Toast.LENGTH_SHORT).show()
                    false
                }

                else -> false
            }
        }
    }

    // search  function

    private fun setupSearchAndFilter() {
        binding.cartBtn.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        // Search functionality
        binding.searchContainer.editText?.setOnEditorActionListener { _, _, _ ->
            val searchQuery = binding.searchContainer.editText?.text.toString()
            if (searchQuery.isNotEmpty()) {
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


    //  function for handling bestfood

    private fun setupBestFoodRecyclerView() {
        val foodObjects = mutableListOf<Foods>()
        val database = FirebaseDatabase.getInstance()
        val foodRef = database.getReference("Foods")

        // initialize the adapter and send data to productDetailActivity
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
            // handle how a best food added to cart
            onAddToCartClick = { food ->

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
                            Toast.makeText(this@MainActivity,"Database error :${error.message}",Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(this@MainActivity, "Item quantity updated in cart", Toast.LENGTH_SHORT).show()
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
                                Toast.makeText(this@MainActivity, "Item added to cart", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this@MainActivity, "Failed to add item: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // function for setting choose category

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
                // Notify adapter that data changed
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

