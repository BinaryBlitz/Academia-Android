package com.academiaexpress.utils;

import android.app.Activity;
import android.graphics.Point;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.academiaexpress.ui.views.circularReveal.animation.SupportAnimator;
import com.academiaexpress.ui.views.circularReveal.animation.ViewAnimationUtils;
import com.nineoldandroids.view.ViewPropertyAnimator;

@SuppressWarnings("unused")
public class Animations {

    //region Circular Reveal
    public static void animateRevealShow(final View v, Activity activity) {
        int cx = 0;
        int cy = 0;

        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        int finalRadius = Math.max(width, height);
        SupportAnimator animator =
                ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, finalRadius);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(350);
        animator.addListener(new SupportAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart() {
                v.setVisibility(View.VISIBLE);
                v.bringToFront();
            }

            @Override
            public void onAnimationEnd() { }

            @Override
            public void onAnimationCancel() { }

            @Override
            public void onAnimationRepeat() { }
        });
        animator.start();
    }

    public static void animateRevealHide(final View v) {
        int cx = 0;
        int cy = 0;

        int finalRadius = v.getWidth();

        SupportAnimator animator =
                ViewAnimationUtils.createCircularReveal(v, cx, cy, finalRadius, 0);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(350);
        animator.addListener(new SupportAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart() { }

            @Override
            public void onAnimationEnd() {
                v.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel() { }

            @Override
            public void onAnimationRepeat() { }
        });
        animator.start();
    }
    //endregion

    public static void showView(final View fa_button) {
        fa_button.setVisibility(View.VISIBLE);
        ViewPropertyAnimator.animate(fa_button).cancel();
        ViewPropertyAnimator.animate(fa_button).scaleX(1).scaleY(1).setDuration(200).start();
    }

    public static void hideView(final View fa_button) {
        ViewPropertyAnimator.animate(fa_button).cancel();
        ViewPropertyAnimator.animate(fa_button).scaleX(0).scaleY(0).setDuration(200).start();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fa_button.setVisibility(View.GONE);
            }
        }, 200);
    }
}
