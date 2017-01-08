package com.academiaexpress.Fragments;

import android.support.v4.app.Fragment;
import android.widget.ScrollView;

import com.academiaexpress.Data.DeliveryMeal;
import com.academiaexpress.Data.DeliveryOrder;

public abstract class BaseProductFragment extends Fragment {
    public ScrollView getScrollView() {
        return null;
    }
    public void setPart(DeliveryOrder.OrderPart part) {}
    public void setInfo(DeliveryMeal meal) {}
    public void animateScroll() {}
}
