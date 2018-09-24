package com.bbproject.noconoco.custom.view.smartimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("ALL")
public class SmartImageView extends AppCompatImageView {
    private static final int LOADING_THREADS = 4;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(LOADING_THREADS);

    private SmartImageTask currentTask;
    private String mUrl = "";
    private boolean mAdjustHorizontal = false;
    private boolean mAdjustVertical = false;
    private int mAdjustX = -1;
    private int mAdjustY = -1;

    public SmartImageView(Context context) {
        super(context);
    }

    public SmartImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SmartImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public static void cancelAllTasks() {
        threadPool.shutdownNow();
        threadPool = Executors.newFixedThreadPool(LOADING_THREADS);
    }

    public String getImageUrl() {
        return mUrl;
    }

    // Helpers to set image by URL
    public void setImageUrl(String url) {
        setImage(new WebImage(url));
    }

    public void setImageUrl(String url, final boolean black) {
        mUrl = url;
        setImage(new WebImage(url), black);
    }

    public void setImageUrl(String url, final boolean black, SmartImageTask.OnCompleteListener completeListener) {
        mUrl = url;
        setImage(new WebImage(url), black, completeListener);
    }

    public void setImageUrl(String url, final Integer fallbackResource, final boolean black) {
        mUrl = url;
        setImage(new WebImage(url), fallbackResource, black);
    }

    public void setImageUrl(String url, final Integer fallbackResource, final boolean black, SmartImageTask.OnCompleteListener completeListener) {
        mUrl = url;
        setImage(new WebImage(url), fallbackResource, black, completeListener);
    }

    public void setImageUrl(String url, final Integer fallbackResource, final Integer loadingResource, final boolean black) {
        mUrl = url;
        setImage(new WebImage(url), fallbackResource, loadingResource, black);
    }

    public void setImageUrl(String url, final Integer fallbackResource, final Integer loadingResource, final boolean black, SmartImageTask.OnCompleteListener completeListener) {
        mUrl = url;
        setImage(new WebImage(url), fallbackResource, loadingResource, black, completeListener);
    }

    // Helpers to set image by contact address book id
    public void setImageContact(long contactId) {
        setImage(new ContactImage(contactId));
    }

    public void setImageContact(long contactId, final boolean black) {
        setImage(new ContactImage(contactId), black);
    }

    public void setImageContact(long contactId, final Integer fallbackResource, final boolean black) {
        setImage(new ContactImage(contactId), fallbackResource, black);
    }

    public void setImageContact(long contactId, final Integer fallbackResource, final Integer loadingResource, final boolean black) {
        setImage(new ContactImage(contactId), fallbackResource, fallbackResource, black);
    }

    // Set image using SmartImage object
    public void setImage(final SmartImage image) {
        setImage(image, null, null, false, null);
    }

    public void setImage(final SmartImage image, final boolean black) {
        setImage(image, null, null, black, null);
    }

    public void setImage(final SmartImage image, final boolean black, final SmartImageTask.OnCompleteListener completeListener) {
        setImage(image, null, null, black, completeListener);
    }

    public void setImage(final SmartImage image, final Integer fallbackResource, final boolean black) {
        setImage(image, fallbackResource, fallbackResource, black, null);
    }

    public void setImage(final SmartImage image, final Integer fallbackResource, final boolean black, SmartImageTask.OnCompleteListener completeListener) {
        setImage(image, fallbackResource, fallbackResource, black, completeListener);
    }

    public void setImage(final SmartImage image, final Integer fallbackResource, final Integer loadingResource, final boolean black) {
        setImage(image, fallbackResource, loadingResource, black, null);
    }

    public void setImage(final SmartImage image, final Integer fallbackResource, final Integer loadingResource, final boolean black, final SmartImageTask.OnCompleteListener completeListener) {

        // Set a loading resource
        if (loadingResource != null) {
            setImageResource(loadingResource);
        }

        // Cancel any existing tasks for this image view
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }

        // Set up the new task
        currentTask = new SmartImageTask(getContext(), image, black);
        currentTask.setOnCompleteHandler(new CompleteHandler(this, fallbackResource, completeListener));

        // Run the task in a threadpool
        threadPool.execute(currentTask);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mAdjustHorizontal) {
            setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth() * mAdjustY / mAdjustX); //Snap to width
        } else if (mAdjustVertical) {
            setMeasuredDimension(getMeasuredHeight() * mAdjustX / mAdjustY, getMeasuredHeight()); //Snap to height
        }
    }

    public void setAdjustHorizontal(boolean pBool) {
        mAdjustHorizontal = pBool;
    }

    public void setAdjustVertical(boolean pBool) {
        mAdjustVertical = pBool;
    }

    public void setAdjustSizeImage(int x, int y) {
        mAdjustX = x;
        mAdjustY = y;
    }

    private static class CompleteHandler extends SmartImageTask.OnCompleteHandler {
        private final WeakReference<SmartImageView> mView;
        private final Integer fallbackResource;
        private final SmartImageTask.OnCompleteListener completeListener;

        private CompleteHandler(SmartImageView pView, Integer pFallbackResource, SmartImageTask.OnCompleteListener pCompleteListener) {
            mView = new WeakReference<>(pView);
            fallbackResource = pFallbackResource;
            completeListener = pCompleteListener;
        }

        @Override
        public void onComplete(Bitmap bitmap) {
            if (bitmap != null) {
                mView.get().setImageBitmap(bitmap);
            } else {
                // Set fallback resource
                if (fallbackResource != null) {
                    mView.get().setImageResource(fallbackResource);
                }
            }

            if (completeListener != null) {
                completeListener.onComplete(bitmap);
            }
        }
    }
}
