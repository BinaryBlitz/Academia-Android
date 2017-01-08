package com.academiaexpress.Activities;

import com.google.gson.JsonObject;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.academiaexpress.Adapters.DeliveryPastAdapter;
import com.academiaexpress.Base.BaseActivity;
import com.academiaexpress.Data.DeliveryOrder;
import com.academiaexpress.R;
import com.academiaexpress.Server.DeviceInfoStore;
import com.academiaexpress.Server.ServerApi;
import com.academiaexpress.Utils.Image;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailsActivity extends BaseActivity {

    public static DeliveryOrder order;
    DeliveryPastAdapter adapter;
    int rate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_desc_layout);

        Image.loadPhoto(R.drawable.back1, (ImageView) findViewById(R.id.imageView21));

        RecyclerView view = (RecyclerView) findViewById(R.id.recyclerView);
        view.setItemAnimator(new DefaultItemAnimator());
        view.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.imageView23).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ImageView) findViewById(R.id.imageView23)).setImageResource(R.drawable.fill_star);

                ((ImageView) findViewById(R.id.imageView234)).setImageResource(R.drawable.star);
                ((ImageView) findViewById(R.id.imageView231)).setImageResource(R.drawable.star);
                ((ImageView) findViewById(R.id.imageView232)).setImageResource(R.drawable.star);
                ((ImageView) findViewById(R.id.imageView233)).setImageResource(R.drawable.star);
                rate = 1;
            }
        });

        findViewById(R.id.imageView233).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ImageView) findViewById(R.id.imageView23)).setImageResource(R.drawable.fill_star);
                ((ImageView) findViewById(R.id.imageView233)).setImageResource(R.drawable.fill_star);

                ((ImageView) findViewById(R.id.imageView234)).setImageResource(R.drawable.star);
                ((ImageView) findViewById(R.id.imageView231)).setImageResource(R.drawable.star);
                ((ImageView) findViewById(R.id.imageView232)).setImageResource(R.drawable.star);
                rate = 2;
            }
        });

        findViewById(R.id.imageView232).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ImageView) findViewById(R.id.imageView23)).setImageResource(R.drawable.fill_star);
                ((ImageView) findViewById(R.id.imageView233)).setImageResource(R.drawable.fill_star);
                ((ImageView) findViewById(R.id.imageView232)).setImageResource(R.drawable.fill_star);

                ((ImageView) findViewById(R.id.imageView234)).setImageResource(R.drawable.star);
                ((ImageView) findViewById(R.id.imageView231)).setImageResource(R.drawable.star);
                rate = 3;
            }
        });


        findViewById(R.id.imageView231).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ImageView) findViewById(R.id.imageView23)).setImageResource(R.drawable.fill_star);
                ((ImageView) findViewById(R.id.imageView233)).setImageResource(R.drawable.fill_star);
                ((ImageView) findViewById(R.id.imageView232)).setImageResource(R.drawable.fill_star);
                ((ImageView) findViewById(R.id.imageView231)).setImageResource(R.drawable.fill_star);

                ((ImageView) findViewById(R.id.imageView234)).setImageResource(R.drawable.star);

                rate = 4;
            }
        });

        findViewById(R.id.imageView234).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ImageView) findViewById(R.id.imageView23)).setImageResource(R.drawable.fill_star);
                ((ImageView) findViewById(R.id.imageView233)).setImageResource(R.drawable.fill_star);
                ((ImageView) findViewById(R.id.imageView232)).setImageResource(R.drawable.fill_star);
                ((ImageView) findViewById(R.id.imageView231)).setImageResource(R.drawable.fill_star);
                ((ImageView) findViewById(R.id.imageView234)).setImageResource(R.drawable.fill_star);

                rate = 5;
            }
        });

        int price = 0;

        if (order.getRating() == 0) {
            ((ImageView) findViewById(R.id.imageView23)).setImageResource(R.drawable.star);
            ((ImageView) findViewById(R.id.imageView234)).setImageResource(R.drawable.star);
            ((ImageView) findViewById(R.id.imageView231)).setImageResource(R.drawable.star);
            ((ImageView) findViewById(R.id.imageView232)).setImageResource(R.drawable.star);
            ((ImageView) findViewById(R.id.imageView233)).setImageResource(R.drawable.star);
            rate = 0;
        } else if (order.getRating() == 1) {
            ((ImageView) findViewById(R.id.imageView23)).setImageResource(R.drawable.fill_star);

            ((ImageView) findViewById(R.id.imageView234)).setImageResource(R.drawable.star);
            ((ImageView) findViewById(R.id.imageView231)).setImageResource(R.drawable.star);
            ((ImageView) findViewById(R.id.imageView232)).setImageResource(R.drawable.star);
            ((ImageView) findViewById(R.id.imageView233)).setImageResource(R.drawable.star);
            rate = 1;
        } else if (order.getRating() == 2) {
            ((ImageView) findViewById(R.id.imageView23)).setImageResource(R.drawable.fill_star);
            ((ImageView) findViewById(R.id.imageView233)).setImageResource(R.drawable.fill_star);

            ((ImageView) findViewById(R.id.imageView234)).setImageResource(R.drawable.star);
            ((ImageView) findViewById(R.id.imageView231)).setImageResource(R.drawable.star);
            ((ImageView) findViewById(R.id.imageView232)).setImageResource(R.drawable.star);
            rate = 2;
        } else if (order.getRating() == 3) {
            ((ImageView) findViewById(R.id.imageView23)).setImageResource(R.drawable.fill_star);
            ((ImageView) findViewById(R.id.imageView233)).setImageResource(R.drawable.fill_star);
            ((ImageView) findViewById(R.id.imageView232)).setImageResource(R.drawable.fill_star);

            ((ImageView) findViewById(R.id.imageView234)).setImageResource(R.drawable.star);
            ((ImageView) findViewById(R.id.imageView231)).setImageResource(R.drawable.star);
            rate = 3;
        } else if (order.getRating() == 4) {
            ((ImageView) findViewById(R.id.imageView23)).setImageResource(R.drawable.fill_star);
            ((ImageView) findViewById(R.id.imageView233)).setImageResource(R.drawable.fill_star);
            ((ImageView) findViewById(R.id.imageView232)).setImageResource(R.drawable.fill_star);
            ((ImageView) findViewById(R.id.imageView231)).setImageResource(R.drawable.fill_star);

            ((ImageView) findViewById(R.id.imageView234)).setImageResource(R.drawable.star);

            rate = 4;
        } else if (order.getRating() == 5) {
            ((ImageView) findViewById(R.id.imageView23)).setImageResource(R.drawable.fill_star);
            ((ImageView) findViewById(R.id.imageView233)).setImageResource(R.drawable.fill_star);
            ((ImageView) findViewById(R.id.imageView232)).setImageResource(R.drawable.fill_star);
            ((ImageView) findViewById(R.id.imageView231)).setImageResource(R.drawable.fill_star);
            ((ImageView) findViewById(R.id.imageView234)).setImageResource(R.drawable.fill_star);

            rate = 5;
        }


        for (int i = 0; i < order.getParts().size(); i++) {
            price += order.getParts().get(i).getPrice();
        }

        ((TextView) findViewById(R.id.textView62)).setText(order.getAddress());

        if (order.getPrice() > price) findViewById(R.id.textView50).setVisibility(View.VISIBLE);
        else findViewById(R.id.textView50).setVisibility(View.GONE);

        findViewById(R.id.textView7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendReview();
            }
        });

        adapter = new DeliveryPastAdapter(this);
        adapter.setInc(false);
        adapter.setCollection(order.getParts());

        view.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        ((TextView) findViewById(R.id.textView35)).setText(getIntent().getStringExtra("price"));

        findViewById(R.id.guillotine_hamburger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (order.isReviewd()) sendReview();
                else finish();
            }
        });

        check();
    }

    public void check() {
        if (order.isOnTheWay()) {
            findViewById(R.id.comment).setVisibility(View.GONE);
        } else if (order.isReviewd()) {
            ((EditText) findViewById(R.id.editText7)).setText(order.getReview());
            findViewById(R.id.editText7).setClickable(false);
            findViewById(R.id.editText7).setEnabled(false);
            findViewById(R.id.editText7).setFocusable(false);
            findViewById(R.id.editText7).setHovered(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (order.isReviewd()) sendReview();
        else OrderDetailsActivity.super.onBackPressed();
    }

    private JsonObject generateReview() {
        JsonObject object = new JsonObject();
        object.addProperty("rating", rate);

        final JsonObject toSend = new JsonObject();
        toSend.add("order", object);

        return toSend;
    }

    private void parseReview() {
        if (((EditText) findViewById(R.id.editText7)).getText().toString().length() != 0) {
            findViewById(R.id.editText7).setClickable(false);
            findViewById(R.id.editText7).setEnabled(false);
            findViewById(R.id.editText7).setFocusable(false);
            findViewById(R.id.editText7).setHovered(true);
        }

        Snackbar.make(findViewById(R.id.main), R.string.note_sent, Snackbar.LENGTH_SHORT).show();
        finish();
    }

    private void sendReview() {
        if (rate == 0) return;

        final ProgressDialog dialog = new ProgressDialog();
        dialog.show();

        ServerApi.get(this).api().note(generateReview(), order.getId(), DeviceInfoStore.getToken(this)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                dialog.dismiss();
                if (response.isSuccessful()) parseReview();
                else onInternetConnectionError();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                dialog.dismiss();
                onInternetConnectionError();
            }
        });
    }
}
