package com.academiaexpress.Activities

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.academiaexpress.Base.BaseActivity
import com.academiaexpress.Data.DeliveryOrder
import com.academiaexpress.R
import com.academiaexpress.Utils.AndroidUtilities
import com.academiaexpress.Utils.AppConfig
import com.academiaexpress.Utils.Image
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent

class DeliveryProcessActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery_process)

        initElements()
        setOnClickListeners()
    }

    private fun initElements() {
        Image.loadPhoto(R.drawable.load_pic, findViewById(R.id.imageView17) as ImageView)
        Answers.getInstance().logCustom(CustomEvent(getString(R.string.event_order_paid)))
       // MapActivity.selected_final = ""

        val animation = AnimationUtils.loadAnimation(this, R.anim.rotate_anim)
        findViewById(R.id.imageView19).startAnimation(animation)
    }

    private fun openDetails() {
        val intent = Intent(this@DeliveryProcessActivity, OrderDetailsActivity::class.java)
        OrderDetailsActivity.order = DeliveryOrder(null, ProductsActivity.price, ProductsActivity.collection, TimeActivity.id.toInt())
        OrderDetailsActivity.order.isOnTheWay = true
        intent.putExtra(EXTRA_PRICE, getString(R.string.order_by_sum) + ProductsActivity.price + getString(R.string.ruble_sign))
        startActivity(intent)
    }

    private fun setOnClickListeners() {
        findViewById(R.id.textView).setOnClickListener { openDetails() }

        findViewById(R.id.textViewfd).setOnClickListener { AndroidUtilities.call(this@DeliveryProcessActivity, AppConfig.phone) }
    }

    private fun finishActivity(c: Class<*>) {
        val intent = Intent(this, c)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (ClosedActivity.closed) finishActivity(ClosedActivity::class.java)
        else finishActivity(StartActivity::class.java)
    }

    companion object {
        private val EXTRA_PRICE = "price"
    }
}
