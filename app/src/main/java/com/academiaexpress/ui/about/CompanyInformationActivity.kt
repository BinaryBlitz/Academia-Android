package com.academiaexpress.ui.about

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.academiaexpress.R

class CompanyInformationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_info)

        findViewById(R.id.drawer_indicator).setOnClickListener { finish() }
    }
}