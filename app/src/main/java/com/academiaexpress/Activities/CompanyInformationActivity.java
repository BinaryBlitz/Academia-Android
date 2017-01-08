package com.academiaexpress.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.academiaexpress.R;

public class CompanyInformationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rek_layout);

        findViewById(R.id.drawer_indicator).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}