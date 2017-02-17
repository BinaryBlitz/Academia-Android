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
import com.academiaexpress.Utils.AndroidUtilities;
import com.academiaexpress.Utils.Image;
import com.academiaexpress.Utils.LogUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrdersActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    private OrdersAdapter adapter;
    private SwipeRefreshLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        initElements();
        setOnClickListeners();
        initRecyclerView();
        initSwipeRefresh();
    }

    private void setOnClickListeners() {
        findViewById(R.id.guillotine_hamburger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initElements() {
        Image.loadPhoto(R.drawable.back1, (ImageView) findViewById(R.id.background));
    }

    private void initRecyclerView() {
        RecyclerView view = (RecyclerView) findViewById(R.id.recyclerView);
        view.setItemAnimator(new DefaultItemAnimator());
        view.setLayoutManager(new LinearLayoutManager(this));
        view.setHasFixedSize(true);

        adapter = new OrdersAdapter(this);
        view.setAdapter(adapter);
    }

    private void initSwipeRefresh() {
        layout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        layout.setOnRefreshListener(this);
        layout.setColorSchemeResources(R.color.colorPrimaryDark);
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
                if (response.isSuccessful()) {
                    parseOrders(response.body());
                } else {
                    onInternetConnectionError();
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                layout.setRefreshing(false);
                onInternetConnectionError();
            }
        });
    }

    private DeliveryOrder parseOrder(JsonObject object, ArrayList<DeliveryOrder.OrderPart> parts) {
        DeliveryOrder order = new DeliveryOrder(parseDate(AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("created_at"))),
                AndroidUtilities.INSTANCE.getIntFieldFromJson(object.get("total_price")), parts,
                AndroidUtilities.INSTANCE.getIntFieldFromJson(object.get("id")));

        order.setOnTheWay(AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("status")).equals("on_the_way"));
        order.setReview(AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("review")));
        order.setRating(AndroidUtilities.INSTANCE.getIntFieldFromJson(object.get("rating")));
        order.setReviewd(!object.get("review").isJsonNull());

        return order;
    }

    private void parseCollection(ArrayList<DeliveryOrder> collection, JsonArray array) {
        for (int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).getAsJsonObject().get("order").getAsJsonObject();
            JsonArray items = object.get("line_items").getAsJsonArray();
            ArrayList<DeliveryOrder.OrderPart> parts = new ArrayList<>();
            for (int j = 0; j < items.size(); j++) {
                parts.add(parsePart(items.get(j).getAsJsonObject()));
            }

            DeliveryOrder order = parseOrder(object, parts);
            if (!order.isOnTheWay()) {
                collection.add(order);
            } else {
                collection.add(0, order);
            }
        }
    }

    private void parseOrders(JsonArray array) {
        ArrayList<DeliveryOrder> collection = new ArrayList<>();
        try {
            parseCollection(collection, array);
            addHeaderElements(collection);
            adapter.setCollection(collection);
        } catch (Exception e) {
            LogUtil.logException(e);
        }
    }

    private void addHeaderElements(ArrayList<DeliveryOrder> collection) {
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
    }

    private DeliveryOrder.OrderPart parsePart(JsonObject object) {
        return new DeliveryOrder.OrderPart(
                        AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("dish").getAsJsonObject().get("name")),
                        AndroidUtilities.INSTANCE.getIntFieldFromJson(object.get("dish").getAsJsonObject().get("price")),
                        AndroidUtilities.INSTANCE.getIntFieldFromJson(object.get("quantity")));
    }

    private Date parseDate(String str) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(str.split("\\.")[0]);
        } catch (ParseException e) {
            return null;
        }
    }
}
