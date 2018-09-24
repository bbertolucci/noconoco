package com.bbproject.noconoco.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bbproject.noconoco.R;
import com.bbproject.noconoco.activities.MainActivity;
import com.bbproject.noconoco.constants.CategoryType;
import com.bbproject.noconoco.constants.Constants;
import com.bbproject.noconoco.constants.QualityType;
import com.bbproject.noconoco.constants.SettingConstants;
import com.bbproject.noconoco.model.json.Show;
import com.bbproject.noconoco.connection.ConnectNoco;
import com.bbproject.noconoco.custom.view.SquareLayout;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadOptions;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CastDialog extends Dialog {

    private static final String TAG = "CastDialog";
    private final MainActivity mActivity;
    private final SeekBar mProgress;
    private final ImageButton mPlayButton;
    private final ImageView mOeilRefreshView;
    private final ImageView mOeilView;
    private final RelativeLayout mCtrlView;
    private final Handler mHandler = new Handler();
    private Show mShow;
    private String mTitle;
    private CastContext mCastContext;
    private CastSession mCastSession;
    private SessionManagerListener<CastSession> mSessionManagerListener;
    private RemoteMediaClient mRemoteMediaClient;
    private final Runnable mTimedTask = new Runnable() {

        @Override
        public void run() {
            if (null != mRemoteMediaClient) {
                mRemoteMediaClient.requestStatus();
                if (null != mRemoteMediaClient.getMediaStatus()) {
                    long pos = mRemoteMediaClient.getMediaStatus().getStreamPosition();
                    mProgress.setProgress((int) (1000 * pos / mRemoteMediaClient.getStreamDuration()));
                    mHandler.postDelayed(mTimedTask, 1000);
                }
            }
        }
    };
    private boolean mIsPlaying;
    private boolean mIsCasting = false;
    private boolean mApplicationStarted = false;
    private String mQuality = "";
    private String mAudioLang = "";
    private String mSubLang = "";
    private Map<String, String> mOriginalLanguage = new HashMap<>();
    private Map<String, Map<String, String>> mSubTitleByLang = new HashMap<>();
    private Map<String, Map<String, ArrayList<String>>> mQualityByLangAndSub = new HashMap<>();
    private boolean mVideoIsLoaded;

    public CastDialog(MainActivity pActivity) {
        super(pActivity);
        mActivity = pActivity;
        Window win = getWindow();
        if (null != win) {
            WindowManager.LayoutParams wlmp = win.getAttributes();
            wlmp.gravity = Gravity.CENTER;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                win.setBackgroundDrawable(mActivity.getResources().getDrawable(android.R.color.black, mActivity.getTheme()));
            } else {
                win.setBackgroundDrawable(mActivity.getResources().getDrawable(android.R.color.black));
            }
            win.setAttributes(wlmp);
            win.requestFeature(Window.FEATURE_NO_TITLE);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        setupCastListener();

        mVideoIsLoaded = false;

        setTitle(null);
        setCancelable(true);
        setOnCancelListener(null);
        setContentView(R.layout.dialog_cast);
//        sendMessage("{\"message\":1,\"content\":"+pShow.toJSON()+"}");
        mProgress = findViewById(R.id.mediacontroller_progress);
        mProgress.setMax(1000);
        mProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar bar) {
            }

            public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
                if (!fromuser) {
                    // We're not interested in programmatically generated changes to
                    // the progress bar's position.
                    return;
                }
                long duration = mRemoteMediaClient.getStreamDuration();
                long newposition = (duration * progress) / 1000L;
                mRemoteMediaClient.seek(newposition);
            }

            public void onStopTrackingTouch(SeekBar bar) {
            }
        });
        SeekBar mSoundProgress = findViewById(R.id.soundcontroller_progress);
        mSoundProgress.setMax(100);
        mSoundProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar bar) {
            }

            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                if (!fromUser) {
                    // We're not interested in programmatically generated changes to
                    // the progress bar's position.
                    return;
                }
                //mRemoteMediaPlayer.setStreamVolume(mApiClient, (double)progress/100);
                mRemoteMediaClient.setStreamVolume((double) progress / 100);
            }

            public void onStopTrackingTouch(SeekBar bar) {
            }
        });


