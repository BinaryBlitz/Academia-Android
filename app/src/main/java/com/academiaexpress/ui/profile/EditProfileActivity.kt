package com.academiaexpress.ui.profile

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import com.academiaexpress.R
import com.academiaexpress.data.User
import com.academiaexpress.network.DeviceInfoStore
import com.academiaexpress.network.ServerApi
import com.academiaexpress.ui.BaseActivity
import com.academiaexpress.ui.main.ProductsActivity
import com.academiaexpress.ui.main.StartActivity
import com.academiaexpress.utils.AndroidUtilities
import com.academiaexpress.utils.Image
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditProfileActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        initElements()
        setOnClickListeners()
        loadInfo()
    }

    private fun initElements() {
        Image.loadPhoto(R.drawable.back1, findViewById(R.id.background) as ImageView)
    }

    private fun setOnClickListeners() {
        findViewById(R.id.save).setOnClickListener(View.OnClickListener {
            if (!AndroidUtilities.isConnected(this@EditProfileActivity)) {
                return@OnClickListener
            }
            if (check()) {
                updateUser(userFromFields, false)
            }
        })

        findViewById(R.id.logOut).setOnClickListener { logOut() }

        findViewById(R.id.guillotine_hamburger).setOnClickListener { finish() }
    }

    private fun logOut() {
        DeviceInfoStore.resetUser(this@EditProfileActivity)
        DeviceInfoStore.resetToken(this@EditProfileActivity)

        updateUser(generateLogOutJson(), true)
    }

    private fun generateLogOutJson(): JsonObject {
        val obj = JsonObject()
        val user = JsonObject()

        user.addProperty("device_token", "")
        user.addProperty("platform", "")
        obj.add("user", user)

        return user
    }

    private fun parse(flag: Boolean) {
        if (flag) {
            openStartActivity()
        } else {
            finish()
        }
    }

    private fun openStartActivity() {
        val intent = Intent(this@EditProfileActivity, StartActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun openProductsActivity() {
        val intent = Intent(this@EditProfileActivity, ProductsActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun updateUser(obj: JsonObject, flag: Boolean) {
        val dialog = ProgressDialog(this@EditProfileActivity)
        dialog.show()

        ServerApi.get(this).api().updateUser(obj, DeviceInfoStore.getToken(this)).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                dialog.dismiss()
                parse(flag)
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                dialog.dismiss()
                onInternetConnectionError()
            }
        })
    }

    fun check(): Boolean {
        if ((findViewById(R.id.firstName) as EditText).text.toString().isEmpty()) {
            Snackbar.make(findViewById(R.id.main), R.string.name_error, Snackbar.LENGTH_SHORT).show()
            return false
        } else if ((findViewById(R.id.lastName) as EditText).text.toString().isEmpty()) {
            Snackbar.make(findViewById(R.id.main), R.string.lastname_error, Snackbar.LENGTH_SHORT).show()
            return false
        } else if (!isValidEmail((findViewById(R.id.email) as EditText).text.toString())) {
            Snackbar.make(findViewById(R.id.main), R.string.email_error, Snackbar.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    val userFromFields: JsonObject
        get() {
            val obj = JsonObject()
            val user = JsonObject()

            user.addProperty("first_name", (findViewById(R.id.firstName) as EditText).text.toString())
            user.addProperty("last_name", (findViewById(R.id.lastName) as EditText).text.toString())
            user.addProperty("email", (findViewById(R.id.email) as EditText).text.toString())
            obj.add("user", user)

            DeviceInfoStore.saveUser(this, User(
                    (findViewById(R.id.firstName) as EditText).text.toString(),
                    (findViewById(R.id.lastName) as EditText).text.toString(),
                    (findViewById(R.id.email) as EditText).text.toString(), ""))

            return obj
        }

    fun loadInfo() {
        if (DeviceInfoStore.getUser(this) == "null") {
            return
        }

        val myProfile = User.fromString(DeviceInfoStore.getUser(this)) ?: return

        (findViewById(R.id.firstName) as EditText).setText(myProfile.firstName)
        (findViewById(R.id.lastName) as EditText).setText(myProfile.lastName)
        (findViewById(R.id.email) as EditText).setText(myProfile.email)
    }

    fun isValidEmail(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }
}