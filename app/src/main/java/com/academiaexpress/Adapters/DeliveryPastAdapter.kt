package com.academiaexpress.Adapters

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.academiaexpress.Activities.EditOrderActivity
import com.academiaexpress.Activities.ProductsActivity
import com.academiaexpress.Data.DeliveryOrder
import com.academiaexpress.R
import java.util.*

class DeliveryPastAdapter(private var context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    internal var collection: ArrayList<DeliveryOrder.OrderPart> = ArrayList()
    var isInc = false

    private val EXTRA_PRICE = "price"
    private val EXTRA_COUNT = "count"
    private val EXTRA_NAME = "name"
    private val EXTRA_INDEX = "index"

    fun setCollection(collection: ArrayList<DeliveryOrder.OrderPart>) {
        this.collection = collection
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_order_past, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder

        holder.count.text = ProductsActivity.collection[position].count.toString() + context.getString(R.string.delivery_adapter_postfix)
        setOrderText(position, holder)
        holder.price.text = ProductsActivity.collection[position].price.toString() + context.getString(R.string.ruble_sign)

        if (isInc) {
            holder.itemView.setOnClickListener { openActivity(holder.adapterPosition) }
        }
    }

    private fun setOrderText(position: Int, holder: ViewHolder) {
        val content = SpannableString(ProductsActivity.collection[position].name)
        content.setSpan(UnderlineSpan(), 0, ProductsActivity.collection[position].name!!.length, 0)
        holder.name.text = content
    }

    private fun openActivity(position: Int) {
        val intent = Intent(context, EditOrderActivity::class.java)
        intent.putExtra(EXTRA_PRICE, ProductsActivity.collection[position].price)
        intent.putExtra(EXTRA_COUNT, ProductsActivity.collection[position].count)
        intent.putExtra(EXTRA_NAME, ProductsActivity.collection[position].name)
        intent.putExtra(EXTRA_INDEX, position)
        context.startActivity(intent)
    }

    override fun getItemCount(): Int {
        return collection.size
    }

    private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val count: TextView = itemView.findViewById(R.id.count) as TextView
        val name: TextView = itemView.findViewById(R.id.name) as TextView
        val price: TextView = itemView.findViewById(R.id.price) as TextView

    }
}