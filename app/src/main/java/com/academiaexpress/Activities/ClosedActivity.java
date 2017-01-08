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
import com.academiaexpress.Utils.DateUtils;
import com.academiaexpress.Utils.Image;
import com.academiaexpress.Utils.LogUtil;
import com.academiaexpress.Utils.MoneyValues;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClosedActivity extends BaseActivity {

    public static boolean closed = false;
    public static String imageUrl = "";

    private static final String EXTRA_CLOSED = "closed";
    private static final String EXTRA_FIRST = "first";
    private static final int EARLY_HOUR = 6;

    private void parseUser(JsonObject object) {
        saveMoneyValues(object);
        setupUIForMoneyValues();
        saveUser(object);
    }

    private void setupUIForMoneyValues() {
        if (MoneyValues.countOfOrders == 0) setupIfEmptyOrder();
        else setupIfNotEmptyOrders();
    }

    private void setupIfEmptyOrder() {
        findViewById(R.id.textView19fd).setVisibility(View.GONE);
    }

    private void setupIfNotEmptyOrders() {
        findViewById(R.id.textView19fd).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.textView19fd)).setText(Integer.toString(MoneyValues.countOfOrders));
        ((TextView) findViewById(R.id.textView6)).setText(getString(R.string.orders_upcase) + " (" + Integer.toString(MoneyValues.countOfOrders) + ")");
    }

    private void saveMoneyValues(JsonObject object) {
        MoneyValues.balance = AndroidUtilities.INSTANCE.getIntFieldFromJson(object.get("balance"));
        MoneyValues.promocode = AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("promo_code"));
        MoneyValues.discount = AndroidUtilities.INSTANCE.getIntFieldFromJson(object.get("discount"));
        MoneyValues.promoUsed = AndroidUtilities.INSTANCE.getBooleanFieldFromJson(object.get("promo_used"));
    }

    private void saveUser(JsonObject object) {
        DeliveryUser deliveryUser = new DeliveryUser(
                AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("first_name")),
                AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("last_name")),
                AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("email")),
                AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("phone_number")));

        DeviceInfoStore.saveUser(this, deliveryUser);
    }

    private void getUser() {
        ServerApi.get(this).api().getUser(DeviceInfoStore.getToken(this)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) parseUser(response.body());
                else onInternetConnectionError();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                onInternetConnectionError();
            }
        });
    }

    private void initScreen() {
        try { FinalPageFragment.collection.clear(); } catch (Exception e) { LogUtil.logException(e); }

        closed = true;
        Answers.getInstance().logCustom(new CustomEvent(getString(R.string.event_sign_in)));

        if (getIntent().getBooleanExtra(EXTRA_CLOSED, false)) findViewById(R.id.textView).setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_closed);

        initScreen();
        setupUIForMoneyValues();
        initElements();
        loadBackground();
        setOnClickListeners();
        getUser();
        setTexts();
    }

    private void setTexts() {
        setUpperText();
        setBottomText();
    }

    private void setUpperText() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);

        if (hour < EARLY_HOUR) setTextToUpperText(getString(R.string.hello_late));
        else setTextToUpperText(getString(R.string.hello_early));
    }

    private void setTextToUpperText(String text) {
        try {
            ((TextView) findViewById(R.id.textView36)).setText(text + ", " +
                    DeliveryUser.Companion.fromString(DeviceInfoStore.getUser(this)).getFirstName());
        } catch (Exception e) {
            ((TextView) findViewById(R.id.textView36)).setText(text);
        }
    }

    private void setBottomText() {
        try {
            if (isValidDate()) setClosedTextToBottomTextView();
            else setValidDateToBottomTextView();
        } catch (Exception e) { setClosedTextToBottomTextView(); }
    }

    private void setValidDateToBottomTextView() {
        ((TextView) findViewById(R.id.textView36hj)).setText(
                getString(R.string.now_closed_valid) +
                        DateUtils.INSTANCE.getCalendarDate(this, getIntent().getStringExtra("open_time"))
                        + getString(R.string.in_code) +
                        DateUtils.INSTANCE.getTimeStringRepresentation(getDateFromIntent()));
    }

    private void setClosedTextToBottomTextView() {
        ((TextView) findViewById(R.id.textView36hj)).setText(getString(R.string.now_closed_str));
    }

    private Date getDateFromIntent() {
        Calendar calendar = DateUtils.INSTANCE.getCalendarFromString(getIntent().getStringExtra("open_time"));
        if (calendar == null) return null;
        else return calendar.getTime();
    }

    private boolean isValidDate() {
        Calendar calendar = DateUtils.INSTANCE.getCalendarFromString(getIntent().getStringExtra("open_time"));
        return calendar != null && DateUtils.INSTANCE.getTimeStringRepresentation(calendar.getTime()).isEmpty();
    }

    private void initElements() {
        if (getIntent().getBooleanExtra("preorder", false)) findViewById(R.id.textView).setVisibility(View.VISIBLE);
        else findViewById(R.id.textView).setVisibility(View.GONE);

        findViewById(R.id.menu_layout).setVisibility(View.GONE);
    }

    private void loadBackground() {
        if (imageUrl.isEmpty()) setDefaultBackground();
        else setServerBackground();
    }

    private void setDefaultBackground() {
        Image.loadPhoto(R.drawable.back2, (ImageView) findViewById(R.id.imageView17));
    }

    private void setServerBackground() {
        findViewById(R.id.textView36).setVisibility(View.GONE);
        findViewById(R.id.textView36hj).setVisibility(View.GONE);
        Image.loadPhoto(imageUrl, (ImageView) findViewById(R.id.imageView17));
    }

    private void openProductsActivity() {
        Intent intent = new Intent(ClosedActivity.this, ProductsActivity.class);
        startActivity(intent);
        finish();
    }

    private void openEditProfileActivity() {
        Intent intent = new Intent(ClosedActivity.this, EditProfileActivity.class);
        intent.putExtra(EXTRA_FIRST, false);
        startActivity(intent);
    }

    private void openActivity(Class c) {
        Intent intent = new Intent(ClosedActivity.this, c);
        startActivity(intent);
    }

    private void setOnClickListeners() {
        findViewById(R.id.textView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!AndroidUtilities.INSTANCE.isConnected(ClosedActivity.this)) return;
                openProductsActivity();
            }
        });

        findViewById(R.id.textView2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditProfileActivity();
            }
        });

        findViewById(R.id.textView5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivity(HelpActivity.class);
            }
        });

        findViewById(R.id.textView6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivity(OrdersActivity.class);
            }
        });

        findViewById(R.id.content_hamburger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        Animations.animateRevealShow(findViewById(R.id.menu_layout), ClosedActivity.this);
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
