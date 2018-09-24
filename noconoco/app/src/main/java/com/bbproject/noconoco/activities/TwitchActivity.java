package com.bbproject.noconoco.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.InputType;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bbproject.noconoco.R;
import com.bbproject.noconoco.constants.CategoryType;
import com.bbproject.noconoco.constants.Constants;
import com.bbproject.noconoco.constants.SettingConstants;
import com.bbproject.noconoco.connection.AsyncResponse;
import com.bbproject.noconoco.connection.ConnectTwitch;
import com.bbproject.noconoco.custom.controller.CustomMediaController;
import com.bbproject.noconoco.custom.controller.CustomMediaController.PauseCrtl;
import com.bbproject.noconoco.custom.view.CustomVideoView;
import com.bbproject.noconoco.connection.GetTokenTwitch;
import com.bbproject.noconoco.custom.view.MyTextView;
import com.bbproject.noconoco.utils.SettingUtils;
import com.bbproject.noconoco.custom.view.smartimage.SmartImageView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class TwitchActivity extends FragmentActivity implements AsyncResponse, PauseCrtl {

    private static final String TAG = "TwitchActivity";
    private static final int IMG_WIDTH = 512;
    private static final int IMG_HEIGHT = 288;
    private final SettingUtils mSettings = new SettingUtils(this);
    private final HashMap<String, String> mColorMap = new HashMap<>();
    private CustomVideoView mVidView;
    private ImageButton mPlay;
    private ImageView mAnimView;
    private ImageView mAnimView2;
    private Animation mAnim;
    private boolean mIsPlayable = false;
    private RelativeLayout mVidLayView;
    private boolean mHasStarted = false;
    private boolean mIsPaused = false;
    private boolean mIsBuffering = true;
    private CustomMediaController mVidControl;
    private String mUrl = "";
    private ScrollView mChatScrollView;
    private String mChatText = "";
    private RelativeLayout mRelCtrl;
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

        // The volume buttons should affect the media volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        setContentView(R.layout.activity_video);

        // Define reference to loading image
        mAnimView2 = findViewById(R.id.sansoeil);
        mAnimView = findViewById(R.id.flower);

        // Video view Reference
        mVidView = findViewById(R.id.myVideoPlay);

        // Reference to the layout containing the video bar and animation displayed over the video
        mVidLayView = findViewById(R.id.myVideoLayout);

        // Reference to the layout containing video controller bar
        mRelCtrl = mVidLayView.findViewById(R.id.myVideoCtrl);

        // Adjust the thumbnail to fit the width of the screen
        SmartImageView thumb = findViewById(R.id.myVideoThumb);
        thumb.setAdjustHorizontal(true);
        thumb.setAdjustSizeImage(IMG_WIDTH, IMG_HEIGHT);
        // We hide completly the thumbnail because there is no thumbnail
        findViewById(R.id.nextLayout).setVisibility(View.GONE);

        // We hide also some useless button in this context
        findViewById(R.id.share).setVisibility(View.GONE);
        findViewById(R.id.titlemenu).setVisibility(View.GONE);

        // Define click action on play button
        mPlay = findViewById(R.id.play);
        mPlay.setVisibility(View.VISIBLE);
        mPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View pView) {
                Log.d(TAG, "mPlay>click>Play");
                playAction();
                if (!mHasStarted) {
                    Log.d(TAG, "mPlay>click>Show");
                    // Hide video control bar
                    mRelCtrl.setVisibility(View.GONE);

                    // Check Buffering animation
                    showBuffering();

                    play(mUrl);
                } else {
                    // Resume the video
                    mVidView.start();

                    // Update video control bar
                    mVidControl.updatePausePlay();
                }
            }
        });

        // Instantiate the video bar controller
        mVidControl = new CustomMediaController(this);
        mVidControl.setAnchorView(mVidView);

        // Attach the video bar controller to the video view
        mVidView.setMediaController(mVidControl);

        // Change tab name
        ((MyTextView)findViewById(R.id.tabinfoTitle)).setText(R.string.tchat);

        // We hide also some useless thing in this context
        findViewById(R.id.free).setVisibility(View.GONE);
        mVidLayView.setBackgroundResource(android.R.color.transparent);
        mAnimView2.setVisibility(View.GONE);
        findViewById(R.id.limit).setVisibility(View.GONE);
        findViewById(R.id.record).setVisibility(View.GONE);
        findViewById(R.id.delete).setVisibility(View.GONE);

        // Reference to the chat scrollview
        mChatScrollView = findViewById(R.id.textAreaScroller);

        // Get a twitch token
        new GetTokenTwitch(this, CategoryType.TOKEN_NO_USER).execute();
    }

    /**
     * Reload and initialize a show
     */
    private void restart() {
        mHasStarted = false;
        mIsPlayable = false;
        mIsPaused = false;
        mIsBuffering = true;
        // Connection to irc chat
        try {
            connectIrc();
        } catch (Exception e) {
            Log.e(TAG, "An exception occured ", e);
        }
        // Reset Video control bar
        mRelCtrl.removeAllViews();
        mRelCtrl.setVisibility(View.VISIBLE);

    }

    @Override
    public void processFinish(String pResponse, int pNextStep) {

        Log.d(TAG, "CategoryType " + pNextStep + " - Response received");

        if (pResponse.startsWith("NG")) {
            Log.e(TAG, "CategoryType " + pNextStep + " - Url returns an error : " + pResponse);
        } else if (CategoryType.TOKEN_NO_USER == pNextStep) {
            try {
                // Get a token for non-oauth user
                JSONObject rootObject = new JSONObject(pResponse);
                String sig = rootObject.getString("sig");
                String token = rootObject.getString("token");
                token = URLEncoder.encode(token, "UTF-8");
                mUrl = Constants.TWITCH_URL + token + "&sig=" + sig;
                restart();

                playAction();

                mVidLayView.findViewById(R.id.myVideoCtrl).setVisibility(View.GONE);

                showBuffering();

                play(mUrl);

            } catch (Exception e) {
                Log.e(TAG, "An Exception occured !", e);
                finish();
            }
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
                ((MyTextView) findViewById(R.id.resume)).setText(pSpanned);
                mChatScrollView.post(new Runnable() {
                    public void run() {
                        mChatScrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    /**
     * Play action with url parameter
     */
    private void play(String pUrl) {
        Log.d(TAG, "Play : " + pUrl);
        Uri vidUri = Uri.parse(pUrl);
        mVidView.setVideoURI(vidUri);
        hideBuffering();
        mHasStarted = true;
        mIsBuffering = false;
        mVidView.start();
    }

    /**
     * Method called asynchronously by ConnectTwitch when connection error occurred
     */
    @Override
    public void processRetry(String pMethod, Map<String, String> pMapParam,
                             String pTokenType,
                             String pUrl, int pNextStep, int pRetry) {
        Log.d(TAG, "Retry : " + pUrl + " " + pTokenType);
        // Retry call with a delay of 5s.
        new ConnectTwitch(pMethod, pMapParam, pTokenType, this, pUrl, pNextStep, pRetry, 5000);
    }

    @Override
    public Context getContext() {
        return this;
    }

    /**
     * Method called asynchronously by ConnectTwitch when connection error occurred after X retry.
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
     * Function called by Android system when device change view mode (horizontal or vertical)
     */
    @SuppressLint("ObsoleteSdkInt")
    @Override
    public void onConfigurationChanged(Configuration pNewConfig) {
        super.onConfigurationChanged(pNewConfig);
        // Set a fullscreen mode on landscape orientation and resize to fit screen in portrait orientation
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
            RelativeLayout layout = findViewById(R.id.myVideoLayout2);
            LayoutParams params = (LayoutParams) layout.getLayoutParams();//LayoutParams params=new LayoutParams(paramsNotFullscreen);
            params.setMargins(0, 0, 0, 0);
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layout.setLayoutParams(params);
            mVidView.setLayoutParams(params);
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

    /**
     * Manage connection to the twitch irc chat with autologin if token was stored or display a login webview
     */
    private void connectIrc() {
        // The server to connect to and our details.
        String token = mSettings.getString(SettingConstants.TOKEN_TWITCH, "");
        if ("".equals(token)) {
            // If no token, we display a connect Button
            final RelativeLayout connect = findViewById(R.id.connect);
            connect.setVisibility(View.VISIBLE);
            connect.setOnClickListener(new OnClickListener() {
                @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled", "AddJavascriptInterface"})
                @Override
                public void onClick(View pView) {
                    // When user click on connect Button, webview is displayed
                    final RelativeLayout relativeLayout = findViewById(R.id.webviewLayout);
                    relativeLayout.setVisibility(View.VISIBLE);
                    WebView webView = findViewById(R.id.webview);
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.getSettings().setLoadWithOverviewMode(true);
                    webView.getSettings().setUseWideViewPort(true);
                    webView.setVerticalScrollbarOverlay(true);
                    // Javascript injection for a perfect smartphone fitting
                    JavaScriptInterface jsInterface = new JavaScriptInterface(/*vWebView*/);
                    webView.addJavascriptInterface(jsInterface, "Andro");
                    webView.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(final WebView pView, final String pUrl) {
                            String script = "var elements = document.getElementsByTagName('meta');" +
                                    "for(var key in elements){" +
                                    "  var elm = elements[key];" +
                                    "  if (elm.name == 'viewport'){" +
                                    "    elm.parentNode.removeChild(elm);" +
                                    "  }" +
                                    "}" +
                                    " " +
                                    "var meta = document.createElement('meta');" +
                                    "meta.setAttribute('name', 'viewport');" +
                                    "meta.setAttribute('content','width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=no');" +
                                    "document.getElementsByTagName('head')[0].appendChild(meta);";
                            script += "$('.span12>div').css('width','100%');";
                            script += "$('#oauth_submit').click(function() {" +
                                    "Andro.calllogin( $('#login').val() );" +
                                    "});";
                            pView.loadUrl("javascript:(function(){" + script + "})()");
                        }

                        @Override
                        public boolean shouldOverrideUrlLoading(final WebView pView, final String pUrl) {
                            //http://twitchapps.com/tmi/#access_token=9f91mv5q4gq4ehcch38l9lzm77isjc&scope=chat_login
                            // Split url to get the token
                            if (pUrl.contains("access_token")) {
                                String[] urlSplit = pUrl.split("#");
                                String[] parameterSplit = urlSplit[1].split("&");
                                String token = parameterSplit[0].replace("access_token=", "");
                                mSettings.setString(SettingConstants.TOKEN_TWITCH, token);
                                relativeLayout.setVisibility(View.GONE);
                                connect.setVisibility(View.GONE);

                                try {
                                    // Reload connect function after hidding webview
                                    connectIrc();
                                } catch (Exception e) {
                                    Log.e(TAG, "An exception occured ", e);
                                }
                                return true;
                            }
                            return false;
                        }
                    });
                    // Load url on webview
                    webView.loadUrl(Constants.TWITCH_LOGIN_URL);
                    webView.requestFocus(View.FOCUS_DOWN);
                }
            });
        } else {
            // Auto-login
            //new myIrc().execute();

            TextView text = findViewById(R.id.resume);
            text.setPadding(text.getPaddingLeft(), text.getPaddingTop(), text.getPaddingRight(), 60);

            final EditText editText = findViewById(R.id.editpost);
            editText.setVisibility(View.VISIBLE);
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            findViewById(R.id.editok).setVisibility(View.VISIBLE);

            Thread thread = new Thread() {
                public void run() {
                    try {
                        String token = mSettings.getString(SettingConstants.TOKEN_TWITCH, "");

                        final String login = mSettings.getString(SettingConstants.LOGIN_TWITCH, "");
                        final String server = Constants.TWITCH_SERVER_URL;

                        // The channel which the bot will join.
                        final String channel = Constants.TWITCH_CHANNEL;

                        Socket socket = new Socket(server, Constants.TWITCH_PORT);
                        final BufferedWriter writer = new BufferedWriter(
                                new OutputStreamWriter(socket.getOutputStream()));
                        final BufferedReader reader = new BufferedReader(
                                new InputStreamReader(socket.getInputStream()));

                        findViewById(R.id.editok).setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            String edit = editText.getText().toString();
                                            writer.write("PRIVMSG " + channel + " :" + edit + "\r\n");
                                            writer.flush();
                                            String color = getColor(login);
                                            mChatText += "<font color=\"" + color + "\">" + login + ":</font> " + edit + "<br/>";
                                            printText(Html.fromHtml(mChatText));

                                            editText.setText("");
                                            editText.requestFocus();
                                            hideSoftKeyboard(editText);
                                        } catch (IOException ignored) {
                                        }
                                    }
                                });
                            }
                        });

                        // Log on to the server.
                        writer.write("PASS oauth:" + token + "\r\n"); // mplyjgxqc2t185dn3o4f6zhk3f2ny4
                        writer.write("NICK " + login + "\r\n");
                        //writer.write("USER " + login + " 8 * : Java IRC Hacks Bot\r\n");
                        writer.flush();

                        // Read lines from the server until it tells us we have connected.
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.contains("004")) {
                                // We are now logged in.
                                Log.d(TAG, "Logged successful.");
                                break;
                            } else if (line.contains("433")) {
                                Log.d(TAG, "Nickname is already in use.");
                                return;
                            }
                        }

                        // Join the channel.
                        writer.write("JOIN " + channel + "\r\n");
                        writer.flush();
                        mChatText += "<h2>" + getString(R.string.twitch_welcome) + "</h2>";
                        printText(Html.fromHtml(mChatText));

                        // Keep reading lines from the server.
                        while ((line = reader.readLine()) != null) {
                            if (line.toUpperCase().startsWith("PING ")) {
                                // We must respond to PINGs to avoid being disconnected.
                                writer.write("PONG " + line.substring(5) + "\r\n");
                                writer.flush();
                                Log.d(TAG, "PONG");
                            } else {
                                if (line.contains("PRIVMSG " + channel + " :")) {
                                    String[] channelAr = line.split("PRIVMSG " + channel + " :");
                                    String[] userChanAr = channelAr[0].split("!");
                                    String user = userChanAr[0].substring(1);
                                    String color = getColor(user);
                                    mChatText += "<font color=\"" + color + "\">" + user + ":</font> " + channelAr[1] + "<br/>";
                                    printText(Html.fromHtml(mChatText));
                                }
                                // Print the raw line received by the bot.
                                Log.d(TAG, line);
                            }
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "An IOException occurred!", e);
                    }
                }
            };
            thread.start();
        }
    }

    /**
     * Hide keyboard
     */
    private void hideSoftKeyboard(View pView) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (null != inputMethodManager)
            inputMethodManager.hideSoftInputFromWindow(pView.getWindowToken(), 0);
    }

    /**
     * Javascript interface, to be use from webview
     */
    class JavaScriptInterface {

        //private WebView mAppView;
        private JavaScriptInterface(/*WebView appView*/) {
            //this.mAppView = appView;
        }

        @JavascriptInterface
        public void calllogin(String pLogin) {
            mSettings.setString(SettingConstants.LOGIN_TWITCH, pLogin);
        }
    }
}