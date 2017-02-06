package com.academiaexpress.Utils;

import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class Image {
    public static void loadPhoto(final String path, final ImageView imageView) {
        if (path == null || path.isEmpty()) return;
        Picasso.with(imageView.getContext())
                .load(path)
                .fit()
                .centerCrop()
                .into(imageView);
    }

    public static void loadDishPhoto(final String path, final ImageView imageView) {
        if (path == null || path.isEmpty()) return;
        Picasso.with(imageView.getContext())
                .load(path)
                .resize(550, 1000)
                .centerCrop()
                .noFade()
                .into(imageView);
    }

    public static void loadPhoto(final int path, final ImageView imageView) {
        Picasso.with(imageView.getContext())
                .load(path)
                .fit()
                .centerCrop()
                .into(imageView);
    }
}
