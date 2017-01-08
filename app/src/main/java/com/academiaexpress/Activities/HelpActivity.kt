package com.academiaexpress.Activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.widget.ImageView
import android.widget.TextView
import com.academiaexpress.Base.BaseActivity
import com.academiaexpress.R
import com.academiaexpress.Utils.AndroidUtilities
import com.academiaexpress.Utils.AppConfig
import com.academiaexpress.Utils.Image

class HelpActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        initElements()
        setOnClickListeners()
    }

    private fun initElements() {
        Image.loadPhoto(R.drawable.back1, findViewById(R.id.imageView21) as ImageView)
        setPhoneText()
    }

    private fun setPhoneText() {
        val content = SpannableString(AppConfig.phoneStr)
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        (findViewById(R.id.editText3fd) as TextView).text = content
    }

    private fun sendEmail() {
        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", AppConfig.email, null))
        emailIntent.putExtra(Intent.EXTRA_TEXT, " ")
        startActivity(Intent.createChooser(emailIntent, getString(R.string.send)))
    }

    private fun setOnClickListeners() {
        findViewById(R.id.guillotine_hamburger).setOnClickListener { finish() }

        findViewById(R.id.editText3).setOnClickListener { sendEmail() }

        findViewById(R.id.textView56).setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.termsUrl))
            startActivity(browserIntent)
        }

        findViewById(R.id.editText3fd).setOnClickListener { AndroidUtilities.call(this@HelpActivity, AppConfig.phone) }

        findViewById(R.id.textView57).setOnClickListener {
            val intent = Intent(this@HelpActivity, CompanyInformationActivity::class.java)
            startActivity(intent)
        }
    }
}
