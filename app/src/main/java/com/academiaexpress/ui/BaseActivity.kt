package com.academiaexpress.ui

import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import com.academiaexpress.R

open class BaseActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        foreground = true
    }

    override fun onPause() {
        super.onPause()
        foreground = false
    }

    fun onInternetConnectionError() {
        Snackbar.make(findViewById(R.id.main), R.string.lost_connection_str, Snackbar.LENGTH_SHORT).show()
    }

    fun onLocationError() {
        Snackbar.make(findViewById(R.id.main), R.string.loca_error, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        var foreground = false
    }
}
