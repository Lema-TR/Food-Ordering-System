package com.example.food_ordering_system.Activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.food_ordering_system.R
import com.example.food_ordering_system.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.File

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference

        window.statusBarColor = resources.getColor(R.color.light_pink)

        setVariable()

    }

    private fun setVariable() {
        binding.signupBtn.setOnClickListener {
            val username = binding.nameInput.text.toString().trim()
            val email = binding.emailInput.text.toString().trim().lowercase()
            val password = binding.passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = firebaseAuth.currentUser?.uid
                        val user = User(username, email)

                        if (uid != null) {
                            databaseRef.child("Users").child(uid).setValue(user)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT).show()
                                    // Navigate to home or login screen if needed
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Signup succeeded but failed to save user info", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
