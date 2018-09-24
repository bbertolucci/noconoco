package com.bbproject.noconoco.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bbproject.noconoco.R;
import com.bbproject.noconoco.constants.CategoryType;
import com.bbproject.noconoco.constants.Constants;
import com.bbproject.noconoco.constants.QualityType;
import com.bbproject.noconoco.constants.SettingConstants;
import com.bbproject.noconoco.debug.DbgMode;
import com.bbproject.noconoco.model.json.Show;
import com.bbproject.noconoco.services.DownloadService;
import com.bbproject.noconoco.connection.AsyncResponse;
import com.bbproject.noconoco.connection.ConnectNoco;
import com.bbproject.noconoco.custom.controller.CustomMediaController;
import com.bbproject.noconoco.custom.controller.CustomMediaController.PauseCrtl;
import com.bbproject.noconoco.custom.view.CustomVideoView;
import com.bbproject.noconoco.custom.view.MyTextView;
import com.bbproject.noconoco.utils.ObjectSerializer;
import com.bbproject.noconoco.utils.SettingUtils;
import com.bbproject.noconoco.custom.view.SquareLayout;
import com.bbproject.noconoco.custom.view.smartimage.SmartImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VideoActivity extends FragmentActivity implements AsyncResponse, PauseCrtl {

    private static final String TAG = "VideoActivity";
    private static final int IMG_WIDTH = 512;
    private static final int IMG_HEIGHT = 288;
    private final SettingUtils mSettings = new SettingUtils(this);
    private final HashMap<String, String> mColorMap = new HashMap<>();
    private Map<String, String> mOriginalLanguage = new HashMap<>();
    private Map<String, Map<String, String>> mSubTitleByLang = new HashMap<>();
    private Map<String, Map<String, ArrayList<String>>> mQualityByLangAndSub = new HashMap<>();
    private CustomVideoView mVidView;
    private String mQuality = "";
    private String mAudioLang = "";
    private String mSubLang = "";
    private ImageButton mPlay;
    private ImageView mAnimView;
    private ImageView mAnimView2;
    private Animation mAnim;
    private LinearLayout mNextLayout;
    private boolean mIsPlayable = false;
    private RelativeLayout mVidLayView;
    private boolean mHasStarted = false;
    private boolean mIsPaused = false;
    private boolean mIsBuffering = true;
    private CustomMediaController mVidControl;
    private SmartImageView mVidThumb;
    private Show mShow;
    private boolean mRecorded = false;
    private boolean mIsOffline = false;
    private boolean mCantDelete = false;
    private boolean mCantRecord = false;
    private ArrayList<SmartImageView> mListRandomId = new ArrayList<>();
    private Integer mInitUserRating;
    private RelativeLayout mInfo;
    private RelativeLayout mRelCtrl;
    private LayoutInflater mInflater;
    private int mPosition = 0; // Last known position
    private boolean mRestart = false;

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);

        // Set a fullscreen mode depending on Android version and landscape orientation
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT < 16) {
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            View decorView = getWindow().getDecorView();
            int uiOptions;
            if (SDK_INT >= 11) {
                if (SDK_INT < 14) {
                    uiOptions = View.STATUS_BAR_HIDDEN;
                } else if (SDK_INT < 16) {
                    uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                } else if (SDK_INT < 19) {
                    uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LOW_PROFILE
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar
                } else {
                    uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                }
                decorView.setSystemUiVisibility(uiOptions);
            }
        }
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (null == mInflater) {
            throw new AssertionError("LayoutInflater not found.");
        }

        // The volume buttons should affect the media volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Get Activity Parameter
        Intent intent = getIntent();
        mShow = (Show) intent.getSerializableExtra("show");
        mRecorded = intent.getBooleanExtra("recorded", false);
        mIsOffline = intent.getBooleanExtra("is_offline", false);

        // Set the view
        setContentView(R.layout.activity_video);

        // Check parental control
        if (!"".equals(mShow.getRatingFr())) {
            int csa = Integer.parseInt(mShow.getRatingFr());
            int parentalControlLevel = mSettings.getInteger(SettingConstants.PARENTAL_CONTROL, 99);
            if (parentalControlLevel <= csa) {
                finish();
            }
        }

        // If the video was recorded, we disable some button concerning the next video
        if (mRecorded) {
            View view = findViewById(R.id.playupright);
            view.setVisibility(View.GONE);
            view = findViewById(R.id.playup);
            view.setVisibility(View.GONE);
            view = findViewById(R.id.playdown);
            view.setVisibility(View.GONE);
        }

        // Define reference to loading image
        mAnimView2 = findViewById(R.id.sansoeil);
        mAnimView = findViewById(R.id.flower);

        // Video view is hidden at start to display a preview(Thumbnail) image
        mVidView = findViewById(R.id.myVideoPlay);
        mVidView.setVisibility(View.INVISIBLE);

        // Reference to the layout containing the video bar and animation displayed over the video
        mVidLayView = findViewById(R.id.myVideoLayout);

        // Reference to the layout containing video controller bar
        mRelCtrl = mVidLayView.findViewById(R.id.myVideoCtrl);

        // Adjust the thumbnail to fit the width of the screen
        mVidThumb = findViewById(R.id.myVideoThumb);
        mVidThumb.setAdjustHorizontal(true);
        mVidThumb.setAdjustSizeImage(IMG_WIDTH, IMG_HEIGHT);

        // Reference to the layout containing preview image and play button for the next video
        mNextLayout = findViewById(R.id.nextLayout);

        // If the video is not played offline (means not started by SubActivity)
        if (!mIsOffline) {
            // When the user rating bar is changed we send the modification to the server
            RatingBar userRating = findViewById(R.id.ratingBar);
            userRating.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

                @Override
                public void onRatingChanged(RatingBar pRatingBar, float pRating, boolean pFromUser) {
                    if (pFromUser) {
                        // User rating can't be less than 1 star
                        if (pRating == 0) {
                            pRating = 1;
                            pRatingBar.setRating(1);
                        }
                        int vRate = Math.round(pRating);
                        new ConnectNoco("POST", null, "Bearer", VideoActivity.this, Constants.GET_SHOWS + mShow.getIdShow() + Constants.RATE + vRate, CategoryType.RATING_SEND);
                        mShow.setUserRating(vRate);
                    }
                }
            });

            // Display info tab
            mInfo = findViewById(R.id.info);

            // Reference to the layout in charge of the video rating
            RelativeLayout vRela = findViewById(R.id.tabnotation);

            vRela.setVisibility(View.VISIBLE);
            vRela.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View pView) {
                    // Highlight info tab and hide other tab
                    pView.setBackgroundResource(R.drawable.back_tab_top_blue);
                    findViewById(R.id.tabinfo).setBackgroundResource(R.drawable.back_tab_top);
                    findViewById(R.id.tabcomment).setBackgroundResource(R.drawable.back_tab_top);
                    findViewById(R.id.tabinfo_content).setVisibility(View.GONE);
                    findViewById(R.id.tabinfo_comment).setVisibility(View.GONE);
                    findViewById(R.id.tabinfo_notation).setVisibility(View.VISIBLE);
                    findViewById(R.id.editok).setVisibility(View.GONE);
                    findViewById(R.id.bot).setVisibility(View.VISIBLE);
                }
            });

        }
        findViewById(R.id.tabinfo).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View pView) {
                // Highlight info tab and hide other tab
                pView.setBackgroundResource(R.drawable.back_tab_top_blue);
                findViewById(R.id.tabnotation).setBackgroundResource(R.drawable.back_tab_top);
                findViewById(R.id.tabcomment).setBackgroundResource(R.drawable.back_tab_top);
                findViewById(R.id.tabinfo_notation).setVisibility(View.GONE);
                findViewById(R.id.tabinfo_comment).setVisibility(View.GONE);
                findViewById(R.id.tabinfo_content).setVisibility(View.VISIBLE);
                findViewById(R.id.editok).setVisibility(View.GONE);
                findViewById(R.id.bot).setVisibility(View.VISIBLE);
            }
        });

        // Define click action on play button
        mPlay = findViewById(R.id.play);
        mPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View pView) {
                Log.d(TAG, "mPlay>click>Play");
                playAction();
                if (!mHasStarted) {
                    // If it's the first time that user click on play button
                    Log.d(TAG, Constants.GET_SHOWS + mShow.getIdShow() + Constants.GET_SHOWS_VIDEO + mQuality + "/" + mAudioLang + (!"none".equals(mSubLang) ? (!"".equals(mSubLang) ? (!"null".equals(mSubLang) ? "?sub_lang=" + mSubLang : "") : "") : ""));
                    Log.d(TAG, "mPlay>click>Show");
                    // Hide video control bar
                    mRelCtrl.setVisibility(View.GONE);

                    // Check Buffering animation
                    showBuffering();

                    if (mRecorded) {
                        // If video was recorded play immediately
                        mIsPlayable = true;
                        play(mShow.getPath());
                    } else {
                        // If video is not recorded, ask server api to generate a icon_download link
                        new ConnectNoco("GET", null, "Bearer", VideoActivity.this, Constants.GET_SHOWS + mShow.getIdShow() + Constants.GET_SHOWS_VIDEO + mQuality + "/" + mAudioLang + (!mSubLang.equals("none") ? (!mSubLang.equals("") ? (!mSubLang.equals("null") ? "?sub_lang=" + mSubLang : "") : "") : ""), CategoryType.PLAY);
                    }
                } else {
                    // Resume the video
                    mVidView.start();

                    // Update video control bar
                    mVidControl.updatePausePlay();
                }
            }
        });

        // Define Click Action on Share Button
        Button shareButton = findViewById(R.id.share);
        shareButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View pView) {
                // Create an vIntent (default should be open as a simple mail)
                Intent vIntent = new Intent();
                vIntent.setAction(Intent.ACTION_SEND);
                vIntent.setType("text/plain");
                vIntent.putExtra(Intent.EXTRA_SUBJECT, ((!"null".equals(mShow.getShowTT())) ? (mShow.getShowTT() + " | ") : "") + mShow.getFamilyTT() + " | noco");
                vIntent.putExtra(Intent.EXTRA_TEXT, Constants.SHARE_URL + mShow.getIdShow() + "/" + stringForUrl(mShow.getPartnerName()) + "/" + stringForUrl(mShow.getFamilyOT()) + "/" + stringForUrl(mShow.getShowOT()) + "\n\n" + getString(R.string.mail_message) + " " + Constants.NOCONOCO_URL);
                startActivity(Intent.createChooser(vIntent, "Share"));
            }
        });

        // Instantiate the video bar controller
        mVidControl = new CustomMediaController(this);
        mVidControl.setAnchorView(mVidView);

        // Attach the video bar controller to the video view
        mVidView.setMediaController(mVidControl);

        // Initialize the data
        restart(mShow, mRecorded);

        onConfigurationChanged(getResources().getConfiguration());

    }

    /**
     * Reload and initialize a show
     */
    private void restart(Show pShow) {
        restart(pShow, false);
    }

    /**
     * Reload and initialize a show
     */
    private void restart(Show pShow, boolean pRecorded) {
        mOriginalLanguage = new HashMap<>(); // Init language list
        mSubTitleByLang = new HashMap<>(); // Init subtitle language list
        mQualityByLangAndSub = new HashMap<>(); // Init quality list by language and subtile
        mShow = pShow; // Current Show object
        mRecorded = pRecorded; // Show recorded status
        mCantDelete = false; // User can't delete the video from device
        mCantRecord = false; // Video can't be recorded
        mHasStarted = false; // Video has been started (play button pushed one time)
        mIsPlayable = false; // Video can be read or not
        mIsPaused = false; // Video playing status
        mIsBuffering = true; // Video Buffering status
        mListRandomId = new ArrayList<>(); // List of random video for next play
        mNextLayout.setVisibility(View.GONE); // Hide next video layer

        // Check how many video are already recorded and initialize button
        ArrayList<Show> currentList = new ArrayList<>();
        try {
            //noinspection unchecked
            currentList = (ArrayList<Show>) ObjectSerializer.deserialize(mSettings.getString(SettingConstants.SHOW_LIST, ObjectSerializer.serialize(new ArrayList<Show>())));
        } catch (IOException e) {
            Log.e(TAG, "An unexpected error occurred : " + e.toString());
        }
        int count = 0;
        for (Show show : currentList) {
            if (show.getIdShow().equals(mShow.getIdShow())) {
                if (show.getRecordedStatus() != 2) {
                    mRecorded = false;
                    mCantDelete = true;
                } else {
                    mShow = show;
                    mRecorded = true;
                }
            }
            if (show.getShowPaid() != 1) {
                count++;
            }
        }
        if (count == Constants.MAX_OFFLINE_FILE) mCantRecord = true;

        // Set a thumbnail for the video background (before playing)
        if (Constants.DEMO_MODE) {
            mVidThumb.setImageResource(DbgMode.getDemoImage());
        } else {
            mVidThumb.setImageUrl(mShow.getScreenshot512(), false);
        }

        // Set a thumbnail for the next video (after playing with replay icon)
        SmartImageView thumb1 = findViewById(R.id.image1);
        thumb1.setAdjustHorizontal(true);
        thumb1.setAdjustSizeImage(IMG_WIDTH, IMG_HEIGHT);
        if (Constants.DEMO_MODE) {
            thumb1.setImageResource(DbgMode.getDemoImage());
        } else {
            thumb1.setImageUrl(mShow.getScreenshot256(), false);
        }

        if (!mIsOffline) {
            // If actvity was not started from SubActivity, rating bar are set
            mInitUserRating = mShow.getUserRating();
            ((RatingBar) findViewById(R.id.globalRatingBar)).setRating(mShow.getGlobalRating());
            ((RatingBar) findViewById(R.id.ratingBar)).setRating(mInitUserRating);
        }
        // Reset Video control bar
        mRelCtrl.removeAllViews();
        mRelCtrl.setVisibility(View.VISIBLE);

        // Display Share Button
        findViewById(R.id.share).setVisibility(View.VISIBLE);

        // Display Free label if the video is free (no need oauth to access it)
        MyTextView textView = findViewById(R.id.free);
        if ("1".equals(mShow.getGuestFree())) {
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }

        // Set Video layer with a transparent background
        mVidLayView.setBackgroundResource(android.R.color.transparent);

        // Hide animation icon
        mAnimView2.setVisibility(View.GONE);

        // Set a label with the number of downloaded video. Paid video (Video bought by the user) are not counted
        MyTextView limit = findViewById(R.id.limit);
        count = 0;
        for (Show show : currentList) {
            if (show.getShowPaid() != 1) {
                count++;
            }
        }
        String myTextView = getString(R.string.video_limit) + " " + count + "/" + Constants.MAX_OFFLINE_FILE;
        limit.setText(myTextView);

        // In case the video was bought by the user, there is no icon_download restriction
        if (1 == mShow.getShowPaid()) {
            limit.setVisibility(View.GONE);
            TextView text = findViewById(R.id.show_title);
            text.setText(mShow.getPath());
        }

        if (mRecorded) {
            // Define a reference to the record and delete button
            Button recordBtn = findViewById(R.id.record);
            Button deleteBtn = findViewById(R.id.delete);
            // If the video was recorded we hide some recording button and displa directly the play button
            recordBtn.setVisibility(View.GONE);
            limit.setVisibility(View.GONE);
            deleteBtn.setVisibility(View.VISIBLE);
            mPlay.setVisibility(View.VISIBLE);
            mAnimView2.setVisibility(View.VISIBLE);

            // Define a click action on the delete button
            deleteBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View pView) {
                    // File is deleted from disk
                    File file = new File(mShow.getPath());
                    if (!file.delete()) {
                        Log.e(TAG, "Error while deleting : " + mShow.getPath());
                    }
                    try {
                        // Show deleted is removed from recorded list
                        //noinspection unchecked
                        ArrayList<Show> vCurrentList = (ArrayList<Show>) ObjectSerializer.deserialize(mSettings.getString(SettingConstants.SHOW_LIST, ObjectSerializer.serialize(new ArrayList<Show>())));
                        for (Show vShow : vCurrentList) {
                            if (mShow.getIdShow().equals(vShow.getIdShow())) {
                                vCurrentList.remove(vShow);
                            }
                        }
                        // List of recorded show is stored on disk
                        mSettings.setString(SettingConstants.SHOW_LIST, ObjectSerializer.serialize(vCurrentList));
                    } catch (IOException e) {
                        Log.e(TAG, "An unexpected error occurred : " + e.toString());
                    }
                    // Activity is closed and parameters are send to mainActivity to refresh data
                    Intent intent = new Intent();
                    intent.putExtra("has_been_played", 1);
                    intent.putExtra("id_show", mShow.getIdShow());
                    if (mInitUserRating.intValue() != mShow.getUserRating().intValue()) {
                        intent.putExtra("new_rating", mShow.getUserRating());
                    }
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        } else {
            // Define a reference to the record
            Button recordBtn = findViewById(R.id.record);

            // Get the data for the current show
            new ConnectNoco("GET", null, "Bearer", VideoActivity.this, Constants.GET_SHOWS + mShow.getIdShow() + Constants.GET_SHOWS_MEDIAS, CategoryType.MEDIAS);
            // Get the list of unread video (for next video)
            new ConnectNoco("GET", null, "Bearer", VideoActivity.this, Constants.GET_SHOWS_UNREAD_ONLY + "0&" + Constants.BY_FAMILY_KEY + mShow.getFamilyKey(), CategoryType.UNREAD);
            // Get the list of user recommended video (for next video)
            new ConnectNoco("GET", null, "Bearer", VideoActivity.this, Constants.MAY_ALSO_LIKE + mShow.getIdFamily() + "?" + Constants.MAL_ELM_PER_PAGE, CategoryType.MAY_ALSO_LIKE);
            // Get the list of channel recommended video (for next video)
            new ConnectNoco("GET", null, "Bearer", VideoActivity.this, Constants.FIND_SHOWS_RECOMMENDED + 0, CategoryType.RECOMMENDED_LIST);

            if (mCantDelete)
                recordBtn.setVisibility(View.GONE); // Video is currently downloading in background so we hide delete button
            else {
                // Define click action on record button
                recordBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Thread t = new Thread() {
                            public void run() {
                                // Launch a service process to icon_download the video and close the activity
                                Intent vServiceIntent = new Intent(VideoActivity.this, DownloadService.class);
                                mShow.setStreamUrl(Constants.GET_SHOWS + mShow.getIdShow() + Constants.GET_SHOWS_VIDEO + mQuality + "/" + mAudioLang + (!mSubLang.equals("none") ? (!mSubLang.equals("") ? (!mSubLang.equals("null") ? "?sub_lang=" + mSubLang : "") : "") : ""));
                                vServiceIntent.putExtra("type", "addUrl");
                                vServiceIntent.putExtra("show", (Serializable) mShow);
                                Log.d(TAG, "Start Download Service for show id : " + mShow.getIdShow());
                                vServiceIntent.setAction("noconoco.icon_download.launch");
                                startService(vServiceIntent);
                                Intent intent = new Intent();
                                intent.putExtra("has_been_played", 1);
                                intent.putExtra("id_show", mShow.getIdShow());
                                if (mInitUserRating.intValue() != mShow.getUserRating().intValue()) {
                                    intent.putExtra("new_rating", mShow.getUserRating());
                                }
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        };
                        t.start();
                    }
                });
            }
            // Check buffering icon
            showBuffering();
        }
        // Display the video family type
        TextView text = findViewById(R.id.family_title);
        text.setText(mShow.getFamilyTT());

        if (1 != mShow.getShowPaid()) {
            // Create a show title depending on different data
            // Video bought by user keep their original name
            String textTitle = (!"null".equals(mShow.getShowTT()) ? (mShow.getShowTT()) : "");
            if (0 < mShow.getEpisode() + mShow.getSeason()) {
                if (!"".equals(textTitle)) textTitle += " - ";
                if (0 < mShow.getSeason()) {
                    textTitle += getString(R.string.prefix_season) + mShow.getSeason();
                    if (0 < mShow.getEpisode()) {
                        textTitle += getString(R.string.prefix_episode) + mShow.getEpisode() + getString(R.string.suffix_episode);
                    }
                } else if (0 < mShow.getEpisode()) {
                    textTitle += "#" + mShow.getEpisode();
                }
            }
            text = findViewById(R.id.show_title);
            text.setText(textTitle);
        }

        // Display a description of the show
        text = findViewById(R.id.resume);
        text.setText(!"null".equals(mShow.getShowResume()) ? mShow.getShowResume() : (!"null".equals(mShow.getFamilyResume()) ? mShow.getFamilyResume() : ""));

        // Display the banner as a background for the information tab
        SmartImageView smartImageView = findViewById(R.id.banner);
        smartImageView.setAdjustHorizontal(true);
        smartImageView.setAdjustSizeImage(1400, 525);
        if (Constants.DEMO_MODE) {
            //TODO vSmartImageView.setImageResource...
        } else {
            smartImageView.setImageUrl(mShow.getBanner());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVidView != null) {
            // Resume video with the last known position
            mVidView.seekTo(mPosition);
            // Pause the video
            pauseAction();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Store the position when device is stopped
        if (null != mVidView) mPosition = mVidView.getCurrentPosition();
    }

    @Override
    public void processFinish(String pResponse, int pNextStep) {

        Log.d(TAG, "CategoryType " + pNextStep + " - Response received");
        if (pResponse.startsWith("NG")) {
            Log.e(TAG, "CategoryType " + pNextStep + " - Url returns an error : " + pResponse);
        } else try {
            switch (pNextStep) {
                case CategoryType.MEDIAS: {
                    // Analyse the url answer for MEDIAS step (describe all the possibility for playing)
                    // {"fr":{"audio_lang":{"fr":"fran\u00e7ais","en":"French"},"video_list":{"none":{"sub_lang":{"fr":"none","en":"none"},"quality_list":{"LQ":{"id_show":39144,"quality_key":"LQ","videobitrate":500000,"audiobitrate":96000,"framerate":25,"framecount":11118,"res_width":512,"res_lines":288,"duration":444720,"filesize":33251049,"mosaique":"2017-02-10 18:45:28","modified":"2017-02-10 18:45:28"},"HQ":{"id_show":39144,"quality_key":"HQ","videobitrate":1479000,"audiobitrate":127976,"framerate":50,"framecount":22238,"res_width":688,"res_lines":384,"duration":444760,"filesize":89661603,"mosaique":"2017-02-10 18:45:28","modified":"2017-02-10 18:45:28"},"TV":{"id_show":39144,"quality_key":"TV","videobitrate":2434000,"audiobitrate":127976,"framerate":50,"framecount":22238,"res_width":1024,"res_lines":576,"duration":444760,"filesize":142908633,"mosaique":"2017-02-10 18:45:28","modified":"2017-02-10 18:45:28"},"HD_720":{"id_show":39144,"quality_key":"HD_720","videobitrate":3406000,"audiobitrate":127976,"framerate":50,"framecount":22238,"res_width":1280,"res_lines":720,"duration":444760,"filesize":197368802,"mosaique":"2017-02-10 18:45:28","modified":"2017-02-10 18:45:28"}}}}}}
                    // Get user setting for favorites language and quality
                    String audioLang = mSettings.getString(SettingConstants.AUDIO_LANGUAGE, "");
                    String subLang = mSettings.getString(SettingConstants.SUB_LANGUAGE, "");
                    String qualityMobile = mSettings.getString(SettingConstants.QUALITY_MOBILE, "");
                    int i = 0;
                    Log.d(TAG, ">>>>>>>>" + pResponse);
                    JSONObject rootObject = new JSONObject(pResponse);
                    Iterator<String> keysFirst = rootObject.keys();
                    while (keysFirst.hasNext()) {
                        String keyFirst = keysFirst.next();
                        // Select the video language base on available languages and user favorites
                        if ((audioLang.equals(keyFirst)) ||
                                (("original".equals(audioLang)) && (i == 0) && !"fr".equals(keyFirst)) ||
                                (("".equals(mAudioLang) && !keysFirst.hasNext()))) {
                            mAudioLang = keyFirst;
                        }

                        JSONObject first = rootObject.getJSONObject(keyFirst);

                        // Get the most suitable audio language
                        JSONObject second = first.getJSONObject("audio_lang");
                        Iterator<String> keySecond = second.keys();
                        while (keySecond.hasNext()) {
                            String key2 = keySecond.next();
                            if ((Locale.getDefault().getLanguage().equals(key2)) || (!keySecond.hasNext())) {
                                mOriginalLanguage.put(keyFirst, second.getString(key2));
                                Log.d(TAG, keyFirst + " " + second.getString(key2));
                                break;
                            }
                        }

                        second = first.getJSONObject("video_list");
                        keySecond = second.keys();
                        Map<String, ArrayList<String>> mapQualityBySub = new HashMap<>();
                        while (keySecond.hasNext()) {
                            String key = keySecond.next();
                            // Select the subtitle language base on available languages and user favorites
                            if ((!"".equals(mAudioLang)) && (("null".equals(subLang) && ("none".equals(key))) ||
                                    (subLang.equals(key)) ||
                                    ("".equals(mSubLang) && !keySecond.hasNext()))) {
                                mSubLang = key;
                            }
                            JSONObject third = second.getJSONObject(key);

                            // Get the most suitable subtitle language by audio lang
                            JSONObject fourth = third.getJSONObject("sub_lang");
                            Iterator<String> keyFourth = fourth.keys();
                            HashMap<String, String> subtitleMap = new HashMap<>();
                            while (keyFourth.hasNext()) {
                                String key4 = keyFourth.next();
                                if ((Locale.getDefault().getLanguage().equals(key4)) || (!keyFourth.hasNext())) {
                                    subtitleMap.put(key, fourth.getString(key4));
                                    break;
                                }
                            }
                            mSubTitleByLang.put(keyFirst, subtitleMap);

                            // Select the video quality base on available qualities and user favorites
                            fourth = third.getJSONObject("quality_list");
                            keyFourth = fourth.keys();
                            ArrayList<String> qualityList = new ArrayList<>();
                            while (keyFourth.hasNext()) {
                                String key4 = keyFourth.next();
                                if ((!"".equals(mAudioLang)) &&
                                        ((("".equals(mQuality) && qualityMobile.equals(key4)) || ("".equals(mQuality) && "none".equals(key4) && !keyFourth.hasNext())) ||
                                                (("".equals(mQuality) && !keysFirst.hasNext() && !keyFourth.hasNext())))) {
                                    mQuality = key4;
                                }
                                qualityList.add(key4);
                            }
                            mapQualityBySub.put(key, qualityList);
                            Log.d(TAG, key + " " + second.getString(key));
                        }
                        // Add a Map of Quality object by subtitle and audio lang
                        mQualityByLangAndSub.put(keyFirst, mapQualityBySub);

                        ++i;
                    }
                    // Hide loading animation
                    hideBuffering();
                    // Display play button
                    mAnimView2.setVisibility(View.VISIBLE);
                    mPlay.setVisibility(View.VISIBLE);
                    if (!mCantDelete && !mCantRecord) {
                        // Display others buttons or not
                        Button button = findViewById(R.id.record);
                        button.setVisibility(View.VISIBLE);
                        MyTextView limit = findViewById(R.id.limit);
                        if (1 == mShow.getShowPaid()) {
                            limit.setVisibility(View.GONE);
                        } else {
                            limit.setVisibility(View.VISIBLE);
                        }
                    }

                    // reload video bar controller with new data
                    reloadCtrl();
                    break;
                }
                case CategoryType.PLAY: {
                    // Get the generated url for playing the video
                    //{"code":"INVALID_PARAMETERS","error":"id_show, quality_key and audio_lang are required","description":"Check API documentation for more informations about shows operations."}
                    Log.d(TAG, ">>>>>>>>" + pResponse);
                    JSONObject rootObject = new JSONObject(pResponse);
                    String codeResponse = rootObject.getString("code_reponse");
                    switch (codeResponse) {
                        case "1":
                            //{
                            //  "file": "http://video.noco.tv/nol/8/d/PN_S18s13_notime08.mp4?st=9euhjELia4oUTNEkwFl54Q&e=1523176042&u=39&d=0&videoname=",
                            //  "quality_key": "HD_720",
                            //  "is_abo": 1,
                            //  "cross_access": 0,
                            //  "quotafr_free": 0,
                            //  "user_free": 1,
                            //  "guest_free": 1,
                            //  "code_reponse": 1
                            //}
                            mIsPlayable = true;
                            String file = rootObject.getString("file");
                            mShow.setPath(file);
                            // Play the video
                            play(file);
                            break;
                        case "2": {
                            // Warning message
                            JSONObject first = rootObject.getJSONObject("popmessage");
                            String message = first.getString("message");
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                            break;
                        }
                        case "3": {
                            // Error message
                            JSONObject first = rootObject.getJSONObject("popmessage");
                            String message = first.getString("message");
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                            finish();
                            break;
                        }
                    }

                    break;
                }
                case CategoryType.COMMENT: {
                    // Scrapping the forum to get username and comments
                    List<String[]> list = new ArrayList<>();
                    String[] msg = pResponse.split("(<!-- (/ )?message -->)");
                    int vCount = msg.length;
                    for (int i = 0; i < vCount; i += 2) {
                        String[] user = {"", ""};
                        String[] uname = msg[i].split("class=\"bigusername\" ");
                        if (uname.length > 1) {
                            String[] link_tag = uname[1].split("</a>");
                            String[] link = link_tag[0].split(">");
                            user[0] = link[1];
                        } else {
                            break;
                        }
                        String content = msg[i + 1];
                        content = content.replaceAll("<img.+/(img)*>", "");
                        content = content.replace("<div class=\"smallfont\" style=\"margin-bottom:2px\">Citation:</div>", "Citation: ...");
                        content = content.replaceAll("<table(.|\n|<br>|<br/>|<br />)*?</table>", "");
                        content = content.replace("style=\"back_icon_blue:1px inset", "style=\"background: #f5f5f5;color: #000000;  back_icon_blue: 2px #FEFEFE solid;");
                        user[1] = content;
                        list.add(user);
                    }
                    String chatText = "";
                    for (String[] string : list) {
                        String color = getColor(string[0]);
                        chatText += "<font color=\"" + color + "\">" + string[0] + ":</font> " + string[1] + "<br/>";
                        printText(Html.fromHtml(chatText));
                    }
                    Log.d(TAG, "CategoryType " + pNextStep + " - Comment ok : " + pResponse);

                    break;
                }
                case CategoryType.UNREAD: {
                    // Get List of unread video and prepare next video list (same video, 1 unread, 4 similar, 4 recommended )
                    ArrayList<Show> listShows = new ArrayList<>();
                    Show.decodeJSON(pResponse, listShows);
                    SmartImageView thumb2 = findViewById(R.id.image2);
                    if (listShows.size() > 0) {
                        makeThumb(thumb2, listShows.get(0), R.id.play2, R.id.t2);
                    } else {
                        mListRandomId.add(thumb2);
                    }
                    break;
                }
                case CategoryType.MAY_ALSO_LIKE: {
                    // Get List of similar video and prepare next video list (same video, 1 unread, 4 similar, 4 recommended )
                    ArrayList<Show> listShows = new ArrayList<>();
                    Show.decodeJSON(pResponse, listShows);
                    SmartImageView thumb3 = findViewById(R.id.image3);
                    SmartImageView thumb4 = findViewById(R.id.image4);
                    SmartImageView thumb5 = findViewById(R.id.image5);
                    SmartImageView thumb6 = findViewById(R.id.image6);
                    if (listShows.size() > 0) {
                        makeThumb(thumb3, listShows.get(0), R.id.play3, R.id.t3);
                        if (listShows.size() > 1) {
                            makeThumb(thumb4, listShows.get(1), R.id.play4, R.id.t4);
                            if (listShows.size() > 2) {
                                makeThumb(thumb5, listShows.get(2), R.id.play5, R.id.t5);
                                if (listShows.size() > 3) {
                                    makeThumb(thumb6, listShows.get(3), R.id.play6, R.id.t6);
                                } else {
                                    mListRandomId.add(thumb6);
                                }
                            } else {
                                mListRandomId.add(thumb5);
                                mListRandomId.add(thumb6);
                            }
                        } else {
                            mListRandomId.add(thumb4);
                            mListRandomId.add(thumb5);
                            mListRandomId.add(thumb6);
                        }
                    } else {
                        mListRandomId.add(thumb3);
                        mListRandomId.add(thumb4);
                        mListRandomId.add(thumb5);
                        mListRandomId.add(thumb6);
                    }

                    break;
                }
                case CategoryType.RECOMMENDED_LIST: {
                    // Get List of recommended video and prepare next video list (same video, 1 unread, 4 similar, 4 recommended )
                    ArrayList<Show> listShows = new ArrayList<>();
                    Show.decodeJSON(pResponse, listShows);
                    SmartImageView thumb7 = findViewById(R.id.image7);
                    SmartImageView thumb8 = findViewById(R.id.image8);
                    SmartImageView thumb9 = findViewById(R.id.image9);
                    SmartImageView thumb10 = findViewById(R.id.image10);
                    if (listShows.size() > 0) {
                        makeThumb(thumb7, listShows.get(0), R.id.play7, R.id.t7);
                        if (listShows.size() > 1) {
                            makeThumb(thumb8, listShows.get(1), R.id.play8, R.id.t8);
                            if (listShows.size() > 2) {
                                makeThumb(thumb9, listShows.get(2), R.id.play9, R.id.t9);
                                if (listShows.size() > 3) {
                                    makeThumb(thumb10, listShows.get(3), R.id.play10, R.id.t10);
                                } else {
                                    mListRandomId.add(thumb10);
                                }
                            } else {
                                mListRandomId.add(thumb9);
                                mListRandomId.add(thumb10);
                            }
                        } else {
                            mListRandomId.add(thumb8);
                            mListRandomId.add(thumb9);
                            mListRandomId.add(thumb10);
                        }
                    } else {
                        mListRandomId.add(thumb7);
                        mListRandomId.add(thumb8);
                        mListRandomId.add(thumb9);
                        mListRandomId.add(thumb10);
                    }
                    // Call RANDOM video to complete in next video list in case of hole.
                    new ConnectNoco("GET", null, "Bearer", VideoActivity.this, Constants.GET_SHOWS_RANDOM + mListRandomId.size(), CategoryType.RANDOM);

                    break;
                }
                case CategoryType.RANDOM: {
                    // Get RANDOM video list to complete in next video list in case of hole.
                    ArrayList<Show> listShows = new ArrayList<>();
                    Show.decodeJSON(pResponse, listShows);
                    int i = 0;
                    for (Iterator<SmartImageView> iterator = mListRandomId.iterator(); iterator.hasNext(); i++) {
                        if (listShows.size() > i) {
                            SmartImageView thumb = iterator.next();
                            switch (thumb.getId()) {
                                case R.id.image2: {
                                    makeThumb(thumb, listShows.get(i), R.id.play2, R.id.t2);
                                    break;
                                }
                                case R.id.image3: {
                                    makeThumb(thumb, listShows.get(i), R.id.play3, R.id.t3);
                                    break;
                                }
                                case R.id.image4: {
                                    makeThumb(thumb, listShows.get(i), R.id.play4, R.id.t4);
                                    break;
                                }
                                case R.id.image5: {
                                    makeThumb(thumb, listShows.get(i), R.id.play5, R.id.t5);
                                    break;
                                }
                                case R.id.image6: {
                                    makeThumb(thumb, listShows.get(i), R.id.play6, R.id.t6);
                                    break;
                                }
                                case R.id.image7: {
                                    makeThumb(thumb, listShows.get(i), R.id.play7, R.id.t7);
                                    break;
                                }
                                case R.id.image8: {
                                    makeThumb(thumb, listShows.get(i), R.id.play8, R.id.t8);
                                    break;
                                }
                                case R.id.image9: {
                                    makeThumb(thumb, listShows.get(i), R.id.play9, R.id.t9);
                                    break;
                                }
                                case R.id.image10: {
                                    makeThumb(thumb, listShows.get(i), R.id.play10, R.id.t10);
                                    break;
                                }
                            }
                        } else {
                            break;
                        }
                    }
                    break;
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, "CategoryType " + pNextStep + " - A JSONException occurred !", e);
        }
    }

    /**
     * Get Random color per user for comments color.
     * Each user will be store to keep the consistency of visual color when reading comments
     */
    private String getColor(String pUser) {
        if (mColorMap.containsKey(pUser)) return mColorMap.get(pUser);
        int R = 128 + (int) (Math.random() * 128);
        int G = 128 + (int) (Math.random() * 128);
        int B = 128 + (int) (Math.random() * 128);
        String hex = String.format("#%02x%02x%02x", R, G, B);
        mColorMap.put(pUser, hex);
        return hex;
    }

    private void printText(final Spanned pSpanned) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Set text of textview on main thread (content was cal in async context)
                ((MyTextView) findViewById(R.id.comment_textView)).setText(pSpanned);
                final ScrollView commentScrollView = findViewById(R.id.tabinfo_comment);
                commentScrollView.post(new Runnable() {
                    public void run() {
                        commentScrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    /**
     * Reload Video Controller bar
     */
    private void reloadCtrl() {
        int j = 0;
        int k = 100;
        int l = 200;
        // Remove all the view attached to video controller bar
        mRelCtrl.removeAllViews();
        for (Map.Entry<String, Map<String, ArrayList<String>>> entry : mQualityByLangAndSub.entrySet()) {
            // Contrust top-right square buttons to change audio language
            j++;
            Log.d(TAG, "" + entry.getKey() + " " + mOriginalLanguage.get(entry.getKey()));
            SquareLayout square = (SquareLayout) mInflater.inflate(R.layout.view_btn_square, mRelCtrl, false);
            LayoutParams params = new LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            if (j == 1) params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            else params.addRule(RelativeLayout.LEFT_OF, j - 1);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            square.setId(j);
            square.setTag(entry.getKey());
            // Every time we change audio language, we have to reconstruct the displayed quality square buttons, and the displayed subtitle buttons
            square.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAudioLang = (String) v.getTag();
                    mSubLang = "";
                    String subLang = mSettings.getString(SettingConstants.SUB_LANGUAGE, "");
                    for (Map.Entry<String, String> entry : mSubTitleByLang.get(mAudioLang).entrySet()) {
                        if ((("null".equals(subLang)) && ("none".equals(entry.getKey())))
                                || (subLang.equals(entry.getKey()))
                                ) {
                            mSubLang = entry.getKey();
                        }
                    }
                    if ("".equals(mSubLang)) {
                        subLang = Locale.getDefault().getLanguage();
                        for (Map.Entry<String, String> entry : mSubTitleByLang.get(mAudioLang).entrySet()) {
                            if (subLang.equals(entry.getKey())) {
                                mSubLang = subLang;
                            }
                        }
                        if ("".equals(mSubLang)) {
                            Map.Entry<String, String> entry = mSubTitleByLang.get(mAudioLang).entrySet().iterator().next();
                            if (null != entry) mSubLang = entry.getKey();
                        }
                    }
                    //Set new quality list
                    getQuality();
                }
            });
            params.setMargins(5, 5, 5, 5);
            ((TextView) square.findViewById(R.id.text)).setText(entry.getKey());
            mRelCtrl.addView(square, params);
            if (mAudioLang.equals(entry.getKey())) {
                // set special decoration on selected one
                square.setSelected(true);
                // display sub-top right button for subtitles language
                for (Map.Entry<String, ArrayList<String>> entry2 : entry.getValue().entrySet()) {
                    k++;
                    Log.d(TAG, "" + entry2.getKey() + " " + mSubTitleByLang.get(entry.getKey()).get(entry2.getKey()));

                    @SuppressLint("InflateParams")
                    SquareLayout square2 = (SquareLayout) mInflater.inflate(R.layout.view_btn_square, null);
                    LayoutParams params2 = new LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    if (k == 101)
                        params2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                    else params2.addRule(RelativeLayout.LEFT_OF, k - 1);
                    params2.addRule(RelativeLayout.BELOW, j);
                    square2.setId(k);
                    square2.setTag(entry2.getKey());
                    // Every time we change subtitle language, we have to reconstruct the displayed quality square buttons
                    square2.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mSubLang = (String) v.getTag();
                            //Set new quality list
                            getQuality();
                        }
                    });
                    params2.setMargins(5, 5, 5, 5);
                    ((TextView) square2.findViewById(R.id.text)).setText(entry2.getKey());
                    mRelCtrl.addView(square2, params2);
                    if (mSubLang.equals(entry2.getKey())) {
                        // set special decoration on selected one
                        square2.setSelected(true);
                        // display top left button for video quality
                        List<String> listQuality = new ArrayList<>();
                        for (int z = 0; z < 5; z++) {
                            String search = "";
                            switch (z) {
                                case 0: {
                                    search = "LQ";
                                    break;
                                }
                                case 1: {
                                    search = "HQ";
                                    break;
                                }
                                case 2: {
                                    search = "TV";
                                    break;
                                }
                                case 3: {
                                    search = "HD_720";
                                    break;
                                }
                                case 4: {
                                    search = "HD_1080";
                                    break;
                                }
                            }
                            for (String key : entry2.getValue()) {
                                if (search.equals(key)) {
                                    listQuality.add(search);
                                    break;
                                }
                            }
                        }
                        for (; l < listQuality.size() + 200; ) {
                            l++;
                            @SuppressLint("InflateParams")
                            SquareLayout square3 = (SquareLayout) mInflater.inflate(R.layout.view_btn_square, null);
                            LayoutParams params3 = new LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                            );
                            if (l == 201)
                                params3.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                            else params3.addRule(RelativeLayout.RIGHT_OF, l - 1);
                            square3.setId(l);
                            square3.setTag(listQuality.get(l - 201));
                            square3.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mQuality = (String) v.getTag();
                                    // Reload video control bar every time quality is changed
                                    reloadCtrl();
                                }
                            });
                            params3.setMargins(5, 5, 5, 5);
                            ((TextView) square3.findViewById(R.id.text)).setText(QualityType.getQualityNameByKey(listQuality.get(l - 201)));
                            mRelCtrl.addView(square3, params3);
                            if (mQuality.equals(listQuality.get(l - 201))) {
                                // set special decoration on selected one
                                square3.setSelected(true);
                            }
                        }
                    }
                }
                // If there isn't any subtitle possible, we hide the subtitle layout
                if (k == 101 && "none".equals(mSubLang))
                    mVidLayView.findViewById(k).setVisibility(View.GONE);
            }
        }
        // Create a label to display at the left of audio language buttons
        TextView text = new TextView(this);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.LEFT_OF, j);
        int vt = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, getResources().getDisplayMetrics()));
        params.setMargins(5, 5 + vt, 5, 5 + vt);
        text.setText(R.string.video_lang);
        text.setTextColor(Color.WHITE);
        text.setShadowLayer(2, 1, 1, R.color.text_shadow);
        mRelCtrl.addView(text, params);

        // Create a label to display at the left of subtitle language buttons if existing any
        if (k != 101 || (!"none".equals(mSubLang))) {
            text = new TextView(this);
            params = new LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.addRule(RelativeLayout.BELOW, j);
            params.addRule(RelativeLayout.LEFT_OF, k);
            params.setMargins(5, 5 + vt, 5, 5 + vt);
            text.setText(R.string.video_sub);
            text.setTextColor(Color.WHITE);
            text.setShadowLayer(2, 1, 1, R.color.text_shadow);
            mRelCtrl.addView(text, params);
        }
    }

    /**
     * Set the quality list depending on language and subtitle language selected
     */
    private void getQuality() {
        String q = mQuality;
        mQuality = "";

        for (String key : mQualityByLangAndSub.get(mAudioLang).get(mSubLang)) {
            if (q.equals(key)) {
                mQuality = key;
            }
        }

        // Try to match favorites user quality
        if ("".equals(mQuality)) {
            String qualityMobile = mSettings.getString(SettingConstants.QUALITY_MOBILE, "");
            for (String key : mQualityByLangAndSub.get(mAudioLang).get(mSubLang)) {
                if (qualityMobile.equals(key)) {
                    mQuality = key;
                }
            }
        }

        // Try to match favorites user quality
        if ("".equals(mQuality)) {
            String qualityMobile = Locale.getDefault().getLanguage();
            for (String key : mQualityByLangAndSub.get(mAudioLang).get(mSubLang)) {
                if (qualityMobile.equals(key)) {
                    mQuality = key;
                }
            }
        }
        // Try to get at least one quality
        if ("".equals(mQuality)) {
            for (int z = 0; z < 5 && "".equals(mQuality); z++) {
                String search = "";
                switch (z) {
                    case 0: {
                        search = "LQ";
                        break;
                    }
                    case 1: {
                        search = "HQ";
                        break;
                    }
                    case 2: {
                        search = "TV";
                        break;
                    }
                    case 3: {
                        search = "HD_720";
                        break;
                    }
                    case 4: {
                        search = "HD_1080";
                        break;
                    }
                }
                for (String key : mQualityByLangAndSub.get(mAudioLang).get(mSubLang)) {
                    if (search.equals(key)) {
                        mQuality = key;
                        break;
                    }
                }
            }
        }
        // Reload all the buttons
        reloadCtrl();
    }

    /**
     * Play action with url parameter
     */
    private void play(String pUrl) {
        // Display the controls
        mVidView.setVisibility(View.VISIBLE);
        mVidControl.setVisibility(View.VISIBLE);
        mVidControl.unlock();

        if (mRecorded) {
            // If video was recorded we load the video from disk
            Log.d(TAG, "Play :" + pUrl);
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(pUrl);
                mVidView.setVideoFD(fileInputStream.getFD());
            } catch (IOException e) {
                Log.e(TAG, "An unexpected error occurred : " + e.toString());
            } finally {
                if (null != fileInputStream) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "An unexpected error occurred : " + e.toString());
                    }
                }
            }
        } else {
            // We load video from url if video was not recorded
            Log.d(TAG, ">>>" + pUrl);
            Uri vidUri = Uri.parse(pUrl);
            mVidView.setVideoURI(vidUri);
        }

        // When video is on error
        mVidView.setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
                Log.e(TAG, "Video is on error :" + arg1);
                return true;
            }
        });
        // When video is finished
        mVidView.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // Display thumbnails for next video
                showReplay();
            }
        });
        // When video is loaded
        mVidView.setOnPreparedListener(new OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                // No automatic loop
                mp.setLooping(false);
                // Hide loading animation
                hideBuffering();
                mHasStarted = true;
                mIsBuffering = false;
                // Hide the background (layer over video)
                mVidThumb.setVisibility(View.GONE);
                // Start at the last position
                mVidView.seekTo(mShow.getResumePlay());
                // Play video
                mVidView.start();
                // If paused button was pressed : video on pause
                if (mIsPaused) mVidView.pause();
                // When seek position is loaded
                mp.setOnSeekCompleteListener(new OnSeekCompleteListener() {
                    @Override
                    public void onSeekComplete(MediaPlayer mp) {
                        Log.d(TAG, "pos:" + mp.getCurrentPosition() + " " + mVidView.getCurrentPosition());
                    }
                });

                mp.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
                    @Override
                    public void onBufferingUpdate(MediaPlayer mp, int percent) {

                        if (percent < 100) {
                            mVidControl.setSecondaryProgress(percent);
                        }
                    }
                });
                mp.setOnInfoListener(new OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {

                        // Display or hide the animation depending on the buffering state
                        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                            Log.d(TAG, "Buffering>Show");
                            showBuffering();
                        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                            Log.d(TAG, "Buffering>Hide");
                            hideBuffering();
                        }
                        return false;
                    }
                });
            }
        });
    }

    /**
     * Method called asynchronously by ConnectNoco when connection error occurred
     */
    @Override
    public void processRetry(String pMethod, Map<String, String> pMapParam,
                             String pTokenType,
                             String pUrl, int pNextStep, int pRetry) {
        Log.d(TAG, "Retry : " + pUrl + " " + pTokenType);
        // Retry call with a delay of 5s.
        new ConnectNoco(pMethod, pMapParam, pTokenType, VideoActivity.this, pUrl, pNextStep, pRetry, 5000);
    }

    @Override
    public Context getContext() {
        return this;
    }

    /**
     * Method called asynchronously by ConnectNoco when connection error occurred after X retry.
     * Finish the activity
     */
    @Override
    public void restart(int pNextStep) {
        if (!mRestart) {
            // Apply restart only one time.
            mRestart = true;
            Toast.makeText(getApplicationContext(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Hide the loading animation
     */
    public void hideBuffering() {
        Log.d(TAG, "hideBuff " + mIsPaused + " " + mIsBuffering);

        if (null != mAnim) {
            mIsBuffering = false;
            if (null != mAnimView) {
                mAnimView.setAnimation(null);
                mAnimView.clearAnimation();
                mAnimView.setVisibility(View.GONE);
            }
            if (!mIsPaused) {
                if (null != mVidLayView)
                    mVidLayView.setBackgroundResource(android.R.color.transparent);
                if (null != mAnimView2) mAnimView2.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Show the loading animation
     */
    public void showBuffering() {
        Log.d(TAG, "showBuff " + mIsPaused + " " + mIsBuffering);
        if (!mIsPaused) {
            mIsBuffering = true;
            mVidLayView.setBackgroundResource(R.color.black_transparent);

            mAnimView2.setVisibility(View.VISIBLE);
            mAnimView.setVisibility(View.VISIBLE);
            if (mAnim == null) {
                mAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
                mAnim.setRepeatCount(Animation.INFINITE);
                mAnimView.startAnimation(mAnim);
            } else {
                mAnimView.startAnimation(mAnim);
            }
        }
    }

    /**
     * Function called on play action
     */
    @Override
    public void playAction() {
        Log.d(TAG, "play " + mIsPaused + " " + mIsBuffering);
        if (mShow.getMarkRead() == 0) mShow.setMarkRead(1);
        mIsPaused = false;
        mVidLayView.setBackgroundResource(android.R.color.transparent);
        mAnimView2.setVisibility(View.GONE);
        mPlay.setVisibility(View.GONE);
        if (mIsBuffering) {
            Log.d(TAG, "playAction>Hide");
            hideBuffering();
        }
    }

    /**
     * Function called on pause action
     */
    @Override
    public void pauseAction() {
        Log.d(TAG, "pause " + mIsPaused + " " + mIsBuffering);

        mIsPaused = true;
        mVidLayView.setBackgroundResource(R.color.black_transparent);
        if (null != mAnimView2) mAnimView2.setVisibility(View.VISIBLE);
        if (null != mPlay) mPlay.setVisibility(View.VISIBLE);
        if (mIsBuffering) {
            Log.d(TAG, "pauseAction>Hide");
            hideBuffering();
        }
    }

    /**
     * Get method
     *
     * @return value of mIsBuffering
     */
    @Override
    public boolean getIsBuffering() {
        return mIsBuffering;
    }

    /**
     * Function called on replay action
     */
    private void showReplay() {
        Log.d(TAG, "enter replay");
        mVidView.setVisibility(View.INVISIBLE);
        mVidControl.setVisibility(View.INVISIBLE);

        mNextLayout.setVisibility(View.VISIBLE);
        ImageButton replay = findViewById(R.id.replay);
        replay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mNextLayout.setVisibility(View.GONE);
                play(mShow.getPath());
            }
        });
    }

    /**
     * Function called by Android system when device change view mode (horizontal or vertical)
     */
    @SuppressLint("ObsoleteSdkInt")
    @Override
    public void onConfigurationChanged(Configuration pNewConfig) {
        super.onConfigurationChanged(pNewConfig);
        // Set a fullscreen mode on landscape orientation and resize to fit screen in portrait orientation
        mInfo = findViewById(R.id.info);
        if (pNewConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            View decorView = getWindow().getDecorView();
            int SDK_INT = android.os.Build.VERSION.SDK_INT;
            int uiOptions;
            if (SDK_INT < 14) {
                uiOptions = View.STATUS_BAR_VISIBLE;
            } else {
                uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                decorView.setSystemUiVisibility(uiOptions);
            }
            RelativeLayout layout = findViewById(R.id.myVideoLayout2);
            LayoutParams params = (LayoutParams) layout.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            float height = metrics.widthPixels * IMG_HEIGHT / IMG_WIDTH;
            params.height = Math.round(height);
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layout.setLayoutParams(params);
            mVidView.setLayoutParams(params);
            LinearLayout linearLayout = findViewById(R.id.nextLayout);
            ViewGroup.LayoutParams linearparams = linearLayout.getLayoutParams();
            linearparams.height = Math.round(height);
            linearparams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            linearLayout.setLayoutParams(linearparams);
            mInfo.setVisibility(View.VISIBLE);

        } else if (pNewConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            View decorView = getWindow().getDecorView();
            int SDK_INT = android.os.Build.VERSION.SDK_INT;
            int uiOptions;
            if (SDK_INT >= 11) {
                if (SDK_INT < 14) {
                    uiOptions = View.STATUS_BAR_HIDDEN;
                } else if (SDK_INT < 16) {
                    uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                } else if (SDK_INT < 19) {
                    uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LOW_PROFILE
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar
                } else {
                    uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                }
                decorView.setSystemUiVisibility(uiOptions);
            }
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            float height = metrics.widthPixels * IMG_HEIGHT / IMG_WIDTH;
            RelativeLayout layout = findViewById(R.id.myVideoLayout2);
            LayoutParams params = (LayoutParams) layout.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            params.height = Math.round(height);
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layout.setLayoutParams(params);
            mVidView.setLayoutParams(params);
            mInfo.setVisibility(View.GONE);
        }
    }

    /* **
     * Function called on change screen Action
     *//*
    @Override
    public void changeScreen(int full) {
	    // Refresh screen in case of orientation changed
        if (Configuration.ORIENTATION_PORTRAIT == full)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }*/

    /**
     * Manage sound control and use media volume sound control bar
     */
    @Override
    public void showSoundControl() {
        // Manage sound control
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (null != am) {
            int curVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            am.setStreamVolume(AudioManager.STREAM_MUSIC, curVolume, AudioManager.FLAG_SHOW_UI);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean isPlayable() {
        return mIsPlayable;
    }

    private String stringForUrl(String pString) {
        return pString.toLowerCase().replaceAll(" ", "-");
    }

    /**
     * Function called on Android back button pressed action
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("has_been_played", mShow.getMarkRead());
        intent.putExtra("id_show", mShow.getIdShow());
        if (mInitUserRating.intValue() != mShow.getUserRating().intValue()) {
            intent.putExtra("new_rating", mShow.getUserRating());
        }
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    /**
     * Return the title of show depending on meta
     *
     * @return String
     */
    private String getTitle(Show pShow) {
        String textTitle = (!"null".equals(pShow.getFamilyTT()) ? (pShow.getFamilyTT()) : "");
        textTitle += (!"".equals(textTitle) ? " - " : "") + (!"null".equals(pShow.getShowTT()) ? (pShow.getShowTT()) : "");
        String tmp = (!"null".equals(pShow.getShowTT()) ? (pShow.getShowTT()) : "");
        if (0 < pShow.getEpisode() + pShow.getSeason()) {
            if (!"".equals(tmp)) textTitle += " - ";
            if (0 < pShow.getSeason()) {
                textTitle += getString(R.string.prefix_season) + pShow.getSeason();
                if (0 < pShow.getEpisode()) {
                    textTitle += getString(R.string.prefix_episode) + pShow.getEpisode() + getString(R.string.suffix_episode);
                }
            } else if (0 < pShow.getEpisode()) {
                textTitle += "#" + pShow.getEpisode();
            }

        }
        return textTitle;
    }

    private void makeThumb(SmartImageView pThumb, Show pShow, int pPlayButtonId, int pTextViewId) {
        pThumb.setAdjustHorizontal(true);
        pThumb.setAdjustSizeImage(IMG_WIDTH, IMG_HEIGHT);
        if (Constants.DEMO_MODE) {
            pThumb.setImageResource(DbgMode.getDemoImage());
        } else {
            if (pThumb.getId() == R.id.image2) {
                pThumb.setImageUrl(pShow.getScreenshot256(), false);
            } else {
                pThumb.setImageUrl(pShow.getScreenshot128(), false);
            }
        }
        CustomOnClickListener customClick = new CustomOnClickListener(pShow);
        pThumb.setOnClickListener(customClick);
        findViewById(pPlayButtonId).setOnClickListener(customClick);
        ((MyTextView) findViewById(pTextViewId)).setText(getTitle(pShow));

    }

    /**
     * Custom OnClickListener
     */
    private class CustomOnClickListener implements OnClickListener {

        private final Show mShow;

        private CustomOnClickListener(Show pShow) {
            this.mShow = pShow;
        }

        @Override
        public void onClick(View arg0) {
            mVidThumb.setVisibility(View.VISIBLE);
            mVidControl.lock();
            restart(mShow);
        }
    }
}