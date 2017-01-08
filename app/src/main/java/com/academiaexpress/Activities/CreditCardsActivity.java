package com.academiaexpress.Activities;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.academiaexpress.Adapters.CardsAdapter;
import com.academiaexpress.Base.BaseActivity;
import com.academiaexpress.R;
import com.academiaexpress.Utils.Image;

public class CreditCardsActivity extends BaseActivity {

    CardsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit_cards);
        Image.loadPhoto(R.drawable.back1, (ImageView) findViewById(R.id.imageView21));

        findViewById(R.id.guillotine_hamburger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        RecyclerView view = (RecyclerView) findViewById(R.id.recyclerView);
        view.setItemAnimator(new DefaultItemAnimator());
        view.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CardsAdapter(this);
        view.setAdapter(adapter);
        adapter.setCollection(DeliveryFinalActivity.collection);
        adapter.notifyDataSetChanged();

        findViewById(R.id.editText3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeliveryFinalActivity.newCard = true;
                finish();
            }
        });

        findViewById(R.id.editText35).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeliveryFinalActivity.newCard = false;
                finish();
            }
        });
    }
}
