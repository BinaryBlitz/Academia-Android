package com.academiaexpress.Activities;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.util.Pair;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;
import android.widget.TextView;

import com.academiaexpress.Base.BaseActivity;
import com.academiaexpress.Custom.ProgressDialog;
import com.academiaexpress.Data.DeliveryMeal;
import com.academiaexpress.Data.DeliveryOrder;
import com.academiaexpress.Fragments.BaseProductFragment;
import com.academiaexpress.Fragments.DishFragment;
import com.academiaexpress.Fragments.FinalPageFragment;
import com.academiaexpress.Fragments.LunchFragment;
import com.academiaexpress.R;
import com.academiaexpress.Server.DeviceInfoStore;
import com.academiaexpress.Server.ServerApi;
import com.academiaexpress.Utils.Animations;
import com.academiaexpress.Utils.LogUtil;
import com.academiaexpress.Utils.MoneyValues;

import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductsActivity extends BaseActivity {

    public ArrayList<DeliveryMeal> products;
    public ArrayList<BaseProductFragment> fragments;
    FinalPageFragment fragment;
    public static int product_count = 0;
    public static int price = 0;
    static boolean canceled = false;

    private ViewPager defaultViewpager;
    private CircleIndicator defaultIndicator;

    public static ArrayList<DeliveryOrder.OrderPart> collection = new ArrayList<>();

    public void addProduct(DeliveryOrder.OrderPart part) {
        if(collection.indexOf(part) != -1) {
            collection.get(collection.indexOf(part)).incCount();
        } else {
            part.setCount(1);
            collection.add(part);
        }
        price += part.getPrice();
        product_count++;
        findViewById(R.id.textView19).setVisibility(View.VISIBLE);
        findViewById(R.id.next_btn).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.textView19)).setText(Integer.toString(product_count));
    }

    private void iniFields() {
        products = new ArrayList<>();
        fragments = new ArrayList<>();
        collection = new ArrayList<>();

        collection.clear();
        product_count = 0;
        price = 0;

        canceled = false;
    }

    private void initElements() {
        findViewById(R.id.textView19).setVisibility(View.GONE);
        findViewById(R.id.next_btn).setVisibility(View.GONE);

        findViewById(R.id.textView19).setVisibility(View.GONE);
        findViewById(R.id.next_btn).setVisibility(View.GONE);

        findViewById(R.id.menu_layout).setVisibility(View.GONE);
    }

    private void setOnClickListeners() {
        findViewById(R.id.textView2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProductsActivity.this, AfterProfileActivity.class);
                intent.putExtra("first", false);
                startActivity(intent);
            }
        });

        findViewById(R.id.textView5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProductsActivity.this, HelpActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.textView6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProductsActivity.this, OrdersActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.next_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProductsActivity.this, DeliveryFinalActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.content_hamburger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        Animations.animateRevealShow(findViewById(R.id.menu_layout), ProductsActivity.this);
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
    }

    private void initPager() {
        defaultViewpager = (ViewPager) findViewById(R.id.viewpager_default);
        defaultIndicator = (CircleIndicator) findViewById(R.id.indicator_default);

        defaultViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                if (position == products.size()) setScrollListener(fragment.getScrollView());
                else setScrollListener(fragments.get(position).getScrollView());
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });
    }

    private void setStatistics() {
        if(MoneyValues.countOfOrders == 0) {
            findViewById(R.id.textView19fd).setVisibility(View.GONE);
        } else {
            findViewById(R.id.textView19fd).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.textView19fd)).setText(Integer.toString(MoneyValues.countOfOrders));
            ((TextView) findViewById(R.id.textView6)).setText(
                    "ЗАКАЗЫ" + " (" + Integer.toString(MoneyValues.countOfOrders) + ")");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.food_layout);

        iniFields();
        initElements();
        initPager();
        setOnClickListeners();
        setStatistics();
        getDay();
    }

    private void getDay() {
        final ProgressDialog dialog = new ProgressDialog();
        dialog.show(getFragmentManager(), "delivery");

        ServerApi.get(this).api().getDay(DeviceInfoStore.getToken(this)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                dialog.dismiss();
                if (response.isSuccessful()) parseDay(response.body());
                else onInternetConnectionError();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                dialog.dismiss();
                onInternetConnectionError();
            }
        });
    }

    private void parseDay(JsonObject object) {
        JsonArray array1 = object.get("dishes").getAsJsonArray();
        JsonArray array2 = object.get("lunches").getAsJsonArray();

        for (int i = 0; i < array2.size(); i++) {
            JsonObject object1 = array2.get(i).getAsJsonObject();
            String energy = "";

            energy += object1.get("proteins").getAsString() + "energy";
            energy += object1.get("fats").getAsString() + "energy";
            energy += object1.get("carbohydrates").getAsString() + "energy";
            energy += object1.get("calories").getAsString();

            ArrayList<Pair<String, String>> ingr = new ArrayList<>();
            if (!object1.get("ingredients").isJsonNull()) {
                JsonArray ingr_json = object1.get("ingredients").getAsJsonArray();

                for (int j = 0; j < ingr_json.size(); j++) {
                    JsonObject in = ingr_json.get(j).getAsJsonObject();
                    ingr.add(new Pair<>(
                            in.get("name").getAsString(),
                            in.get("weight").getAsString()));
                }
            }
            ArrayList<Pair<String, String>> bages = new ArrayList<>();
            if (!object1.get("badges").isJsonNull()) {
                JsonArray bages_json = object1.get("badges").getAsJsonArray();

                for (int j = 0; j < bages_json.size(); j++) {
                    JsonObject in = bages_json.get(j).getAsJsonObject();
                    bages.add(new Pair<>(
                            in.get("image_url").getAsString(),
                            in.get("name").getAsString()));
                }
            }

            products.add(new DeliveryMeal(object1.get("name").getAsString(),
                    object1.get("subtitle").getAsString(),
                    object1.get("price").getAsInt(),
                    ingr,
                    object1.get("image_url").getAsString(),
                    object1.get("description").getAsString(),
                    bages,
                    object1.get("id").getAsString(),
                    object1.get("proteins").isJsonNull() ? null : energy,
                    object1.get("out_of_stock").isJsonNull() ? true : object1.get("out_of_stock").getAsBoolean()
            ));

            BaseProductFragment fragment = new LunchFragment();
            DeliveryOrder.OrderPart part = new DeliveryOrder.OrderPart(products.get(i).getPrice(),
                    products.get(i).getMealName(), products.get(i).getId());
            part.setCount(0);
            fragment.setPart(part);
            fragment.setInfo(products.get(i));
            fragments.add(fragment);
        }

        for (int i = 0; i < array1.size(); i++) {
            JsonObject object1 = array1.get(i).getAsJsonObject();
            ArrayList<Pair<String, String>> ingr = new ArrayList<>();

            String energy = "";

            energy += object1.get("proteins").getAsString() + "energy";
            energy += object1.get("fats").getAsString() + "energy";
            energy += object1.get("carbohydrates").getAsString() + "energy";
            energy += object1.get("calories").getAsString();

            if (!object1.get("ingredients").isJsonNull()) {
                JsonArray ingr_json = object1.get("ingredients").getAsJsonArray();

                for (int j = 0; j < ingr_json.size(); j++) {
                    JsonObject in = ingr_json.get(j).getAsJsonObject();
                    ingr.add(new Pair<>(
                            in.get("image_url").getAsString(),
                            in.get("name").getAsString()));
                }
            }
            ArrayList<Pair<String, String>> bages = new ArrayList<>();
            if (!object1.get("badges").isJsonNull()) {
                JsonArray bages_json = object1.get("badges").getAsJsonArray();

                for (int j = 0; j < bages_json.size(); j++) {
                    JsonObject in = bages_json.get(j).getAsJsonObject();
                    bages.add(new Pair<>(
                            in.get("image_url").getAsString(),
                            in.get("name").getAsString()));
                }
            }


            products.add(new DeliveryMeal(object1.get("name").getAsString(),
                    object1.get("subtitle").getAsString(),
                    object1.get("price").getAsInt(),
                    ingr,
                    object1.get("image_url").getAsString(),
                    object1.get("description").getAsString(),
                    bages,
                    object1.get("id").getAsString(),
                    object1.get("proteins").isJsonNull() ? null : energy,
                    object1.get("out_of_stock").isJsonNull() ? true : object1.get("out_of_stock").getAsBoolean()
            ));

            BaseProductFragment fragment = new DishFragment();
            DeliveryOrder.OrderPart part = new DeliveryOrder.OrderPart(products.get(array2.size() + i).getPrice(),
                    products.get(array2.size() + i).getMealName(), products.get(array2.size() + i).getId());
            part.setCount(0);
            fragment.setPart(part);
            fragment.setInfo(products.get(array2.size() + i));
            fragments.add(fragment);
        }

        final DemoPagerAdapter defaultPagerAdapter = new DemoPagerAdapter(getSupportFragmentManager());
        defaultViewpager.setAdapter(defaultPagerAdapter);
        defaultIndicator.setViewPager(defaultViewpager);
        defaultViewpager.setOffscreenPageLimit(products.size() + 1);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    fragments.get(0).animateScroll();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setScrollListener(fragments.get(0).getScrollView());
                        }
                    }, 800);
                } catch (Exception e) {LogUtil.logException(e); }
            }
        }, 550);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(product_count == 0) {
            findViewById(R.id.textView19).setVisibility(View.GONE);
            findViewById(R.id.next_btn).setVisibility(View.GONE);
        } else {
            findViewById(R.id.textView19).setVisibility(View.VISIBLE);
            findViewById(R.id.next_btn).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.textView19)).setText(Integer.toString(product_count));
        }
    }

    public void setScrollListener(final NestedScrollView scrollView) {
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = scrollView.getScrollY(); //for verticalScrollView
                setOpacityOfElements(1 - (scrollY / DishFragment.convertDpToPixel(36f, ProductsActivity.this)));
            }
        });
    }

    public void setScrollListener(final ScrollView scrollView) {
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = scrollView.getScrollY(); //for verticalScrollView
                setOpacityOfElements(1 - (scrollY / DishFragment.convertDpToPixel(36f, ProductsActivity.this)));
            }
        });
    }

    public void setOpacityOfElements(float f) {
        findViewById(R.id.imageView81).setAlpha(f);
        findViewById(R.id.indicator_default).setAlpha(f);
    }

    public class DemoPagerAdapter extends FragmentPagerAdapter {
        public DemoPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if(i != products.size()) {
                return fragments.get(i);
            } else {
                FinalPageFragment fragment = new FinalPageFragment();
                ProductsActivity.this.fragment = fragment;
                return fragment;
            }
        }

        @Override
        public int getCount() {
            return products.size() + 1;
        }
    }
}
