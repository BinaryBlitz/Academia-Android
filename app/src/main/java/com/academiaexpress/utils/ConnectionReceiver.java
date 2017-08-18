package com.academiaexpress.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.academiaexpress.ui.BaseActivity;
import com.academiaexpress.ui.main.LostInternetConnectionActivity;

public class ConnectionReceiver extends BroadcastReceiver {
    public ConnectionReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
            if (!isConnected) {
                if (!LostInternetConnectionActivity.Companion.getOpened()) {
                    if (BaseActivity.Companion.getForeground()) {
                        Intent intent1 = new Intent(context, LostInternetConnectionActivity.class);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent1);
                        LostInternetConnectionActivity.Companion.setOpened(true);
                    }
                }
            }
        }
    }
}
