package com.example.food_ordering_system.Domain

class Price {


        private var id: Int=0
        private var priceValue:String=""

        fun getId(): Int = id
        fun setId(value: Int) {
            id = value
        }


        fun getPriceValue(): String =  priceValue
        fun setPriceValue(value: String) {
        priceValue= value.trim()
        }
    }
