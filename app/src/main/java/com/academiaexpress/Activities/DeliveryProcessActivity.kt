package com.academiaexpress.Activities

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.academiaexpress.Base.BaseActivity
import com.academiaexpress.Data.Order
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
        Image.loadPhoto(R.drawable.load_pic, findViewById(R.id.background) as ImageView)
        Answers.getInstance().logCustom(CustomEvent(getString(R.string.event_order_paid)))
        MapActivity.selectedLocationName = ""
        MapActivity.selectedLocation = null

        val animation = AnimationUtils.loadAnimation(this, R.anim.rotate_anim)
        findViewById(R.id.indicator).startAnimation(animation)
    }

    private fun openDetails() {
        onBackPressed()
    }

    private fun setOnClickListeners() {
        findViewById(R.id.show_order).setOnClickListener { openDetails() }

        findViewById(R.id.call).setOnClickListener { AndroidUtilities.call(this@DeliveryProcessActivity, AppConfig.phone) }
    }

    private fun finishActivity(c: Class<*>) {
        val intent = Intent(this, c)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        ProductsActivity.collection = ArrayList()
        ProductsActivity.price = 0
        ProductsActivity.product_count = 0

        if (ClosedActivity.closed) {
            finishActivity(ClosedActivity::class.java)
        } else {
            finishActivity(StartActivity::class.java)
        }
    }

    companion object {
        private val EXTRA_PRICE = "price"
    }
}
