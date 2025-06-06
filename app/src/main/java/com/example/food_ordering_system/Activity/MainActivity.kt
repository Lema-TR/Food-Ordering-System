package com.example.food_ordering_system.Activity

import com.cloudinary.android.MediaManager
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.food_ordering_system.Adapter.BestFoodAdapter
import com.example.food_ordering_system.Domain.Foods
import com.example.food_ordering_system.Domain.Location
import com.example.food_ordering_system.Domain.Time
import com.example.food_ordering_system.R
import com.example.food_ordering_system.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val config: HashMap<String, String> = hashMapOf(
            "cloud_name" to "dwjfvmjfh",
            "api_key" to "763756178671368",
            "api_secret" to "1fhz5923fnJSQ4seVRageDhU8QI"
        )
        MediaManager.init(this, config)

        val spFilterByLocation: Spinner = binding.spFilterByLocation
        val spFilterByTime: Spinner = binding.spFilterByTime
        val database = FirebaseDatabase.getInstance()
        val locationRef = database.getReference("Location")
        val timeRef = database.getReference("Time")
        val foodRef = database.getReference("Foods")

        val locationList = mutableListOf<String>()
        val locationObjects = mutableListOf<Location>()

        val timeList = mutableListOf<String>()
        val timeObjects = mutableListOf<Time>()

        val foodObjects = mutableListOf<Foods>()

        // fetching data for filter by location spinner
        locationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                locationList.clear()
                locationObjects.clear()

                for (locationSnapshot in snapshot.children) {
                    val location = locationSnapshot.getValue(Location::class.java)
                    if (location != null) {
                        locationObjects.add(location)
                        locationList.add(location.location)
                        Log.d("mylog", location.location)
                    }
                }

                val adapter = ArrayAdapter(
                    this@MainActivity,
                    R.layout.spinner_item,
                    locationList
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spFilterByLocation.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@MainActivity,
                    "Failed to load locations: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        // fetching data for filter by time spinner
        timeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                timeList.clear()
                timeObjects.clear()
                Log.d("mylog", "test-0")


                for (timeSnapshot in snapshot.children) {
                    val time = timeSnapshot.getValue(Time::class.java)
                    if (time != null) {
                        timeObjects.add(time)
                        timeList.add(time.Timevalue)
                        Log.d("mylog", time.Timevalue)
                        Log.d("mylog", "test-1")
                    } else {
                        Log.d("mylog", "test-2")
                    }
                }

                val adapter = ArrayAdapter(
                    this@MainActivity,
                    R.layout.spinner_item,
                    timeList
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spFilterByTime.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@MainActivity,
                    "Failed to load times: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        // fetching data for the best food displayer card
        val adapter = BestFoodAdapter(foodObjects)
        binding.bestFoodView.adapter = adapter
        binding.bestFoodView.layoutManager=LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)

        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                foodObjects.clear()

                for (foodsnapshot in snapshot.children) {
                    val food = foodsnapshot.getValue(Foods::class.java)

                    if (food != null) {
                        foodObjects.add(food)
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@MainActivity,
                    "Failed to load times: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

    }
}

