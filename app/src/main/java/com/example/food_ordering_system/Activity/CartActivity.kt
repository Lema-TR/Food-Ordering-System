package com.example.food_ordering_system.Activity

import android.app.AlertDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.food_ordering_system.Adapter.CartAdapter
import com.example.food_ordering_system.Domain.CartItem
import com.example.food_ordering_system.databinding.ActivityCartBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding
    private lateinit var adapter: CartAdapter
    private val cartItems = mutableListOf<CartItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupCheckoutButton()
        loadCartItems()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupRecyclerView() {
        adapter = CartAdapter(
            cartItems,
            placeOrderClickListener = { item ->
                // Handle place order click - not used in this implementation
            },
            deleteOrderClickListener = { item ->
                // Show confirmation dialog before deleting
                showDeleteConfirmationDialog(item)
            },
            quantityChangeListener = { item, newQuantity ->
                // Handle quantity change
                updateCartItemQuantity(item, newQuantity)
            }
        )

        binding.cartRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            adapter = this@CartActivity.adapter
        }
    }

    private fun setupCheckoutButton() {
        binding.checkoutButton.setOnClickListener {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showCheckoutDialog()
        }
    }

    private fun showCheckoutDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Processing Checkout")
            .setMessage("Please wait while we process your order...")
            .setCancelable(false)
            .create()

        dialog.show()


        binding.checkoutButton.postDelayed({
            dialog.dismiss()
            Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show()
            clearCart()
        }, 2000) // 2 seconds delay to show processing
    }

    private fun showDeleteConfirmationDialog(item: CartItem) {
        AlertDialog.Builder(this)
            .setTitle("Remove Item")
            .setMessage("Are you sure you want to remove ${item.title} from your cart?")
            .setPositiveButton("Yes") { _, _ ->
                deleteCartItem(item)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun loadCartItems() {
        binding.progressBar.visibility = View.VISIBLE
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val cartRef = FirebaseDatabase.getInstance().getReference("Cart")
        cartRef.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (cartSnapshot in snapshot.children) {
                        val cartId = cartSnapshot.child("cartId").getValue(Int::class.java) ?: continue
                        loadCartItemsForCartId(cartId)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@CartActivity, "Error loading cart: ${error.message}", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                }
            })
    }

    private fun loadCartItemsForCartId(cartId: Int) {
        val cartItemRef = FirebaseDatabase.getInstance().getReference("CartItem")
        cartItemRef.orderByChild("cartId").equalTo(cartId.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    cartItems.clear()
                    for (itemSnapshot in snapshot.children) {
                        var cartItem = itemSnapshot.getValue(CartItem::class.java)
                        if (cartItem != null) {
                            cartItem.cartItemId = itemSnapshot.key ?: "" // Store the key for updates
                            cartItems.add(cartItem)
                        }
                    }
                    adapter.notifyDataSetChanged()
                    updateCartSummary()
                    binding.progressBar.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@CartActivity, "Error loading items: ${error.message}", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                }
            })
    }

    private fun updateCartItemQuantity(item: CartItem, newQuantity: Int) {
        if (newQuantity < 1) {
            deleteCartItem(item)
            return
        }

        val cartItemRef = FirebaseDatabase.getInstance().getReference("CartItem")
        cartItemRef.child(item.cartItemId).updateChildren(
            mapOf(
                "quantity" to newQuantity,
                "totalPrice" to (item.price * newQuantity)
            )
        ).addOnSuccessListener {
            // Update local item
            item.quantity = newQuantity
            item.totalPrice = item.price * newQuantity
            adapter.notifyDataSetChanged()
            updateCartSummary()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error updating quantity: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteCartItem(item: CartItem) {
        val cartItemRef = FirebaseDatabase.getInstance().getReference("CartItem")
        cartItemRef.child(item.cartItemId).removeValue()
            .addOnSuccessListener {
                cartItems.remove(item)
                adapter.notifyDataSetChanged()
                updateCartSummary()
                Toast.makeText(this, "Item removed from cart", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error removing item: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearCart() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val cartRef = FirebaseDatabase.getInstance().getReference("Cart")
        
        cartRef.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (cartSnapshot in snapshot.children) {
                        val cartId = cartSnapshot.child("cartId").getValue(Int::class.java) ?: continue
                        // Delete all cart items for this cart
                        val cartItemRef = FirebaseDatabase.getInstance().getReference("CartItem")
                        cartItemRef.orderByChild("cartId").equalTo(cartId.toDouble())
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(itemsSnapshot: DataSnapshot) {
                                    for (itemSnapshot in itemsSnapshot.children) {
                                        itemSnapshot.ref.removeValue()
                                    }
                                    // Clear local list and update UI
                                    cartItems.clear()
                                    adapter.notifyDataSetChanged()
                                    updateCartSummary()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@CartActivity, "Error clearing cart: ${error.message}", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@CartActivity, "Error clearing cart: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateCartSummary() {
        val totalItems = cartItems.sumOf { it.quantity }
        val totalPrice = cartItems.sumOf { it.totalPrice }

        binding.totalItemsText.text = "Total Items: $totalItems"
        binding.totalPriceText.text = "Total Price: ETB %.2f".format(totalPrice)
        
        // Enable/disable checkout button based on cart items
        binding.checkoutButton.isEnabled = cartItems.isNotEmpty()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}