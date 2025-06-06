package com.example.food_ordering_system.Domain

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.core.Path

class Category {

    private var id: Int = 0
    private var imagePath: String = ""
    private var name: String = ""

    fun getId(): Int = id
    fun setId(value: Int) { id = value }

    fun getImagePath(): String = imagePath
    fun setImagePath(value: String) { imagePath = value.trim() }

    fun getName(): String = name
    fun setName(value: String) {
        name = value.trim().replaceFirstChar { it.uppercase() }
    }
}
