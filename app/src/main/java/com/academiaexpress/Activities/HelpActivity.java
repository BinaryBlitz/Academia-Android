package com.academiaexpress.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.academiaexpress.Base.BaseActivity;
import com.academiaexpress.R;
import com.academiaexpress.Utils.Image;

public class HelpActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_layout);

        Image.loadPhoto(R.drawable.back1, (ImageView) findViewById(R.id.imageView21));

        findViewById(R.id.guillotine_hamburger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        SpannableString content = new SpannableString("8-800-100-14-26");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        ((TextView) findViewById(R.id.editText3fd)).setText(content);

        findViewById(R.id.editText3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "a-akademiya@bk.ru", null));
                emailIntent.putExtra(Intent.EXTRA_TEXT, " ");
                startActivity(Intent.createChooser(emailIntent, "Отправить..."));
            }
        });

        findViewById(R.id.textView56).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://academia-delivery.herokuapp.com/docs/terms_of_service.html"));
                startActivity(browserIntent);
            }
        });

        findViewById(R.id.editText3fd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "tel:" + "88001001426";
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse(uri));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(HelpActivity.this,
                                new String[]{Manifest.permission.CALL_PHONE},
                                2);
                    } else {
                        startActivity(intent);
                    }
                } else {
                    startActivity(intent);
                }
            }
        });

        findViewById(R.id.textView57).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HelpActivity.this, CompanyInformationActivity.class);
                startActivity(intent);
            }
        });

    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 2: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String uri = "tel:" + "88001001426";
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse(uri));
                    startActivity(intent);
                }
            }
        }
    }
}
