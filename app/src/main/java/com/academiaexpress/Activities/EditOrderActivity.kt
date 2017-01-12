package com.academiaexpress.Activities

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.academiaexpress.Base.BaseActivity
import com.academiaexpress.R
import com.academiaexpress.Utils.Image

class EditOrderActivity : BaseActivity() {
    private var count: Int = 0
    private var price: Int = 0
    private var finalPrice: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_order)

        initElements()
        setOnClickListeners()
    }

    private fun dec() {
        if (count == 1) return

        count--
        finalPrice -= price
        setText()
    }

    private fun inc() {
        count++
        finalPrice += price
        setText()
    }

    private fun setOnClickListeners() {
        findViewById(R.id.textView28fd).setOnClickListener {
            DeliveryFinalActivity.INDEX = intent.getIntExtra(EXTRA_INDEX, 0)
            DeliveryFinalActivity.newCount = count
            finish()
        }

        findViewById(R.id.textView28).setOnClickListener {
            DeliveryFinalActivity.INDEX = -2
            DeliveryFinalActivity.r_INDEX = intent.getIntExtra(EXTRA_INDEX, 0)
            finish()
        }

        findViewById(R.id.textView32fdfd).setOnClickListener { inc() }

        findViewById(R.id.textView32).setOnClickListener { dec() }
    }

    private fun initElements() {
        Image.loadPhoto(R.drawable.back1, findViewById(R.id.imageView21) as ImageView)

        count = intent.getIntExtra(EXTRA_COUNT, 0)
        price = intent.getIntExtra(EXTRA_PRICE, 0)

        finalPrice = price * count

        (findViewById(R.id.textView29) as TextView).text = intent.getStringExtra(EXTRA_NAME)

        setText()
    }

    fun setText() {
        (findViewById(R.id.textView31) as TextView).text = getString(R.string.current_price) + finalPrice + getString(R.string.ruble_sign)
        (findViewById(R.id.textView30) as TextView).text = Integer.toString(count)
    }

    companion object {
        private val EXTRA_COUNT = "count"
        private val EXTRA_PRICE = "price"
        private val EXTRA_NAME = "name"
        private val EXTRA_INDEX = "index"
    }
}
