package com.example.food_ordering_system.Activity



import android.app.Activity
import android.content.Intent
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
    private var selectedImageUri: Uri? = null

    companion object {
        private const val REQUEST_IMAGE_PICK = 101
        private const val BIO_MAX_LENGTH = 150
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Image click to open gallery
        binding.imageProfile.setOnClickListener {
            openImageGallery()
        }

        // Bio character count
        binding.etBio.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val count = s?.length ?: 0
                binding.tvBioCharCount.text = "$count/$BIO_MAX_LENGTH"
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Save button logic
        binding.btnSaveChanges.setOnClickListener {
            saveProfile()
        }
    }

    private fun openImageGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            Glide.with(this)
                .load(selectedImageUri)
                .centerCrop()
                .into(binding.imageProfile)
        }
    }

    private fun saveProfile() {
        val fullName = binding.etFullName.text.toString().trim()
        val nickname = binding.etNickname.text.toString().trim()
        val bio = binding.etBio.text.toString().trim()

        if (fullName.isEmpty() || nickname.isEmpty() || bio.isEmpty()) {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        // Upload image, save to database, etc. (placeholder)
        Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show()
    }
}
