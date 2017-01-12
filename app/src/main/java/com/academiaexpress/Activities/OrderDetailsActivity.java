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
import android.widget.RatingBar;
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
    private RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        initElements();
        setOnClickListeners();
        processOrder();
        check();
    }

    private void processOrder() {
        if (order.getParts() == null) return;

        int price = 0;

        for (int i = 0; i < order.getParts().size(); i++) price += order.getParts().get(i).getPrice();

        ((TextView) findViewById(R.id.textView62)).setText(order.getAddress());

        if (order.getPrice() > price) findViewById(R.id.textView50).setVisibility(View.VISIBLE);
        else findViewById(R.id.textView50).setVisibility(View.GONE);

        ((TextView) findViewById(R.id.textView35)).setText(getIntent().getStringExtra("price"));

        ratingBar.setRating(order.getRating());
    }

    private void initRecyclerView() {
        RecyclerView view = (RecyclerView) findViewById(R.id.recyclerView);
        view.setItemAnimator(new DefaultItemAnimator());
        view.setLayoutManager(new LinearLayoutManager(this));
        view.setHasFixedSize(true);

        initAdapter(view);
    }

    private void initAdapter(RecyclerView view) {
        DeliveryPastAdapter adapter = new DeliveryPastAdapter(this);
        adapter.setInc(false);
        adapter.setCollection(order.getParts());

        view.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void initElements() {
        Image.loadPhoto(R.drawable.back1, (ImageView) findViewById(R.id.imageView21));
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        initRecyclerView();
    }

    private void setOnClickListeners() {
        findViewById(R.id.guillotine_hamburger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (order.isReviewd()) sendReview();
                else finish();
            }
        });

        findViewById(R.id.textView7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendReview();
            }
        });
    }

    private void disableEditText() {
        EditText editText = (EditText) findViewById(R.id.editText7);
        editText.setText(order.getReview());
        editText.setClickable(false);
        editText.setEnabled(false);
        editText.setFocusable(false);
        editText.setHovered(true);
    }

    private void hideCommentBox() {
        findViewById(R.id.comment).setVisibility(View.GONE);
    }

    public void check() {
        if (order.isOnTheWay()) hideCommentBox();
        else if (order.isReviewd()) disableEditText();
    }

    @Override
    public void onBackPressed() {
        if (order.isReviewd()) sendReview();
        else OrderDetailsActivity.super.onBackPressed();
    }

    private JsonObject generateReview() {
        JsonObject object = new JsonObject();
        object.addProperty("rating", (int) ratingBar.getRating());

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
        if (ratingBar.getRating() == 0f) return;

        final ProgressDialog dialog = new ProgressDialog(this);
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