//        mShow = pShow;
//        mTitle = pShow.getFamilyTT() + " - " + pShow.getShowTT();
        mPlayButton = findViewById(R.id.pause);
        mPlayButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mIsPlaying) {
                    mIsPlaying = false;
                    v.setBackgroundResource(R.drawable.icon_play_2);
                    onPauseAction();
                } else {
                    mIsPlaying = true;
                    v.setBackgroundResource(R.drawable.icon_pause);
                    onPlayAction(mShow.getIdShow());
                }
            }
        });

        mCtrlView = findViewById(R.id.ctrl);
        mOeilView = findViewById(R.id.sansoeil);
        mOeilRefreshView = findViewById(R.id.oeilrefresh);
/*        if(null==mOeilRefreshView.getAnimation()) {
            Animation rotation = AnimationUtils.loadAnimation(mActivity, R.anim.rotate);
            rotation.setRepeatCount(Animation.INFINITE);
            mOeilRefreshView.startAnimation(rotation);
        }

        new ConnectNoco("GET",null,"Bearer", mActivity, Constants.GET_SHOWS+pShow.getIdShow()+Constants.GET_SHOWS_MEDIAS, CategoryType.MEDIAS);//.execute();
*/
    }

    @Override
    public void dismiss() {
        if (mIsPlaying) {
            mRemoteMediaClient.stop();
        }
        super.dismiss();
    }

    public void loadShow(Show pShow) {
        mVideoIsLoaded = false;
        mShow = pShow;
        sendMessage("{\"message\":1,\"content\":" + pShow.toJSON() + "}");
        mTitle = pShow.getFamilyTT() + " - " + pShow.getShowTT();
        if (null == mOeilRefreshView.getAnimation()) {
            Animation rotation = AnimationUtils.loadAnimation(mActivity, R.anim.rotate);
            rotation.setRepeatCount(Animation.INFINITE);
            mOeilRefreshView.startAnimation(rotation);
        }

        new ConnectNoco("GET", null, "Bearer", mActivity, Constants.GET_SHOWS + pShow.getIdShow() + Constants.GET_SHOWS_MEDIAS, CategoryType.MEDIAS);
    }

    private void setupCastListener() {
        mCastContext = CastContext.getSharedInstance(mActivity);
        mCastContext.addCastStateListener(new CastStateListener() {
            @Override
            public void onCastStateChanged(int i) {
                switch (i) {
                    case CastState.NO_DEVICES_AVAILABLE:
                    case CastState.NOT_CONNECTED:
                    case CastState.CONNECTED: {
                        mActivity.findViewById(R.id.chromecast).setVisibility(View.VISIBLE);
                        break;
                    }
                    case CastState.CONNECTING: {
                        break;
                    }
                }
            }
        });
        mSessionManagerListener = new SessionManagerListener<CastSession>() {

            @Override
            public void onSessionEnded(CastSession session, int error) {
                if (session == mCastSession) {
                    mCastSession = null;
                }
//                if (null != mCast) {
                //mCast.
                dismiss();
//                }
                ((ImageView) mActivity.findViewById(R.id.castback)).setImageResource(R.drawable.quantum_ic_cast_white_36);
                teardown();
            }

            @Override
            public void onSessionResumed(CastSession session, boolean wasSuspended) {
                mCastSession = session;
                loadRemoteMedia();
            }

            @Override
            public void onSessionStarted(CastSession session, String sessionId) {
                mIsCasting = true;
                mApplicationStarted = true;
                mCastSession = session;
                ((ImageView) mActivity.findViewById(R.id.castback)).setImageResource(R.drawable.quantum_ic_cast_connected_white_24);
            }

            @Override
            public void onSessionStarting(CastSession session) {
            }

            @Override
            public void onSessionStartFailed(CastSession session, int error) {
            }

            @Override
            public void onSessionEnding(CastSession session) {

            }

            @Override
            public void onSessionResuming(CastSession session, String sessionId) {
            }

            @Override
            public void onSessionResumeFailed(CastSession session, int error) {
            }

            @Override
            public void onSessionSuspended(CastSession session, int reason) {
            }
        };
        mCastContext.getSessionManager().addSessionManagerListener(mSessionManagerListener, CastSession.class);

    }

    private void reloadCtrl() {
        int j = 0;
        int k = 100;
        int l = 200;
        String la = "";
        String su = "";
        String qu = "";
        for (Map.Entry<String, Map<String, ArrayList<String>>> entry : mQualityByLangAndSub.entrySet()) {
            la += (!"".equals(la) ? ",\"" : "\"") + entry.getKey() + "\"";
            if (mAudioLang.equals(entry.getKey())) {
                for (Map.Entry<String, ArrayList<String>> entry2 : mQualityByLangAndSub.get(entry.getKey()).entrySet()) {
                    su += (!"".equals(su) ? ",\"" : "\"") + entry2.getKey() + "\"";
                    if (mSubLang.equals(entry2.getKey())) {
                        for (String key : mQualityByLangAndSub.get(entry.getKey()).get(entry2.getKey())) {
                            qu += (!"".equals(qu) ? ",\"" : "\"") + QualityType.getQualityNameByKey(key) + "\"";
                        }
                    }
                }
            }
        }

        RelativeLayout relCtrl = findViewById(R.id.myVideoCtrl);
        relCtrl.removeAllViews();
        for (Map.Entry<String, Map<String, ArrayList<String>>> entry : mQualityByLangAndSub.entrySet()) {
            j++;
            Log.d(TAG, "" + entry.getKey() + " " + mOriginalLanguage.get(entry.getKey()));

            LayoutInflater inflate = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (null == inflate) {
                throw new AssertionError("LayoutInflater not found.");
            }
            SquareLayout square = (SquareLayout) inflate.inflate(R.layout.view_btn_square, relCtrl, false);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            if (j == 1) params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            else params.addRule(RelativeLayout.LEFT_OF, j - 1);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            square.setId(j);
            square.setTag(entry.getKey());
            square.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAudioLang = (String) v.getTag();
                    mSubLang = "";
                    String subLang = mActivity.getSettings().getString(SettingConstants.SUB_LANGUAGE, "");
                    for (Map.Entry<String, String> entry : mSubTitleByLang.get(mAudioLang).entrySet()) {
                        if (((subLang.equals("null")) && ("none".equals(entry.getKey())))
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
                    }
                    if ("".equals(mSubLang)) {
                        Map.Entry<String, String> entry = mSubTitleByLang.get(mAudioLang).entrySet().iterator().next();
                        mSubLang = entry.getKey();
                    }

                    String q = mQuality;
                    mQuality = "";

                    for (String key : mQualityByLangAndSub.get(mAudioLang).get(mSubLang)) {
                        if (q.equals(key)) {
                            mQuality = key;
                        }
                    }
                    if (mQuality.isEmpty()) {
                        String qualityMobile = mActivity.getSettings().getString(SettingConstants.QUALITY_MOBILE, "");
                        for (String key : mQualityByLangAndSub.get(mAudioLang).get(mSubLang)) {
                            if (qualityMobile.equals(key)) {
                                mQuality = key;
                            }
                        }
                    }
                    if (mQuality.isEmpty()) {
                        String qualityMobile = Locale.getDefault().getLanguage();
                        for (String key : mQualityByLangAndSub.get(mAudioLang).get(mSubLang)) {
                            if (qualityMobile.equals(key)) {
                                mQuality = key;
                            }
                        }
                    }
                    if (mQuality.isEmpty()) {
                        for (int z = 0; z < 5 && mQuality.isEmpty(); z++) {
                            String search = "";
                            switch (z) {
                                case 0:
                                    search = "LQ";
                                    break;
                                case 1:
                                    search = "HQ";
                                    break;
                                case 2:
                                    search = "TV";
                                    break;
                                case 3:
                                    search = "HD_720";
                                    break;
                                case 4:
                                    search = "HD_1080";
                                    break;
                            }
                            for (String key : mQualityByLangAndSub.get(mAudioLang).get(mSubLang)) {
                                if (search.equals(key)) {
                                    mQuality = key;
                                    break;
                                }
                            }
                        }
                    }
                    reloadCtrl();
                    if (mVideoIsLoaded) {
                        mVideoIsLoaded = false;
                        onPlayAction(mShow.getIdShow());
                    }
                }
            });
            params.setMargins(5, 5, 5, 5);
            ((TextView) square.findViewById(R.id.text)).setText(entry.getKey());
            relCtrl.addView(square, params);
            if (mAudioLang.equals(entry.getKey())) {
                square.setSelected(true);
                for (Map.Entry<String, ArrayList<String>> entry2 : entry.getValue().entrySet()) {
                    k++;
                    Log.d(TAG, "" + entry2.getKey() + " " + mSubTitleByLang.get(entry.getKey()).get(entry2.getKey()));
                    LayoutInflater inflate2 = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    if (null == inflate2) {
                        throw new AssertionError("LayoutInflater not found.");
                    }

                    SquareLayout square2 = (SquareLayout) inflate2.inflate(R.layout.view_btn_square, relCtrl, false);
                    RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    if (k == 101)
                        params2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                    else params2.addRule(RelativeLayout.LEFT_OF, k - 1);
                    params2.addRule(RelativeLayout.BELOW, j);
                    square2.setId(k);
                    square2.setTag(entry2.getKey());
                    square2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mSubLang = (String) v.getTag();
                            String q = mQuality;
                            mQuality = "";

                            for (String key : mQualityByLangAndSub.get(mAudioLang).get(mSubLang)) {
                                if (q.equals(key)) {
                                    mQuality = key;
                                }
                            }
                            if (mQuality.isEmpty()) {
                                String qualityMobile = mActivity.getSettings().getString(SettingConstants.QUALITY_MOBILE, "");
                                for (String key : mQualityByLangAndSub.get(mAudioLang).get(mSubLang)) {
                                    if (qualityMobile.equals(key)) {
                                        mQuality = key;
                                    }
                                }
                            }
                            if (mQuality.isEmpty()) {
                                String qualityMobile = Locale.getDefault().getLanguage();
                                for (String key : mQualityByLangAndSub.get(mAudioLang).get(mSubLang)) {
                                    if (qualityMobile.equals(key)) {
                                        mQuality = key;
                                    }
                                }
                            }
                            if (mQuality.isEmpty()) {
                                for (int z = 0; z < 5 && mQuality.isEmpty(); z++) {
                                    String search = "";
                                    switch (z) {
                                        case 0:
                                            search = "LQ";
                                            break;
                                        case 1:
                                            search = "HQ";
                                            break;
                                        case 2:
                                            search = "TV";
                                            break;
                                        case 3:
                                            search = "HD_720";
                                            break;
                                        case 4:
                                            search = "HD_1080";
                                            break;
                                    }
                                    for (String key : mQualityByLangAndSub.get(mAudioLang).get(mSubLang)) {
                                        if (search.equals(key)) {
                                            mQuality = key;
                                            break;
                                        }
                                    }
                                }
                            }
                            reloadCtrl();
                            if (mVideoIsLoaded) {
                                mVideoIsLoaded = false;
                                onPlayAction(mShow.getIdShow());
                            }
                        }
                    });
                    params2.setMargins(5, 5, 5, 5);
                    ((TextView) square2.findViewById(R.id.text)).setText(entry2.getKey());
                    relCtrl.addView(square2, params2);
                    if (mSubLang.equals(entry2.getKey())) {
                        square2.setSelected(true);
                        List<String> listQuality = new ArrayList<>();
                        for (int z = 0; z < 5; z++) {
                            String search = "";
                            switch (z) {
                                case 0:
                                    search = "LQ";
                                    break;
                                case 1:
                                    search = "HQ";
                                    break;
                                case 2:
                                    search = "TV";
                                    break;
                                case 3:
                                    search = "HD_720";
                                    break;
                                case 4:
                                    search = "HD_1080";
                                    break;
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
//                            Log.d(TAG,""+vListQuality.get(l-201)+" "+mQualityByLangAndSub.get(entry.getKey()).get(entry2.getKey()).get(vListQuality.get(l-201)));
                            LayoutInflater inflate3 = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            if (null == inflate3) {
                                throw new AssertionError("LayoutInflater not found.");
                            }
                            SquareLayout square3 = (SquareLayout) inflate3.inflate(R.layout.view_btn_square, relCtrl, false);
                            RelativeLayout.LayoutParams params3 = new RelativeLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                            );
                            if (l == 201)
                                params3.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                            else params3.addRule(RelativeLayout.RIGHT_OF, l - 1);
                            square3.setId(l);
                            square3.setTag(listQuality.get(l - 201));
                            square3.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mQuality = (String) v.getTag();
                                    reloadCtrl();
                                    if (mVideoIsLoaded) {
                                        mVideoIsLoaded = false;
                                        onPlayAction(mShow.getIdShow());
                                    }
                                }
                            });
                            params3.setMargins(5, 5, 5, 5);
                            ((TextView) square3.findViewById(R.id.text)).setText(QualityType.getQualityNameByKey(listQuality.get(l - 201)));
                            relCtrl.addView(square3, params3);
                            if (mQuality.equals(listQuality.get(l - 201))) {
                                square3.setSelected(true);
                            }
                        }
                    }
                }
                if (k == 101 && "none".equals(mSubLang)) findViewById(k).setVisibility(View.GONE);
            }
        }
        TextView text = new TextView(mActivity);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.LEFT_OF, j);
        int vt = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, mActivity.getResources().getDisplayMetrics()));
        params.setMargins(5, 5 + vt, 5, 5 + vt);
        text.setText(R.string.video_lang);
        text.setTextColor(Color.WHITE);
        text.setShadowLayer(2, 1, 1, R.color.text_shadow);
        relCtrl.addView(text, params);
        if (k != 101 || (!"none".equals(mSubLang))) {
            text = new TextView(mActivity);
            params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.addRule(RelativeLayout.BELOW, j);
            params.addRule(RelativeLayout.LEFT_OF, k);
            params.setMargins(5, 5 + vt, 5, 5 + vt);
            text.setText(R.string.video_sub);
            text.setTextColor(Color.WHITE);
            text.setShadowLayer(2, 1, 1, R.color.text_shadow);
            relCtrl.addView(text, params);
        }
        sendMessage("{\"message\":2,\"content\":{\"selectLang\":\"" + mAudioLang + "\",\"selectSub\":\"" + mSubLang + "\",\"selectQua\":\"" + QualityType.getQualityNameByKey(mQuality) + "\",\"lang\":[" + la + "],\"sub\":[" + su + "],\"qua\":[" + qu + "]}}");
    }

    private void sendMessage(String pMessage) {

        if (mCastSession != null) {
            Log.d(TAG, mActivity.getPackageName());
            mCastSession.sendMessage("urn:x-cast:" + mActivity.getPackageName(), pMessage)
//    	     Cast.CastApi.sendMessage(mApiClient, )
                    .setResultCallback(
                            new ResultCallback<Status>() {
                                @Override
                                public void onResult(@NonNull Status result) {
                                    if (!result.isSuccess()) {
                                        Log.e(TAG, "Sending message failed");
                                    }
                                }
                            });
        }
    }

    public void initMedia(String pResponse) throws JSONException {

        mOriginalLanguage = new HashMap<>();
        mSubTitleByLang = new HashMap<>();
        mQualityByLangAndSub = new HashMap<>();
        mQuality = "";
        mAudioLang = "";
        mSubLang = "";

        String audioLang = mActivity.getSettings().getString(SettingConstants.AUDIO_LANGUAGE, "");
        int i = 0;
        JSONObject root_object = new JSONObject(pResponse);
        Iterator<String> keysfirst = root_object.keys();
        while (keysfirst.hasNext()) {
            String keyfirst = keysfirst.next();
            if ((audioLang.equals(keyfirst)) ||
                    ((audioLang.equals("original")) && (i == 0) && !"fr".equals(keyfirst)) ||
                    ((mAudioLang.equals("") && !keysfirst.hasNext()))) {
                mAudioLang = keyfirst;
            }
            JSONObject first = root_object.getJSONObject(keyfirst);
            JSONObject second = first.getJSONObject("audio_lang");
            Iterator<String> keyssecond = second.keys();
            while (keyssecond.hasNext()) {
                String keysecond = keyssecond.next();
                if ((Locale.getDefault().getLanguage().equals(keysecond)) || (!keyssecond.hasNext())) {
                    mOriginalLanguage.put(keyfirst, second.getString(keysecond));
                    Log.d(TAG, keyfirst + " " + second.getString(keysecond));
                    break;
                }
            }
            second = first.getJSONObject("video_list");
            keyssecond = second.keys();
            String subLang = mActivity.getSettings().getString(SettingConstants.SUB_LANGUAGE, "");
            String qualityMobile = mActivity.getSettings().getString(SettingConstants.QUALITY_MOBILE, "");
            //Map<String,Map<String,Quality>> vMapQualityBySub = new HashMap<>();
            Map<String, ArrayList<String>> mapQualityBySub = new HashMap<>();
            while (keyssecond.hasNext()) {
                String keysecond = keyssecond.next();
                if ((!mAudioLang.equals("")) &&
                        ((subLang.equals("null") && ("none".equals(keysecond))) ||
                                (subLang.equals(keysecond)) ||
                                (mSubLang.equals("") && !keyssecond.hasNext()))) {
                    mSubLang = keysecond;
                }
                JSONObject third = second.getJSONObject(keysecond);

                JSONObject fourth = third.getJSONObject("sub_lang");

                Iterator<String> keysfourth = fourth.keys();
                HashMap<String, String> subtitleMap = new HashMap<>();
                while (keysfourth.hasNext()) {
                    String keyfourth = keysfourth.next();
                    if ((Locale.getDefault().getLanguage().equals(keyfourth)) || (!keysfourth.hasNext())) {
                        subtitleMap.put(keysecond, fourth.getString(keyfourth));
                        break;
                    }
                }
                mSubTitleByLang.put(keyfirst, subtitleMap);

                fourth = third.getJSONObject("quality_list");
                keysfourth = fourth.keys();
                ArrayList<String> qualityList = new ArrayList<>();
                while (keysfourth.hasNext()) {
                    String keyfourth = keysfourth.next();
                    if ((!mAudioLang.equals("")) &&
                            (((mQuality.equals("") && qualityMobile.equals(keyfourth)) || (mQuality.equals("") && "none".equals(keysecond) && !keysfourth.hasNext())) ||
                                    ((mQuality.equals("") && !keysfirst.hasNext() && !keysfourth.hasNext())))) {
                        mQuality = keyfourth;
                    }
                    qualityList.add(keyfourth);
                }
                mapQualityBySub.put(keysecond, qualityList);
                Log.d(TAG, keysecond + " " + second.getString(keysecond));
            }
            mQualityByLangAndSub.put(keyfirst, mapQualityBySub);

            ++i;
        }

        mOeilView.setVisibility(View.GONE);
        mOeilRefreshView.setVisibility(View.GONE);
        mCtrlView.setVisibility(View.VISIBLE);
        reloadCtrl();

    }

    public void play(String pResponse) throws JSONException {
        JSONObject root_object = new JSONObject(pResponse);
        String code_response = root_object.getString("code_reponse");
        switch (code_response) {
            case "1":
                String file = root_object.getString("file");
                startVideo(file);
                break;
            case "2":
            case "3": {
                JSONObject first = root_object.getJSONObject("popmessage");
                String message = first.getString("message");
                Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
                hide();
                break;
            }
        }
    }

    public void teardown() {
        if (mCastSession != null) {
            if (mApplicationStarted) {
                try {
                    mHandler.removeCallbacks(mTimedTask);

                    if (null != mRemoteMediaClient) {
                        mCastSession.removeMessageReceivedCallbacks(mRemoteMediaClient.getNamespace());
                        //Cast.CastApi.removeMessageReceivedCallbacks( mApiClient, mRemoteMediaClient.getNamespace() );
                        mRemoteMediaClient = null;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Exception while removing application " + e);
                }
                mApplicationStarted = false;
            }
            if (mCastSession.isConnected()) {
                mRemoteMediaClient.stop();
                mCastContext.getSessionManager().endCurrentSession(true);
                mIsCasting = false;
            }
        }
        mVideoIsLoaded = false;
    }

    private void loadRemoteMedia() {
        if (null == mCastContext) {
            return;
        }
        mRemoteMediaClient = mCastSession.getRemoteMediaClient();
        mRemoteMediaClient.registerCallback(new RemoteMediaClient.Callback() {
            @Override
            public void onStatusUpdated() {
                int status = mRemoteMediaClient.getPlayerState();
                if (status == MediaStatus.PLAYER_STATE_PLAYING) {
                    mIsPlaying = true;
                } else {
                    mPlayButton.setBackgroundResource(R.drawable.icon_play_2);
                    mIsPlaying = false;
                    mVideoIsLoaded = false;
                }
            }
        });
    }

    private void onPauseAction() {
        if (mRemoteMediaClient == null || !mVideoIsLoaded)
            return;
        mRemoteMediaClient.pause();
        mHandler.removeCallbacks(mTimedTask);
    }

    private void onPlayAction(String pIdshow) {
        if (!mVideoIsLoaded)
            new ConnectNoco("GET", null, "Bearer", mActivity, Constants.GET_SHOWS + pIdshow + Constants.GET_SHOWS_VIDEO + mQuality + "/" + mAudioLang + (!"none".equals(mSubLang) ? (!"".equals(mSubLang) ? (!"null".equals(mSubLang) ? "?sub_lang=" + mSubLang : "") : "") : ""), CategoryType.PLAY);
        else {
            if (null != mRemoteMediaClient) {
                long duration = mRemoteMediaClient.getStreamDuration();
                long newPosition = (duration * mProgress.getProgress()) / 1000L;
                mRemoteMediaClient.seek(newPosition);
                mRemoteMediaClient.play();
                mHandler.postDelayed(mTimedTask, 1000);
            }
        }
    }

    private void startVideo(String pPath) {

        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, mTitle);

        MediaInfo mediaInfo = new MediaInfo.Builder(pPath)
                .setContentType("video/mp4")
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
                .build();
        MediaLoadOptions options = new MediaLoadOptions.Builder()
                .setAutoplay(true)
                .build();
        try {
            mRemoteMediaClient.load(mediaInfo, options).setResultCallback(new ResultCallback<RemoteMediaClient.MediaChannelResult>() {
                @Override
                public void onResult(@NonNull RemoteMediaClient.MediaChannelResult mediaChannelResult) {
                    if (mediaChannelResult.getStatus().isSuccess()) {
                        mVideoIsLoaded = true;
                        mHandler.postDelayed(mTimedTask, 1000);
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Exception : " + e);
        }
    }

    public boolean isCasting() {
        return mIsCasting;
    }

    public void pauseCast() {
        mCastContext.getSessionManager().removeSessionManagerListener(mSessionManagerListener, CastSession.class);
    }

    public void resumeCast() {
        if (null == mCastContext) {
            mCastContext = CastContext.getSharedInstance(mActivity);
        }
        if (null != mCastContext) {
            mCastContext.getSessionManager().addSessionManagerListener(mSessionManagerListener, CastSession.class);
            if (null == mCastSession) {
                mCastSession = mCastContext.getSessionManager().getCurrentCastSession();
            }
        }
    }
}