package com.academiaexpress.Activities

import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.ImageView
import com.academiaexpress.Adapters.CardsAdapter
import com.academiaexpress.Base.BaseActivity
import com.academiaexpress.R
import com.academiaexpress.Utils.Image

class CreditCardsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credit_cards)
        initScreen()
        initRecyclerView()
        setOnClickListeners()
    }

    private fun initScreen() {
        Image.loadPhoto(R.drawable.back1, findViewById(R.id.imageView21) as ImageView)
    }

    private fun initRecyclerView() {
        val view = findViewById(R.id.recyclerView) as RecyclerView
        view.itemAnimator = DefaultItemAnimator()
        view.layoutManager = LinearLayoutManager(this)
        view.setHasFixedSize(true)

        initAdapter(view)
    }

    private fun initAdapter(view: RecyclerView) {
        val adapter = CardsAdapter(this)
        view.adapter = adapter

        adapter.setCollection(DeliveryFinalActivity.collection)
        adapter.notifyDataSetChanged()
    }

    private fun setOnClickListeners() {
        findViewById(R.id.guillotine_hamburger).setOnClickListener { finish() }

        findViewById(R.id.editText3).setOnClickListener {
            DeliveryFinalActivity.newCard = true
            finish()
        }

        findViewById(R.id.editText35).setOnClickListener {
            DeliveryFinalActivity.newCard = false
            finish()
        }
    }
}
