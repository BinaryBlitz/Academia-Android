package com.academiaexpress.Adapters

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.academiaexpress.Activities.ProductsActivity
import com.academiaexpress.Data.DeliveryOrder
import com.academiaexpress.Data.MiniProduct
import com.academiaexpress.Fragments.DishFragment
import com.academiaexpress.R
import com.academiaexpress.Utils.Image
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import java.util.*

class ProductsAdapter(private var context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var collection: ArrayList<MiniProduct>? = null
    private var partsCollection: ArrayList<DeliveryOrder.OrderPart>? = null

    init { collection = ArrayList<MiniProduct>() }

    fun setCollection(collection: ArrayList<MiniProduct>) {
        this.collection = collection
        partsCollection = ArrayList<DeliveryOrder.OrderPart>()
        for (i in collection.indices) {
            partsCollection!!.add(DeliveryOrder.OrderPart(collection[i].price, collection[i].name!!, collection[i].id))
        }

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_product_mini, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder

        if (collection == null) {
            return
        }

        holder.name.text = collection!![position].name
        holder.description.text = collection!![position].ingridients
        holder.price.text = collection!![position].price.toString() + context.getString(R.string.ruble_sign)

        Image.loadPhoto(collection!![position].photoLink, holder.avatar)

        if (collection!![position].count == 0) {
            hideCountTextView(holder)
        } else {
            showCountTextView(holder, position)
        }

        holder.itemView.findViewById(R.id.imageView5).setOnClickListener { addProduct(holder, holder.adapterPosition) }
    }

    private fun hideCountTextView(holder: ViewHolder) {
        holder.itemView.findViewById(R.id.count_indicator).visibility = View.GONE
    }

    private fun showCountTextView(holder: ViewHolder, position: Int) {
        holder.itemView.findViewById(R.id.count_indicator).visibility = View.VISIBLE
        (holder.itemView.findViewById(R.id.count_indicator) as TextView).text = Integer.toString(collection!![position].count)
    }

    private fun addProduct(holder: ViewHolder, position: Int) {
        if (collection == null) {
            return
        }

        if (!DishFragment.answer) {
            Answers.getInstance().logCustom(CustomEvent(context.getString(R.string.event_order_added)))
        }
        DishFragment.answer = true

        holder.itemView.findViewById(R.id.count_indicator).visibility = View.VISIBLE
        collection!![position].count = collection!![position].count + 1
        (holder.itemView.findViewById(R.id.count_indicator) as TextView).text = Integer.toString(collection!![position].count)
        (context as ProductsActivity).addProduct(partsCollection!![position])
    }

    override fun getItemCount(): Int {
        if (collection == null) {
            return 0
        }
        return collection!!.size
    }

    private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name) as TextView
        val description: TextView = itemView.findViewById(R.id.description) as TextView
        val price: TextView = itemView.findViewById(R.id.price) as TextView
        val avatar: ImageView = itemView.findViewById(R.id.image) as ImageView

    }
}