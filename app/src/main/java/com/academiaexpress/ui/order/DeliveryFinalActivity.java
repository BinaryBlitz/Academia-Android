package com.academiaexpress.ui.order;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.academiaexpress.R;
import com.academiaexpress.data.CreditCard;
import com.academiaexpress.data.Dish;
import com.academiaexpress.data.Order;
import com.academiaexpress.extras.Extras;
import com.academiaexpress.extras.RequestCodes;
import com.academiaexpress.network.DeviceInfoStore;
import com.academiaexpress.network.ServerApi;
import com.academiaexpress.ui.BaseActivity;
import com.academiaexpress.ui.main.ProductsActivity;
import com.academiaexpress.ui.main.adapters.DeliveryAdapter;
import com.academiaexpress.utils.AndroidUtilities;
import com.academiaexpress.utils.AppConfig;
import com.academiaexpress.utils.Image;
import com.academiaexpress.utils.LogUtil;
import com.google.android.gms.maps.model.LatLng;
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

    private static final String EXTRA_PRICE = "price";

    private int REQUEST_MAP_CODE = 1001;
    private String selectedLocationName = "";
    private LatLng selectedLocation;

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
        Image.loadPhoto(R.drawable.back1, (ImageView) findViewById(R.id.background));

        RecyclerView view = (RecyclerView) findViewById(R.id.recyclerView);
        view.setItemAnimator(new DefaultItemAnimator());
        view.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DeliveryAdapter(this);
        adapter.setInc(true);
        view.setAdapter(adapter);
        collection = new ArrayList<>();

        newCard = false;
        binding = "";

        ((TextView) findViewById(R.id.price)).setText(getPriceText());
    }

    private void setOnClickListeners() {
        findViewById(R.id.guillotine_hamburger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProductsActivity.canceled = true;
                finish();
            }
        });

        findViewById(R.id.map_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeliveryFinalActivity.this, MapActivity.class);
                startActivityForResult(intent, REQUEST_MAP_CODE);
            }
        });

        findViewById(R.id.next_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeliveryFinalActivity.this, CreditCardsActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.select_another_address_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeliveryFinalActivity.this, MapActivity.class);
                startActivityForResult(intent, REQUEST_MAP_CODE);
            }
        });

        findViewById(R.id.make_order).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!check()) {
                    return;
                }

                Intent intent = new Intent(DeliveryFinalActivity.this, TimeActivity.class);
                intent.putExtra(EXTRA_PRICE, getPriceText());
                intent.putExtra(Extras.EXTRA_LOCATION_NAME, selectedLocationName);
                intent.putExtra(Extras.EXTRA_LOCATION_LATLNG, selectedLocation);
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
        if (selectedLocationName == null || selectedLocationName.isEmpty()) {
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
            ((TextView) findViewById(R.id.next_btn)).setText(collection.get(0).getNumber());
            findViewById(R.id.next_btn).setVisibility(View.VISIBLE);
        } else {
            newCard = true;
            findViewById(R.id.next_btn).setVisibility(View.GONE);
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
        ((TextView) findViewById(R.id.price)).setText(getPriceText());
    }

    private void removeItem() {
        Order.OrderPart part = ProductsActivity.collection.get(itemToEdit);
        ProductsActivity.product_count -= part.getCount();
        ProductsActivity.price -= part.getPrice() * part.getCount();

        removeMiniProducts();
        adapter.remove(itemToEdit);
    }

    private void removeMiniProducts() {
        for (int i = 0; i < StuffFragment.Companion.getCollection().size(); i++) {
            Dish product = StuffFragment.Companion.getCollection().get(i);
            if (product.getMealName() != null && product.getMealName().equals(ProductsActivity.collection.get(itemToEdit).getName())) {
                StuffFragment.Companion.getCollection().get(i).setCount(0);
                break;
            }
        }
    }

    private void editItemCount() {
        for (int i = 0; i < StuffFragment.Companion.getCollection().size(); i++) {
            Dish product = StuffFragment.Companion.getCollection().get(i);
            if (product.getMealName() != null && product.getMealName().equals(ProductsActivity.collection.get(itemToEdit).getName())) {
                StuffFragment.Companion.getCollection().get(i).setCount(newCount);
                break;
            }
        }

        adapter.changeItem(itemToEdit, newCount);
    }

    private void updateUI() {
        if (collection.size() != 0) {
            ((TextView) findViewById(R.id.next_btn)).setText(collection.get(cardIndex).getNumber());
        }

        if (!selectedLocationName.isEmpty()) {
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
        ((TextView) findViewById(R.id.next_btn)).setText(R.string.new_card);
        if (collection.size() != 0) {
            findViewById(R.id.checkmark).setVisibility(View.VISIBLE);
        }
    }

    private void setSelectedCard() {
        findViewById(R.id.checkmark).setVisibility(View.GONE);
    }

    private void setFreeDelivery() {
        findViewById(R.id.delivery_help).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.free_delivery)).setText(R.string.free_delivery);
    }

    private void setPriceForDelivery() {
        findViewById(R.id.delivery_help).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.free_delivery)).setText(R.string.free_delivery_from);
    }

    private void showLocationButtons() {
        ((TextView) findViewById(R.id.selected_location)).setText(selectedLocationName);
        findViewById(R.id.selected_location).setVisibility(View.VISIBLE);
        findViewById(R.id.select_another_address_btn).setVisibility(View.VISIBLE);
        findViewById(R.id.address_help_text).setVisibility(View.VISIBLE);
        findViewById(R.id.map_btn).setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((resultCode == Activity.RESULT_OK) && (requestCode == RequestCodes.REQUEST_MAP_CODE)) {
            selectedLocationName = data.getStringExtra(Extras.EXTRA_LOCATION_NAME);
            selectedLocation = data.getParcelableExtra(Extras.EXTRA_LOCATION_LATLNG);
        }

        }
}
