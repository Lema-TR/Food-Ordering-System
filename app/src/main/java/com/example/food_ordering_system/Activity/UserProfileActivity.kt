package com.example.food_ordering_system.Activity

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.food_ordering_system.databinding.ActivityUserProfileBinding

class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var sharedPrefs: SharedPreferences
    private var selectedImageUri: Uri? = null

    companion object {
        private const val REQUEST_IMAGE_PICK = 101
        private const val BIO_MAX_LENGTH = 150
        private const val PREFS_NAME = "UserProfilePrefs"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Restore state from rotation
        if (savedInstanceState != null) {
            selectedImageUri = savedInstanceState.getParcelable("SELECTED_IMAGE_URI")
            selectedImageUri?.let { loadImage(it) }
        } else {
            // Restore from SharedPreferences only if not rotating
            restoreFromSharedPreferences()
        }

        setupViews()
    }

    private fun setupViews() {
        // Profile image click listener
        binding.imageProfile.setOnClickListener {
            openImageGallery()
        }

        // Bio character counter
        binding.etBio.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.tvBioCharCount.text = "${s?.length ?: 0}/$BIO_MAX_LENGTH"
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Initialize bio counter
        binding.tvBioCharCount.text = "${binding.etBio.text.length}/$BIO_MAX_LENGTH"

        // Save button click listener
        binding.btnSaveChanges.setOnClickListener {
            saveProfile()
        }
    }

    private fun openImageGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
        }
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedImageUri = uri
                loadImage(uri)
            }
        }
    }

    private fun loadImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(binding.imageProfile)
    }

    private fun restoreFromSharedPreferences() {
        binding.etFullName.setText(sharedPrefs.getString("full_name", ""))
        binding.etNickname.setText(sharedPrefs.getString("nickname", ""))
        binding.etBio.setText(sharedPrefs.getString("bio", ""))

        sharedPrefs.getString("image_uri", null)?.let { uriString ->
            selectedImageUri = Uri.parse(uriString)
            loadImage(selectedImageUri!!)
        }
    }

    private fun saveToSharedPreferences() {
        sharedPrefs.edit().apply {
            putString("full_name", binding.etFullName.text.toString())
            putString("nickname", binding.etNickname.text.toString())
            putString("bio", binding.etBio.text.toString())
            selectedImageUri?.let { putString("image_uri", it.toString()) }
            apply() // Asynchronous save
        }
    }

    private fun saveProfile() {
        val fullName = binding.etFullName.text.toString().trim()
        val nickname = binding.etNickname.text.toString().trim()
        val bio = binding.etBio.text.toString().trim()

        if (fullName.isEmpty() || nickname.isEmpty() || bio.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (bio.length > BIO_MAX_LENGTH) {
            Toast.makeText(this, "Bio exceeds character limit", Toast.LENGTH_SHORT).show()
            return
        }

        saveToSharedPreferences()
        Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selectedImageUri?.let {
            outState.putParcelable("SELECTED_IMAGE_URI", it)
        }
    }

    override fun onPause() {
        super.onPause()
        // Save current state when leaving the activity
        saveToSharedPreferences()
    }
}