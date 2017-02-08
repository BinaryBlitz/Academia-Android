package com.academiaexpress.Activities

import com.google.gson.JsonObject

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.ImageView

import com.academiaexpress.Base.BaseActivity
import com.academiaexpress.Data.DeliveryUser
import com.academiaexpress.R
import com.academiaexpress.Server.DeviceInfoStore
import com.academiaexpress.Server.ServerApi
import com.academiaexpress.Utils.AndroidUtilities
import com.academiaexpress.Utils.Image
import com.google.firebase.iid.FirebaseInstanceId

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateAccountActivity : BaseActivity() {
    private val EXTRA_NAME = "name"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_profile)

        Image.loadPhoto(R.drawable.back1, findViewById(R.id.imageView21) as ImageView)
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        findViewById(R.id.guillotine_hamburger).setOnClickListener { finish() }

        findViewById(R.id.textView7).setOnClickListener(View.OnClickListener {
            if (!AndroidUtilities.isConnected(this@CreateAccountActivity)) {
                return@OnClickListener
            }
            if (check()) {
                createUser()
            }
        })
    }

    private fun parse() {
        val intent = Intent(this@CreateAccountActivity, FirstDeliveryScreen::class.java)
        intent.putExtra(EXTRA_NAME, (findViewById(R.id.editText2) as EditText).text.toString())
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun createUser() {
        val dialog = ProgressDialog(this@CreateAccountActivity)
        dialog.show()

        ServerApi.get(this).api().createUser(userFromFields).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                dialog.dismiss()
                if (response.isSuccessful) parse()
                else onInternetConnectionError()
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                dialog.dismiss()
                onInternetConnectionError()
            }
        })
    }

    fun check(): Boolean {
        if ((findViewById(R.id.editText2) as EditText).text.toString().isEmpty()) {
            Snackbar.make(findViewById(R.id.main), R.string.name_error, Snackbar.LENGTH_SHORT).show()
            return false
        } else if ((findViewById(R.id.editText) as EditText).text.toString().isEmpty()) {
            Snackbar.make(findViewById(R.id.main), R.string.lastname_error, Snackbar.LENGTH_SHORT).show()
            return false
        } else if (!isValidEmail((findViewById(R.id.editText3) as EditText).text.toString())) {
            Snackbar.make(findViewById(R.id.main), R.string.email_error, Snackbar.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    val userFromFields: JsonObject
        get() {
            val obj = JsonObject()
            val user = JsonObject()

            user.addProperty("first_name", (findViewById(R.id.editText2) as EditText).text.toString())
            user.addProperty("last_name", (findViewById(R.id.editText) as EditText).text.toString())
            user.addProperty("email", (findViewById(R.id.editText3) as EditText).text.toString())
            user.addProperty("phone_number", intent.getStringExtra("phone"))
            user.addProperty("verification_token", intent.getStringExtra("token"))
            user.addProperty("device_token", FirebaseInstanceId.getInstance().token)
            user.addProperty("platform", intent.getStringExtra("token"))

            val deliveryUser = DeliveryUser(
                    (findViewById(R.id.editText2) as EditText).text.toString(),
                    (findViewById(R.id.editText) as EditText).text.toString(),
                    (findViewById(R.id.editText3) as EditText).text.toString(),
                    "")

            DeviceInfoStore.saveUser(this, deliveryUser)

            obj.add("user", user)

            return obj
        }

    fun isValidEmail(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }
}
