package com.academiaexpress.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.academiaexpress.Base.BaseActivity
import com.academiaexpress.R
import com.academiaexpress.Server.DeviceInfoStore
import com.academiaexpress.Server.ServerApi
import com.academiaexpress.Utils.AndroidUtilities
import com.academiaexpress.Utils.Image
import com.academiaexpress.Utils.MoneyValues
import com.google.gson.JsonArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FirstDeliveryScreen : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_delivery)

        initElements()
        setOnClickListeners()
    }

    private fun initElements() {
        Image.loadPhoto(R.drawable.back1, findViewById(R.id.imageView21) as ImageView)

        (findViewById(R.id.textView8) as TextView).text =
                getString(R.string.congratulations) + intent.getStringExtra("name") + getString(R.string.you_logged_in)
    }

    private fun setOnClickListeners() {
        findViewById(R.id.textView).setOnClickListener { getOrders() }
    }

    private fun getOrders() {
        val dialog = ProgressDialog(this)
        dialog.show()

        ServerApi.get(this).api().getOrders(DeviceInfoStore.getToken(this)).enqueue(object : Callback<JsonArray> {
            override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                dialog.dismiss()
                if (response.isSuccessful) parseOrders(response.body())
            }

            override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                dialog.dismiss()
            }
        })
    }

    private fun openSplash() {
        val intent = Intent(this@FirstDeliveryScreen, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun parseOrders(array: JsonArray) {
        MoneyValues.countOfOrders = 0
        (0..array.size() - 1)
                .map { array.get(it).asJsonObject.get("order").asJsonObject }
                .filter { AndroidUtilities.getStringFieldFromJson(it.get("status")) == "on_the_way" }
                .forEach { MoneyValues.countOfOrders++ }

        openSplash()
    }
}
