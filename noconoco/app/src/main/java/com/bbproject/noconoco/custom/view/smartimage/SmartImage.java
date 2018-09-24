package com.bbproject.noconoco.custom.view.smartimage;

import android.content.Context;
import android.graphics.Bitmap;

@SuppressWarnings("ALL")
interface SmartImage {
    Bitmap getBitmap(Context context, boolean black);
}