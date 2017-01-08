package com.academiaexpress.Data

import java.util.ArrayList

class DeliveryOrder(var date: String?, var price: Int, var parts: ArrayList<DeliveryOrder.OrderPart>?, var id: String?) {

    class OrderPart {
        var name: String? = null
        var price: Int = 0
        var id: String? = null
        var count: Int = 0

        constructor(name: String, price: Int, count: Int) {
            this.name = name
            this.price = price
            this.count = count
        }

        constructor(price: Int, name: String, id: String) {
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
