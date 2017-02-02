package com.academiaexpress.Activities;

import com.google.gson.JsonObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.academiaexpress.Base.BaseActivity;
import com.academiaexpress.Data.DeliveryUser;
import com.academiaexpress.Fragments.FinalPageFragment;
import com.academiaexpress.R;
import com.academiaexpress.Server.DeviceInfoStore;
import com.academiaexpress.Server.ServerApi;
import com.academiaexpress.Utils.AndroidUtilities;
import com.academiaexpress.Utils.Animations;
import com.academiaexpress.Utils.Image;
import com.academiaexpress.Utils.MoneyValues;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StartActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        ClosedActivity.closed = false;

        try { FinalPageFragment.Companion.getCollection().clear(); } catch (Exception ignored) { }

        Image.loadPhoto(R.drawable.back_final_page, (ImageView) findViewById(R.id.imageView9));

        initScreen();
        setOnClickListeners();

        findViewById(R.id.menu_layout).setVisibility(View.GONE);
    }

    private void parseUser(JsonObject object) {
        MoneyValues.balance = object.get("balance").getAsInt();
        MoneyValues.promocode = object.get("promo_code").getAsString();
        MoneyValues.discount = object.get("discount").getAsInt();
        MoneyValues.promoUsed = object.get("promo_used").getAsBoolean();
        if(MoneyValues.countOfOrders == 0) {
            findViewById(R.id.textView19fd).setVisibility(View.GONE);
        } else {
            findViewById(R.id.textView19fd).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.textView19fd)).setText(Integer.toString(MoneyValues.countOfOrders));
            ((TextView) findViewById(R.id.textView6)).setText(
                    "ЗАКАЗЫ" + " (" + Integer.toString(MoneyValues.countOfOrders) + ")");
        }

        DeliveryUser deliveryUser = new DeliveryUser(
                object.get("first_name").getAsString(),
                object.get("last_name").getAsString(),
                object.get("email").getAsString(),
                object.get("phone_number").getAsString()
        );

        DeviceInfoStore.saveUser(this, deliveryUser);
    }

    private void getUser() {
        ServerApi.get(this).api().getUser(DeviceInfoStore.getToken(this)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) parseUser(response.body());
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) { }
        });
    }

    private void initScreen() {
        if (!DeviceInfoStore.getToken(this).equals("null")) {
            Answers.getInstance().logCustom(new CustomEvent("Вход в приложение"));
            initIfLoggedIn();
        } else {
            Answers.getInstance().logCustom(new CustomEvent("Пользовательская авторизация"));
            initNotLoggedIn();
        }

        initOrders();
    }

    private void initOrders() {
        if (MoneyValues.countOfOrders == 0) {
            findViewById(R.id.textView19fd).setVisibility(View.GONE);
        } else {
            findViewById(R.id.textView19fd).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.textView19fd)).setText(Integer.toString(MoneyValues.countOfOrders));
            ((TextView) findViewById(R.id.textView6)).setText(
                    "ЗАКАЗЫ" + " (" + Integer.toString(MoneyValues.countOfOrders) + ")");
        }
    }

    private void initIfLoggedIn() {
        Answers.getInstance().logCustom(new CustomEvent("Вход в приложение"));
        ((TextView) findViewById(R.id.textView)).setText("ПОСМОТРЕТЬ МЕНЮ");
        findViewById(R.id.textView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!AndroidUtilities.INSTANCE.isConnected(StartActivity.this)) return;
                Intent intent = new Intent(StartActivity.this, ProductsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        getUser();
    }

    private void initNotLoggedIn() {
        Answers.getInstance().logCustom(new CustomEvent("Пользовательская авторизация"));
        findViewById(R.id.content_hamburger).setVisibility(View.GONE);
        findViewById(R.id.textView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, AuthActivity.class);
                startActivity(intent);
                finish();
            }
        });

        MoneyValues.balance = 0;
        MoneyValues.countOfOrders = 0;
        MoneyValues.discount = 0;
        MoneyValues.promoUsed = true;
    }

    private void setOnClickListeners() {
        findViewById(R.id.textView2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, EditProfileActivity.class);
                intent.putExtra("first", false);
                startActivity(intent);
            }
        });

        findViewById(R.id.textView5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, HelpActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.textView6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, OrdersActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.content_hamburger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        Animations.animateRevealShow(findViewById(R.id.menu_layout), StartActivity.this);
                    }
                });
            }
        });

        findViewById(R.id.guillotine_hamburger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animations.animateRevealHide(findViewById(R.id.menu_layout));
            }
        });

    }
}
