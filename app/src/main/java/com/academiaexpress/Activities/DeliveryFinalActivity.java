package com.academiaexpress.Activities;

import com.academiaexpress.Utils.AndroidUtilities;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.academiaexpress.Adapters.DeliveryAdapter;
import com.academiaexpress.Base.BaseActivity;
import com.academiaexpress.Data.CreditCard;
import com.academiaexpress.Fragments.FinalPageFragment;
import com.academiaexpress.R;
import com.academiaexpress.Server.DeviceInfoStore;
import com.academiaexpress.Server.ServerApi;
import com.academiaexpress.Utils.Image;
import com.academiaexpress.Utils.LogUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeliveryFinalActivity extends BaseActivity {
    DeliveryAdapter adapter;

    public static int INDEX = -1;
    public static int r_INDEX = -1;
    public static int newCount = -1;

    public static int cardIndex = 0;

    public static String id = "";

    public static ArrayList<CreditCard> collection;
    public static String binding = "";
    public static boolean newCard = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);

        initElements();
        setOnClickListeners();
        getCards();
    }

    private void initElements() {
        Image.loadPhoto(R.drawable.back1, (ImageView) findViewById(R.id.imageView21));

        RecyclerView view = (RecyclerView) findViewById(R.id.recyclerView);
        view.setItemAnimator(new DefaultItemAnimator());
        view.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DeliveryAdapter(this);
        adapter.setInc(true);
        view.setAdapter(adapter);
        collection = new ArrayList<>();

        newCard = false;
        binding = "";

        ((TextView) findViewById(R.id.textView26)).setText("ВАШ ЗАКАЗ НА " +
                (ProductsActivity.price >= 1000 ? ProductsActivity.price : ProductsActivity.price + 200)
                + " Р");
    }

    private void setOnClickListeners() {
        findViewById(R.id.guillotine_hamburger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProductsActivity.canceled = true;
                finish();
            }
        });

        findViewById(R.id.editText3f).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeliveryFinalActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.editText35).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeliveryFinalActivity.this, CreditCardsActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.textView41).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeliveryFinalActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.editText3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (MapActivity.selected_final == null || MapActivity.selected_final.isEmpty()) {
//                    Snackbar.make(findViewById(R.id.main), "Выберите адресс доставки.", Snackbar.LENGTH_SHORT).show();
//                    return;
//                }

                if (ProductsActivity.price == 0) {
                    Snackbar.make(findViewById(R.id.main), "Заказ пуст.", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (binding.equals("") && !newCard) {
                    Snackbar.make(findViewById(R.id.main), "Карта не выбрана.", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(DeliveryFinalActivity.this, TimeActivity.class);
                intent.putExtra("price", "ВАШ ЗАКАЗ НА " +
                        (ProductsActivity.price >= 1000 ? ProductsActivity.price : ProductsActivity.price + 200)
                        + " Р");
                startActivity(intent);
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
                if (lhs.getDate().before(rhs.getDate())) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });

        if (collection.size() != 0) {
            binding = collection.get(0).getBinding();
            ((TextView) findViewById(R.id.editText35)).setText(collection.get(0).getNumber());
            findViewById(R.id.editText35).setVisibility(View.VISIBLE);
        } else {
            newCard = true;
            findViewById(R.id.editText35).setVisibility(View.GONE);
        }
    }

    private void getCards() {
        ServerApi.get(this).api().getCards(DeviceInfoStore.getToken(this)).enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if (response.isSuccessful()) {
                    parseCards(response.body());
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

    @Override
    protected void onResume() {
        super.onResume();
        if (collection.size() != 0) {
            ((TextView) findViewById(R.id.editText35)).setText(collection.get(cardIndex).getNumber());
        }

        if (newCard) {
            ((TextView) findViewById(R.id.editText35)).setText("НОВАЯ КАРТА");
            if (collection.size() != 0) {
                findViewById(R.id.imageView20).setVisibility(View.VISIBLE);
            }
        } else {
            findViewById(R.id.imageView20).setVisibility(View.GONE);
        }

        if (INDEX != -1) {
            if (INDEX == -2) {
                ProductsActivity.product_count -= ProductsActivity.collection.get(r_INDEX).getCount();
                ProductsActivity.price -=
                        ProductsActivity.collection.get(r_INDEX).getPrice() * ProductsActivity.collection.get(r_INDEX).getCount();
                for (int i = 0; i < FinalPageFragment.Companion.getCollection().size(); i++) {
                    if (FinalPageFragment.Companion.getCollection().get(i).getName().equals(ProductsActivity.collection.get(r_INDEX).getName())) {
                        FinalPageFragment.Companion.getCollection().get(i).setCount(0);
                        break;
                    }
                }
                adapter.remove(r_INDEX);
            } else {
                for (int i = 0; i < FinalPageFragment.Companion.getCollection().size(); i++) {
                    if (FinalPageFragment.Companion.getCollection().get(i).getName().equals(ProductsActivity.collection.get(INDEX).getName())) {
                        FinalPageFragment.Companion.getCollection().get(i).setCount(newCount);
                        break;
                    }
                }

                adapter.changeItem(INDEX, newCount);
            }
            INDEX = -1;
            ((TextView) findViewById(R.id.textView26)).setText("ВАШ ЗАКАЗ НА " + ProductsActivity.price + " Р");
        }

//        if (!MapActivity.selected_final.isEmpty()) {
//            ((TextView) findViewById(R.id.textView40)).setText(MapActivity.selected_final);
//            ((TextView) findViewById(R.id.textView40)).setVisibility(View.VISIBLE);
//            ((TextView) findViewById(R.id.textView41)).setVisibility(View.VISIBLE);
//            ((TextView) findViewById(R.id.textView42)).setVisibility(View.VISIBLE);
//            findViewById(R.id.editText3f).setVisibility(View.GONE);
//            MapActivity.selected = "";
//        }

        if (ProductsActivity.price >= 1000) {
            ((TextView) findViewById(R.id.textView18)).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.textView4)).setText("Доставка бесплатна.");
        } else {
            ((TextView) findViewById(R.id.textView18)).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.textView4)).setText("Бесплатная доставка от 1000 руб.");
        }
    }
}
