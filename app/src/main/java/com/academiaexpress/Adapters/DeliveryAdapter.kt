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
import com.academiaexpress.R

class DeliveryAdapter(private var context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var isInc = false

    private val EXTRA_PRICE = "price"
    private val EXTRA_COUNT = "count"
    private val EXTRA_NAME = "name"
    private val EXTRA_INDEX = "index"

    fun changeItem(index: Int, count: Int) {
        ProductsActivity.price -= ProductsActivity.collection[index].price * ProductsActivity.collection[index].count
        ProductsActivity.product_count -= ProductsActivity.collection[index].count
        ProductsActivity.collection[index].count = count
        ProductsActivity.price += ProductsActivity.collection[index].price * ProductsActivity.collection[index].count
        ProductsActivity.product_count += ProductsActivity.collection[index].count
        notifyItemChanged(index)
    }

    fun remove(position: Int) {
        ProductsActivity.collection.removeAt(position)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.order_part_card, parent, false)
        return NewsViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as NewsViewHolder

        holder.date.text = ProductsActivity.collection[position].count.toString() + context.getString(R.string.delivery_adapter_postfix)

        val content = SpannableString(ProductsActivity.collection[position].name)
        content.setSpan(UnderlineSpan(), 0, ProductsActivity.collection[position].name!!.length, 0)
        holder.order.text = content
        holder.price.text = ProductsActivity.collection[position].price.toString() + context.getString(R.string.ruble_sign)

        if (isInc) {
            holder.itemView.setOnClickListener {
                val intent = Intent(context, EditOrderActivity::class.java)
                intent.putExtra(EXTRA_PRICE, ProductsActivity.collection[position].price)
                intent.putExtra(EXTRA_COUNT, ProductsActivity.collection[position].count)
                intent.putExtra(EXTRA_NAME, ProductsActivity.collection[position].name)
                intent.putExtra(EXTRA_INDEX, position)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return if (ProductsActivity.collection == null) 0 else ProductsActivity.collection.size
    }

    private inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.textView27) as TextView
        val order: TextView = itemView.findViewById(R.id.textView21) as TextView
        val price: TextView = itemView.findViewById(R.id.textView22) as TextView
    }
}