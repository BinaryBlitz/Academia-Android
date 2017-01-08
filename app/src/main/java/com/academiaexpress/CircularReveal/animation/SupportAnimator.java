package com.academiaexpress.CircularReveal.animation;

import android.view.animation.Interpolator;

public abstract class SupportAnimator {

    public abstract Object get();

    public abstract void start();

    public abstract void setDuration(int duration);

    public abstract void setInterpolator(Interpolator value);

    public abstract void addListener(AnimatorListener listener);

    public interface AnimatorListener {
        void onAnimationStart();
        void onAnimationEnd();
        void onAnimationCancel();
        void onAnimationRepeat();
    }

}
