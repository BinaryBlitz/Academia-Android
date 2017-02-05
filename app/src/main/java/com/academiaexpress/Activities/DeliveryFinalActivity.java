package com.academiaexpress.Activities;

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
import com.academiaexpress.Data.DeliveryOrder;
import com.academiaexpress.Data.MiniProduct;
import com.academiaexpress.Fragments.FinalPageFragment;
import com.academiaexpress.R;
import com.academiaexpress.Server.DeviceInfoStore;
import com.academiaexpress.Server.ServerApi;
import com.academiaexpress.Utils.AndroidUtilities;
import com.academiaexpress.Utils.AppConfig;
import com.academiaexpress.Utils.Image;
import com.academiaexpress.Utils.LogUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeliveryFinalActivity extends BaseActivity {
    private DeliveryAdapter adapter;

    private static int NO_ACTION = -1;
    public static int REMOVE_ACTION = -2;

    public static int itemToEdit = NO_ACTION;
    public static int newCount = NO_ACTION;
    public static int cardIndex = 0;

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

        ((TextView) findViewById(R.id.textView26)).setText(getPriceText());
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
                if (!check()) return;

                Intent intent = new Intent(DeliveryFinalActivity.this, TimeActivity.class);
                intent.putExtra("price", getPriceText());
                startActivity(intent);
            }
        });
    }

    private String getPriceText() {
        return getString(R.string.your_order_code) +
                (ProductsActivity.price >= AppConfig.freeDeliveryFrom ? ProductsActivity.price : ProductsActivity.price + AppConfig.deliveryPrice)
                + getString(R.string.ruble_sign);
    }

    private boolean check() {
        if (MapActivity.selectedLocationName == null || MapActivity.selectedLocationName.isEmpty()) {
            Snackbar.make(findViewById(R.id.main), R.string.select_delivery_place, Snackbar.LENGTH_SHORT).show();
            return false;
        }

        if (ProductsActivity.price == 0) {
            Snackbar.make(findViewById(R.id.main), R.string.empty_order, Snackbar.LENGTH_SHORT).show();
            return false;
        }

        if (binding.isEmpty() && !newCard) {
            Snackbar.make(findViewById(R.id.main), R.string.card_not_selected, Snackbar.LENGTH_SHORT).show();
            return false;
        }

        return true;
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

        updateUI();

        if (itemToEdit != NO_ACTION) {
            editOrder();
        }
    }

    private void editOrder() {
        if (newCount == REMOVE_ACTION) {
            removeItem();
        } else {
            editItemCount();
        }

        itemToEdit = NO_ACTION;
        ((TextView) findViewById(R.id.textView26)).setText(getPriceText());
    }

    private void removeItem() {
        DeliveryOrder.OrderPart part = ProductsActivity.collection.get(itemToEdit);
        ProductsActivity.product_count -= part.getCount();
        ProductsActivity.price -= part.getPrice() * part.getCount();

        removeMiniProducts();
        adapter.remove(itemToEdit);
    }

    private void removeMiniProducts() {
        for (int i = 0; i < FinalPageFragment.Companion.getCollection().size(); i++) {
            MiniProduct product = FinalPageFragment.Companion.getCollection().get(i);
            if (product.getName() != null && product.getName().equals(ProductsActivity.collection.get(itemToEdit).getName())) {
                FinalPageFragment.Companion.getCollection().get(i).setCount(0);
                break;
            }
        }
    }

    private void editItemCount() {
        for (int i = 0; i < FinalPageFragment.Companion.getCollection().size(); i++) {
            MiniProduct product = FinalPageFragment.Companion.getCollection().get(i);
            if (product.getName() != null && product.getName().equals(ProductsActivity.collection.get(itemToEdit).getName())) {
                FinalPageFragment.Companion.getCollection().get(i).setCount(newCount);
                break;
            }
        }

        adapter.changeItem(itemToEdit, newCount);
    }

    private void updateUI() {
        if (collection.size() != 0) {
            ((TextView) findViewById(R.id.editText35)).setText(collection.get(cardIndex).getNumber());
        }

        if (!MapActivity.selectedLocationName.isEmpty()) {
            showLocationButtons();
        }

        if (ProductsActivity.price >= AppConfig.freeDeliveryFrom) {
            setFreeDelivery();
        } else {
            setPriceForDelivery();
        }

        if (newCard) {
            setNewCard();
        } else {
            setSelectedCard();
        }
    }

    private void setNewCard() {
        ((TextView) findViewById(R.id.editText35)).setText(R.string.new_card);
        if (collection.size() != 0) {
            findViewById(R.id.imageView20).setVisibility(View.VISIBLE);
        }
    }

    private void setSelectedCard() {
        findViewById(R.id.imageView20).setVisibility(View.GONE);
    }

    private void setFreeDelivery() {
        findViewById(R.id.textView18).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.textView4)).setText(R.string.free_delivery);
    }

    private void setPriceForDelivery() {
        findViewById(R.id.textView18).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.textView4)).setText(R.string.free_delivery_from);
    }

    private void showLocationButtons() {
        ((TextView) findViewById(R.id.textView40)).setText(MapActivity.selectedLocationName);
        findViewById(R.id.textView40).setVisibility(View.VISIBLE);
        findViewById(R.id.textView41).setVisibility(View.VISIBLE);
        findViewById(R.id.textView42).setVisibility(View.VISIBLE);
        findViewById(R.id.editText3f).setVisibility(View.GONE);
    }
}
