package com.academiaexpress.Activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.academiaexpress.R
import com.academiaexpress.Utils.Image

class LostInternetConnectionActivity : AppCompatActivity() {

    internal var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "android.net.conn.CONNECTIVITY_CHANGE") {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork = cm.activeNetworkInfo
                val isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting
                if (isConnected) {
                    opened = false
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lost_internet_connection)
        initElements()
    }

    private fun initElements() {
        Image.loadPhoto(R.drawable.load_pic, findViewById(R.id.imageView17) as ImageView)
        val animation = AnimationUtils.loadAnimation(this, R.anim.rotate_anim)
        findViewById(R.id.imageView19).startAnimation(animation)
    }

    public override fun onResume() {
        super.onResume()

        val filter = IntentFilter()
        filter.addAction(ACTION)

        this.registerReceiver(this.receiver, filter)
    }

    public override fun onPause() {
        super.onPause()
        this.unregisterReceiver(this.receiver)
    }

    override fun onBackPressed() { }

    companion object {
        var opened = false
        internal val ACTION = "android.net.conn.CONNECTIVITY_CHANGE"
    }
}
