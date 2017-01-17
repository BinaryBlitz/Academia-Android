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
import com.academiaexpress.Data.MiniProduct
import com.academiaexpress.R
import com.academiaexpress.Server.DeviceInfoStore
import com.academiaexpress.Server.ServerApi
import com.academiaexpress.Utils.AndroidUtilities
import com.academiaexpress.Utils.Image
import com.google.gson.JsonArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class FinalPageFragment : Fragment() {
    private var adapter: ProductsAdapter? = null
    private var list: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_final_page, container, false)
    }

    val scrollView: NestedScrollView
        get() = view!!.findViewById(R.id.scroll) as NestedScrollView


    private fun initList() {
        list = view!!.findViewById(R.id.recyclerView) as RecyclerView
        list!!.itemAnimator = DefaultItemAnimator()
        list!!.layoutManager = LinearLayoutManager(activity)
        list!!.isNestedScrollingEnabled = false
        adapter = ProductsAdapter(activity)
        list!!.adapter = adapter
    }

    private fun initButtonClick() {
        val layout = view!!.findViewById(R.id.main) as FrameLayout
        val params = layout.layoutParams as LinearLayout.LayoutParams
        params.height = AndroidUtilities.getScreenHeight(activity) - AndroidUtilities.getStatusBarHeight(context)

        view!!.findViewById(R.id.textView).setOnClickListener {
            (view!!.findViewById(R.id.scroll) as NestedScrollView).smoothScrollTo(0,
                    AndroidUtilities.getScreenHeight(activity) - AndroidUtilities.getStatusBarHeight(context))
        }
    }

    override fun onStart() {
        super.onStart()

        Image.loadPhoto(R.drawable.back3, view!!.findViewById(R.id.imageView3) as ImageView)

        initButtonClick()
        initList()

        if (collection.size == 0) getStuff()
        else reInitList()
    }

    private fun reInitList() {
        adapter!!.setCollection(collection)
        view!!.layoutParams.height = AndroidUtilities.dpToPx(context, (adapter!!.itemCount * 100).toFloat())
    }

    private fun getStuff() {
        ServerApi.get(context).api().getStuff(DeviceInfoStore.getToken(context)).enqueue(object : Callback<JsonArray> {
            override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                if (response.isSuccessful) {
                    parseStuff(response.body())
                }
            }

            override fun onFailure(call: Call<JsonArray>, t: Throwable) { }
        })
    }

    private fun parseStuff(array: JsonArray) {
        (0..array.size() - 1)
                .map { array.get(it).asJsonObject }
                .map {
                    MiniProduct(AndroidUtilities.getStringFieldFromJson(it.get("name")),
                            AndroidUtilities.getStringFieldFromJson(it.get("description")),
                            AndroidUtilities.getIntFieldFromJson(it.get("price")),
                            AndroidUtilities.getStringFieldFromJson(it.get("image_url")),
                            AndroidUtilities.getIntFieldFromJson(it.get("id"))
                    )
                }
                .forEach { collection.add(it) }

        adapter!!.setCollection(collection)
        view!!.layoutParams.height = AndroidUtilities.dpToPx(context, (adapter!!.itemCount * 100).toFloat())
    }

    companion object {
        var collection = ArrayList<MiniProduct>()
    }
}