package com.academiaexpress.Activities;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.academiaexpress.Adapters.OrdersAdapter;
import com.academiaexpress.Base.BaseActivity;
import com.academiaexpress.Data.DeliveryOrder;
import com.academiaexpress.R;
import com.academiaexpress.Server.DeviceInfoStore;
import com.academiaexpress.Server.ServerApi;
import com.academiaexpress.Utils.Image;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrdersActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    OrdersAdapter adapter;
    private SwipeRefreshLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.orders_screen);

        Image.loadPhoto(R.drawable.back1, (ImageView) findViewById(R.id.imageView21));

        RecyclerView view = (RecyclerView) findViewById(R.id.recyclerView);
        view.setItemAnimator(new DefaultItemAnimator());
        view.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrdersAdapter(this);
        view.setAdapter(adapter);

        layout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        layout.setOnRefreshListener(this);
        layout.setColorSchemeResources(R.color.colorPrimaryDark);

        findViewById(R.id.guillotine_hamburger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        layout.setRefreshing(true);
        getOrders();
    }

    @Override
    public void onRefresh() {
        getOrders();
    }

    private void getOrders() {
        ServerApi.get(this).api().getOrders(DeviceInfoStore.getToken(this)).enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                layout.setRefreshing(false);
                if (response.isSuccessful()) parseOrders(response.body());
                else onInternetConnectionError();
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                layout.setRefreshing(false);
                onInternetConnectionError();
            }
        });
    }

    private void parseOrders(JsonArray array) {
        ArrayList<DeliveryOrder> collection = new ArrayList<>();
        try {
            for (int i = 0; i < array.size(); i++) {
                JsonObject object = array.get(i).getAsJsonObject().get("order").getAsJsonObject();
                JsonArray array1 = object.get("line_items").getAsJsonArray();
                ArrayList<DeliveryOrder.OrderPart> parts = new ArrayList<>();
                for (int j = 0; j < array1.size(); j++) {
                    JsonObject object1 = array1.get(j).getAsJsonObject();

                    DeliveryOrder.OrderPart part =
                            new DeliveryOrder.OrderPart(
                                    object1.get("dish").getAsJsonObject().get("name").getAsString(),
                                    object1.get("dish").getAsJsonObject().get("price").getAsInt(),
                                    object1.get("quantity").getAsInt()
                            );

                    parts.add(part);
                }

                String date_str = "";

                try {
                    Calendar calendar = Calendar.getInstance();
                    Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(
                            object.get("created_at").getAsString().split("\\.")[0]);
                    calendar.setTime(date);
                    date_str = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)) + "." +
                            Integer.toString(calendar.get(Calendar.MONTH) + 1) + "." +
                            Integer.toString(calendar.get(Calendar.YEAR));
                } catch (ParseException e) {
                    e.printStackTrace();
                }


                DeliveryOrder order = new DeliveryOrder(date_str,
                        object.get("total_price").getAsInt(), parts, object.get("id").getAsString());
                order.setOnTheWay(object.get("status").getAsString().equals("on_the_way"));
                order.setReview(object.get("review").getAsString());
                try {
                    order.setRating(object.get("rating").getAsInt());
                } catch (Exception e) {

                }
                order.setReviewd(!object.get("review").isJsonNull());

                if (!order.isOnTheWay()) {
                    collection.add(order);
                } else {
                    collection.add(0, order);
                }
            }
            if (collection.size() != 0) {
                for (int i = 0; i < collection.size(); i++) {
                    if (!collection.get(i).isOnTheWay()) {
                        collection.add(i, null);
                        break;
                    }
                }
                collection.add(0, null);
            } else {
                collection.add(0, null);
                collection.add(1, null);
            }
            adapter.setCollection(collection);
            layout.setRefreshing(false);
        } catch (Exception e) {
            layout.setRefreshing(false);
            e.printStackTrace();
        }
    }
}
