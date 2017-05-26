package com.academiaexpress.ui.order

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.academiaexpress.R
import com.academiaexpress.data.Dish
import com.academiaexpress.ui.main.adapters.ProductsAdapter
import java.util.*

class StuffFragment : Fragment() {
    private lateinit var adapter: ProductsAdapter
    private lateinit var list: RecyclerView

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_final_page, container, false)
    }

    private fun initList() {
        list = view?.findViewById(R.id.recyclerView) as RecyclerView
        list.itemAnimator = DefaultItemAnimator()
        list.layoutManager = LinearLayoutManager(activity)
        list.isNestedScrollingEnabled = false
        adapter = ProductsAdapter(activity)
        list.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        initList()
        setStuff()
    }

    fun setStuff() {
        adapter.setCollection(collection)
        adapter.notifyDataSetChanged()
    }

    companion object {
        var collection = ArrayList<Dish>()
    }
}