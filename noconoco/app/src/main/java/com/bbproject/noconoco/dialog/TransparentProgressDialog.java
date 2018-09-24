package com.bbproject.noconoco.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.bbproject.noconoco.R;

import java.util.Random;

public class TransparentProgressDialog extends Dialog {

    private final static String TAG = "TransparentProgressDlg";
    private final ImageView mIv3;
    private final Context mContext;
    private final int mType;
    private ImageView mIv1;
    private Animation mAnim;
    private AnimationSet mReplaceAnimation;
    private Bitmap mBitmap;
    private boolean mOpen = false;

    public TransparentProgressDialog(Context pContext, int pType) {
        super(pContext, R.style.TransparentProgressDialog);
        mContext = pContext;
        mType = pType;
        Window w = getWindow();
        if (null != w) {
            WindowManager.LayoutParams wlmp = w.getAttributes();
            wlmp.gravity = Gravity.CENTER;
            w.setAttributes(wlmp);
            w.requestFeature(Window.FEATURE_NO_TITLE);
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTitle(null);
        setCancelable(false);
        setOnCancelListener(null);

        setContentView(R.layout.dialog_loading);
        mIv3 = findViewById(R.id.noise);
        switch (mType) {
            case 1:
                mIv1 = findViewById(R.id.oeil);
                mIv1.setVisibility(View.VISIBLE);
                break;
            case 2:
                mIv1 = findViewById(R.id.oeil2);
                mIv1.setVisibility(View.VISIBLE);
                break;
            case 3:
                mIv1 = findViewById(R.id.flower);
                mIv1.setVisibility(View.VISIBLE);
                break;
        }

    }

    @Override
    public void show() {
        super.show();
        try {
            mOpen = true;
            switch (mType) {
                case 1:
                    mReplaceAnimation = new AnimationSet(true);
                    // animations should be applied on the finish line
                    mReplaceAnimation.setFillAfter(true);

                    float dist = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, mContext.getResources().getDisplayMetrics());

                    //1
                    float posx = 0;
                    float posy = 0;
                    float pos2x = dist * (float) Math.cos(Math.toRadians(45));
                    float pos2y = dist * (float) Math.sin(Math.toRadians(45));

                    TranslateAnimation trans = new TranslateAnimation(
                            posx, pos2x,
                            posy, pos2y);
                    trans.setDuration(500);
                    trans.setInterpolator(new AccelerateInterpolator());

                    mReplaceAnimation.addAnimation(trans);

                    //2
                    posx = dist * (float) Math.cos(Math.toRadians(45));
                    posy = dist * (float) Math.sin(Math.toRadians(45));
                    pos2x = 0;
                    pos2y = -dist;

                    trans = new TranslateAnimation(
                            posx, pos2x,
                            posy, pos2y);
                    trans.setDuration(500);
                    trans.setStartOffset(1000);
                    trans.setInterpolator(new AccelerateInterpolator());

                    mReplaceAnimation.addAnimation(trans);

                    //3
                    posx = 0;
                    posy = -dist;
                    pos2x = -dist * (float) Math.cos(Math.toRadians(45));
                    pos2y = -dist * (float) Math.sin(Math.toRadians(45));

                    trans = new TranslateAnimation(
                            posx, pos2x,
                            posy, pos2y);
                    trans.setDuration(500);
                    trans.setStartOffset(1500);
                    trans.setInterpolator(new AccelerateInterpolator());

                    mReplaceAnimation.addAnimation(trans);

                    //4
                    posx = -dist * (float) Math.cos(Math.toRadians(45));
                    posy = -dist * (float) Math.sin(Math.toRadians(45));
                    pos2x = 0;
                    pos2y = dist;

                    trans = new TranslateAnimation(
                            posx, pos2x,
                            posy, pos2y);
                    trans.setDuration(500);
                    trans.setStartOffset(2000);
                    trans.setInterpolator(new AccelerateInterpolator());

                    mReplaceAnimation.addAnimation(trans);

                    //5
                    posx = 0;
                    posy = dist;
                    pos2x = dist;
                    pos2y = 0;

                    trans = new TranslateAnimation(
                            posx, pos2x,
                            posy, pos2y);
                    trans.setDuration(500);
                    trans.setStartOffset(2500);
                    trans.setInterpolator(new AccelerateInterpolator());

                    mReplaceAnimation.addAnimation(trans);

                    //6
                    posx = dist;
                    posy = 0;
                    pos2x = -dist;
                    pos2y = 0;

                    trans = new TranslateAnimation(
                            posx, pos2x,
                            posy, pos2y);
                    trans.setDuration(500);
                    trans.setStartOffset(3000);
                    trans.setInterpolator(new AccelerateInterpolator());

                    mReplaceAnimation.addAnimation(trans);

                    //7
                    posx = -dist;
                    posy = 0;
                    pos2x = dist * (float) Math.cos(Math.toRadians(45));
                    pos2y = -dist * (float) Math.sin(Math.toRadians(45));

                    trans = new TranslateAnimation(
                            posx, pos2x,
                            posy, pos2y);
                    trans.setDuration(500);
                    trans.setStartOffset(3500);
                    trans.setInterpolator(new AccelerateInterpolator());

                    mReplaceAnimation.addAnimation(trans);

                    //8
                    posx = dist * (float) Math.cos(Math.toRadians(45));
                    posy = -dist * (float) Math.sin(Math.toRadians(45));
                    pos2x = -dist * (float) Math.cos(Math.toRadians(45));
                    pos2y = dist * (float) Math.sin(Math.toRadians(45));

                    trans = new TranslateAnimation(
                            posx, pos2x,
                            posy, pos2y);
                    trans.setDuration(500);
                    trans.setStartOffset(4000);
                    trans.setInterpolator(new AccelerateInterpolator());
                    //trans.setRepeatCount(Animation.INFINITE);

                    mReplaceAnimation.addAnimation(trans);

                    //9
                    posx = -dist * (float) Math.cos(Math.toRadians(45));
                    posy = dist * (float) Math.sin(Math.toRadians(45));
                    pos2x = 0;
                    pos2y = 0;

                    trans = new TranslateAnimation(
                            posx, pos2x,
                            posy, pos2y);
                    trans.setDuration(500);
                    trans.setStartOffset(4500);
                    trans.setInterpolator(new AccelerateInterpolator());
                    mReplaceAnimation.addAnimation(trans);

                    mIv1.setAnimation(mReplaceAnimation);
                    mIv1.startAnimation(mReplaceAnimation);
                    mReplaceAnimation.setAnimationListener(new AnimationListener() {

                        public void onAnimationEnd(Animation animation) {
                            mIv1.startAnimation(mReplaceAnimation);
                        }

                        public void onAnimationRepeat(Animation animation) {
                            // ignore
                        }

                        public void onAnimationStart(Animation animation) {
                            // ignore
                        }
                    });
                    break;
                case 2:

                    mAnim = new AlphaAnimation(1, 0.9f);
                    mAnim.setInterpolator(new DecelerateInterpolator());
                    mAnim.setDuration(33);
                    mIv3.setAnimation(mAnim);
                    mIv3.startAnimation(mAnim);


                    mAnim.setAnimationListener(new AnimationListener() {

                        public void onAnimationEnd(Animation animation) {
                            nextImage();
                        }

                        public void onAnimationRepeat(Animation animation) {
                            // ignore
                        }

                        public void onAnimationStart(Animation animation) {
                            // ignore
                        }
                    });

                    loadAnimJoy();
                    break;
                case 3:
                    Animation rotation = AnimationUtils.loadAnimation(mContext, R.anim.rotate);
                    rotation.setRepeatCount(Animation.INFINITE);
                    mIv1.startAnimation(rotation);
                    break;
            }

        } catch (Exception vE) {
            Log.e(TAG, "An Exception Occured", vE);
        }
    }

