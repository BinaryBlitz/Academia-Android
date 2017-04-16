package com.academiaexpress.Data

import android.util.Pair

import java.util.ArrayList

class Dish(var mealName: String?,
           var ingredients: String?,
           var price: Int,
           var ingredientsList: ArrayList<Pair<String, String>>?,
           var photoLink: String?,
           var description: String?,
           var badges: ArrayList<Pair<String, String>>?,
           var id: Int?,
           var energy: String?,
           var isOutOfStock: Boolean,
           var count: Int)
