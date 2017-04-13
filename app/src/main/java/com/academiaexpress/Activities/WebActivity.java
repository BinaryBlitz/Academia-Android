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
                if (url.contains(SUCCESS)) {
                    if (DeliveryFinalActivity.newCard) {
                        TimeActivity.isPaymentStarted = false;
                        finish();
                    } else {
                        openProcessActivity();
                    }
                } else if (url.contains(FAILURE)) {
                    TimeActivity.isPaymentStarted = true;
                    finish();
                }

                return false;
            }
        });
    }

    private void parseUrl(String url) {
        TimeActivity.errors = !url.contains(SUCCESS);

        if (url.contains(SUCCESS)) {
            parseSuccess();
        } else if (url.contains(FAILURE)) {
            TimeActivity.isPaymentStarted = true;
            finish();
        }
    }

    private void parseSuccess() {
        TimeActivity.isPaymentStarted = false;
        if (DeliveryFinalActivity.newCard) {
            finish();
        } else {
            openProcessActivity();
        }
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