    private void nextImage() {
        int widthpx = 100;
        int heightpx = 100;
        int[] pixels = new int[widthpx * heightpx];
        Random random = new Random();
        int index;
        // iteration through pixels
        for (int y = 0; y < heightpx; ++y) {
            for (int x = 0; x < widthpx; ++x) {
                // get current index in 2D-matrix
                index = y * widthpx + x;
                // get random color
                int randColor = random.nextInt(2) == 0 ? Color.BLACK : Color.WHITE;
                // OR
                pixels[index] |= randColor;
            }
        }
        mBitmap = Bitmap.createBitmap(widthpx, heightpx, Config.ARGB_8888);
        mBitmap.setPixels(pixels, 0, widthpx, 0, 0, widthpx, heightpx);
        mIv3.setImageBitmap(mBitmap);
        mIv3.startAnimation(mAnim);
    }

    private void loadAnimJoy() {
        mReplaceAnimation = new AnimationSet(false);
        mReplaceAnimation.setFillAfter(false);
        Random random = new Random();
        float margin = 50;
        float dist = 60; // (float)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45f, mContext.getResources().getDisplayMetrics()));
        float distmin = 10; //(float)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, mContext.getResources().getDisplayMetrics()));
        float randomDist = margin + random.nextInt(Math.round(dist - distmin) + 1) + distmin;
        //1
        float posx = 0;
        float posy = margin;
        float pos2x = 0;
        float pos2y = margin - randomDist;
        Log.d(TAG, "" + randomDist);
        TranslateAnimation trans = new TranslateAnimation(
                posx, pos2x,
                posy, pos2y);
        trans.setDuration((long) ((5 * randomDist)));
        trans.setInterpolator(new DecelerateInterpolator());

        //trans.setRepeatCount(Animation.INFINITE);

        mReplaceAnimation.addAnimation(trans);

        ScaleAnimation stretch = new ScaleAnimation(1.5f, 0.8f, 0.5f, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        stretch.setDuration((long) ((5 * randomDist) / 2));
        stretch.setInterpolator(new AccelerateInterpolator());
        mReplaceAnimation.addAnimation(stretch);

        stretch = new ScaleAnimation(0.8f, 1.0f, 1.2f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        stretch.setDuration((long) ((5 * randomDist) / 2));
        stretch.setStartOffset((long) ((5 * randomDist) / 2));
        stretch.setInterpolator(new DecelerateInterpolator());
        mReplaceAnimation.addAnimation(stretch);


        //2
        posx = 0;
        posy = margin - randomDist;
        pos2x = 0;
        pos2y = margin;

        trans = new TranslateAnimation(
                posx, pos2x,
                posy, pos2y);
        trans.setDuration((long) (5 * randomDist));
        trans.setStartOffset((long) (5 * randomDist));
        trans.setInterpolator(new AccelerateInterpolator());
        mReplaceAnimation.addAnimation(trans);

        stretch = new ScaleAnimation(1.0f, 0.9f, 1.0f, 1.1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        stretch.setDuration((long) (5 * randomDist) / 2);
        stretch.setStartOffset((long) (5 * randomDist) / 2);
        stretch.setInterpolator(new AccelerateInterpolator());
        mReplaceAnimation.addAnimation(stretch);

        stretch = new ScaleAnimation(0.9f, 1.5f, 1.1f, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        stretch.setDuration((long) ((5 * randomDist) / 2));
        stretch.setStartOffset((long) (3 * (5 * randomDist) / 2));
        stretch.setInterpolator(new DecelerateInterpolator());
        mReplaceAnimation.addAnimation(stretch);


        mIv1.setAnimation(mReplaceAnimation);
        mIv1.startAnimation(mReplaceAnimation);
        mReplaceAnimation.setAnimationListener(new AnimationListener() {

            public void onAnimationEnd(Animation animation) {
                loadAnimJoy();
            }

            public void onAnimationRepeat(Animation animation) {
                // ignore
            }

            public void onAnimationStart(Animation animation) {
                // ignore
            }
        });
    }

    @Override
    public void dismiss() {
        if (mOpen) {
            mOpen = false;
            try {
                super.dismiss();
                if (null != mBitmap) {
                    mBitmap.recycle();
                    mBitmap = null;
                }
            } catch (Exception vE) {
                Log.e(TAG, "An Exception occured");
            }
        }
    }

    protected void finalize() {
        if (null != mBitmap) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    public void setText(String pText) {
        TextView text = findViewById(R.id.text);
        text.setText(pText);
    }
}
