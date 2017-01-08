package com.academiaexpress.Fragments;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.academiaexpress.Activities.ProductsActivity;
import com.academiaexpress.Custom.ExpandableHeightGridView;
import com.academiaexpress.Data.DeliveryMeal;
import com.academiaexpress.Data.DeliveryOrder;
import com.academiaexpress.R;
import com.academiaexpress.Utils.Image;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

public class DishFragment extends BaseProductFragment {

    private DeliveryMeal meal;
    public static boolean answer = false;
    private DeliveryOrder.OrderPart part;

    public void animateScroll() {
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        final int screenHeight = size.y;
        ((ScrollView) getView().findViewById(R.id.scroll)).smoothScrollTo(0,
                (screenHeight - getStatusBarHeight()) / 5);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ((ScrollView) getView().findViewById(R.id.scroll)).smoothScrollTo((screenHeight - getStatusBarHeight()) / 5, 0);
            }
        }, 500);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.product_card, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        if(!meal.isCanBuy()) {
            getView().findViewById(R.id.textViewfdfdfsfs).setVisibility(View.GONE);
        } else {
            getView().findViewById(R.id.textViewfdfdfsfs).setVisibility(View.VISIBLE);
        }

        getView().findViewById(R.id.textViewfdfdfsfs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        ((TextView) getView().findViewById(R.id.textView9)).setText(meal.getMealName());
        ((TextView) getView().findViewById(R.id.textView10)).setText(meal.getIngridients().equals("null") ? "" : meal.getIngridients());
        ((TextView) getView().findViewById(R.id.textView16)).setText(meal.getDescription().equals("null") ? ""
        : meal.getDescription());
        ((TextView) getView().findViewById(R.id.textView11)).setText(Integer.toString(meal.getPrice()) + Html.fromHtml("<html>&#x20bd</html>").toString());
        Image.loadDishPhoto(meal.getPhotoLink(), ((ImageView) getView().findViewById(R.id.imageView3)));

        if(meal.getEnergy() == null) {
            getView().findViewById(R.id.en_content).setVisibility(View.GONE);
        } else {
            getView().findViewById(R.id.en_content).setVisibility(View.VISIBLE);

            String[] energy = meal.getEnergy().split("energy");

            try {
                ((TextView) getView().findViewById(R.id.textView52a)).setText(energy[0]);
                ((TextView) getView().findViewById(R.id.textView53a)).setText(energy[1]);
                ((TextView) getView().findViewById(R.id.textView54a)).setText(energy[2]);
                ((TextView) getView().findViewById(R.id.textView55a)).setText(energy[3]);
            } catch (Exception e) {

            }
        }

        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        int screenHeight = size.y;
        FrameLayout layout = (FrameLayout) getView().findViewById(R.id.main);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)layout.getLayoutParams();
        params.height = screenHeight - getStatusBarHeight();

        final LayoutInflater mInflater = LayoutInflater.from(getContext());

        BaseAdapter adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return meal.getIngridients_list().size();
            }

            @Override
            public Object getItem(int position) {
                return meal.getIngridients();
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                convertView = mInflater.inflate(R.layout.ingr_card, null);
                TextView textview = (TextView) convertView.findViewById(R.id.textView17);
                textview.setText(meal.getIngridients_list().get(position).second);

                ImageView textview2 = (ImageView) convertView.findViewById(R.id.imageView6);
                Image.loadPhoto(meal.getIngridients_list().get(position).first, textview2);

                return convertView;

            }
        };

        ExpandableHeightGridView view = (ExpandableHeightGridView) getView().findViewById(R.id.gridView);
        view.setExpanded(true);
        view.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        BaseAdapter adapter2 = new BaseAdapter() {
            @Override
            public int getCount() {
                return meal.getBadges().size();
            }

            @Override
            public Object getItem(int position) {
                return meal.getBadges();
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                convertView = mInflater.inflate(R.layout.ingr_card, null);
                TextView textview = (TextView) convertView.findViewById(R.id.textView17);
                textview.setText(meal.getBadges().get(position).second);

                ImageView textview2 = (ImageView) convertView.findViewById(R.id.imageView6);
                textview2.setPadding((int)convertDpToPixel(25f, textview.getContext()),
                        (int)convertDpToPixel(25f, textview.getContext()),
                        (int)convertDpToPixel(25f, textview.getContext()),
                        (int)convertDpToPixel(25f, textview.getContext()));
                Image.loadPhoto(meal.getBadges().get(position).first, textview2);

                return convertView;

            }
        };

        ExpandableHeightGridView view2 = (ExpandableHeightGridView) getView().findViewById(R.id.gridView2);
        view2.setExpanded(true);
        view2.setAdapter(adapter2);
        adapter2.notifyDataSetChanged();

//        view2.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                (int)(convertDpToPixel(150, getContext()) * (meal.getBadges().size() / 3 + 1))));

        getView().findViewById(R.id.textView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextView) getView().findViewById(R.id.textView)).setText("ЗАКАЗАТЬ ЕЩЕ");
                if(!answer) {
                    Answers.getInstance().logCustom(new CustomEvent("Товар добавлен"));
                }
                answer = true;
                ((ProductsActivity) getActivity()).addProduct(part);
            }
        });

    }

    public ScrollView getScrollView() {
        return (ScrollView) getView().findViewById(R.id.scroll);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result; // + (int) convertDpToPixel(56f, getContext());
    }

    public void setPart(DeliveryOrder.OrderPart part) {
        this.part = part;
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
