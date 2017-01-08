package com.academiaexpress.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.academiaexpress.R;

public class LostInternetConnectionActivity extends AppCompatActivity {

    public static boolean opened = false;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                ConnectivityManager cm =
                        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();
                if (isConnected) {
                    opened = false;
                    finish();
                }
            }
        }
    };

    static final String ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    private static final String TAG = "ConnectionReceiver";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.no_inet_dialog);

        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_anim);
        findViewById(R.id.imageView19).startAnimation(animation);
    }

    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION);

        this.registerReceiver(this.receiver, filter);
    }

    public void onPause() {
        super.onPause();

        this.unregisterReceiver(this.receiver);
    }

    @Override
    public void onBackPressed() {
    }
}
