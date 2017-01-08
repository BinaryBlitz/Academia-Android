package com.academiaexpress.Fragments;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.academiaexpress.Adapters.ProductsAdapter;
import com.academiaexpress.Data.DeliveryMeal;
import com.academiaexpress.Data.MiniProduct;
import com.academiaexpress.R;
import com.academiaexpress.Server.DeviceInfoStore;
import com.academiaexpress.Server.ServerApi;
import com.academiaexpress.Utils.Image;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FinalPageFragment extends Fragment {

    private DeliveryMeal meal;
    private ProductsAdapter adapter;
    private RecyclerView view;
    public static ArrayList<MiniProduct> collection = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.final_page_layout, container, false);
    }

    public NestedScrollView getScrollView() {
        return (NestedScrollView) getView().findViewById(R.id.scroll);
    }

    @Override
    public void onStart() {
        super.onStart();

        Image.loadPhoto(R.drawable.back3, (ImageView) getView().findViewById(R.id.imageView3));

        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        final int screenHeight = size.y;
        FrameLayout layout = (FrameLayout) getView().findViewById(R.id.main);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
        params.height = screenHeight - getStatusBarHeight();

        getView().findViewById(R.id.textView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((NestedScrollView) getView().findViewById(R.id.scroll)).smoothScrollTo(0, screenHeight - getStatusBarHeight());
            }
        });

        view = (RecyclerView) getView().findViewById(R.id.recyclerView);
        view.setItemAnimator(new DefaultItemAnimator());
        view.setLayoutManager(new LinearLayoutManager(getActivity()));
        view.setNestedScrollingEnabled(false);
        adapter = new ProductsAdapter(getActivity());
        view.setAdapter(adapter);

        if (collection.size() == 0) {
            getStuff();
        } else {
            adapter.setCollection(collection);
            view.getLayoutParams().height = (int) convertDpToPixel(
                    adapter.getItemCount() * 100, getContext());
        }
    }

    private void getStuff() {
        ServerApi.get(getContext()).api().getStuff(DeviceInfoStore.getToken(getContext())).enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if (response.isSuccessful()) parseStuff(response.body());
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
            }
        });
    }

    private void parseStuff(JsonArray array) {
        for (int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).getAsJsonObject();
            MiniProduct product = new MiniProduct(object.get("name").getAsString(), object.get("description").getAsString(),
                    object.get("price").getAsInt(), object.get("image_url").getAsString(),
                    object.get("id").getAsString());
            collection.add(product);
        }

        adapter.setCollection(collection);
        view.getLayoutParams().height = (int) convertDpToPixel(
                adapter.getItemCount() * 100, getContext());
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public void setInfo(DeliveryMeal meal) {
        this.meal = meal;
    }

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);
    }
}