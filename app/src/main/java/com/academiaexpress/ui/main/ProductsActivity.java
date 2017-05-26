package com.academiaexpress.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.academiaexpress.R;
import com.academiaexpress.data.Dish;
import com.academiaexpress.data.Order;
import com.academiaexpress.network.DeviceInfoStore;
import com.academiaexpress.network.ServerApi;
import com.academiaexpress.ui.BaseActivity;
import com.academiaexpress.ui.main.base.BaseProductFragment;
import com.academiaexpress.ui.main.adapters.SmartFragmentStatePagerAdapter;
import com.academiaexpress.ui.order.StuffFragment;
import com.academiaexpress.ui.help.HelpActivity;
import com.academiaexpress.ui.order.DeliveryFinalActivity;
import com.academiaexpress.ui.order.OrdersActivity;
import com.academiaexpress.utils.AndroidUtilities;
import com.academiaexpress.utils.Animations;
import com.academiaexpress.utils.CategoriesUtility;
import com.academiaexpress.utils.LogUtil;
import com.academiaexpress.utils.MoneyValues;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductsActivity extends BaseActivity {
    private static final int DELAY_BEFORE_FRAGMENT_ANIMATION = 800;
    private static final int ANIMATION_DURATION = 500;
    private static final float BUTTON_HIDE_OFFSET = 36f;

    private static final String EXTRA_FIRST = "first";
    private static final String EXTRA_ID = "id";
    private static final String EXTRA_ADDITIONAL = "additional";
    private boolean isStuff = false;
    private static ArrayList<Dish> products = new ArrayList<>();
    private static ArrayList<Fragment> fragments = new ArrayList<>();
    public static int product_count = 0;
    public static int price = 0;
    public static boolean canceled = false;

    private ViewPager defaultViewpager;
    private CircleIndicator defaultIndicator;
    private MyPagerAdapter defaultPagerAdapter;

    public static ArrayList<Order.OrderPart> collection = new ArrayList<>();

    public void addPart(Order.OrderPart part) {
        part.setCount(1);
        collection.add(part);
    }

    private void recalculatePrice(Order.OrderPart part) {
        price += part.getPrice();
        product_count++;
    }

    private void incrementPart(Order.OrderPart part) {
        collection.get(collection.indexOf(part)).incCount();
    }

    public void addProduct(Order.OrderPart part) {
        if (collection.indexOf(part) != -1) {
            incrementPart(part);
        } else {
            addPart(part);
        }

        recalculatePrice(part);

        showMenu();
    }

    private void iniFields() {
        fragments = new ArrayList<>();
        products.clear();
        canceled = false;
    }

    private void initElements() {
        findViewById(R.id.indicator).setVisibility(View.GONE);
        findViewById(R.id.next_btn).setVisibility(View.GONE);

        findViewById(R.id.indicator).setVisibility(View.GONE);
        findViewById(R.id.next_btn).setVisibility(View.GONE);

        findViewById(R.id.menu_layout).setVisibility(View.GONE);
    }

    private void setOnClickListeners() {
        findViewById(R.id.menu_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProductsActivity.this, EditProfileActivity.class);
                intent.putExtra(EXTRA_FIRST, false);
                startActivity(intent);
            }
        });

        findViewById(R.id.menu_help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProductsActivity.this, HelpActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.menu_orders).setOnClickListener(new View.OnClickListener() {
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
                if (!isStuff) {
                    setScrollListener(((BaseProductFragment) fragments.get(position)).getScrollView());
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void setupIfEmptyOrder() {
        findViewById(R.id.orders_indicator).setVisibility(View.GONE);
    }

    private void setupUIForMoneyValues() {
        if (MoneyValues.countOfOrders == 0) {
            setupIfEmptyOrder();
        } else {
            setupIfNotEmptyOrders();
        }
    }

    private void setupIfNotEmptyOrders() {
        ((TextView) findViewById(R.id.menu_orders)).setText(getString(R.string.orders_upcase) + " (" + Integer.toString(MoneyValues.countOfOrders) + ")");
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

        CategoriesUtility.INSTANCE.showCategoriesList(((LinearLayout) findViewById(R.id.menu_list)), this);
    }

    private void getDay() {
        isStuff = getIntent().getBooleanExtra(EXTRA_ADDITIONAL, false);

        ServerApi.get(this).api().getDishes(getIntent().getIntExtra(EXTRA_ID, 0), DeviceInfoStore.getToken(this))
                .enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if (response.isSuccessful()) {
                    parseDishes(response.body());
                } else {
                    findViewById(R.id.loading_indicator).setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                findViewById(R.id.loading_indicator).setVisibility(View.GONE);
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

    private Dish parseDish(JsonObject object) {
        LogUtil.logError(object.toString());
        return new Dish(AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("name")),
                AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("subtitle")),
                AndroidUtilities.INSTANCE.getIntFieldFromJson(object.get("price")),
                getIngredientsForDish(object),
                AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("image_url")),
                AndroidUtilities.INSTANCE.getStringFieldFromJson(object.get("description")),
                getBadges(object),
                AndroidUtilities.INSTANCE.getIntFieldFromJson(object.get("id")),
                object.get("proteins") == null || object.get("proteins").isJsonNull() ? null : parseEnergy(object),
                (object.get("out_of_stock") == null || object.get("out_of_stock").isJsonNull()) ||
                        AndroidUtilities.INSTANCE.getBooleanFieldFromJson(object.get("out_of_stock")),
                0);
    }

    @SuppressWarnings("ConstantConditions")
    private void addDishFragment(int i) {
        final BaseProductFragment fragment = new DishFragment();
        Order.OrderPart part = new Order.OrderPart(products.get(i).getPrice(),
                products.get(i).getMealName(), products.get(i).getId());
        part.setCount(0);
        fragment.setPart(part);
        fragment.setInfo(products.get(i));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fragments.add(fragment);
                defaultPagerAdapter.notifyDataSetChanged();
            }
        });
    }

    private void parseDishes(JsonArray array) {
        defaultPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        defaultViewpager.setAdapter(defaultPagerAdapter);

        for (int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).getAsJsonObject();
            products.add(parseDish(object));
            if (!isStuff) {
                addDishFragment(i);
            }
        }

        if (isStuff) {
            StuffFragment.Companion.setCollection(products);
            fragments.add(initFinalPage());
        }

        setupPages();
    }

    private void setupPages() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                defaultPagerAdapter.notifyDataSetChanged();
                defaultViewpager.setOffscreenPageLimit(fragments.size());
                if (isStuff) {
                    return;
                }

                defaultIndicator.setViewPager(defaultViewpager);
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
        }, DELAY_BEFORE_FRAGMENT_ANIMATION);
    }

    private void listenToScroll() {
        findViewById(R.id.loading_indicator).setVisibility(View.GONE);

        if (!isStuff) {
            return;
        }

        ((BaseProductFragment) fragments.get(0)).animateScroll();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setScrollListener(((BaseProductFragment) fragments.get(0)).getScrollView());
            }
        }, ANIMATION_DURATION);
    }

    private StuffFragment initFinalPage() {
        return new StuffFragment();
    }

    private void hideMenu() {
        findViewById(R.id.indicator).setVisibility(View.GONE);
        findViewById(R.id.next_btn).setVisibility(View.GONE);
    }

    private void showMenu() {
        findViewById(R.id.indicator).setVisibility(View.VISIBLE);
        findViewById(R.id.next_btn).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.indicator)).setText(Integer.toString(product_count));
    }

    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.menu_layout).setVisibility(View.GONE);

        if (product_count == 0) {
            hideMenu();
        } else {
            showMenu();
        }
    }

    public void setScrollListener(final ScrollView scrollView) {
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = scrollView.getScrollY();
                setOpacityOfElements(1 - (scrollY / AndroidUtilities.INSTANCE.dpToPx(ProductsActivity.this, BUTTON_HIDE_OFFSET)));
            }
        });
    }

    public void setOpacityOfElements(float f) {
        findViewById(R.id.logo).setAlpha(f);
        findViewById(R.id.indicator_default).setAlpha(f);
    }

    public class MyPagerAdapter extends SmartFragmentStatePagerAdapter {
        MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }
}
