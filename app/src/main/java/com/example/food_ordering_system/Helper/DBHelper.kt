package com.example.food_ordering_system.Helper

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, "UserDB", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        // Create userAuthenticator table
        val createUserAuth = """
        CREATE TABLE userAuthenticator (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT,
            email TEXT UNIQUE,
            password TEXT
        )
    """.trimIndent()
        db.execSQL(createUserAuth)

        // Create Category table
        val createCategory = """
        CREATE TABLE Category (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            imagePath TEXT,
            name TEXT
        )
    """.trimIndent()
        db.execSQL(createCategory)

        // Create Location table
        val createLocation = """
        CREATE TABLE Location (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            loc TEXT
        )
    """.trimIndent()
        db.execSQL(createLocation)

        // Create Price table
        val createPrice = """
        CREATE TABLE Price (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            priceValue TEXT
        )
    """.trimIndent()
        db.execSQL(createPrice)

        // Create Time table
        val createTime = """
        CREATE TABLE Time (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            timeValue TEXT
        )
    """.trimIndent()
        db.execSQL(createTime)

        // Create Foods table
        val createFoods = """
        CREATE TABLE Foods (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            categoryId INTEGER,
            description TEXT,
            bestFood INTEGER,
            locationId INTEGER,
            imagePath INTEGER,
            price REAL,
            priceId INTEGER,
            star INTEGER,
            timeId INTEGER,
            timeValue INTEGER,
            title TEXT,
            numberInCart INTEGER
        )
    """.trimIndent()
        db.execSQL(createFoods)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS userAuthenticator")
        db.execSQL("DROP TABLE IF EXISTS Category")
        db.execSQL("DROP TABLE IF EXISTS Location")
        db.execSQL("DROP TABLE IF EXISTS Price")
        db.execSQL("DROP TABLE IF EXISTS Time")
        db.execSQL("DROP TABLE IF EXISTS Foods")
        onCreate(db)
    }

    fun insertUser(name: String, email: String, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues()
        values.put("name", name)
        values.put("email", email)
        values.put("password", password)

        val result = db.insert("userAuthenticator", null, values)
        return result != -1L
    }

    fun checkUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM userAuthenticator WHERE email = ? AND password = ?"
        val cursor = db.rawQuery(query, arrayOf(email, password))

        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

}
