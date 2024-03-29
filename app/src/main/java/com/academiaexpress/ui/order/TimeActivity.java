package com.academiaexpress.ui.order;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.academiaexpress.extras.Extras;
import com.academiaexpress.ui.BaseActivity;
import com.academiaexpress.ui.main.ClosedActivity;
import com.academiaexpress.ui.main.ProductsActivity;
import com.academiaexpress.ui.main.SplashActivity;
import com.academiaexpress.ui.views.customViews.NumberPicker;
import com.academiaexpress.data.CreditCard;
import com.academiaexpress.R;
import com.academiaexpress.network.DeviceInfoStore;
import com.academiaexpress.network.ServerApi;
import com.academiaexpress.utils.AndroidUtilities;
import com.academiaexpress.utils.AppConfig;
import com.academiaexpress.utils.Image;
import com.academiaexpress.utils.LogUtil;
import com.academiaexpress.utils.MoneyValues;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TimeActivity extends BaseActivity implements TimePickerDialog.OnTimeSetListener {

    public static boolean now = true;
    public static String selected = "";
    public static boolean errors = false;
    static String id = "";
    static boolean isPaymentStarted = false;

    private String selectedLocationName = "";
    private LatLng selectedLocation = null;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);

        selectedLocationName = getIntent().getStringExtra(Extras.EXTRA_LOCATION_NAME);
        selectedLocation = getIntent().getParcelableExtra(Extras.EXTRA_LOCATION_LATLNG);

        initElements();
        setOnClickListeners();

        progressDialog = new ProgressDialog(this);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                getUser();
            }
        });
    }

    private void initElements() {
        now = true;
        errors = false;

        Image.loadPhoto(R.drawable.back1, (ImageView) findViewById(R.id.background));

        findViewById(R.id.selected_indicator).setVisibility(View.VISIBLE);
        findViewById(R.id.selected_time_indicator).setVisibility(View.GONE);

        if (ClosedActivity.closed) {
            setClosed();
        }
    }

    private void setClosed() {
        findViewById(R.id.select_time_btn).setVisibility(View.VISIBLE);
        findViewById(R.id.now_btn).setVisibility(View.GONE);
        findViewById(R.id.selected_indicator).setVisibility(View.GONE);
        findViewById(R.id.selected_time_indicator).setVisibility(View.GONE);
    }

    private void setOnClickListeners() {
        findViewById(R.id.select_time_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show();
            }
        });

        findViewById(R.id.now_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (now) {
                    setNow();
                } else {
                    setSelectedTime();
                }

                now = !now;
            }
        });

        findViewById(R.id.guillotine_hamburger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.pay_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!AndroidUtilities.INSTANCE.isConnected(TimeActivity.this)) {
                    return;
                }

                isPaymentStarted = true;

                if (DeliveryFinalActivity.newCard) {
                    addOrder();
                } else {
                    processExistingCard();
                }
            }
        });
    }

    private void processExistingCard() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    showDialog();
                }
            }
        });
    }

    private void showDialog() {
        new AlertDialog.Builder(TimeActivity.this)
                .setTitle(getString(R.string.app_name))
                .setMessage(R.string.are_you_sure)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addOrder();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create().show();
    }

    private void setNow() {
        findViewById(R.id.selected_time_indicator).setVisibility(View.GONE);
        findViewById(R.id.selected_indicator).setVisibility(View.GONE);
    }

    private void setSelectedTime() {
        findViewById(R.id.selected_indicator).setVisibility(View.VISIBLE);
        findViewById(R.id.selected_time_indicator).setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isPaymentStarted) {
            showErrorDialog();
            isPaymentStarted = false;
            return;
        }

        if (id.isEmpty() || errors) {
            return;
        }

        if (DeliveryFinalActivity.newCard) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    progressDialog.show();
                }
            });
            processCards();
        } else {
            openProcessActivity();
        }
    }

    private void showErrorDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error)
                .setMessage(R.string.error_helper)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void openProcessActivity() {
        Intent intent = new Intent(TimeActivity.this, DeliveryProcessActivity.class);
        startActivity(intent);
        finish();
    }

    private void processCards() {
        getCards();
        DeliveryFinalActivity.newCard = false;
    }

    private void initDialog(Dialog dialog) {
        dialog.setTitle(R.string.select_time_code);
        dialog.setContentView(R.layout.dialog);
    }

    public void show() {
        if (SplashActivity.hours.size() == 0) {
            Snackbar.make(findViewById(R.id.main), R.string.no_time_error, Snackbar.LENGTH_LONG).show();
            return;
        }

        final Dialog dialog = new Dialog(TimeActivity.this);
        initDialog(dialog);

        final NumberPicker numberPicker = (NumberPicker) dialog.findViewById(R.id.numberPicker1);
        final String[] values = getContentForPicker();
        initPicker(numberPicker, values);
        initOkButton(dialog, numberPicker, values);
        initCancelButton(dialog);

        dialog.show();
    }

    private void initCancelButton(final Dialog dialog) {
        View cancelBtn = dialog.findViewById(R.id.cancelBtn);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void initOkButton(final Dialog dialog, final NumberPicker numberPicker, final String[] values) {
        View okBtn = dialog.findViewById(R.id.okBtn);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                okClick(dialog, numberPicker, values);
            }
        });
    }

    private void okClick(final Dialog dialog, final NumberPicker numberPicker, final String[] values) {
        findViewById(R.id.selected_indicator).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.select_time_btn)).setText(values[numberPicker.getValue()]);
        processSelectedTime(numberPicker, values);
        dialog.dismiss();
        findViewById(R.id.selected_time_indicator).setVisibility(View.VISIBLE);
    }

    private void processSelectedTime(final NumberPicker numberPicker, final String[] values) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss z", Locale.getDefault());

        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(values[numberPicker.getValue()].split(" - ")[0].split(":")[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(values[numberPicker.getValue()].split(" - ")[0].split(":")[1]));
        calendar.set(Calendar.SECOND, 0);

        format.setTimeZone(TimeZone.getDefault());
        selected = format.format(calendar.getTime());
    }

    private String[] getContentForPicker() {
        final String[] values = new String[SplashActivity.hours.size()];
        for (int i = 0; i < SplashActivity.hours.size(); i++) {
            values[i] = SplashActivity.hours.get(i);
        }

        return values;
    }

    private void initPicker(NumberPicker numberPicker, String[] values) {
        numberPicker.setDisplayedValues(values);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(SplashActivity.hours.size() - 1);
        numberPicker.setWrapSelectorWheel(false);
    }

    private void addOrder() {
        final JsonObject object = new JsonObject();
        JsonArray array = new JsonArray();

        for (int i = 0; i < ProductsActivity.collection.size(); i++) {
            JsonObject part = new JsonObject();
            try {
                part.addProperty("dish_id", ProductsActivity.collection.get(i).getId());
                part.addProperty("quantity", ProductsActivity.collection.get(i).getCount());
            } catch (Exception e) {
                e.printStackTrace();
            }
            array.add(part);
        }

        object.add("line_items_attributes", array);
        object.addProperty("address", selectedLocationName);
        object.addProperty("latitude", selectedLocation.latitude);
        object.addProperty("longitude", selectedLocation.longitude);
        if (!now) {
            object.addProperty("scheduled_for", selected);
        }

        JsonObject toSend = new JsonObject();

        toSend.add("order", object);

        sendOrder(toSend);
    }

    private void parseOrder(JsonObject object) {
        id = object.get("id").getAsString();
        if (DeliveryFinalActivity.newCard) {
            createCard();
        } else {
            sendPayment(id, DeliveryFinalActivity.binding);
        }
    }

    private void parseCreateCard(JsonObject object) {
        try {
            Intent intent = new Intent(TimeActivity.this, WebActivity.class);
            intent.putExtra("url", object.get("url").getAsString());
            startActivity(intent);
        } catch (Exception e) {
            LogUtil.logException(e);
        }
    }

    private void createCard() {
        ServerApi.get(this).api().createCard(DeviceInfoStore.getToken(this)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    parseCreateCard(response.body());
                } else {
                    onInternetConnectionError();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                onInternetConnectionError();
            }
        });
    }

    private void sendOrder(JsonObject toSend) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.show();

        ServerApi.get(this).api().sendOrder(toSend, DeviceInfoStore.getToken(this)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                dialog.dismiss();
                if (response.isSuccessful()) {
                    parseOrder(response.body());
                } else {
                    onInternetConnectionError();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                dialog.dismiss();
                onInternetConnectionError();
            }
        });
    }

    private void saveMoneyValues(JsonObject object) {
        MoneyValues.balance = AndroidUtilities.INSTANCE.getIntFieldFromJson(object.get("balance"));
        MoneyValues.promocode = AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("promo_code"));
        MoneyValues.discount = AndroidUtilities.INSTANCE.getIntFieldFromJson(object.get("discount"));
        MoneyValues.promoUsed = AndroidUtilities.INSTANCE.getBooleanFieldFromJson(object.get("promo_used"));
    }

    private void parseUser(JsonObject object) {
        saveMoneyValues(object);
        setUI();
    }

    private void setUI() {
        parsePrices();

        if (MoneyValues.discount == 0) {
            findViewById(R.id.discount).setVisibility(View.GONE);
        }

        if (MoneyValues.balance == 0) {
            findViewById(R.id.bonuses).setVisibility(View.GONE);
        }
    }

    private void parsePrices() {
        int price = (ProductsActivity.price >= AppConfig.freeDeliveryFrom ? ProductsActivity.price : ProductsActivity.price + AppConfig.deliveryPrice);

        ((TextView) findViewById(R.id.final_price)).setText(getString(R.string.full_price) + price + getString(R.string.ruble_sign));
        ((TextView) findViewById(R.id.discount)).setText(getString(R.string.discount) + Math.round(price * (MoneyValues.discount / 100.0)) + getString(R.string.ruble_sign));
        ((TextView) findViewById(R.id.bonuses)).setText(getString(R.string.bonuses) + getBonuses(price) + getString(R.string.ruble_sign));
        ((TextView) findViewById(R.id.price)).setText(getString(R.string.final_price) + calculateFinalPrice(price) + getString(R.string.ruble_sign));
    }

    private long getBonuses(int price) {
        return MoneyValues.balance > price - Math.round(price * (MoneyValues.discount / 100.0)) ?
                price - Math.round(price * (MoneyValues.discount / 100.0)) : MoneyValues.balance;
    }

    private long calculateFinalPrice(int price) {
        return (price - Math.round(price * (MoneyValues.discount / 100.0))) - MoneyValues.balance > 0 ?
                (price - Math.round(price * (MoneyValues.discount / 100.0))) - MoneyValues.balance : 0;
    }

    private void getUser() {
        ServerApi.get(this).api().getUser(DeviceInfoStore.getToken(this)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    parseUser(response.body());
                } else {
                    onInternetConnectionError();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                onInternetConnectionError();
            }
        });
    }

    private Date parseDate(String dateString) {
        Date date = null;
        String formatPattern = "yyyy-MM-dd'T'HH:mm:ss";
        try {
            SimpleDateFormat format = new SimpleDateFormat(formatPattern, Locale.getDefault());
            date = format.parse(dateString);
        } catch (Exception e) {
            LogUtil.logException(e);
        }

        return date;
    }

    private void parseCards(JsonArray array) {
        ArrayList<CreditCard> collection = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).getAsJsonObject();
            collection.add(new CreditCard(
                    AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("number")),
                    AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("binding_id")),
                    parseDate(AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("created_at")))
            ));
        }

        Collections.sort(collection, new Comparator<CreditCard>() {
            @Override
            public int compare(CreditCard lhs, CreditCard rhs) {
                if (lhs.getDate() != null && lhs.getDate().before(rhs.getDate())) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });


        if (collection.size() != 0) {
            sendPayment(id, collection.get(0).getBinding());
            id = "";
        }
    }

    private JsonObject generatePayJson(String binding) {
        JsonObject toSend = new JsonObject();
        JsonObject object = new JsonObject();

        object.addProperty("binding_id", binding);
        toSend.add("payment", object);

        return toSend;
    }

    private void getCards() {
        ServerApi.get(this).api().getCards(DeviceInfoStore.getToken(this)).enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    parseCards(response.body());
                } else {
                    onInternetConnectionError();
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                progressDialog.dismiss();
                onInternetConnectionError();
            }
        });
    }

    private void parsePayment() {
        Intent intent = new Intent(TimeActivity.this, DeliveryProcessActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendPayment(String id, String binding) {
        progressDialog.show();

        ServerApi.get(this).api().pay(generatePayJson(binding), id, DeviceInfoStore.getToken(this)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    parsePayment();
                } else {
                    onInternetConnectionError();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressDialog.dismiss();
                onInternetConnectionError();
            }
        });
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        findViewById(R.id.selected_indicator).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.select_time_btn)).setText((hourOfDay >= 10 ? Integer.toString(hourOfDay) : "0" + Integer.toString(hourOfDay)) + ":" +
                        (minute >= 10 ? Integer.toString(minute) : "0" + Integer.toString(minute)));
        parseCalendar(hourOfDay, minute);
    }

    private void parseCalendar(int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        selected = format.format(calendar.getTime());
    }
}