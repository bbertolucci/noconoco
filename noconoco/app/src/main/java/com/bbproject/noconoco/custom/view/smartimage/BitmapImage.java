package com.bbproject.noconoco.custom.view.smartimage;

import android.content.Context;
import android.graphics.Bitmap;

@SuppressWarnings("ALL")
public class BitmapImage implements SmartImage {
    private Bitmap bitmap;

    public BitmapImage(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap(Context context, boolean black) {
        return bitmap;
    }
}