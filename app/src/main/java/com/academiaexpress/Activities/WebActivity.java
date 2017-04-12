package com.academiaexpress.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.academiaexpress.Base.BaseActivity;
import com.academiaexpress.R;

public class WebActivity extends BaseActivity {
    private static final String SUCCESS = "sakses";
    private static final String FAILURE = "feylur";
    private static final String EXTRA_URL = "url";
    private static final String EXTRA_NEW_CARD = "newCard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        WebView webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

        webView.loadUrl(getIntent().getStringExtra(EXTRA_URL));

        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                TimeActivity.errors = !url.contains(SUCCESS);
                TimeActivity.isPaymentStarted = true;
                if (url.contains(SUCCESS)) {
                    openProcessActivity();
                } else {
                    finish();
                }

                return false;
            }
        });
    }

    private void openProcessActivity() {
        Intent intent = new Intent(WebActivity.this, DeliveryProcessActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        TimeActivity.errors = true;
        TimeActivity.isPaymentStarted = true;
    }
}
