package com.academiaexpress.Data

import android.util.Pair

import java.util.ArrayList

class DeliveryMeal(var mealName: String?,
                   var ingridients: String?,
                   var price: Int,
                   var ingridientsList: ArrayList<Pair<String, String>>?,
                   var photoLink: String?,
                   var description: String?,
                   var badges: ArrayList<Pair<String, String>>?,
                   var id: Int?,
                   var energy: String?,
                   var isCanBuy: Boolean)
