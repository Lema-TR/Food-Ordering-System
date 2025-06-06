package com.example.food_ordering_system.Domain

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.core.Path

data class Category(
    val id: Int = 0 ,
    val name: String = "",
    val imagepath: String =""  // Resource ID for the category icon

)
