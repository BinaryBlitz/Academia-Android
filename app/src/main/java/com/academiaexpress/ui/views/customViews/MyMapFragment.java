package com.academiaexpress.ui.views.customViews;

import com.google.android.gms.maps.SupportMapFragment;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.academiaexpress.ui.order.MapActivity;

public class MyMapFragment {
    public static class TouchableWrapper extends FrameLayout {

        private long lastTouched = 0;
        private static final long SCROLL_TIME = 200L;
        private UpdateMapAfterUserInteraction updateMapAfterUserInteraction;

        public TouchableWrapper(Context context) {
            super(context);
            try {
                updateMapAfterUserInteraction = (MapActivity) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString() + " must implement UpdateMapAfterUserInterection");
            }
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastTouched = SystemClock.uptimeMillis();
                    break;
                case MotionEvent.ACTION_UP:
                    final long now = SystemClock.uptimeMillis();
                    if (now - lastTouched > SCROLL_TIME) {
                        // Update the map
                        updateMapAfterUserInteraction.onUpdateMapAfterUserInteraction();
                    }
                    break;
            }
            return super.dispatchTouchEvent(ev);
        }

        public interface UpdateMapAfterUserInteraction {
            void onUpdateMapAfterUserInteraction();
        }
    }

    public static class MySupportMapFragment extends SupportMapFragment {
        public View mOriginalContentView;
        public TouchableWrapper mTouchView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
            mOriginalContentView = super.onCreateView(inflater, parent, savedInstanceState);
            mTouchView = new TouchableWrapper(getActivity());
            mTouchView.addView(mOriginalContentView);
            return mTouchView;
        }

        @Override
        public View getView() {
            return mOriginalContentView;
        }
    }
}
