package com.academiaexpress.Activities;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.academiaexpress.Base.BaseActivity;
import com.academiaexpress.Custom.ProgressDialog;
import com.academiaexpress.R;
import com.academiaexpress.Server.DeviceInfoStore;
import com.academiaexpress.Server.ServerApi;
import com.academiaexpress.Utils.Image;
import com.academiaexpress.Utils.MoneyValues;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FirstDeliveryScreen extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_delivery_layout);

        Image.loadPhoto(R.drawable.back1, (ImageView) findViewById(R.id.imageView21));

        ((TextView) findViewById(R.id.textView8)).setText("Поздравляем, " + getIntent().getStringExtra("name") +
            "\nВы зарегистрировались!");

        findViewById(R.id.textView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ProgressDialog dialog = new ProgressDialog();
                dialog.show(getFragmentManager(), "matesapp");

                getOrders();
            }
        });
    }

    private void getOrders() {
        ServerApi.get(this).api().getOrders(DeviceInfoStore.getToken(this)).enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if (response.isSuccessful()) parseOrders(response.body());
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) { }
        });
    }

    private void parseOrders(JsonArray array) {
        MoneyValues.countOfOrders = 0;
        for (int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).getAsJsonObject().get("order").getAsJsonObject();
            if (object.get("status").getAsString().equals("on_the_way")) MoneyValues.countOfOrders++;
        }

        Intent intent2 = new Intent(FirstDeliveryScreen.this, SplashActivity.class);
        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent2);
        finish();
    }
}
