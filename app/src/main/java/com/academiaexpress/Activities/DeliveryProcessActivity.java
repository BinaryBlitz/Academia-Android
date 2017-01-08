package com.academiaexpress.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.Html;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.academiaexpress.Base.BaseActivity;
import com.academiaexpress.Data.DeliveryOrder;
import com.academiaexpress.R;
import com.academiaexpress.Utils.Image;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

public class DeliveryProcessActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delivery_process_layout);

        Image.loadPhoto(R.drawable.load_pic, (ImageView) findViewById(R.id.imageView17));
        Answers.getInstance().logCustom(new CustomEvent("Заказ оплачен"));

        findViewById(R.id.textView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeliveryProcessActivity.this, OrderDetailsActivity.class);
                OrderDetailsActivity.order = new DeliveryOrder(null, ProductsActivity.price, ProductsActivity.collection, TimeActivity.id);
                OrderDetailsActivity.order.setOnTheWay(true);
                intent.putExtra("price", "Заказ на сумму: " + ProductsActivity.price + Html.fromHtml("<html>&#x20bd</html>").toString());
                startActivity(intent);
            }
        });

        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_anim);
        findViewById(R.id.imageView19).startAnimation(animation);

        findViewById(R.id.textViewfd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "tel:" + "88001001426";
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse(uri));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(DeliveryProcessActivity.this,
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

        MapActivity.selected_final = "";
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(ClosedActivity.closed) {
            Intent intent = new Intent(this, ClosedActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(this, StartActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 2: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    String uri = "tel:" + "88001001426";
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse(uri));
                    startActivity(intent);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
