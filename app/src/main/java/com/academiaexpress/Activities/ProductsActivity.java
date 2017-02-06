package com.academiaexpress.Activities;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import android.app.ProgressDialog;
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
import com.academiaexpress.Data.DeliveryMeal;
import com.academiaexpress.Data.DeliveryOrder;
import com.academiaexpress.Fragments.BaseProductFragment;
import com.academiaexpress.Fragments.DishFragment;
import com.academiaexpress.Fragments.FinalPageFragment;
import com.academiaexpress.Fragments.LunchFragment;
import com.academiaexpress.R;
import com.academiaexpress.Server.DeviceInfoStore;
import com.academiaexpress.Server.ServerApi;
import com.academiaexpress.Utils.AndroidUtilities;
import com.academiaexpress.Utils.Animations;
import com.academiaexpress.Utils.LogUtil;
import com.academiaexpress.Utils.MoneyValues;

import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductsActivity extends BaseActivity {

    private ArrayList<DeliveryMeal> products;
    private ArrayList<BaseProductFragment> fragments;
    private FinalPageFragment fragment;
    public static int product_count = 0;
    public static int price = 0;
    static boolean canceled = false;

    private ViewPager defaultViewpager;
    private CircleIndicator defaultIndicator;
    private ProgressDialog dialog;

    public static ArrayList<DeliveryOrder.OrderPart> collection = new ArrayList<>();

    public void addPart(DeliveryOrder.OrderPart part) {
        part.setCount(1);
        collection.add(part);
    }

    private void recalculatePrice(DeliveryOrder.OrderPart part) {
        price += part.getPrice();
        product_count++;
    }

    private void incrementPart(DeliveryOrder.OrderPart part) {
        collection.get(collection.indexOf(part)).incCount();
    }

    public void addProduct(DeliveryOrder.OrderPart part) {
        if (collection.indexOf(part) != -1) incrementPart(part);
        else addPart(part);

        recalculatePrice(part);

        showMenu();
    }

    private void iniFields() {
        products = new ArrayList<>();
        fragments = new ArrayList<>();
        collection = new ArrayList<>();

        collection.clear();
        product_count = 0;
        price = 0;

        canceled = false;
        dialog = new ProgressDialog(this);
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
                Intent intent = new Intent(ProductsActivity.this, EditProfileActivity.class);
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
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == products.size()) setScrollListener(fragment.getScrollView());
                else setScrollListener(fragments.get(position).getScrollView());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void setupIfEmptyOrder() {
        findViewById(R.id.textView19fd).setVisibility(View.GONE);
    }

    private void setupUIForMoneyValues() {
        if (MoneyValues.countOfOrders == 0) {
            setupIfEmptyOrder();
        } else {
            setupIfNotEmptyOrders();
        }
    }

    private void setupIfNotEmptyOrders() {
        findViewById(R.id.textView19fd).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.textView19fd)).setText(Integer.toString(MoneyValues.countOfOrders));
        ((TextView) findViewById(R.id.textView6)).setText(getString(R.string.orders_upcase) + " (" + Integer.toString(MoneyValues.countOfOrders) + ")");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        iniFields();
        initElements();
        initPager();
        setOnClickListeners();
        setupUIForMoneyValues();
        getDay();
    }

    private void getDay() {
        dialog.show();

        ServerApi.get(this).api().getDay(DeviceInfoStore.getToken(this)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    parseDay(response.body());
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

    private String parseEnergy(JsonObject object) {
        String energy = "";

        energy += AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("proteins")) + "energy";
        energy += AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("fats")) + "energy";
        energy += AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("carbohydrates")) + "energy";
        energy += AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("calories"));

        return energy;
    }

    private ArrayList<Pair<String, String>> getIngredientsForDish(JsonObject object) {
        ArrayList<Pair<String, String>> ingredients = new ArrayList<>();

        if (object.get("ingredients") != null && !object.get("ingredients").isJsonNull()) {
            JsonArray ingredientsJson = object.get("ingredients").getAsJsonArray();

            for (int j = 0; j < ingredientsJson.size(); j++) {
                JsonObject ingredient = ingredientsJson.get(j).getAsJsonObject();
                ingredients.add(new Pair<>(
                        AndroidUtilities.INSTANCE.getStringFieldFromJson(ingredient.get("image_url")),
                        AndroidUtilities.INSTANCE.getStringFieldFromJson(ingredient.get("name"))));
            }
        }

        return ingredients;
    }

    private ArrayList<Pair<String, String>> getIngredientsForLunch(JsonObject object) {
        ArrayList<Pair<String, String>> ingredients = new ArrayList<>();

        if (object.get("ingredients") != null && !object.get("ingredients").isJsonNull()) {
            JsonArray ingredientsJson = object.get("ingredients").getAsJsonArray();

            for (int j = 0; j < ingredientsJson.size(); j++) {
                JsonObject ingredient = ingredientsJson.get(j).getAsJsonObject();
                ingredients.add(new Pair<>(
                        AndroidUtilities.INSTANCE.getStringFieldFromJson(ingredient.get("name")),
                        AndroidUtilities.INSTANCE.getStringFieldFromJson(ingredient.get("weight"))));
            }
        }

        return ingredients;
    }

    private ArrayList<Pair<String, String>> getBadges(JsonObject object) {
        ArrayList<Pair<String, String>> badges = new ArrayList<>();

        if (object.get("badges") != null && !object.get("badges").isJsonNull()) {
            JsonArray ingredientsJson = object.get("badges").getAsJsonArray();

            for (int j = 0; j < ingredientsJson.size(); j++) {
                JsonObject badge = ingredientsJson.get(j).getAsJsonObject();
                badges.add(new Pair<>(
                        AndroidUtilities.INSTANCE.getStringFieldFromJson(badge.get("image_url")),
                        AndroidUtilities.INSTANCE.getStringFieldFromJson(badge.get("name"))));
            }
        }

        return badges;
    }

    private DeliveryMeal parseDish(JsonObject object) {
        return new DeliveryMeal(AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("name")),
                AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("subtitle")),
                AndroidUtilities.INSTANCE.getIntFieldFromJson(object.get("price")),
                getIngredientsForDish(object),
                AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("image_url")),
                AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("description")),
                getBadges(object),
                AndroidUtilities.INSTANCE.getIntFieldFromJson(object.get("id")),
                object.get("proteins") == null || object.get("proteins").isJsonNull() ? null : parseEnergy(object),
                (object.get("out_of_stock") == null || object.get("out_of_stock").isJsonNull()) ||
                        AndroidUtilities.INSTANCE.getBooleanFieldFromJson(object.get("out_of_stock")));
    }

    private DeliveryMeal parseLunch(JsonObject object) {
        return new DeliveryMeal(AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("name")),
                AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("subtitle")),
                AndroidUtilities.INSTANCE.getIntFieldFromJson(object.get("price")),
                getIngredientsForLunch(object),
                AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("image_url")),
                AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("description")),
                getBadges(object),
                AndroidUtilities.INSTANCE.getIntFieldFromJson(object.get("id")),
                object.get("proteins") == null || object.get("proteins").isJsonNull() ? null : parseEnergy(object),
                (object.get("out_of_stock") == null || object.get("out_of_stock").isJsonNull()) ||
                        AndroidUtilities.INSTANCE.getBooleanFieldFromJson(object.get("out_of_stock")));
    }

    @SuppressWarnings("ConstantConditions")
    private void addDishFragment(JsonArray array, int i) {
        if (products.get(array.size() + i) == null || products.get(array.size() + i).getId() == null) {
            return;
        }

        BaseProductFragment fragment = new DishFragment();
        DeliveryOrder.OrderPart part = new DeliveryOrder.OrderPart(products.get(array.size() + i).getPrice(),
                products.get(array.size() + i).getMealName(), products.get(array.size() + i).getId());
        part.setCount(0);
        fragment.setPart(part);
        fragment.setInfo(products.get(array.size() + i));
        fragments.add(fragment);
    }

    @SuppressWarnings("ConstantConditions")
    private void addLunchFragment(int i) {
        if (products.get(i) == null || products.get(i).getId() == null) {
            return;
        }

        BaseProductFragment fragment = new LunchFragment();
        DeliveryOrder.OrderPart part = new DeliveryOrder.OrderPart(products.get(i).getPrice(),
                products.get(i).getMealName(), products.get(i).getId());
        part.setCount(0);
        fragment.setPart(part);
        fragment.setInfo(products.get(i));
        fragments.add(fragment);
    }

    private void parseDishes(JsonArray array) {
        for (int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).getAsJsonObject();
            products.add(parseDish(object));
            addDishFragment(array, i);
        }
    }

    private void parseLunches(JsonArray array) {
        for (int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).getAsJsonObject();
            products.add(parseLunch(object));
            addLunchFragment(i);
        }
    }

    private void setupPages() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final DemoPagerAdapter defaultPagerAdapter = new DemoPagerAdapter(getSupportFragmentManager());
                defaultViewpager.setAdapter(defaultPagerAdapter);
                defaultViewpager.setOffscreenPageLimit(products.size() + 1);
                defaultViewpager.setAdapter(defaultPagerAdapter);
                defaultIndicator.setViewPager(defaultViewpager);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 200);
                initScrolls();
            }
        });
    }

    private void initScrolls() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    listenToScroll();
                } catch (Exception e) {
                    LogUtil.logException(e);
                }
            }
        }, 150);
    }

    private void listenToScroll() {
        fragments.get(0).animateScroll();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setScrollListener(fragments.get(0).getScrollView());
            }
        }, 600);
    }

    private void parseDay(final JsonObject object) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                parseLunches(object.get("lunches").getAsJsonArray());
                parseDishes(object.get("dishes").getAsJsonArray());
                setupPages();
            }
        }).start();
    }

    private void hideMenu() {
        findViewById(R.id.textView19).setVisibility(View.GONE);
        findViewById(R.id.next_btn).setVisibility(View.GONE);
    }

    private void showMenu() {
        findViewById(R.id.textView19).setVisibility(View.VISIBLE);
        findViewById(R.id.next_btn).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.textView19)).setText(Integer.toString(product_count));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (product_count == 0) {
            hideMenu();
        } else {
            showMenu();
        }
    }

    public void setScrollListener(final NestedScrollView scrollView) {
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = scrollView.getScrollY();
                setOpacityOfElements(1 - (scrollY / AndroidUtilities.INSTANCE.dpToPx(ProductsActivity.this, 36f)));
            }
        });
    }

    public void setScrollListener(final ScrollView scrollView) {
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = scrollView.getScrollY();
                setOpacityOfElements(1 - (scrollY / AndroidUtilities.INSTANCE.dpToPx(ProductsActivity.this, 36f)));
            }
        });
    }

    public void setOpacityOfElements(float f) {
        findViewById(R.id.imageView81).setAlpha(f);
        findViewById(R.id.indicator_default).setAlpha(f);
    }

    public class DemoPagerAdapter extends FragmentPagerAdapter {
        DemoPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i != products.size()) {
                return fragments.get(i);
            } else {
                return initFinalPage();
            }
        }

        @Override
        public int getCount() {
            return products.size() + 1;
        }

        private FinalPageFragment initFinalPage() {
            FinalPageFragment fragment = new FinalPageFragment();
            ProductsActivity.this.fragment = fragment;
            return fragment;
        }
    }
}
