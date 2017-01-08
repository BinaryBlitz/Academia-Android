package com.academiaexpress.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.academiaexpress.Base.BaseActivity;
import com.academiaexpress.R;
import com.academiaexpress.Utils.Image;

public class IncDecActivity extends BaseActivity {
    int count;
    int price;
    int all_price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_part_inc_dec_layout);

        Image.loadPhoto(R.drawable.back1, (ImageView) findViewById(R.id.imageView21));

        count = getIntent().getIntExtra("count", 0);
        price = getIntent().getIntExtra("price", 0);

        all_price = price * count;

        ((TextView) findViewById(R.id.textView29)).setText(getIntent().getStringExtra("name"));


        findViewById(R.id.textView32fdfd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                all_price += price;
                setText();
            }
        });

        findViewById(R.id.textView32).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (count == 1) return;

                count--;
                all_price -= price;
                setText();
            }
        });

        setText();

        findViewById(R.id.textView28fd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeliveryFinalActivity.INDEX = getIntent().getIntExtra("index", 0);
                DeliveryFinalActivity.newCount = count;
                finish();
            }
        });

        findViewById(R.id.textView28).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeliveryFinalActivity.INDEX = -2;
                DeliveryFinalActivity.r_INDEX = getIntent().getIntExtra("index", 0);
                finish();
            }
        });

        findViewById(R.id.guillotine_hamburger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void setText() {
        ((TextView) findViewById(R.id.textView31)).setText("Текущая цена: " + all_price + " Р");
        ((TextView) findViewById(R.id.textView30)).setText(Integer.toString(count));
    }
}
