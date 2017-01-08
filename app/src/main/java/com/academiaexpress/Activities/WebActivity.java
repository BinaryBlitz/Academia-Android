package com.academiaexpress.Activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.academiaexpress.Base.BaseActivity;
import com.academiaexpress.R;

public class WebActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_layout);

        WebView webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setBuiltInZoomControls(true);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            webView.getSettings().setDisplayZoomControls(false);
        }

        webView.loadUrl(getIntent().getStringExtra("url"));

        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                TimeActivity.errors = !url.contains("sakses");

                if(url.contains("sakses") || url.contains("feylur")) {
                    if(getIntent().getBooleanExtra("newCard", false)) {
                        Intent intent = new Intent(WebActivity.this, DeliveryProcessActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    finish();
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        TimeActivity.errors = true;
    }
}
