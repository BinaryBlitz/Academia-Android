package com.academiaexpress.Fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.academiaexpress.Adapters.ProductsAdapter
import com.academiaexpress.Data.DeliveryMeal
import com.academiaexpress.R
import com.academiaexpress.Utils.AndroidUtilities
import com.academiaexpress.Utils.Image
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

        Image.loadPhoto(R.drawable.back3, view?.findViewById(R.id.background) as ImageView?)

        initList()
    }

    fun setStuff() {
        adapter.setCollection(collection)
    }

    companion object {
        var collection = ArrayList<DeliveryMeal>()
    }
}