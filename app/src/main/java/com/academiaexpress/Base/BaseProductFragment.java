package com.academiaexpress.Base;

import android.support.v4.app.Fragment;
import android.widget.ScrollView;

import com.academiaexpress.Data.Meal;
import com.academiaexpress.Data.Order;

public abstract class BaseProductFragment extends Fragment {
    public ScrollView getScrollView() {
        return null;
    }
    public void setPart(Order.OrderPart part) {}
    public void setInfo(Meal meal) {}
    public void animateScroll() {}
}
