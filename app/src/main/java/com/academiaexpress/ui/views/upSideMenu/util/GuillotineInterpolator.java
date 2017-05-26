package com.academiaexpress.ui.views.upSideMenu.util;

import android.animation.TimeInterpolator;

public class GuillotineInterpolator implements TimeInterpolator {

    public static final float ROTATION_TIME = 0.46667f;
    public static final float FIRST_BOUNCE_TIME = 0.26666f;
    public static final float SECOND_BOUNCE_TIME = 0.26667f;


    public GuillotineInterpolator() {
    }

    public float getInterpolation(float t) {
        return rotation(t);
    }

    private float rotation(float t) {
        return 1f * t;
    }

    private float firstBounce(float t) {
        return 2.5f * t * t - 3f * t + 1.85556f;
    }

    private float secondBounce(float t) {
        return 0.625f * t * t - 1.08f * t + 1.458f;
    }
}
