package com.academiaexpress.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.academiaexpress.Base.BaseActivity;
import com.academiaexpress.Fragments.DishFragment;
import com.academiaexpress.R;
import com.academiaexpress.Server.DeviceInfoStore;
import com.academiaexpress.Server.ServerApi;
import com.academiaexpress.Utils.AndroidUtilities;
import com.academiaexpress.Utils.CategoriesUtility;
import com.academiaexpress.Utils.MoneyValues;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends BaseActivity {

    public static ArrayList<String> hours;
    public static ArrayList<Calendar> calendars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Answers(), new Crashlytics());
        setContentView(R.layout.activity_splash);

        hours = new ArrayList<>();
        calendars = new ArrayList<>();

        DishFragment.Companion.setAnswer(false);

        initAnimation();
        getOrders();
    }

    private void getCategories(final JsonObject workingHours, final boolean isOpened) {
        ServerApi.get(this).api().getCategories(DeviceInfoStore.getToken(this)).enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if (response.isSuccessful()) {
                    CategoriesUtility.INSTANCE.saveCategories(response.body());
                    openActivity(workingHours, isOpened);
                } else {
                    onInternetConnectionError();
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                onInternetConnectionError();
            }
        });
    }

    private void initAnimation() {
        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_anim);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                findViewById(R.id.imageView3).setVisibility(View.GONE);
                startRequests();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.imageView3).startAnimation(animation);
            }
        });
    }

    private void openAuth() {
        Intent intent = new Intent(SplashActivity.this, StartActivity.class);
        startActivity(intent);
        finish();
    }

    private void startRequests() {
        if (DeviceInfoStore.getToken(SplashActivity.this).equals("null")) {
            openAuth();
        } else {
            getWorkingHours();
        }
    }

    private void showNoInternetDialog() {
        if (!LostInternetConnectionActivity.Companion.getOpened()) {
            Intent intent = new Intent(SplashActivity.this, LostInternetConnectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            LostInternetConnectionActivity.Companion.setOpened(true);
        }
    }

    private void parseHours(JsonArray array, ArrayList<Pair<Calendar, Calendar>> times) {
        for (int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).getAsJsonObject();
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            int startHour = object.get("start_hour").getAsInt();
            int startMin = object.get("start_min").getAsInt();
            int endHour = object.get("end_hour").getAsInt();
            int endMin = object.get("end_min").getAsInt();

            start.set(Calendar.HOUR_OF_DAY, startHour);
            start.set(Calendar.MINUTE, startMin);
            if (i == 0) start.add(Calendar.MINUTE, 60);
            end.set(Calendar.HOUR_OF_DAY, endHour);
            end.set(Calendar.MINUTE, endMin);

            times.add(new Pair<>(start, end));
        }
    }

    private boolean isValid(Calendar calendar, ArrayList<Pair<Calendar, Calendar>> times) {
        boolean result = false;
        for (int i = 0; i < times.size(); i++) {
            if (calendar.before(times.get(i).first)) continue;

            calendar.add(Calendar.MINUTE, 30);
            if (calendar.before(times.get(i).second)) {
                result = true;
                calendar.add(Calendar.MINUTE, -30);
                break;
            }

            calendar.add(Calendar.MINUTE, -30);
        }

        return result;
    }

    private void addFirst(Calendar calendar) {
        calendar.add(Calendar.MINUTE, -30);
        int now = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        calendar.add(Calendar.MINUTE, 30);
        hours.add(now + ":" + (minute >= 30 ? minute : "0" + minute) + " - " +
                (calendar.get(Calendar.HOUR_OF_DAY)) + ":" +
                (calendar.get(Calendar.MINUTE) >= 30 ? calendar.get(Calendar.MINUTE) : "0" + calendar.get(Calendar.MINUTE)));
        calendar.add(Calendar.MINUTE, -60);
        calendars.add(calendar);
    }

    private void addToList(Calendar calendar) {
        if (hours.size() == 0) {
            addFirst(calendar);
        }

        int now = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        calendar.add(Calendar.MINUTE, 30);
        hours.add(now + ":" + (minute >= 30 ? minute : "0" + minute) + " - " +
                (calendar.get(Calendar.HOUR_OF_DAY)) + ":" +
                (calendar.get(Calendar.MINUTE) >= 30 ? calendar.get(Calendar.MINUTE) : "0" + calendar.get(Calendar.MINUTE)));
        calendar.add(Calendar.MINUTE, -30);
        calendars.add(calendar);
    }

    private void generateList(Calendar calendar, ArrayList<Pair<Calendar, Calendar>> times) {
        Calendar close;
        close = times.get(times.size() - 1).second;

        while (!calendar.after(close)) {
            boolean validDate = isValid(calendar, times);
            if (validDate) {
                addToList(calendar);
            }
            calendar.add(Calendar.MINUTE, 30);
        }
    }

    private void preProcess(Calendar calendar, ArrayList<Pair<Calendar, Calendar>> times) {
        Collections.sort(times, new Comparator<Pair<Calendar, Calendar>>() {
            @Override
            public int compare(Pair<Calendar, Calendar> lhs, Pair<Calendar, Calendar> rhs) {
                if (lhs.first.before(rhs.first)) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        calendar.add(Calendar.MINUTE, 50);

        if (calendar.get(Calendar.MINUTE) < 30) {
            calendar.add(Calendar.MINUTE, 30 - calendar.get(Calendar.MINUTE));
        } else {
            calendar.add(Calendar.MINUTE, 60 - calendar.get(Calendar.MINUTE));
        }
    }

    private void postProcess() {
        ArrayList<Integer> ints = new ArrayList<>();
        for (int i = 0; i < hours.size(); i++) {
            for (int j = i + 1; j < hours.size(); j++) {
                if (hours.get(i).equals(hours.get(j))) ints.add(i);
            }
        }

        for (int i = 0; i < ints.size(); i++) {
            hours.remove(ints.get(i).intValue());
            calendars.remove(ints.get(i).intValue());
        }
    }

    private void parseWorkingHours(JsonArray array) {
        final Calendar calendar = Calendar.getInstance();
        final ArrayList<Pair<Calendar, Calendar>> times = new ArrayList<>();

        parseHours(array, times);

        if (times.size() != 0) {
            preProcess(calendar, times);
            generateList(calendar, times);
            postProcess();
        }

        checkOpenTime(times);
    }

    private void openActivity(JsonObject object, boolean flag) {
        if (flag) {
            Intent intent = new Intent(SplashActivity.this, ClosedActivity.class);
            intent.putExtra("open_time", object.get("opens_at").isJsonNull() ? "" : object.get("opens_at").getAsString());
            intent.putExtra("preorder", object.get("dishes") != null && !object.get("dishes").isJsonNull());
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(SplashActivity.this, StartActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void finishActivity(JsonObject object) {
        Intent intent = new Intent(SplashActivity.this, ClosedActivity.class);
        intent.putExtra("closed", true);

        try {
            intent.putExtra("preorder", object.get("dishes") != null && !object.get("dishes").isJsonNull());
            intent.putExtra("open_time", object.get("opens_at").getAsString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        startActivity(intent);
        finish();
    }

    private boolean checkTimes(ArrayList<Pair<Calendar, Calendar>> times) {
        boolean isOpened = checkTime(initStartCalendar(Calendar.getInstance()), times.get(0).second);
        for (int i = 1; i < times.size(); i++) {
            isOpened = checkTime(times.get(i).first, times.get(i).second);
        }

        return isOpened;
    }

    private Calendar initStartCalendar(Calendar defaultTime) {
        defaultTime.add(Calendar.MINUTE, -60);
        return defaultTime;
    }

    private boolean checkTime(Calendar startTime, Calendar endTime) {
        Calendar now = Calendar.getInstance();
        return !(now.after(startTime) && now.before(endTime));
    }

    private void parseOpenTime(JsonObject workingHours, ArrayList<Pair<Calendar, Calendar>> times) {
        try {
            ClosedActivity.imageUrl = workingHours.get("welcome_screen_image_url").isJsonNull() ? "" :
                    workingHours.get("welcome_screen_image_url").getAsString();

            boolean isOpened = checkTimes(times) || !workingHours.get("is_open").getAsBoolean();

            getCategories(workingHours, isOpened);
        } catch (Exception e) {
            finishActivity(workingHours);
        }
    }

    private void checkOpenTime(final ArrayList<Pair<Calendar, Calendar>> times) {

        ServerApi.get(this).api().getDay(DeviceInfoStore.getToken(this)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    parseOpenTime(response.body(), times);
                } else {
                    onInternetConnectionError();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) { }
        });
    }

    private void getWorkingHours() {
        if (!AndroidUtilities.INSTANCE.isConnected(this)) {
            showNoInternetDialog();
            return;
        }

        ServerApi.get(this).api().getWorkingHours(DeviceInfoStore.getToken(this)).enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if (response.isSuccessful()) {
                    parseWorkingHours(response.body());
                } else {
                    onInternetConnectionError();
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) { }
        });
    }

    private void getOrders() {
        ServerApi.get(this).api().getOrders(DeviceInfoStore.getToken(this)).enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if (response.isSuccessful()) {
                    parseOrders(response.body());
                }
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
    }
}
