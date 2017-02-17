package com.academiaexpress.Activities

import android.app.ProgressDialog
import com.google.gson.JsonObject

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView

import com.academiaexpress.Base.BaseActivity
import com.academiaexpress.R
import com.academiaexpress.Server.DeviceInfoStore
import com.academiaexpress.Server.ServerApi
import com.academiaexpress.Utils.AndroidUtilities
import com.academiaexpress.Utils.CodeTimer
import com.academiaexpress.Utils.Image
import com.academiaexpress.Utils.LogUtil

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CodeActivity : BaseActivity() {
    internal var helperTextView: TextView? = null
    internal val myHandler = Handler()

    private val REPEAT_STR = "repeat"
    private val EXTRA_PHONE = "phone"
    private val EXTRA_TOKEN = "token"
    private val EXTRA_FIRST = "first"

    private val SLEEP_TIME = 1000

    internal val myRunnable: Runnable = Runnable {
        str = getString(R.string.send_code_again_after) + (milis.toDouble() / SLEEP_TIME.toDouble()).toInt()
        if (milis < 2 * SLEEP_TIME) {
            str = REPEAT_STR
        }
    }

    private fun UpdateGUI() {
        myHandler.post(myRunnable)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify)
        initElements()
        setOnClickListeners()
        initTimer()
        startAuthProcess()

        phone = intent.getStringExtra(EXTRA_PHONE)
    }

    private fun startAuthProcess() {
        if (phone != intent.getStringExtra(EXTRA_PHONE)) {
            startTimer()
            Handler().post { auth() }
        }
    }

    private fun initElements() {
        Image.loadPhoto(R.drawable.back1, findViewById(R.id.background) as ImageView)
        helperTextView = findViewById(R.id.resend_textView) as TextView
    }

    private fun setOnClickListeners() {
        findViewById(R.id.next_btn).setOnClickListener(View.OnClickListener {
            if (!AndroidUtilities.isConnected(this@CodeActivity)) {
                return@OnClickListener
            }

            verify()
        })

        findViewById(R.id.resend_textView).setOnClickListener(View.OnClickListener {
            if (!AndroidUtilities.isConnected(this@CodeActivity)) {
                return@OnClickListener
            }
            Handler().post {
                Handler().post { auth() }
                startTimer()
            }
        })

        findViewById(R.id.guillotine_hamburger).setOnClickListener { finish() }
    }

    private fun startTimer() {
        CodeTimer.reset()
        CodeTimer.with(object : CodeTimer.OnTimer {
            override fun onTick(millisUntilFinished: Long) {
                milis = millisUntilFinished
                UpdateGUI()
            }

            override fun onFinish() { }
        })

        CodeTimer.start()
    }

    private fun initTimer() {
        val t = object : Thread() {
            override fun run() {
                try {
                    while (!isInterrupted) {
                        Thread.sleep(SLEEP_TIME.toLong())
                        runOnUiThread {
                            if (str == REPEAT_STR) {
                                activateSendAgainButton()
                            } else {
                                helperTextView!!.text = str
                            }

                            helperTextView!!.isClickable = milis < 2 * SLEEP_TIME
                        }
                    }
                } catch (e: InterruptedException) {
                    LogUtil.logException(e)
                }

            }
        }

        t.start()
    }

    private fun activateSendAgainButton() {
        val content = SpannableString(getString(R.string.send_code_again))
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        helperTextView!!.text = content
    }

    private fun authResponse(obj: JsonObject) {
        token = AndroidUtilities.getStringFieldFromJson(obj.get("token"))
        phoneFromServer = AndroidUtilities.getStringFieldFromJson(obj.get("phone_number"))
    }

    private fun auth() {
        ServerApi.get(this).api().authWithPhoneNumber(phone).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    authResponse(response.body())
                } else {
                    onInternetConnectionError()
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                onInternetConnectionError()
            }
        })
    }

    private fun parseVerify(obj: JsonObject) {
        if (obj.get("api_token").isJsonNull) {
            openProfileActivity()
        } else {
            processToken(obj.get("api_token").asString)
        }
    }

    private fun processToken(apiToken: String) {
        DeviceInfoStore.saveToken(this, apiToken)
        openSplashActivity()
    }

    private fun openSplashActivity() {
        val intent = Intent(this@CodeActivity, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun openProfileActivity() {
        val intent = Intent(this@CodeActivity, CreateAccountActivity::class.java)
        intent.putExtra(EXTRA_FIRST, true)
        intent.putExtra(EXTRA_TOKEN, token)
        intent.putExtra(EXTRA_PHONE, getIntent().getStringExtra(EXTRA_PHONE))
        startActivity(intent)
        finish()
    }

    private fun verify() {
        val dialog = ProgressDialog(this)
        dialog.show()

        ServerApi.get(this).api().verify(token, (findViewById(R.id.code_editText) as EditText).text.toString()).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                dialog.dismiss()
                if (response.isSuccessful) {
                    parseVerify(response.body())
                } else {
                    onInternetConnectionError()
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                dialog.dismiss()
                onInternetConnectionError()
            }
        })
    }

    companion object {
        internal var phone = ""
        internal var milis: Long = 0
        internal var token: String = ""
        internal var phoneFromServer: String = ""
        internal var str = ""
    }
}
