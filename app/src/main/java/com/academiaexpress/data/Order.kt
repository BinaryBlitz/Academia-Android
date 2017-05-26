package com.academiaexpress.data

import java.util.*

class Order(var date: Date?, var price: Int, var parts: ArrayList<Order.OrderPart>?, var id: Int) {

    class OrderPart {
        var name: String? = null
        var price: Int = 0
        var id: Int? = null
        var count: Int = 0

        constructor(name: String, price: Int, count: Int) {
            this.name = name
            this.price = price
            this.count = count
        }

        constructor(price: Int, name: String, id: Int) {
            this.name = name
            this.price = price
            this.id = id
            this.count = 1
        }

        fun incCount() {
            count++
        }

    }

    var isReviewd = false

    var rating = 0
    var review: String? = null
    var address: String? = null
    var isOnTheWay: Boolean = false
}
