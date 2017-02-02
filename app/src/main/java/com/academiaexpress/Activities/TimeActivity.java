package com.academiaexpress.Activities;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.academiaexpress.Base.BaseActivity;
import com.academiaexpress.Custom.NumberPicker;
import com.academiaexpress.Data.CreditCard;
import com.academiaexpress.R;
import com.academiaexpress.Server.DeviceInfoStore;
import com.academiaexpress.Server.ServerApi;
import com.academiaexpress.Utils.AndroidUtilities;
import com.academiaexpress.Utils.Image;
import com.academiaexpress.Utils.LogUtil;
import com.academiaexpress.Utils.MoneyValues;
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

public class TimeActivity extends BaseActivity implements
        TimePickerDialog.OnTimeSetListener {

    public static boolean now = true;

    public static String selected = "";

    public static boolean errors = false;

    static String id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.time_layout);

        now = true;
        errors = true;

        Image.loadPhoto(R.drawable.back1, (ImageView) findViewById(R.id.imageView21));

        findViewById(R.id.guillotine_hamburger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.imageView20).setVisibility(View.VISIBLE);
        findViewById(R.id.imageView20d).setVisibility(View.GONE);

        if(ClosedActivity.closed) {
            findViewById(R.id.editText3f).setVisibility(View.VISIBLE);
            findViewById(R.id.editText3d).setVisibility(View.GONE);
            findViewById(R.id.imageView20).setVisibility(View.GONE);
            findViewById(R.id.imageView20d).setVisibility(View.GONE);
        }

        findViewById(R.id.editText3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!AndroidUtilities.INSTANCE.isConnected(TimeActivity.this)) {
                    return;
                }

                if(!DeliveryFinalActivity.newCard) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!isFinishing()){
                                new AlertDialog.Builder(TimeActivity.this)
                                        .setTitle("Академия Экспресс")
                                        .setMessage("Вы уверены?")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                addOrder();
                                            }
                                        })
                                        .setNegativeButton("ОТМЕНА", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        }).create().show();
                            }
                        }
                    });
                } else {
                    addOrder();
                }
            }
        });

        findViewById(R.id.editText3d).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!now) {
                    findViewById(R.id.imageView20).setVisibility(View.VISIBLE);
                    findViewById(R.id.imageView20d).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.imageView20d).setVisibility(View.GONE);
                    findViewById(R.id.imageView20).setVisibility(View.GONE);
                }

                now = !now;
            }
        });

        findViewById(R.id.editText3f).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimeActivity.this.show(new DialogFinishedListener() {
                    @Override
                    public void onDialogFinished(int val) {

                    }
                }, 1);
            }
        });

        getUser();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(id.isEmpty() || errors) {
            return;
        }
        final ProgressDialog dialog = new ProgressDialog(this);

        dialog.show();

        if(DeliveryFinalActivity.newCard) {
            getCards();
            DeliveryFinalActivity.newCard = false;
        } else {
            Intent intent = new Intent(TimeActivity.this, DeliveryProcessActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void show(final DialogFinishedListener listener, int current) {

        final Dialog d = new Dialog(TimeActivity.this);
        d.setTitle("Выберите время");
        d.setContentView(R.layout.dialog);
        View b1 = d.findViewById(R.id.button1);
        View b2 = d.findViewById(R.id.button2);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
        final String[] values = new String[SplashActivity.hours.size()];
        for(int i = 0; i < SplashActivity.hours.size(); i++) {
            values[i] = SplashActivity.hours.get(i);
        }

        if(SplashActivity.hours.size() == 0) {
            Snackbar.
                    make(findViewById(R.id.main), "Нет доступного для заказа времени.",
                            Snackbar.LENGTH_LONG).show();
            return;
        }

        np.setDisplayedValues(
                values);
        np.setMinValue(0);
        np.setMaxValue(SplashActivity.hours.size() - 1);
        np.setWrapSelectorWheel(false);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.imageView20).setVisibility(View.GONE);
                ((TextView) findViewById(R.id.editText3f)).setText(
                       values[np.getValue()]);

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(values[np.getValue()].split(" - ")[0].split(":")[0]));
                calendar.set(Calendar.MINUTE, Integer.parseInt(values[np.getValue()].split(" - ")[0].split(":")[1]));
                calendar.set(Calendar.SECOND, 0);
                format.setTimeZone(TimeZone.getDefault());
                selected = format.format(calendar.getTime());
                selected += "+03:00";
                d.dismiss();
                findViewById(R.id.imageView20d).setVisibility(View.VISIBLE);
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }

    private void addOrder() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.show();

        final JsonObject object = new JsonObject();

        JsonArray array = new JsonArray();

        for(int i = 0; i < ProductsActivity.collection.size(); i++) {
            JsonObject object1 = new JsonObject();
            try {
                object1.addProperty("dish_id", ProductsActivity.collection.get(i).getId());
                object1.addProperty("quantity", ProductsActivity.collection.get(i).getCount());
            } catch (Exception e) {
                e.printStackTrace();
            }
            array.add(object1);
        }

        try {
            object.add("line_items_attributes", array);
//            object.addProperty("address", MapActivity.selected_final);
//            object.addProperty("latitude", MapActivity.selected_lat_lng_final.latitude);
//            object.addProperty("longitude", MapActivity.selected_lat_lng_final.longitude);
            if (!now) object.addProperty("scheduled_for", selected);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObject to_send = new JsonObject();

        try {
            to_send.add("order", object);
        } catch (Exception e) {
            e.printStackTrace();
        }

        sendOrder(to_send);
    }

    private void parseOrder(JsonObject object) {
        id = object.get("id").getAsString();
        if (DeliveryFinalActivity.newCard) createCard();
        else sendPayment(id, DeliveryFinalActivity.binding);
    }

    private void parseCreateCard(JsonObject object) {
        try {
            Intent intent = new Intent(TimeActivity.this, WebActivity.class);
            intent.putExtra("url", object.get("url").getAsString());
            startActivity(intent);
        } catch (Exception e) {
            onInternetConnectionError();
            LogUtil.logException(e);
        }
    }

    private void createCard() {
        ServerApi.get(this).api().createCard(DeviceInfoStore.getToken(this)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) parseCreateCard(response.body());
                else onInternetConnectionError();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                onInternetConnectionError();
            }
        });
    }

    private void sendOrder(JsonObject toSend) {
        ServerApi.get(this).api().sendOrder(toSend, DeviceInfoStore.getToken(this)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) parseOrder(response.body());
                else onInternetConnectionError();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                onInternetConnectionError();
            }
        });
    }

    private void parseUser(JsonObject object) {
        try {
            MoneyValues.balance = object.get("balance").getAsInt();
            MoneyValues.promocode = object.get("promo_code").getAsString();
            MoneyValues.discount = object.get("discount").getAsInt();
            MoneyValues.promoUsed = object.get("promo_used").getAsBoolean();

            int price = (ProductsActivity.price >= 1000 ? ProductsActivity.price : ProductsActivity.price + 200);
            ((TextView) findViewById(R.id.textView64)).setText("Цена с доставкой: " + price + " Р");

            ((TextView) findViewById(R.id.textView65)).setText("Скидка: " + Math.round(price * (MoneyValues.discount / 100.0)) + " Р");

            ((TextView) findViewById(R.id.textView66)).setText("Бонусы: " +
                    ( MoneyValues.balance > price - Math.round(price * (MoneyValues.discount / 100.0)) ?
                            price - Math.round(price * (MoneyValues.discount / 100.0)) : MoneyValues.balance) + " Р");

            ((TextView) findViewById(R.id.textView26)).setText("Итого: " +
                    (((price - Math.round(price * (MoneyValues.discount/100.0))) - MoneyValues.balance) > 0 ?
                            ((price - Math.round(price * (MoneyValues.discount/100.0))) - MoneyValues.balance)  : 0) + " Р");

            if(MoneyValues.discount == 0) findViewById(R.id.textView65).setVisibility(View.GONE);
            if(MoneyValues.balance == 0) findViewById(R.id.textView66).setVisibility(View.GONE);

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void parseCards(JsonArray array) {
        ArrayList<CreditCard> collection = new ArrayList<>();
        for(int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).getAsJsonObject();

            Calendar start = Calendar.getInstance();

            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                Date date = format.parse(object.get("created_at").getAsString());
                start.setTime(date);
            } catch (Exception e) { LogUtil.logException(e); }


//            collection.add(new CreditCard(
//                    object.get("number").getAsString(),
//                    object.get("binding_id").getAsString(),
//                    start
//            ));
        }

        Collections.sort(collection, new Comparator<CreditCard>() {
            @Override
            public int compare(CreditCard lhs, CreditCard rhs) {
                if (lhs.getDate().before(rhs.getDate())) return 1;
                else return -1;
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
                if (response.isSuccessful()) parseCards(response.body());
                else onInternetConnectionError();
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
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
        ServerApi.get(this).api().pay(generatePayJson(binding), id, DeviceInfoStore.getToken(this)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) parsePayment();
                else onInternetConnectionError();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                onInternetConnectionError();
            }
        });
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        findViewById(R.id.imageView20).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.editText3f)).setText(
                (hourOfDay >= 10 ? Integer.toString(hourOfDay) : "0" + Integer.toString(hourOfDay)) + ":" +
                        (minute >= 10 ? Integer.toString(minute) : "0" + Integer.toString(minute)));

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        selected = format.format(calendar.getTime());
    }

    public interface DialogFinishedListener {
        void onDialogFinished(int val);
    }
}