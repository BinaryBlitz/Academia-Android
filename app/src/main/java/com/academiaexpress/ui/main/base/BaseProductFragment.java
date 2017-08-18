package com.academiaexpress.ui.main.base;

import android.support.v4.app.Fragment;
import android.widget.ScrollView;

import com.academiaexpress.data.Dish;
import com.academiaexpress.data.Order;

public abstract class BaseProductFragment extends Fragment {
    public ScrollView getScrollView() {
        return null;
    }
    public void setPart(Order.OrderPart part) {}
    public void setInfo(Dish dish) {}
    public void animateScroll() {}
}
