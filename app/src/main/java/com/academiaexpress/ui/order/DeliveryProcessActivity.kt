package com.academiaexpress.ui.order

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.academiaexpress.R
import com.academiaexpress.ui.BaseActivity
import com.academiaexpress.ui.main.ClosedActivity
import com.academiaexpress.ui.main.ProductsActivity
import com.academiaexpress.ui.main.StartActivity
import com.academiaexpress.utils.AndroidUtilities
import com.academiaexpress.utils.AppConfig
import com.academiaexpress.utils.Image
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
        reset()

        if (ClosedActivity.closed) {
            finishActivity(ClosedActivity::class.java)
        } else {
            finishActivity(StartActivity::class.java)
        }
    }

    private fun reset() {
        ProductsActivity.collection = ArrayList()
        ProductsActivity.price = 0
        ProductsActivity.product_count = 0
        TimeActivity.id = ""
    }

    companion object {
        private val EXTRA_PRICE = "price"
    }
}
