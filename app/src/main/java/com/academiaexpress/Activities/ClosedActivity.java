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
import com.academiaexpress.Utils.LogUtil;
import com.academiaexpress.Utils.MoneyValues;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClosedActivity extends BaseActivity {

    public static boolean closed = false;

    public static String imageUrl = "";

    private void parseUser(JsonObject object) {
        MoneyValues.balance = object.get("balance").getAsInt();
        MoneyValues.promocode = object.get("promo_code").getAsString();
        MoneyValues.discount = object.get("discount").getAsInt();
        MoneyValues.promoUsed = object.get("promo_used").getAsBoolean();
        if (MoneyValues.countOfOrders == 0) {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_closed);

        try { FinalPageFragment.collection.clear(); } catch (Exception ignored) { }

        closed = true;
        Answers.getInstance().logCustom(new CustomEvent("Вход в приложение"));

        if (getIntent().getBooleanExtra("closed", false)) {
            findViewById(R.id.textView).setVisibility(View.GONE);
        }

        if (MoneyValues.countOfOrders == 0) {
            findViewById(R.id.textView19fd).setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.textView19fd)).setText(Integer.toString(MoneyValues.countOfOrders));
            ((TextView) findViewById(R.id.textView6)).setText("ЗАКАЗЫ" + " (" + Integer.toString(MoneyValues.countOfOrders) + ")");
        }

        if (imageUrl.isEmpty()) {
            Image.loadPhoto(R.drawable.back2, (ImageView) findViewById(R.id.imageView17));
        }
        else {
            findViewById(R.id.textView36).setVisibility(View.GONE);
            findViewById(R.id.textView36hj).setVisibility(View.GONE);
            Image.loadPhoto(imageUrl, (ImageView) findViewById(R.id.imageView17));
        }

        getUser();

        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);

        if (hour < 6) {
            try {
                LogUtil.logError("1");
                ((TextView) findViewById(R.id.textView36)).setText("Доброй ночи, " +
                        DeliveryUser.Companion.fromString(DeviceInfoStore.getUser(this)).getFirstName());
            } catch (Exception e) {
                LogUtil.logError("2");
                ((TextView) findViewById(R.id.textView36)).setText("Доброй ночи");
            }
        } else {
            try {
                LogUtil.logError("3");
                ((TextView) findViewById(R.id.textView36)).setText("Доброе утро, " +
                        DeliveryUser.Companion.fromString(DeviceInfoStore.getUser(this)).getFirstName());
            } catch (Exception e) {
                LogUtil.logError("4");
                ((TextView) findViewById(R.id.textView36)).setText("Доброе утро");
            }
        }

        try {
            if (getTimeDate().isEmpty()) {
                LogUtil.logError("5");
                ((TextView) findViewById(R.id.textView36hj)).setText("Сейчас мы закрыты.");
            } else {
                LogUtil.logError("6");
                ((TextView) findViewById(R.id.textView36hj)).setText(
                        "Сейчас мы закрыты. Мы открываемся " + getCalendarDate() + " в " + getTimeDate());
            }

        } catch (Exception e) {
            LogUtil.logError("7");
            ((TextView) findViewById(R.id.textView36hj)).setText("Сейчас мы закрыты.");
        }

        if (getIntent().getBooleanExtra("preorder", false)) {
            findViewById(R.id.textView).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.textView).setVisibility(View.GONE);
        }

        findViewById(R.id.textView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!AndroidUtilities.INSTANCE.isConnected(ClosedActivity.this)) return;
                Intent intent = new Intent(ClosedActivity.this, ProductsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        findViewById(R.id.textView2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClosedActivity.this, EditProfileActivity.class);
                intent.putExtra("first", false);
                startActivity(intent);
            }
        });

        findViewById(R.id.textView5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClosedActivity.this, HelpActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.textView6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClosedActivity.this, OrdersActivity.class);
                startActivity(intent);
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

        findViewById(R.id.menu_layout).setVisibility(View.GONE);

        if (MoneyValues.countOfOrders == 0) {
            findViewById(R.id.textView19fd).setVisibility(View.GONE);
        } else {
            findViewById(R.id.textView19fd).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.textView19fd)).setText(Integer.toString(MoneyValues.countOfOrders));
            ((TextView) findViewById(R.id.textView6)).setText(
                    "ЗАКАЗЫ" + " (" + Integer.toString(MoneyValues.countOfOrders) + ")");
        }
    }

    public static String getWeekString(int number) {
        String month = "";

        switch (number) {
            case Calendar.MONDAY:
                month = "в понедельник";
                break;
            case Calendar.TUESDAY:
                month = "во вторник";
                break;
            case Calendar.WEDNESDAY:
                month = "в среду";
                break;
            case Calendar.THURSDAY:
                month = "в четверг";
                break;
            case Calendar.FRIDAY:
                month = "в пятницу";
                break;
            case Calendar.SATURDAY:
                month = "в субботу";
                break;
            case Calendar.SUNDAY:
                month = "в воскресенье";
                break;
            default:
                return month;
        }

        return month;
    }

    public String getCalendarDate() {
        try {
            Calendar calendar = Calendar.getInstance();
            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(getIntent().getStringExtra("open_time").split("\\.")[0]);
            calendar.setTime(date);

            Calendar calendar2 = Calendar.getInstance();
            if (calendar.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                    && calendar.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)) {
                return "сегодня";
            } else {
                return getWeekString(calendar.get(Calendar.DAY_OF_WEEK));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getTimeDate() {
        try {
            Calendar calendar = Calendar.getInstance();
            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(getIntent().getStringExtra("open_time").split("\\.")[0]);
            calendar.setTime(date);
            return (calendar.get(Calendar.HOUR_OF_DAY) > 9 ?
                    Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)) :
                    "0" + Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)))
                    + ":" + (calendar.get(Calendar.MINUTE) > 9 ?
                    Integer.toString(calendar.get(Calendar.MINUTE)) :
                    "0" + Integer.toString(calendar.get(Calendar.MINUTE)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }
}
