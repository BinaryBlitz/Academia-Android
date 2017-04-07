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
    private val EXTRA_ADDITIONAL = "additional"

    val list: ArrayList<Category> = ArrayList()

    fun saveCategories(array: JsonArray) {
        LogUtil.logError(array.toString())
        (0..array.size() - 1)
                .map { array.get(it).asJsonObject }
                .mapTo(list) {
                    Category(
                            AndroidUtilities.getIntFieldFromJson(it.get("id")),
                            AndroidUtilities.getStringFieldFromJson(it.get("name")),
                            AndroidUtilities.getBooleanFieldFromJson(it.get("complementary"))
                    )
                }
    }

    fun showCategoriesList(layout: LinearLayout, context: Activity) {
        var i = 0
        for ((id, name, isStuff) in list) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_category, null)

            (view.findViewById(R.id.name) as TextView).text = name
            view.setOnClickListener {
                val intent = Intent(context, ProductsActivity::class.java)
                intent.putExtra(EXTRA_ID, id)
                intent.putExtra(EXTRA_ADDITIONAL, isStuff)
                context.startActivity(intent)
                context.finish()
            }

            layout.addView(view, i++)
        }
    }
}
