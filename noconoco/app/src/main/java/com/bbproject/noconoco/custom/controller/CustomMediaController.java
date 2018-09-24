package com.bbproject.noconoco.custom.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.bbproject.noconoco.R;

import java.lang.ref.WeakReference;
import java.util.Locale;

@SuppressWarnings("ALL")
public class CustomMediaController extends MediaController {

    private static final String TAG = "CustomMediaController";
    private static final int sDefaultTimeout = 3000;
    private static final int SHOW_PROGRESS = 2;
    private static final int FADE_OUT = 1;
    private final MyHandler mHandler;
    private final PauseCrtl mDelegate;
    private MediaPlayerControl mPlayer;
    private boolean mDragging;
    private SeekBar mProgress;
    private View mRoot;
    private ImageButton mPauseButton;
    private TextView mText;
    private TextView mTextTotal;
    private boolean mLock = false;
    private boolean mShowing = false;
    private final OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar pBar) {
            if (mDelegate.isPlayable())
                show(3600000);

            mDragging = true;
            mHandler.removeMessages(SHOW_PROGRESS);
        }

        public void onProgressChanged(SeekBar pBar, int pProgress, boolean pFromuser) {
            if (null != mPlayer) {
                long duration = mPlayer.getDuration();
                long newPosition = (duration * pProgress) / 1000L;
                showProgressText(newPosition);
            }
            if (!pFromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }
            Log.d(TAG, "onProgressChanged>Hide");
            mDelegate.hideBuffering();
            if (null != mPlayer) {
                long duration = mPlayer.getDuration();
                long newPosition = (duration * pProgress) / 1000L;
                mPlayer.seekTo((int) newPosition);
            }
            if (mDelegate.getIsBuffering()) {
                mDelegate.hideBuffering();
            }
        }

        @SuppressLint({"NewApi", "ObsoleteSdkInt"})
        private void showProgressText(long millisecond) {
            if (null != mText) {
                long second = (millisecond / 1000) % 60;
                long minute = (millisecond / (1000 * 60)) % 60;
                long hour = (millisecond / (1000 * 60 * 60)) % 24;

                String time = String.format(Locale.FRENCH, "%02d:%02d:%02d", hour, minute, second);
                //String what_to_say = String.valueOf(how_many);
                mText.setText(time);

                long duration = mPlayer.getDuration();
                second = (duration / 1000) % 60;
                minute = (duration / (1000 * 60)) % 60;
                hour = (duration / (1000 * 60 * 60)) % 24;
                time = String.format(Locale.FRENCH, "%02d:%02d:%02d", hour, minute, second);
                mTextTotal.setText(time);

                int seek_label_pos = ((((mProgress.getRight() - mProgress.getLeft() - mProgress.getPaddingRight() - mProgress.getPaddingLeft()) * mProgress.getProgress()) / mProgress.getMax()) + mProgress.getLeft() + mProgress.getPaddingLeft());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mText.setX(seek_label_pos - mText.getWidth() / 2);
                } else {
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); //The WRAP_CONTENT parameters can be replaced by an absolute width and height or the FILL_PARENT option)
                    params.leftMargin = seek_label_pos - mText.getWidth() / 2;
                    mText.setLayoutParams(params);
                }

            } else {
                mText = findViewById(R.id.remaining_time);
                mTextTotal = findViewById(R.id.total_time);
            }
        }

        public void onStopTrackingTouch(SeekBar pBar) {
            mDragging = false;
            setProgress();
            updatePausePlay();
            if (mDelegate.isPlayable())
                show(sDefaultTimeout);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };
    private final View.OnClickListener mPauseListener = new View.OnClickListener() {
        public void onClick(View v) {
            doPauseResume();
            if (mDelegate.isPlayable())
                show(sDefaultTimeout);
        }
    };
    /*private View.OnClickListener mFullListener = new View.OnClickListener() {
        public void onClick(View v) {
            mDelegate.changeScreen(Configuration.ORIENTATION_LANDSCAPE);
            if (mDelegate.isPlayable())
            	show(sDefaultTimeout);
        }
    };*/
    private final View.OnClickListener mSoundListener = new View.OnClickListener() {
        public void onClick(View v) {
            Log.d(TAG, "show sound");
            mDelegate.showSoundControl();
            if (mDelegate.isPlayable())
                show(sDefaultTimeout);
        }
    };

    public CustomMediaController(Context pContext) {
        super(pContext, false);
        mDelegate = (PauseCrtl) pContext;
        mHandler = new MyHandler(this);
    }

    @Override
    public void setMediaPlayer(MediaPlayerControl player) {
        super.setMediaPlayer(player);
        mPlayer = player;
        updatePausePlay();
    }

    public void lock() {
        mLock = true;
    }

    public void unlock() {
        mLock = false;
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public void setAnchorView(View pView) {
        super.setAnchorView(pView);

        pView.setOnTouchListener(new OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (!mLock) {
                        Log.d(TAG, "enter click" + mDelegate.isPlayable());
                        if ((null != mPlayer) && ((mShowing && mPlayer.isPlaying()) || (!mPlayer.isPlaying()))) {
                            doPauseResume();
                        } else {
                            if (mDelegate.isPlayable())
                                show(sDefaultTimeout);
                        }
                    }
                }
                return true;
            }
        });

        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        frameParams.gravity = Gravity.TOP | Gravity.RIGHT;

        removeAllViews();
        View v = makeControllerView();
        addView(v, frameParams);
    }

    private View makeControllerView() {
        LayoutInflater inflate = (LayoutInflater) mDelegate.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (null == inflate) {
            throw new AssertionError("LayoutInflater not found.");
        }
        mRoot = inflate.inflate(R.layout.view_media_controller, this, false);

        initControllerView(mRoot);

        return mRoot;
    }

    private void initControllerView(View pView) {

        mPauseButton = pView.findViewById(R.id.pause);
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }
        mProgress = pView.findViewById(R.id.mediacontroller_progress);
        if (mProgress != null) {
            mText = findViewById(R.id.remaining_time);
            mTextTotal = findViewById(R.id.total_time);
            mProgress.setOnSeekBarChangeListener(mSeekListener);
            mProgress.setMax(1000);
        }

        ImageButton soundButton = pView.findViewById(R.id.sound);
        if (soundButton != null) {
            soundButton.requestFocus();
            soundButton.setOnClickListener(mSoundListener);
        }
    }

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
        }

        return position;
    }

    public void updatePausePlay() {
        if (mRoot == null || mPauseButton == null)
            return;

        if (null != mPlayer && mPlayer.isPlaying()) {
            mPauseButton.setBackgroundResource(R.drawable.icon_pause);
            Log.d(TAG, "updatePausePlay>Play");
        } else {
            mPauseButton.setBackgroundResource(R.drawable.icon_play_3);
            Log.d(TAG, "updatePausePlay>Pause");
        }
    }

    private void doPauseResume() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            Log.d(TAG, "doPauseResume>Hide");
            mDelegate.pauseAction();
        } else {
            mPlayer.start();
            mDelegate.playAction();
        }
        updatePausePlay();
    }

    @Override
    public void show(int pTimeout) {
        super.show(pTimeout);
        if (!mShowing) mShowing = true;
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
        updatePausePlay();
    }

    public void setSecondaryProgress(int pPercent) {
        mProgress.setSecondaryProgress(pPercent * 10);
    }

    @Override
    public void hide() {
        if (mShowing) {
            mShowing = false;
            super.hide();
        }
    }

    public interface PauseCrtl {
        void showBuffering();

        void hideBuffering();

        void playAction();

        void pauseAction();

        boolean getIsBuffering();

        //void changeScreen(int full);

        void showSoundControl();

        boolean isPlayable();

        Context getContext();

    }

    private static class MyHandler extends Handler {

        private final WeakReference<CustomMediaController> mMediaCtrl;

        private MyHandler(CustomMediaController pCustomMediaController) {
            mMediaCtrl = new WeakReference<>(pCustomMediaController);
        }

        @Override
        public void handleMessage(Message pMsg) {
            int pos;
            switch (pMsg.what) {
                case FADE_OUT: {
                    mMediaCtrl.get().hide();
                    break;
                }
                case SHOW_PROGRESS: {
                    pos = mMediaCtrl.get().setProgress();
                    if (!mMediaCtrl.get().mDragging && mMediaCtrl.get().mPlayer.isPlaying()) {
                        pMsg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(pMsg, 1000 - (pos % 1000));
                    }
                    break;
                }
            }
        }
    }
}