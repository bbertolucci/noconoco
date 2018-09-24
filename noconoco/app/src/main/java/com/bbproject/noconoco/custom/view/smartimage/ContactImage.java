package com.bbproject.noconoco.custom.view.smartimage;

import java.io.InputStream;

import android.content.ContentUris;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.ContactsContract;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;

@SuppressWarnings("ALL")
public class ContactImage implements SmartImage {
    private long contactId;

    public ContactImage(long contactId) {
        this.contactId = contactId;
    }

    public Bitmap getBitmap(Context context, boolean black) {
        Bitmap bitmap = null;
        ContentResolver contentResolver = context.getContentResolver();

        try {
            Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(contentResolver, uri);
            if (input != null) {
                /*BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize =8;*/
                bitmap = BitmapFactory.decodeStream(input);//,null,options);
                if (black) bitmap = convertColorIntoBlackAndWhiteImage(bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    private Bitmap convertColorIntoBlackAndWhiteImage(Bitmap orginalBitmap) {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);

        ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(
                colorMatrix);

        Bitmap blackAndWhiteBitmap = orginalBitmap.copy(
                Bitmap.Config.ARGB_8888, true);

        Paint paint = new Paint();
        paint.setColorFilter(colorMatrixFilter);

        Canvas canvas = new Canvas(blackAndWhiteBitmap);
        canvas.drawBitmap(blackAndWhiteBitmap, 0, 0, paint);

        return blackAndWhiteBitmap;
    }
}