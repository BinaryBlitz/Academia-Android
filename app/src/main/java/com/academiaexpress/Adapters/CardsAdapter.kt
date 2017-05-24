package com.academiaexpress.Adapters

import android.app.Activity
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.academiaexpress.Activities.DeliveryFinalActivity
import com.academiaexpress.Data.CreditCard
import com.academiaexpress.R

import java.util.ArrayList

class CardsAdapter(private var context: Activity?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var collection: ArrayList<CreditCard>

    init {
        collection = ArrayList<CreditCard>()
    }

    fun setCollection(collection: ArrayList<CreditCard>) {
        this.collection = collection
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)

        return NewsViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as NewsViewHolder

        holder.date.text = collection[position].number

        if (collection[position].selected) {
            setBackForSelected(holder)
        } else {
            setBackForUnSelected(holder)
        }

        holder.itemView.setOnClickListener { selectCard(holder.adapterPosition) }
    }

    private fun setBackForSelected(holder: NewsViewHolder) {
        holder.itemView.findViewById(R.id.indicator).visibility = View.VISIBLE
        holder.itemView.setBackgroundColor(Color.argb(128, 0, 0, 0))
    }

    private fun setBackForUnSelected(holder: NewsViewHolder) {
        holder.itemView.findViewById(R.id.indicator).visibility = View.GONE
        holder.itemView.setBackgroundColor(Color.argb(0, 0, 0, 0))
    }

    private fun selectCard(position: Int) {
        for (i in collection.indices) {
            collection[i].selected = false
        }

        collection[position].selected = true

        DeliveryFinalActivity.binding = collection[position].binding
        DeliveryFinalActivity.cardIndex = position
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return collection.size
    }

    private inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.name) as TextView
    }
}