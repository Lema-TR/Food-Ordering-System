package com.example.food_ordering_system.Domain

data class Foods (
         var CategoryID: Int = 0,
         var Description: String = "",
         var BestFood: Boolean = true,
         var Id: Int = 0,
         var LocationID: Int = 0,
         var imagepath: String = "",
         var price: Double = 0.0,
         var priceID: Int = 0,
         var star: Double = 0.0,
         var TimeId: Int = 0,
         var TimeValue: String = "",
         var Title: String = ""
        // var numberInCart: Int
)

