package com.academiaexpress.Utils

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.academiaexpress.Activities.ProductsActivity
import com.academiaexpress.Data.Category
import com.academiaexpress.R
import com.google.gson.JsonArray

object CategoriesUtility {
    val EXTRA_ID = "id"
    val list: ArrayList<Category> = ArrayList()

    fun saveCategories(array: JsonArray) {
        (0..array.size() - 1)
                .map { array.get(it).asJsonObject }
                .mapTo(list) {
                    Category(
                            AndroidUtilities.getIntFieldFromJson(it.get("id")),
                            AndroidUtilities.getStringFieldFromJson(it.get("name"))
                    )
                }
    }

    fun showCategoriesList(layout: LinearLayout, context: Activity) {
        for ((id, name) in list) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_category, null)

            (view.findViewById(R.id.name) as TextView).text = name
            view.setOnClickListener {
                val intent = Intent(context, ProductsActivity::class.java)
                intent.putExtra(EXTRA_ID, id)
                context.startActivity(intent)
                context.finish()
            }

            layout.addView(view, 0)
        }
    }
}
