package com.bbproject.noconoco.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.view.ViewPager.PageTransformer;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v7.app.MediaRouteButton;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.bbproject.noconoco.R;
import com.bbproject.noconoco.adapters.CustomLeftListAdapter;
import com.bbproject.noconoco.adapters.SpinnerBaseAdapter;
import com.bbproject.noconoco.adapters.TabsPagerAdapter;
import com.bbproject.noconoco.constants.CategoryType;
import com.bbproject.noconoco.constants.Constants;
import com.bbproject.noconoco.constants.SettingConstants;
import com.bbproject.noconoco.dialog.CastDialog;
import com.bbproject.noconoco.model.FamArrayList;
import com.bbproject.noconoco.model.GroupItem;
import com.bbproject.noconoco.model.ShowArrayList;
import com.bbproject.noconoco.model.SubGroupItem;
import com.bbproject.noconoco.model.json.Abo;
import com.bbproject.noconoco.model.json.Family;
import com.bbproject.noconoco.model.json.Show;
import com.bbproject.noconoco.services.DownloadService;
import com.bbproject.noconoco.connection.AsyncResponse;
import com.bbproject.noconoco.connection.ConnectNoco;
import com.bbproject.noconoco.connection.GetTokenNoco;
import com.bbproject.noconoco.custom.view.MyTextView;
import com.bbproject.noconoco.utils.ObjectSerializer;
import com.bbproject.noconoco.utils.SettingUtils;
import com.bbproject.noconoco.dialog.TransparentProgressDialog;
import com.google.android.gms.cast.framework.CastButtonFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.bbproject.noconoco.constants.Constants.DEMO_MODE;

public class MainActivity extends FragmentActivity implements AsyncResponse {

    private static final String TAG = "MainActivity";
    // Pagination map
    private final SparseIntArray mCurrentPagination = new SparseIntArray();
    // Loading Dialog
    private TransparentProgressDialog progress;
    // Webview for login
    private WebView mWebView;
    // Utils to store userdata
    private SettingUtils mSettings;
    // Subscription map
    private Map<String, Map<String, String>> mSubscriptionsMap = new HashMap<>();
    // Map of Show List
    private SparseArray<ShowArrayList> mListShowMap = new SparseArray<>();
    // Map of Family List
    private SparseArray<FamArrayList> mListFamMap = new SparseArray<>();
    // Subscription List
    private ArrayList<Abo> mListAbo = new ArrayList<>();
    // DrawerLayout
    private DrawerLayout mDrawerLayout;
    // Tabs Pager Adapter
    private TabsPagerAdapter mTabsPagerAdapter;
    // View Pager Adapter
    private ViewPager mViewPager;
    // Boolean to check if we had received an answer to initial request (for subscription list)
    private boolean mAboList = false;
    // Boolean to check if we had received an answer to initial request (for user data)
    private boolean mUserInit = false;
    // Boolean to check if we had received an answer to initial request (for family list)
    private boolean mFamList = false;
    // Current Search field value
    private String mSearch = "";
    // Thumbnail quality
    private int mThumbnailQuality;
    // Playlist selection mode
    private boolean mModeSelectionPlaylist = false;
    // Favorites selection mode
    private boolean mModeSelectionFavorites = false;
    // BroadcastReceiver
    private DataUpdateReceiver dataUpdateReceiver;
    // Layout for Chromecast button
    private RelativeLayout mChromeCast;
    // Layout for Disconnect button
    private RelativeLayout mDisconnect;
    // Parent Image (when parental control is activated)
    private ImageView mParentalImage;
    // Show ID to play (when coming from internet browser)
    private String mShowIdToPlay = "";
    // Discovery mode or user mode
    private boolean mConnected = true;
    //display tiles type
    private boolean mIsGrid = true;
    // Current parental control level
    private Integer mParentalControlLevel = 99;
    // Search input box
    private EditText mSearchText;
    // Cast dialog for video control
    private CastDialog mCast;
    // Are Initial requests over ?
    private boolean mHasInit = false;
    // List of watched ShowId
    private ArrayList<String> mReadList = new ArrayList<>();
    // Map ot rating show  key = showID / value = rating value
    private HashMap<String, Integer> mRatingList = new HashMap<>();
    // Check if application has restarted
    private boolean mRestart = false;

    /**
     * getter
     **/
    public Integer getParentalControlLevel() {
        return mParentalControlLevel;
    }

    public boolean getIsCasting() {
        return (null != mCast) && mCast.isCasting();
    }

    public ShowArrayList getLastShows() {
        return mListShowMap.get(CategoryType.LAST_NEWS);
    }

    public ShowArrayList getFreeShows() {
        return mListShowMap.get(CategoryType.FREE_SHOWS);
    }

    public boolean isConnected() {
        return mConnected;
    }

    public int getThumbnailQuality() {
        return mThumbnailQuality;
    }

    public SettingUtils getSettings() {
        return mSettings;
    }

    public TabsPagerAdapter getTabsPagerAdapter() {
        return mTabsPagerAdapter;
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }

    public boolean getModeSelectionPlaylist() {
        return mModeSelectionPlaylist;
    }

    public boolean getModeSelectionFavorites() {
        return mModeSelectionFavorites;
    }

    /**
     * Initial method to build or recreate an instance
     */
    @SuppressLint({"SetJavaScriptEnabled", "NewApi", "ClickableViewAccessibility", "ObsoleteSdkInt"})
    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Define new instance for Settings Utils and CastDialog.
        mSettings = new SettingUtils(getApplicationContext());
        mCast = new CastDialog(MainActivity.this);

        // Set activity view
        setContentView(R.layout.activity_main);

        if (null != savedInstanceState) {
            // Retrieve a savedInstance
            mSubscriptionsMap = (Map<String, Map<String, String>>) savedInstanceState.getSerializable("SUBSCRIPTION_LIST");
            mListShowMap = savedInstanceState.getSparseParcelableArray("SHOWLISTMAP");
            mListFamMap = savedInstanceState.getSparseParcelableArray("FAMLISTMAP");
            mListAbo = savedInstanceState.getParcelableArrayList("LISTABO");
            mAboList = savedInstanceState.getBoolean("ABOLIST", false);
            mUserInit = savedInstanceState.getBoolean("USERINIT", false);
            mFamList = savedInstanceState.getBoolean("FAMLIST", false);
            mModeSelectionPlaylist = savedInstanceState.getBoolean("MODESELPLAYLIST", false);
            mModeSelectionFavorites = savedInstanceState.getBoolean("MODESELFAV", false);
            mRestart = savedInstanceState.getBoolean("RESTART", false);
            mReadList = savedInstanceState.getStringArrayList("READLIST");
            mRatingList = (HashMap<String, Integer>) savedInstanceState.getSerializable("RATINGLIST");
            mCurrentPagination.put(CategoryType.LAST_NEWS, mListShowMap.get(CategoryType.LAST_NEWS) != null ? mListShowMap.get(CategoryType.LAST_NEWS).size() : 0);
            mCurrentPagination.put(CategoryType.MOST_POPULAR, mListShowMap.get(CategoryType.MOST_POPULAR) != null ? mListShowMap.get(CategoryType.MOST_POPULAR).size() : 0);
            mCurrentPagination.put(CategoryType.UNREAD, mListShowMap.get(CategoryType.UNREAD) != null ? mListShowMap.get(CategoryType.UNREAD).size() : 0);
            mCurrentPagination.put(CategoryType.READ, mListShowMap.get(CategoryType.READ) != null ? mListShowMap.get(CategoryType.READ).size() : 0);
            mCurrentPagination.put(CategoryType.RECOMMENDED_LIST, mListShowMap.get(CategoryType.RECOMMENDED_LIST) != null ? mListShowMap.get(CategoryType.RECOMMENDED_LIST).size() : 0);
            mCurrentPagination.put(CategoryType.PAID_LIST, mListShowMap.get(CategoryType.PAID_LIST) != null ? mListShowMap.get(CategoryType.PAID_LIST).size() : 0);
            mCurrentPagination.put(CategoryType.FREE_SHOWS, mListShowMap.get(CategoryType.FREE_SHOWS) != null ? mListShowMap.get(CategoryType.FREE_SHOWS).size() : 0);
            mCurrentPagination.put(CategoryType.SEARCH_INFO, mListShowMap.get(CategoryType.SEARCH_INFO) != null ? mListShowMap.get(CategoryType.SEARCH_INFO).size() : 0);
        } else {
            // Initialize map with empty list
            mListShowMap.put(CategoryType.USER_PLAYLIST_INFO, new ShowArrayList());
            mListAbo = new ArrayList<>();
            mListFamMap.put(CategoryType.FAM_LIST, new FamArrayList());
            mListShowMap.put(CategoryType.LAST_NEWS, new ShowArrayList());
            mListShowMap.put(CategoryType.MOST_POPULAR, new ShowArrayList());
            mListShowMap.put(CategoryType.UNREAD, new ShowArrayList());
            mListShowMap.put(CategoryType.READ, new ShowArrayList());
            mListShowMap.put(CategoryType.RECOMMENDED_LIST, new ShowArrayList());
            mListShowMap.put(CategoryType.PAID_LIST, new ShowArrayList());
            mListShowMap.put(CategoryType.FREE_SHOWS, new ShowArrayList());
            mListShowMap.put(CategoryType.SEARCH_INFO, new ShowArrayList());
            mListFamMap.put(CategoryType.FAVORITES_LIST, new FamArrayList());
            // Initialize pagination for those list
            mCurrentPagination.put(CategoryType.LAST_NEWS, 0);
            mCurrentPagination.put(CategoryType.MOST_POPULAR, 0);
            mCurrentPagination.put(CategoryType.UNREAD, 0);
            mCurrentPagination.put(CategoryType.READ, 0);
            mCurrentPagination.put(CategoryType.RECOMMENDED_LIST, 0);
            mCurrentPagination.put(CategoryType.PAID_LIST, 0);
            mCurrentPagination.put(CategoryType.FREE_SHOWS, 0);
            mCurrentPagination.put(CategoryType.SEARCH_INFO, 0);
        }
        if (null != getIntent()) {
            Uri data = getIntent().getData();
            if (null != data) {
                // Activity was called from internet browser
                List<String> url = data.getPathSegments();
                if (url.size() > 0 && "emission".equals(url.get(0))) {
                    mShowIdToPlay = url.get(1);
                }
            }
            // Define called mode
            mConnected = getIntent().getBooleanExtra("connected", true);
        }
        // Get stored user settings : display type and parental control
        mIsGrid = mSettings.getBoolean(SettingConstants.GRID_LIST, true);
        mParentalControlLevel = mSettings.getInteger(SettingConstants.PARENTAL_CONTROL, 99);
        if (!mConnected) {
            mParentalControlLevel = 10;
        }

        // get reference to different view displayed in the drawer list
        mChromeCast = findViewById(R.id.chromecast);
        mDisconnect = findViewById(R.id.disconnect);
        mParentalImage = findViewById(R.id.idparental);
        mChromeCast.setVisibility(View.GONE);
        mDisconnect.setVisibility(View.GONE);
        mParentalImage.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mChromeCast.setAlpha(0f);
            mDisconnect.setAlpha(0f);
            mParentalImage.setAlpha(0f);
        } else {
            mChromeCast.setVisibility(View.GONE);
            mDisconnect.setVisibility(View.GONE);
            mParentalImage.setVisibility(View.GONE);
        }

        // set cast button
        MediaRouteButton mediaRouteButton = findViewById(R.id.cast_button);
        CastButtonFactory.setUpMediaRouteButton(this, mediaRouteButton);

        // set icon_disconnect button
        mDisconnect.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //icon_disconnect user
                disconnect();
            }
        });
        //set parental image button (to disable it)
        mParentalImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View pView) {
                //Start parental activity
                Intent configIntent = new Intent(MainActivity.this, ParentalControlActivity.class);
                startActivityForResult(configIntent, 2);
            }
        });

        // Grid / list button to switch between the 2 display mode
        ImageButton btnList = findViewById(R.id.listic);
        btnList.setOnTouchListener(new OnTouchListener() {
            private Integer lastAction = -1;

            @SuppressLint("ObsoleteSdkInt")
            @Override
            public boolean onTouch(final View pView, MotionEvent pEvent) {
                if (pEvent.getAction() == MotionEvent.ACTION_UP && lastAction != MotionEvent.ACTION_UP) {
                    lastAction = MotionEvent.ACTION_UP;
                    View parent = ((View) pView.getParent());
                    parent.setEnabled(true);
                    // Set button animation
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        parent.setPivotX(parent.getWidth() / 2);
                        parent.setPivotY(parent.getHeight() / 2);
                        parent.setScaleX(1f);
                        parent.setScaleY(1f);
                    }
                    if (!pView.isSelected()) {
                        // switch to list
                        mTabsPagerAdapter.changeToList();
                        pView.setSelected(true);
                        mIsGrid = false;
                        mSettings.setBoolean(SettingConstants.GRID_LIST, false);
                    } else {
                        // switch to grid
                        mTabsPagerAdapter.changeToGrid();
                        pView.setSelected(false);
                        mIsGrid = true;
                        mSettings.setBoolean(SettingConstants.GRID_LIST, true);
                    }
                    return true;
                } else if (pEvent.getAction() == MotionEvent.ACTION_DOWN && lastAction != MotionEvent.ACTION_DOWN) {
                    lastAction = MotionEvent.ACTION_DOWN;
                    View parent = ((View) pView.getParent());
                    parent.setEnabled(false);
                    // Set button animation
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        parent.setPivotX(parent.getWidth() / 2);
                        parent.setPivotY(parent.getHeight() / 2);
                        parent.setScaleX(0.5f);
                        parent.setScaleY(0.5f);
                    }
                }
                return false;
            }
        });
        btnList.setSelected(!mIsGrid);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        if (!mConnected) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            findViewById(R.id.left_big_handle).setVisibility(View.GONE);
        }
        progress = new TransparentProgressDialog(this, 3);
        //progress.setCancelable(false);

        mSearchText = findViewById(R.id.search);
        mSearchText.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return (actionId == EditorInfo.IME_ACTION_GO || (null != event && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) && search();
            }
        });
        ImageButton imgBtn = findViewById(R.id.searchButton);
        imgBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                search();
            }
        });

        if (null == savedInstanceState) {
            if (mConnected) {
                Uri uri = getIntent().getData();
                if (null == uri || !"".equals(mShowIdToPlay)) {

                    String vRefreshToken = mSettings.getString(SettingConstants.REFRESH_TOKEN, "");

                    if (DEMO_MODE || !"".equals(vRefreshToken)) {
                        progress.show();
                        new GetTokenNoco(vRefreshToken, true, this, CategoryType.REFRESH_TOKEN).execute();
                    } else {

                        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                        findViewById(R.id.normalLayout).setVisibility(View.GONE);
                        findViewById(R.id.webviewLayout).setVisibility(View.VISIBLE);
                        mWebView = findViewById(R.id.webview);
                        mWebView.getSettings().setJavaScriptEnabled(true);
                        mWebView.getSettings().setLoadWithOverviewMode(true);
                        mWebView.getSettings().setUseWideViewPort(true);
                        mWebView.setVerticalScrollbarOverlay(true);
                        mWebView.setWebViewClient(new WebViewClient() {
                            @Override
                            public void onPageFinished(final WebView pView, final String pUrl) {
                                    /*String vScript = "var elements = document.getElementsByTagName('meta');" +
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
                                            "document.getElementsByTagName('head')[0].appendChild(meta);";*/
                                String vScript = "$('.ui-btn').css('white-space','initial');" +
                                        "$('.ui-listview>.ui-li-static').css('white-space','initial');" +
                                        "$('.ui-listview>.ui-li-divider').css('white-space','initial');" +
                                        "$('.ui-listview>li>a.ui-btn').css('white-space','initial');";
                                pView.loadUrl("javascript:(function(){" + vScript + "})()");
                            }

                            @Override
                            public boolean shouldOverrideUrlLoading(final WebView pView, final String pUrl) {
                                if (pUrl.startsWith("noconoco")) {
                                    final Uri uri = Uri.parse(pUrl);
                                    mParentalControlLevel = 99;
                                    mSettings.setInteger(SettingConstants.PARENTAL_CONTROL, 99);
                                    mSettings.setString(SettingConstants.PARENTAL_CONTROL_PWD, "");
                                    launchInit(uri);
                                    return true;
                                }
                                return false;
                            }

                            @Override
                            public void onReceivedError(WebView pView, int pErrorCode,
                                                        String pDescription, String pFailingUrl) {
                                Toast.makeText(MainActivity.this, getString(R.string.error_webview), Toast.LENGTH_SHORT).show();
                                super.onReceivedError(pView, pErrorCode, pDescription, pFailingUrl);
                            }
                        });
                        mWebView.loadUrl(Constants.WEB_LOGIN_URL);
                        mWebView.requestFocus(View.FOCUS_DOWN);
                    }
                } else {
                    launchInit(uri);
                }
            } else {
                progress.show();
                new GetTokenNoco("", false, this, CategoryType.TOKEN_NO_USER).execute();
            }
        } else {
            initLeftMenu();
            initPage(false);
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "OnResume");
        if (dataUpdateReceiver == null) dataUpdateReceiver = new DataUpdateReceiver();
        registerReceiver(dataUpdateReceiver, new IntentFilter("Refresh"));
        if (null != mTabsPagerAdapter) mTabsPagerAdapter.refreshRecorded();
        if (null != mCast) mCast.resumeCast();
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (dataUpdateReceiver != null) unregisterReceiver(dataUpdateReceiver);
        if (null != mCast) mCast.pauseCast();
        super.onPause();
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public boolean onCreateOptionsMenu(Menu pMenu) {
        if (mConnected) {
            if (mDrawerLayout.getDrawerLockMode(Gravity.LEFT) != DrawerLayout.LOCK_MODE_LOCKED_CLOSED) {
                if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                } else {
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                }
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
            mDrawerLayout.closeDrawer(Gravity.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle pSavedInstanceState) {
        super.onSaveInstanceState(pSavedInstanceState);
        pSavedInstanceState.putSerializable("SUBSCRIPTION_LIST", (Serializable) mSubscriptionsMap);
//        savedInstanceState.putSerializable("SHOWLISTMAP", mListShowMap);
        pSavedInstanceState.putSparseParcelableArray("SHOWLISTMAP", mListShowMap);
        pSavedInstanceState.putSparseParcelableArray("FAMLISTMAP", mListFamMap);
        pSavedInstanceState.putParcelableArrayList("LISTABO", mListAbo);
        //savedInstanceState.putSparseParcelableArray("PAGINATION", mCurrentPagination);
        pSavedInstanceState.putBoolean("ABOLIST", mAboList);
        pSavedInstanceState.putBoolean("USERINIT", mUserInit);
        pSavedInstanceState.putBoolean("FAMLIST", mFamList);
        pSavedInstanceState.putBoolean("MODESELPLAYLIST", mModeSelectionPlaylist);
        pSavedInstanceState.putBoolean("MODESELFAV", mModeSelectionFavorites);
        pSavedInstanceState.putBoolean("RESTART", mRestart);
        pSavedInstanceState.putStringArrayList("READLIST", mReadList);
        pSavedInstanceState.putSerializable("RATINGLIST", mRatingList);
    }

    @Override
    public void onRestoreInstanceState(Bundle pSavedInstanceState) {
        super.onRestoreInstanceState(pSavedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration pNewConfig) {
        super.onConfigurationChanged(pNewConfig);
        if (null != mTabsPagerAdapter) {
            //In case of screen rotation: recenter the tab list and focus on current one
            mTabsPagerAdapter.recenter(pNewConfig);
        }
    }

    private void launchInit(Uri pUri) {
        String code = pUri.getQueryParameter("code");
        if ("".equals(code)) {
            processFinish("LOGIN", CategoryType.RETRY);
        } else {
            progress.show();
            new GetTokenNoco(code, false, this, CategoryType.TOKEN).execute();
        }
    }

    public void processFinish(String pResponse, int pNextStep) {
        Log.d(TAG, "step" + pNextStep);
        if (!pResponse.startsWith("NG")) {
            try {
                switch (pNextStep) {
                    case CategoryType.TOKEN_NO_USER:
                    case CategoryType.TOKEN:
                    case CategoryType.REFRESH_TOKEN: {
                        JSONObject root_object = new JSONObject(pResponse);
                        String accessToken = root_object.getString("access_token");
                        mSettings.setString(SettingConstants.TOKEN, accessToken);

                        Thread t = new Thread() {
                            public void run() {
                                Intent serviceIntent = new Intent(MainActivity.this, DownloadService.class);
                                serviceIntent.putExtra("type", "refresh");
                                Log.d(TAG, "Restart Service");
                                startService(serviceIntent);
                            }
                        };
                        t.start();

                        if (!"".equals(mShowIdToPlay)) {
                            new ConnectNoco("GET", null, "Bearer", this, Constants.FIND_SHOWS_BY_ID + mShowIdToPlay, CategoryType.FROM_WEB);
                        }
                        if (pNextStep == CategoryType.TOKEN_NO_USER) {
                            new ConnectNoco("GET", null, "Bearer", this, Constants.FREE_SHOWS + 0, CategoryType.FREE_SHOWS);
                        } else {
                            String refreshToken = root_object.getString("refresh_token");
                            mSettings.setString(SettingConstants.REFRESH_TOKEN, refreshToken);

                            new ConnectNoco("GET", null, "Bearer", this, Constants.GET_FAM_LIST, CategoryType.FAM_LIST);
                            new ConnectNoco("GET", null, "Bearer", this, Constants.GET_ABO_LIST, CategoryType.ABO_LIST);
                            new ConnectNoco("GET", null, "Bearer", this, Constants.GET_USERS_INIT, CategoryType.USERS_INIT);
                            new ConnectNoco("GET", null, "Bearer", this, Constants.USER_LIST, CategoryType.USER_LIST_BIS);
                            new ConnectNoco("GET", null, "Bearer", this, Constants.FAVORITES_LIST, CategoryType.FAVORITES_LIST_BIS);
                            new ConnectNoco("GET", null, "Bearer", this, Constants.FIND_SHOWS + 0, CategoryType.LAST_NEWS);
                        }
                        break;
                    }
                    case CategoryType.ABO_LIST: {
                        ArrayList<Abo> abo = new ArrayList<>();
                        Abo.decodeJSON(pResponse, abo);
                        mListAbo = abo;
                        mAboList = true;
                        if (mUserInit && mFamList) initLeftMenu();
                        break;
                    }
                    case CategoryType.FAM_LIST: {
                        FamArrayList families = new FamArrayList();
                        Family.decodeJSON(pResponse, families);
                        mListFamMap.put(CategoryType.FAM_LIST, families);
                        mFamList = true;
                        if (mUserInit && mAboList) initLeftMenu();
                        break;
                    }
                    case CategoryType.USERS_INIT: {
                        JSONObject root_object = new JSONObject(pResponse);
                        JSONObject first = root_object.getJSONObject("user");
                        mSettings.setString(SettingConstants.ID_USER, first.getString("id_user"));
                        mSettings.setString(SettingConstants.USERNAME, first.getString("username"));
                        mSettings.setString(SettingConstants.GEOLOC, first.getString("geoloc"));
                        first = root_object.getJSONObject("options");
                        String quality = mSettings.getString(SettingConstants.QUALITY_MOBILE, "");
                        if (quality.isEmpty())
                            mSettings.setString(SettingConstants.QUALITY_MOBILE, first.getString("quality_mobile"));
                        String lang = mSettings.getString(SettingConstants.AUDIO_LANGUAGE, "");
                        if (lang.isEmpty())
                            mSettings.setString(SettingConstants.AUDIO_LANGUAGE, first.getString("audio_language"));
                        String subtitle = mSettings.getString(SettingConstants.SUB_LANGUAGE, "");
                        if (subtitle.isEmpty())
                            mSettings.setString(SettingConstants.SUB_LANGUAGE, first.getString("sub_language"));

                        Map<String, Map<String, String>> map = new HashMap<>();
                        first = root_object.optJSONObject("subscriptions");
                        if (null != first) {
                            Iterator<String> keys = first.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                JSONObject second = (JSONObject) first.get(key);
                                Map<String, String> value = new HashMap<>();
                                value.put("name", second.getString("name"));
                                value.put("soutien", second.getString("soutien"));
                                value.put("date_start", second.getString("date_start"));
                                value.put("date_end", second.getString("date_end"));
                                map.put(key, value);
                            }
                        }
                        mSubscriptionsMap = map;
                        mUserInit = true;

                        if (mAboList && mFamList) initLeftMenu();
                        break;
                    }
                    case CategoryType.SHOW_BY_PARTNER_REFRESH:
                    case CategoryType.SHOW_BY_PARTNER: {
                        ShowArrayList arrayList = new ShowArrayList();
                        int added = Show.decodeJSON(pResponse, arrayList);
                        if (0 < added) {
                            String showName = arrayList.get(0).getPartnerKey();
                            for (Abo abo : mListAbo) {
                                if (showName.equals(abo.getKey())) {
                                    if (CategoryType.SHOW_BY_PARTNER == pNextStep) {
                                        abo.setCurrentPage(0);
                                        abo.setList(arrayList);
                                        mTabsPagerAdapter.addTabOrRefresh(abo.getList(), added >= Constants.SHOWS_PER_PAGE, false, false, CategoryType.ABO_LIST, abo.getKey());
                                    } else {
                                        abo.setCurrentPage(abo.getCurrentPage() + 1);
                                        abo.getList().addAll(arrayList);
                                        mTabsPagerAdapter.refreshList(abo.getList(), added >= Constants.SHOWS_PER_PAGE, false, false, CategoryType.ABO_LIST, abo.getKey());
                                    }
                                    break;
                                }
                            }
                        }
                        break;
                    }
                    case CategoryType.SHOW_BY_FAMILY_REFRESH:
                    case CategoryType.SHOW_BY_FAMILY: {
                        ShowArrayList arrayList = new ShowArrayList();
                        int added = Show.decodeJSON(pResponse, arrayList);
                        if (0 < added) {
                            String showName = arrayList.get(0).getFamilyKey();
                            FamArrayList families = mListFamMap.get(CategoryType.FAM_LIST);
                            for (Family fam : families) {
                                if (showName.equals(fam.getFamilyKey())) {
                                    if (CategoryType.SHOW_BY_FAMILY == pNextStep) {
                                        fam.setCurrentPage(0);
                                        fam.setList(arrayList);
                                        mTabsPagerAdapter.addTabOrRefresh(fam.getList(), added >= Constants.SHOWS_PER_PAGE, false, false, CategoryType.FAM_LIST, fam.getFamilyKey());
                                    } else {
                                        fam.setCurrentPage(fam.getCurrentPage() + 1);
                                        fam.getList().addAll(arrayList);
                                        mTabsPagerAdapter.refreshList(fam.getList(), added >= Constants.SHOWS_PER_PAGE, false, false, CategoryType.FAM_LIST, fam.getFamilyKey());
                                    }
                                    break;
                                }
                            }
                            mListFamMap.put(CategoryType.FAM_LIST, families);
                        }
                        break;
                    }
                    case CategoryType.SEARCH_INFO: {
                        ShowArrayList shows = new ShowArrayList();
                        int added = Show.decodeJSON(pResponse, shows);
                        mListShowMap.put(pNextStep, shows);
                        mCurrentPagination.put(pNextStep, 0);
                        mTabsPagerAdapter.addTabOrRefresh(shows, added >= Constants.SHOWS_PER_PAGE, true, false, CategoryType.SEARCH_INFO, null);
                        break;
                    }
                    case CategoryType.FREE_SHOWS: {
                        ShowArrayList shows = new ShowArrayList();
                        Show.decodeJSON(pResponse, shows);
                        mListShowMap.put(pNextStep, shows);
                        mCurrentPagination.put(pNextStep, 0);
                        if (!mHasInit) {
                            progress.setText(getString(R.string.loading_thumb));
                            initPage(true);
                            progress.dismiss();
                        }
                        break;
                    }
                    case CategoryType.LAST_NEWS: {
                        ShowArrayList shows = new ShowArrayList();
                        int added = Show.decodeJSON(pResponse, shows);
                        mListShowMap.put(pNextStep, shows);
                        mCurrentPagination.put(pNextStep, 0);
                        if (!mHasInit) {
                            progress.setText(getString(R.string.loading_thumb));
                            mHasInit = true;
                            initPage(true);
                            progress.dismiss();
                        } else {
                            mTabsPagerAdapter.addTabOrRefresh(shows, added >= Constants.SHOWS_PER_PAGE, false, false, CategoryType.LAST_NEWS, null);
                        }
                        break;
                    }
                    case CategoryType.PAID_LIST: {
                        ShowArrayList shows = new ShowArrayList();
                        int added = Show.decodeJSON(pResponse, shows);
                        mListShowMap.put(pNextStep, shows);
                        mCurrentPagination.put(pNextStep, 0);
                        mTabsPagerAdapter.addTabOrRefresh(shows, added >= Constants.SHOWS_PER_PAGE, false, false, CategoryType.PAID_LIST, null);
                        break;
                    }
                    case CategoryType.RECOMMENDED_LIST: {
                        ShowArrayList shows = new ShowArrayList();
                        int added = Show.decodeJSON(pResponse, shows);
                        mListShowMap.put(pNextStep, shows);
                        mCurrentPagination.put(pNextStep, 0);
                        mTabsPagerAdapter.addTabOrRefresh(shows, added >= Constants.SHOWS_PER_PAGE, false, false, CategoryType.RECOMMENDED_LIST, null);
                        break;
                    }
                    case CategoryType.MOST_POPULAR: {
                        ShowArrayList shows = new ShowArrayList();
                        int added = Show.decodeJSON(pResponse, shows);
                        mListShowMap.put(pNextStep, shows);
                        mCurrentPagination.put(pNextStep, 0);
                        mTabsPagerAdapter.addTabOrRefresh(shows, added >= Constants.SHOWS_PER_PAGE, false, false, CategoryType.MOST_POPULAR, null);
                        break;
                    }
                    case CategoryType.READ: {
                        ShowArrayList shows = new ShowArrayList();
                        int added = Show.decodeJSON(pResponse, shows);
                        mListShowMap.put(pNextStep, shows);
                        mCurrentPagination.put(pNextStep, 0);
                        mTabsPagerAdapter.addTabOrRefresh(shows, added >= Constants.SHOWS_PER_PAGE, false, false, CategoryType.READ, null);
                        break;
                    }
                    case CategoryType.UNREAD: {
                        ShowArrayList shows = new ShowArrayList();
                        int added = Show.decodeJSON(pResponse, shows);
                        mListShowMap.put(pNextStep, shows);
                        mCurrentPagination.put(pNextStep, 0);
                        mTabsPagerAdapter.addTabOrRefresh(shows, added >= Constants.SHOWS_PER_PAGE, false, false, CategoryType.UNREAD, null);
                        break;
                    }
                    case CategoryType.USER_LIST:
                    case CategoryType.USER_LIST_BIS: {
                        JSONArray root_object = new JSONArray(pResponse);
                        if (0 < root_object.length()) {
                            JSONObject first = root_object.getJSONObject(0);
                            String playlist = URLEncoder.encode(first.getString("playlist"), "UTF-8");
                            if (!"null".equals(playlist)) {
                                if (CategoryType.USER_LIST == pNextStep)
                                    new ConnectNoco("GET", null, "Bearer", this, Constants.FIND_SHOWS_BY_ID + playlist, CategoryType.USER_PLAYLIST_INFO);
                            } else {
                                mListShowMap.put(CategoryType.USER_PLAYLIST_INFO, new ShowArrayList());
                                if (null != mTabsPagerAdapter) {
                                    mTabsPagerAdapter.addTabOrRefresh(new ShowArrayList(), false, false, false, CategoryType.USER_PLAYLIST_INFO, null);
                                    mTabsPagerAdapter.refreshAll();
                                }
                            }
                        }
                        break;
                    }
                    case CategoryType.FAVORITES_LIST:
                    case CategoryType.FAVORITES_LIST_BIS: {
                        JSONArray root_object = new JSONArray(pResponse);
                        if (0 < root_object.length()) {
                            JSONObject first = root_object.getJSONObject(0);
                            String[] p = first.getString("favorites").split(",");
                            FamArrayList vFavorites = new FamArrayList();
                            if (!"null".equals(p[0])) {
                                FamArrayList families = mListFamMap.get(CategoryType.FAM_LIST);
                                for (String aVP : p) {
                                    int id = Integer.parseInt(aVP);
                                    for (Family fam : families) {
                                        if (fam.getIdFamily() == id) {
                                            vFavorites.add(fam);
                                        }
                                    }
                                }
                            }
                            mListFamMap.put(CategoryType.FAVORITES_LIST, vFavorites);
                            if (CategoryType.FAVORITES_LIST == pNextStep) {
                                mTabsPagerAdapter.addTabOrRefresh(vFavorites, false, false, false, CategoryType.FAVORITES_LIST, null);
                                mTabsPagerAdapter.refreshAll();
                            }
                        }
                        break;
                    }
                    case CategoryType.ADDDEL_USER_LIST:
                    case CategoryType.ADDDEL_FAVORITES_LIST: {
                        //Nothing to do
                        break;
                    }
                    case CategoryType.USER_PLAYLIST_INFO: {
                        ShowArrayList playList = new ShowArrayList();
                        Show.decodeJSON(pResponse, playList);
                        mListShowMap.put(CategoryType.USER_PLAYLIST_INFO, playList);
                        mTabsPagerAdapter.addTabOrRefresh(playList, false, false, false, CategoryType.USER_PLAYLIST_INFO, null);
                        mTabsPagerAdapter.refreshAll();
                        break;
                    }
                    case CategoryType.SEARCH_REFRESH:
                    case CategoryType.SEARCH: {
                        JSONArray root_object = new JSONArray(pResponse);
                        int count = root_object.length();
                        if (count > 0) {
                            StringBuilder query = new StringBuilder();
                            for (int i = 0; i < count; i++) {
                                JSONObject first = root_object.getJSONObject(i);
                                if ("show".equals(first.getString("type"))) {
                                    query.append(first.getString("id")).append((i + 1 < count) ? "," : "");
                                }
                            }
                            query = new StringBuilder(URLEncoder.encode(query.toString(), "UTF-8"));
                            if (!query.toString().isEmpty()) {
                                if (CategoryType.SEARCH == pNextStep) {
                                    new ConnectNoco("GET", null, "Bearer", this, Constants.FIND_SHOWS_BY_ID + query.toString(), CategoryType.SEARCH_INFO);
                                } else {
                                    new ConnectNoco("GET", null, "Bearer", this, Constants.FIND_SHOWS_BY_ID + query, CategoryType.SEARCH_INFO_REFRESH);
                                }
                            }
                        }
                        break;
                    }
                    case CategoryType.SEARCH_INFO_REFRESH: {
                        int currentPage = mCurrentPagination.get(CategoryType.SEARCH_INFO);
                        mCurrentPagination.put(CategoryType.SEARCH_INFO, currentPage + 1);
                        ShowArrayList currentList = mListShowMap.get(CategoryType.SEARCH_INFO);
                        int added = Show.decodeJSON(pResponse, currentList);
                        mListShowMap.put(CategoryType.SEARCH_INFO, currentList);
                        mTabsPagerAdapter.refreshList(currentList, added >= Constants.SHOWS_PER_PAGE, CategoryType.SEARCH_INFO, null);
                        break;
                    }
                    case CategoryType.PAID_LIST_REFRESH: {
                        int currentPage = mCurrentPagination.get(CategoryType.PAID_LIST);
                        mCurrentPagination.put(CategoryType.PAID_LIST, currentPage + 1);
                        ShowArrayList currentList = mListShowMap.get(CategoryType.PAID_LIST);
                        int added = Show.decodeJSON(pResponse, currentList);
                        mListShowMap.put(CategoryType.PAID_LIST, currentList);
                        mTabsPagerAdapter.refreshList(currentList, added >= Constants.SHOWS_PER_PAGE, CategoryType.PAID_LIST, null);
                        break;
                    }
                    case CategoryType.RECOMMENDED_LIST_REFRESH: {
                        int currentPage = mCurrentPagination.get(CategoryType.RECOMMENDED_LIST);
                        mCurrentPagination.put(CategoryType.RECOMMENDED_LIST, currentPage + 1);
                        ShowArrayList currentList = mListShowMap.get(CategoryType.RECOMMENDED_LIST);
                        int added = Show.decodeJSON(pResponse, currentList);
                        mListShowMap.put(CategoryType.RECOMMENDED_LIST, currentList);
                        mTabsPagerAdapter.refreshList(currentList, added >= Constants.SHOWS_PER_PAGE, CategoryType.RECOMMENDED_LIST, null);
                        break;
                    }
                    case CategoryType.READ_REFRESH: {
                        int currentPage = mCurrentPagination.get(CategoryType.READ);
                        mCurrentPagination.put(CategoryType.READ, currentPage + 1);
                        ShowArrayList currentList = mListShowMap.get(CategoryType.READ);
                        int added = Show.decodeJSON(pResponse, currentList);
                        mListShowMap.put(CategoryType.READ, currentList);
                        mTabsPagerAdapter.refreshList(currentList, added >= Constants.SHOWS_PER_PAGE, CategoryType.READ, null);
                        break;
                    }
                    case CategoryType.UNREAD_REFRESH: {
                        int currentPage = mCurrentPagination.get(CategoryType.UNREAD);
                        mCurrentPagination.put(CategoryType.UNREAD, currentPage + 1);
                        ShowArrayList currentList = mListShowMap.get(CategoryType.UNREAD);
                        int added = Show.decodeJSON(pResponse, currentList);
                        mListShowMap.put(CategoryType.UNREAD, currentList);
                        mTabsPagerAdapter.refreshList(currentList, added >= Constants.SHOWS_PER_PAGE, CategoryType.UNREAD, null);
                        break;
                    }
                    case CategoryType.FREE_SHOWS_REFRESH: {
                        int currentPage = mCurrentPagination.get(CategoryType.FREE_SHOWS);
                        mCurrentPagination.put(CategoryType.FREE_SHOWS, currentPage + 1);
                        ShowArrayList currentList = mListShowMap.get(CategoryType.FREE_SHOWS);
                        int added = Show.decodeJSON(pResponse, currentList);
                        mListShowMap.put(CategoryType.FREE_SHOWS, currentList);
                        mTabsPagerAdapter.refreshList(currentList, added >= Constants.SHOWS_PER_PAGE, CategoryType.FREE_SHOWS, null);
                        break;
                    }
                    case CategoryType.MOST_POPULAR_REFRESH: {
                        int currentPage = mCurrentPagination.get(CategoryType.MOST_POPULAR);
                        mCurrentPagination.put(CategoryType.MOST_POPULAR, currentPage + 1);
                        ShowArrayList currentList = mListShowMap.get(CategoryType.MOST_POPULAR);
                        int added = Show.decodeJSON(pResponse, currentList);
                        mListShowMap.put(CategoryType.MOST_POPULAR, currentList);
                        mTabsPagerAdapter.refreshList(currentList, added >= Constants.SHOWS_PER_PAGE, CategoryType.MOST_POPULAR, null);
                        break;
                    }
                    case CategoryType.LAST_NEWS_REFRESH: {
                        int currentPage = mCurrentPagination.get(CategoryType.LAST_NEWS);
                        mCurrentPagination.put(CategoryType.LAST_NEWS, currentPage + 1);
                        ShowArrayList currentList = mListShowMap.get(CategoryType.LAST_NEWS);
                        int added = Show.decodeJSON(pResponse, currentList);
                        mListShowMap.put(CategoryType.LAST_NEWS, currentList);
                        mTabsPagerAdapter.refreshList(currentList, added >= Constants.SHOWS_PER_PAGE, CategoryType.LAST_NEWS, null);
                        break;
                    }
                    case CategoryType.FROM_WEB: {
                        ShowArrayList arrayList = new ShowArrayList();
                        Show.decodeJSON(pResponse, arrayList);
                        if (arrayList.size() > 0) {
                            Intent intent = new Intent(MainActivity.this, VideoActivity.class);
                            intent.putExtra("show", (Serializable) arrayList.get(0));
                            intent.putExtra("recorded", false);
                            mShowIdToPlay = "";
                            startActivityForResult(intent, 1);
                        }
                        break;
                    }
                    case CategoryType.MEDIAS: {
                        mCast.initMedia(pResponse);
                        break;
                    }
                    case CategoryType.PLAY: {
                        mCast.play(pResponse);
                        break;
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSONException on " + pNextStep, e);
                switch (pNextStep) {
                    case CategoryType.TOKEN:
                    case CategoryType.REFRESH_TOKEN:
                    case CategoryType.TOKEN_NO_USER: {
                        disconnect();
                        break;
                    }
                }
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "UnsupportedEncodingException on " + pNextStep, e);
            }
        } else {
            Log.e(TAG, "NG:" + pResponse);
            disconnect();
        }

    }

    private void disconnect() {
        mAboList = false;
        mUserInit = false;
        mSettings.setString(SettingConstants.TOKEN, "");
        mSettings.setString(SettingConstants.REFRESH_TOKEN, "");
        Intent intent = new Intent(MainActivity.this, TitleActivity.class);
        if (null != progress) progress.dismiss();
        finish();
        startActivity(intent);
    }

    @SuppressLint("ObsoleteSdkInt")
    private void initPage(boolean pShouldInit) {
        findViewById(R.id.normalLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.webviewLayout).setVisibility(View.GONE);

        if (null != mWebView) {
            mWebView.loadUrl(Constants.WEB_OFF);
            mWebView.destroy();
            mWebView = null;
        }
        mTabsPagerAdapter = new TabsPagerAdapter(MainActivity.this, getSupportFragmentManager(), pShouldInit);//this,0,mConnected);
        mViewPager = findViewById(R.id.pager);
        mViewPager.addOnPageChangeListener(new OnPageChangeListener() {
            private boolean scrollStarted, checkDirection;

            @Override
            public void onPageSelected(int pPosition) {
                mTabsPagerAdapter.setCurrentItem(pPosition, 0);
            }

            @Override
            public void onPageScrollStateChanged(int pState) {
                if (!scrollStarted && pState == ViewPager.SCROLL_STATE_DRAGGING) {
                    scrollStarted = true;
                    checkDirection = true;
                } else {
                    scrollStarted = false;
                }
            }

            @Override
            public void onPageScrolled(int pPosition, float pPositionOffset, int pPositionOffsetPixels) {
                if (checkDirection) {
                    checkDirection = false;
                }
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mViewPager.setPageTransformer(true, new CustomPageTransformer());
        }
        mViewPager.setAdapter(mTabsPagerAdapter);

        mViewPager.post(new Runnable() {
            @Override
            public void run() {
                mViewPager.setCurrentItem(0);
            }
        });
    }

    /**
     * Animation function :
     * On the left side of the screen,
     * A blue vertical bar is present to notify the user that a slider menu is present
     */
    private void addBlueBarAnimation() {
        View view = findViewById(R.id.left_big_handle);
        //Animation is set to reduce the height of the image by half over 30s
        ScaleAnimation anim = new ScaleAnimation(1f, 1f, 1f, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.66f);
        anim.setFillAfter(true);
        anim.setDuration(3000);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setRepeatMode(Animation.REVERSE);
        view.startAnimation(anim);
        view.setVisibility(View.VISIBLE);
    }

    /**
     * Function called to initialized the left slider menu
     * Should be called when all the prerequisites data are available
     */
    private void initLeftMenu() {

        MyTextView textHead = mDrawerLayout.findViewById(R.id.grouphead);
        textHead.setText(mSettings.getString(SettingConstants.USERNAME, ""));

        final Map<String, FamArrayList> groupAbo = new HashMap<>();
        FamArrayList families = mListFamMap.get(CategoryType.FAM_LIST);
        for (Family fam : families) {
            FamArrayList arrayFam = groupAbo.get(fam.getPartnerKey());
            if (null == arrayFam) arrayFam = new FamArrayList();
            arrayFam.add(fam);
            groupAbo.put(fam.getPartnerKey(), arrayFam);
        }

        final ArrayList<GroupItem> groupItemList = new ArrayList<>();
        groupItemList.add(createGroupItem(CategoryType.LAST_NEWS));
        groupItemList.add(createGroupItem(CategoryType.MOST_POPULAR));
        groupItemList.add(createGroupItem(CategoryType.RECOMMENDED_LIST));
        groupItemList.add(createGroupItem(CategoryType.PAID_LIST));
        groupItemList.add(createGroupItem(CategoryType.MUGEN, GroupItem.TWITCH, null));
        ArrayList<SubGroupItem> subGroupItemList = new ArrayList<>();
        for (Entry<String, Map<String, String>> pairs : mSubscriptionsMap.entrySet()) {
            for (Abo abo : mListAbo) {
                if (abo.getKey().equals(pairs.getKey())) {
                    subGroupItemList.add(createSubGroupItem(CategoryType.ABO_VALUE, abo.getKey()));
                    break;
                }
            }
        }
        groupItemList.add(createGroupItem(CategoryType.ABO_LIST, GroupItem.ICON, subGroupItemList));
        for (Map.Entry<String, FamArrayList> entry : groupAbo.entrySet()) {
            Family currentPartner = entry.getValue().get(0);
            subGroupItemList = new ArrayList<>();
            for (Family fam : entry.getValue()) {
                subGroupItemList.add(createSubGroupItem(CategoryType.FAM_VALUE, fam.getFamilyKey()));
            }
            groupItemList.add(createGroupItem(CategoryType.FAM_LIST, currentPartner.getPartnerKey(), GroupItem.ICON, subGroupItemList));
        }
        subGroupItemList = new ArrayList<>();
        subGroupItemList.add(createSubGroupItem(CategoryType.MY_PLAYLIST, R.drawable.icon_playlist_pin));
        subGroupItemList.add(createSubGroupItem(CategoryType.ADD_PLAYLIST));
        groupItemList.add(createGroupItem(CategoryType.USER_PLAYLIST_INFO, GroupItem.ICON, subGroupItemList));

        subGroupItemList = new ArrayList<>();
        subGroupItemList.add(createSubGroupItem(CategoryType.MY_FAVORITES, R.drawable.icon_favorites));
        subGroupItemList.add(createSubGroupItem(CategoryType.ADD_FAVORITES));
        groupItemList.add(createGroupItem(CategoryType.FAVORITES_LIST, GroupItem.ICON, subGroupItemList));

        subGroupItemList = new ArrayList<>();
        subGroupItemList.add(createSubGroupItem(CategoryType.READ, R.drawable.icon_read));
        subGroupItemList.add(createSubGroupItem(CategoryType.UNREAD, R.drawable.icon_notread));
        subGroupItemList.add(createSubGroupItem(CategoryType.RECORDED, R.drawable.icon_downloaded));
        groupItemList.add(createGroupItem(CategoryType.MY_VIDEO, GroupItem.ICON, subGroupItemList));

        subGroupItemList = new ArrayList<>();
        subGroupItemList.add(createSubGroupItem(CategoryType.THUMB_QUALITY));
        subGroupItemList.add(createSubGroupItem(CategoryType.VIDEO_QUALITY));
        subGroupItemList.add(createSubGroupItem(CategoryType.PARENTAL));
        groupItemList.add(createGroupItem(CategoryType.MY_OPTION, GroupItem.ICON, subGroupItemList));

        mThumbnailQuality = mSettings.getInteger(SettingConstants.THUMB_QUALITY, 0);
        CustomLeftListAdapter adapter = new CustomLeftListAdapter(this, groupItemList);
        ExpandableListView drawerList = mDrawerLayout.findViewById(R.id.left_drawer);
        drawerList.setAdapter(adapter);
        drawerList.setOnGroupClickListener(new OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView pParent, View pView, int pGroupPosition, long pId) {
                GroupItem groupItem = groupItemList.get(pGroupPosition);

                switch (groupItem.getCategoryType()) {
                    case CategoryType.PAID_LIST: {
                        closeDrawer();
                        ShowArrayList showList = mListShowMap.get(groupItem.getCategoryType());
                        mCurrentPagination.put(groupItem.getCategoryType(), 0);
                        mTabsPagerAdapter.addTabOrRefresh(showList, showList.size() >= Constants.SHOWS_PER_PAGE, true, true, groupItem.getCategoryType(), null);
                        new ConnectNoco("GET", null, "Bearer", MainActivity.this, Constants.FIND_SHOWS_PAID + 0, groupItem.getCategoryType());
                        break;
                    }
                    case CategoryType.RECOMMENDED_LIST: {
                        closeDrawer();
                        ShowArrayList showList = mListShowMap.get(groupItem.getCategoryType());
                        mCurrentPagination.put(groupItem.getCategoryType(), 0);
                        mTabsPagerAdapter.addTabOrRefresh(showList, showList.size() >= Constants.SHOWS_PER_PAGE, true, true, groupItem.getCategoryType(), null);
                        new ConnectNoco("GET", null, "Bearer", MainActivity.this, Constants.FIND_SHOWS_RECOMMENDED + 0, groupItem.getCategoryType());
                        break;
                    }
                    case CategoryType.MOST_POPULAR: {
                        closeDrawer();
                        ShowArrayList showList = mListShowMap.get(groupItem.getCategoryType());
                        mCurrentPagination.put(groupItem.getCategoryType(), 0);
                        mTabsPagerAdapter.addTabOrRefresh(showList, showList.size() >= Constants.SHOWS_PER_PAGE, true, true, groupItem.getCategoryType(), null);
                        new ConnectNoco("GET", null, "Bearer", MainActivity.this, Constants.FIND_MOST_POPULAR + 0, groupItem.getCategoryType());
                        break;
                    }
                    case CategoryType.LAST_NEWS: {
                        closeDrawer();
                        ShowArrayList showList = mListShowMap.get(groupItem.getCategoryType());
                        mCurrentPagination.put(groupItem.getCategoryType(), 0);
                        mTabsPagerAdapter.addTabOrRefresh(showList, showList.size() >= Constants.SHOWS_PER_PAGE, true, true, groupItem.getCategoryType(), null);
                        new ConnectNoco("GET", null, "Bearer", MainActivity.this, Constants.FIND_SHOWS + 0, groupItem.getCategoryType());
                        break;
                    }
                    case CategoryType.MUGEN: {
                        closeDrawer();
                        Intent configIntent = new Intent(MainActivity.this, TwitchActivity.class);
                        startActivity(configIntent);
                        break;
                    }
                }
                return false;
            }
        });

        drawerList.setOnChildClickListener(new OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView pParent, View pView, int pGroupPosition, int childPosition, long id) {
                Log.d(TAG, pGroupPosition + " " + childPosition);
                GroupItem groupItem = groupItemList.get(pGroupPosition);
                if (null != groupItem) {
                    ArrayList<SubGroupItem> subGroupItemList = groupItem.getSubList();
                    if (null != subGroupItemList && !subGroupItemList.isEmpty()) {
                        SubGroupItem subGroupItem = subGroupItemList.get(childPosition);
                        switch (groupItem.getCategoryType()) {
                            case CategoryType.MY_OPTION: {
                                switch (subGroupItem.getCategoryType()) {
                                    case CategoryType.VIDEO_QUALITY: {
                                        Intent configIntent = new Intent(MainActivity.this, ConfigActivity.class);
                                        startActivity(configIntent);
                                        break;
                                    }
                                    case CategoryType.THUMB_QUALITY: {
                                        String[] data = new String[]{
                                                getResources().getString(R.string.thumbnail_low),
                                                getResources().getString(R.string.thumbnail_normal),
                                                getResources().getString(R.string.thumbnail_high)
                                        };

                                        final SpinnerBaseAdapter spinnerAdapter = new SpinnerBaseAdapter(MainActivity.this, data, mThumbnailQuality);

                                        final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).setAdapter(spinnerAdapter, null).create();
                                        if (null != dialog.getWindow())
                                            dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
                                        dialog.getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
                                        dialog.getListView().setSelection(mThumbnailQuality);
                                        dialog.getListView().setOnItemClickListener(new OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view,
                                                                    int position, long id) {
                                                spinnerAdapter.setSelectedPosition(position);
                                                mThumbnailQuality = position;
                                                mSettings.setInteger(SettingConstants.THUMB_QUALITY, mThumbnailQuality);
                                                dialog.dismiss();
                                                closeDrawer();
                                                mTabsPagerAdapter.refreshAll();
                                            }
                                        });
                                        dialog.show();

                                        break;
                                    }
                                    case CategoryType.PARENTAL: {
                                        Intent configIntent = new Intent(MainActivity.this, ParentalControlActivity.class);
                                        startActivityForResult(configIntent, 2);
                                        break;
                                    }
                                }
                                break;
                            }
                            case CategoryType.MY_VIDEO: {
                                closeDrawer();
                                switch (subGroupItem.getCategoryType()) {
                                    case CategoryType.READ: {
                                        ShowArrayList showList = mListShowMap.get(subGroupItem.getCategoryType());
                                        mCurrentPagination.put(subGroupItem.getCategoryType(), 0);
                                        mTabsPagerAdapter.addTabOrRefresh(showList, true, true, true, subGroupItem.getCategoryType(), null);
                                        new ConnectNoco("GET", null, "Bearer", MainActivity.this, Constants.GET_SHOWS_READ_ONLY + 0, subGroupItem.getCategoryType());
                                        break;
                                    }
                                    case CategoryType.UNREAD: {
                                        ShowArrayList showList = mListShowMap.get(subGroupItem.getCategoryType());
                                        mCurrentPagination.put(subGroupItem.getCategoryType(), 0);
                                        mTabsPagerAdapter.addTabOrRefresh(showList, true, true, true, subGroupItem.getCategoryType(), null);
                                        new ConnectNoco("GET", null, "Bearer", MainActivity.this, Constants.GET_SHOWS_UNREAD_ONLY + 0, subGroupItem.getCategoryType());
                                        break;
                                    }
                                    case CategoryType.RECORDED: {
                                        mTabsPagerAdapter.refreshRecordedAndSelect();
                                        break;
                                    }
                                }
                                break;
                            }
                            case CategoryType.USER_PLAYLIST_INFO: {
                                closeDrawer();
                                if (CategoryType.ADD_PLAYLIST == subGroupItem.getCategoryType()) {
                                    mModeSelectionPlaylist = true;
                                    ShowArrayList playList = mListShowMap.get(CategoryType.USER_PLAYLIST_INFO);
                                    mTabsPagerAdapter.addTabOrRefresh(playList, false, true, true, CategoryType.USER_PLAYLIST_INFO, null);
                                    StringBuilder playlistStr = new StringBuilder();
                                    ShowArrayList playlistAr = mListShowMap.get(CategoryType.USER_PLAYLIST_INFO);
                                    for (Show show : playlistAr) {
                                        playlistStr.append(",").append(show.getIdShow());
                                    }
                                    try {
                                        if (playlistStr.length() > 0) {
                                            playlistStr = new StringBuilder(URLEncoder.encode(playlistStr.substring(1), "UTF-8"));
                                            new ConnectNoco("GET", null, "Bearer", MainActivity.this, Constants.FIND_SHOWS_BY_ID + playlistStr, CategoryType.USER_PLAYLIST_INFO);
                                        } else {
                                            mListShowMap.put(CategoryType.USER_PLAYLIST_INFO, playList);
                                            mTabsPagerAdapter.addTabOrRefresh(playList, false, false, false, CategoryType.USER_PLAYLIST_INFO, null);
                                        }
                                        mDrawerLayout.findViewById(R.id.blueinfo).setVisibility(View.VISIBLE);
                                        ((TextView) mDrawerLayout.findViewById(R.id.textblueinfo)).setText(R.string.playlistinfo);
                                        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                                        mDrawerLayout.findViewById(R.id.close).setOnClickListener(new OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                mModeSelectionPlaylist = false;
                                                mDrawerLayout.findViewById(R.id.blueinfo).setVisibility(View.GONE);
                                                mDrawerLayout.findViewById(R.id.close).setOnClickListener(null);
                                                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                                            }
                                        });
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }
                                } else /*if (CategoryType.MY_PLAYLIST.equals(vSubGroupItem.getCategoryType())) */ {
                                    ShowArrayList playList = mListShowMap.get(CategoryType.USER_PLAYLIST_INFO);
                                    mTabsPagerAdapter.addTabOrRefresh(playList, false, true, true, CategoryType.USER_PLAYLIST_INFO, null);
                                    new ConnectNoco("GET", null, "Bearer", MainActivity.this, Constants.USER_LIST, CategoryType.USER_LIST);
                                }
                                break;
                            }
                            case CategoryType.FAVORITES_LIST: {
                                closeDrawer();
                                if (CategoryType.ADD_PLAYLIST == subGroupItem.getCategoryType()) {
                                    mModeSelectionFavorites = true;
                                    FamArrayList favorites = mListFamMap.get(CategoryType.FAVORITES_LIST);
                                    mTabsPagerAdapter.addTabOrRefresh(favorites, false, false, false, CategoryType.FAVORITES_LIST, null);
                                    mTabsPagerAdapter.refreshAll();
                                    mDrawerLayout.findViewById(R.id.blueinfo).setVisibility(View.VISIBLE);
                                    ((TextView) mDrawerLayout.findViewById(R.id.textblueinfo)).setText(R.string.favoritesinfo);
                                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                                    mDrawerLayout.findViewById(R.id.close).setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            mModeSelectionFavorites = false;
                                            mDrawerLayout.findViewById(R.id.blueinfo).setVisibility(View.GONE);
                                            mDrawerLayout.findViewById(R.id.close).setOnClickListener(null);
                                            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                                        }
                                    });
                                } else /*if (CategoryType.MY_FAVORITES.equals(vSubGroupItem.getCategoryType())) */ {
                                    FamArrayList favorites = mListFamMap.get(CategoryType.FAVORITES_LIST);
                                    mTabsPagerAdapter.addTabOrRefresh(favorites, false, true, true, CategoryType.FAVORITES_LIST, null);
                                    new ConnectNoco("GET", null, "Bearer", MainActivity.this, Constants.FAVORITES_LIST, CategoryType.FAVORITES_LIST);
                                }
                                break;
                            }
                            case CategoryType.ABO_LIST: {
                                for (Abo abo : mListAbo) {
                                    if (subGroupItem.getKey().equals(abo.getKey())) {
                                        closeDrawer();
                                        mTabsPagerAdapter.addTabOrRefresh(abo.getList(), false, true, true, CategoryType.ABO_LIST, abo.getKey());
                                        new ConnectNoco("GET", null, "Bearer", MainActivity.this, Constants.FIND_SHOWS + "0&" + Constants.BY_PARTNER_KEY + abo.getKey(), CategoryType.SHOW_BY_PARTNER);
                                        break;
                                    }
                                }
                                break;
                            }
                            case CategoryType.FAM_LIST: {
                                loop:
                                for (Map.Entry<String, FamArrayList> entry : groupAbo.entrySet()) {
                                    for (Family fam : entry.getValue()) {
                                        if (fam.getPartnerKey().equals(groupItem.getKey()) && fam.getFamilyKey().equals(subGroupItem.getKey())) {
                                            closeDrawer();
                                            mTabsPagerAdapter.addTabOrRefresh(fam.getList(), false, true, true, CategoryType.FAM_LIST, fam.getFamilyKey());
                                            new ConnectNoco("GET", null, "Bearer", MainActivity.this, Constants.FIND_SHOWS + "0&" + Constants.BY_FAMILY_KEY + fam.getFamilyKey(), CategoryType.SHOW_BY_FAMILY);
                                            break loop;
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                return false;
            }
        });

        addBlueBarAnimation();

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mDrawerLayout.setVisibility(View.VISIBLE);
        mDrawerLayout.addDrawerListener(new DrawerListener() {

            @Override
            public void onDrawerStateChanged(int pNewState) {
            }

            @SuppressLint({"NewApi", "ObsoleteSdkInt"})
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                View contentFragment = mDrawerLayout.findViewById(R.id.main);
                if (null != contentFragment) {
                    int width = drawerView.getWidth();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        contentFragment.setTranslationX(width * slideOffset);
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mChromeCast.setAlpha(slideOffset);
                    mDisconnect.setAlpha(slideOffset);
                    if (mParentalControlLevel != 99) {
                        mParentalImage.setAlpha(slideOffset);
                    }
                } else {
                    mChromeCast.setVisibility(View.VISIBLE);
                    mDisconnect.setVisibility(View.VISIBLE);
                    if (mParentalControlLevel != 99) {
                        mParentalImage.setVisibility(View.VISIBLE);
                    }
                }
                if (slideOffset == 0) {
                    mChromeCast.setVisibility(View.GONE);
                    mDisconnect.setVisibility(View.GONE);
                    mParentalImage.setVisibility(View.GONE);
                } else {
                    mChromeCast.setVisibility(View.VISIBLE);
                    mDisconnect.setVisibility(View.VISIBLE);
                    if (mParentalControlLevel != 99) {
                        mParentalImage.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onDrawerOpened(View pDrawerView) {
                mChromeCast.setVisibility(View.VISIBLE);
                mDisconnect.setVisibility(View.VISIBLE);
                if (mParentalControlLevel != 99) {
                    mParentalImage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onDrawerClosed(View pDrawerView) {
                mChromeCast.setVisibility(View.GONE);
                mDisconnect.setVisibility(View.GONE);
                mParentalImage.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void processRetry(String pMethod, Map<String, String> pMapParam,
                             String pTokenType,
                             String pUrl, int pNextStep, int pRetry) {
        progress.setText(getString(R.string.loading_retry) + " " + pRetry + " ...");
        new ConnectNoco(pMethod, pMapParam, pTokenType, this, pUrl, pNextStep, pRetry, 5000);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void restart(int pNextStep) {
        if (!mRestart) {
            mRestart = true;
            if (CategoryType.REFRESH_TOKEN == pNextStep) {
                mSettings.setString(SettingConstants.TOKEN, "");
                mSettings.setString(SettingConstants.REFRESH_TOKEN, "");
            }
            Toast.makeText(getApplicationContext(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, TitleActivity.class);
            if (null != progress) progress.dismiss();
            finish();
            startActivity(intent);
        }
    }

    public void findShowReloadPage(int pCategoryType, String pKey) {
        switch (pCategoryType) {
            case CategoryType.RECORDED: {
                ShowArrayList currentList = new ShowArrayList();
                try {
                    currentList = (ShowArrayList) ObjectSerializer.deserialize(mSettings.getString(SettingConstants.SHOW_LIST, ObjectSerializer.serialize(new ShowArrayList())));
                } catch (IOException e) {
                    Log.e(TAG, "IOException:" + e);
                }
                mTabsPagerAdapter.refreshList(currentList, false, CategoryType.RECORDED, null);
                break;
            }
            case CategoryType.LAST_NEWS: {
                mCurrentPagination.put(CategoryType.LAST_NEWS, 0);
                new ConnectNoco("GET", null, "Bearer", this, Constants.FIND_SHOWS + 0, CategoryType.LAST_NEWS);
                break;
            }
            case CategoryType.USER_PLAYLIST_INFO: {
                ShowArrayList playList = mListShowMap.get(CategoryType.USER_PLAYLIST_INFO);
                mTabsPagerAdapter.refreshList(playList, false, CategoryType.USER_PLAYLIST_INFO, null);
                break;
            }
            case CategoryType.FAVORITES_LIST: {
                ArrayList<Family> favorites = mListFamMap.get(CategoryType.FAVORITES_LIST);
                mTabsPagerAdapter.refreshList(favorites, false, CategoryType.FAVORITES_LIST, null);
                break;
            }
            case CategoryType.MOST_POPULAR: {
                mCurrentPagination.put(CategoryType.MOST_POPULAR, 0);
                new ConnectNoco("GET", null, "Bearer", this, Constants.FIND_MOST_POPULAR + 0, CategoryType.MOST_POPULAR);
                break;
            }
            case CategoryType.RECOMMENDED_LIST: {
                mCurrentPagination.put(CategoryType.RECOMMENDED_LIST, 0);
                new ConnectNoco("GET", null, "Bearer", this, Constants.FIND_SHOWS_RECOMMENDED + 0, CategoryType.RECOMMENDED_LIST);
                break;
            }
            case CategoryType.PAID_LIST: {
                mCurrentPagination.put(CategoryType.PAID_LIST, 0);
                new ConnectNoco("GET", null, "Bearer", this, Constants.FIND_SHOWS_PAID + 0, CategoryType.PAID_LIST);
                break;
            }
            case CategoryType.SEARCH_INFO: {
                mCurrentPagination.put(CategoryType.SEARCH_INFO, 0);
                new ConnectNoco("GET", null, "Bearer", MainActivity.this, Constants.SEARCH + mSearch + "&" + Constants.SHOWS_PER_PAGE + 0, CategoryType.SEARCH);
                break;
            }
            case CategoryType.UNREAD: {
                mCurrentPagination.put(CategoryType.UNREAD, 0);
                new ConnectNoco("GET", null, "Bearer", this, Constants.GET_SHOWS_UNREAD_ONLY + 0, CategoryType.UNREAD);
                break;
            }
            case CategoryType.READ: {
                mCurrentPagination.put(CategoryType.READ, 0);
                new ConnectNoco("GET", null, "Bearer", this, Constants.GET_SHOWS_READ_ONLY + 0, CategoryType.READ);
                break;
            }
            case CategoryType.FREE_SHOWS: {
                mCurrentPagination.put(CategoryType.FREE_SHOWS, 0);
                new ConnectNoco("GET", null, "Bearer", this, Constants.FREE_SHOWS + 0, CategoryType.FREE_SHOWS);
                break;
            }
            case CategoryType.ABO_LIST: {
                for (Abo abo : mListAbo) {
                    if (abo.getKey().equals(pKey)) {
                        closeDrawer();
                        abo.setCurrentPage(0);
                        new ConnectNoco("GET", null, "Bearer", MainActivity.this, Constants.FIND_SHOWS + (abo.getCurrentPage()) + "&" + Constants.BY_PARTNER_KEY + abo.getKey(), CategoryType.SHOW_BY_PARTNER);
                        break;
                    }
                }
                break;
            }
            case CategoryType.FAM_LIST: {
                FamArrayList families = mListFamMap.get(CategoryType.FAM_LIST);
                for (Family fam : families) {
                    if (fam.getFamilyKey().equals(pKey)) {
                        closeDrawer();
                        fam.setCurrentPage(0);
                        new ConnectNoco("GET", null, "Bearer", MainActivity.this, Constants.FIND_SHOWS + (fam.getCurrentPage()) + "&" + Constants.BY_FAMILY_KEY + fam.getFamilyKey(), CategoryType.SHOW_BY_FAMILY);
                        break;
                    }
                }
                mListFamMap.put(CategoryType.FAM_LIST, families);
                break;
            }
        }
    }

    public void findShowNextPage(int pCategoryType, String pKey) {
        switch (pCategoryType) {
            case CategoryType.LAST_NEWS: {
                new ConnectNoco("GET", null, "Bearer", this, Constants.FIND_SHOWS + (mCurrentPagination.get(CategoryType.LAST_NEWS) + 1), CategoryType.LAST_NEWS_REFRESH);
                break;
            }
            case CategoryType.USER_PLAYLIST_INFO: {
                ShowArrayList playList = mListShowMap.get(CategoryType.USER_PLAYLIST_INFO);
                mTabsPagerAdapter.refreshList(playList, false, CategoryType.USER_PLAYLIST_INFO, null);
                break;
            }
            case CategoryType.FAVORITES_LIST: {
                ArrayList<Family> favorites = mListFamMap.get(CategoryType.FAVORITES_LIST);
                mTabsPagerAdapter.refreshList(favorites, false, CategoryType.FAVORITES_LIST, null);
                break;
            }
            case CategoryType.MOST_POPULAR: {
                new ConnectNoco("GET", null, "Bearer", this, Constants.FIND_MOST_POPULAR + (mCurrentPagination.get(CategoryType.MOST_POPULAR) + 1), CategoryType.MOST_POPULAR_REFRESH);
                break;
            }
            case CategoryType.RECOMMENDED_LIST: {
                new ConnectNoco("GET", null, "Bearer", this, Constants.FIND_SHOWS_RECOMMENDED + (mCurrentPagination.get(CategoryType.RECOMMENDED_LIST) + 1), CategoryType.RECOMMENDED_LIST_REFRESH);
                break;
            }
            case CategoryType.PAID_LIST: {
                new ConnectNoco("GET", null, "Bearer", this, Constants.FIND_SHOWS_PAID + (mCurrentPagination.get(CategoryType.PAID_LIST) + 1), CategoryType.PAID_LIST_REFRESH);
                break;
            }
            case CategoryType.SEARCH_INFO: {
                new ConnectNoco("GET", null, "Bearer", MainActivity.this, Constants.SEARCH + mSearch + "&" + Constants.SHOWS_PER_PAGE + (mCurrentPagination.get(CategoryType.SEARCH_INFO) + 1), CategoryType.SEARCH_REFRESH);
                break;
            }
            case CategoryType.UNREAD: {
                new ConnectNoco("GET", null, "Bearer", this, Constants.GET_SHOWS_UNREAD_ONLY + (mCurrentPagination.get(CategoryType.UNREAD) + 1), CategoryType.UNREAD_REFRESH);
                break;
            }
            case CategoryType.READ: {
                new ConnectNoco("GET", null, "Bearer", this, Constants.GET_SHOWS_READ_ONLY + (mCurrentPagination.get(CategoryType.READ) + 1), CategoryType.READ_REFRESH);
                break;
            }
            case CategoryType.FREE_SHOWS: {
                new ConnectNoco("GET", null, "Bearer", this, Constants.FREE_SHOWS + (mCurrentPagination.get(CategoryType.FREE_SHOWS) + 1), CategoryType.FREE_SHOWS_REFRESH);
                break;
            }
            case CategoryType.ABO_LIST: {
                for (Abo abo : mListAbo) {
                    if (abo.getKey().equals(pKey)) {
                        closeDrawer();
                        new ConnectNoco("GET", null, "Bearer", MainActivity.this, Constants.FIND_SHOWS + (abo.getCurrentPage() + 1) + "&" + Constants.BY_PARTNER_KEY + abo.getKey(), CategoryType.SHOW_BY_PARTNER_REFRESH);
                        break;
                    }
                }
                break;
            }
            case CategoryType.FAM_LIST: {
                FamArrayList families = mListFamMap.get(CategoryType.FAM_LIST);
                for (Family fam : families) {
                    if (fam.getFamilyKey().equals(pKey)) {
                        closeDrawer();
                        new ConnectNoco("GET", null, "Bearer", MainActivity.this, Constants.FIND_SHOWS + (fam.getCurrentPage() + 1) + "&" + Constants.BY_FAMILY_KEY + fam.getFamilyKey(), CategoryType.SHOW_BY_FAMILY_REFRESH);
                        break;
                    }
                }
                break;
            }
        }
    }

    @SuppressWarnings("unused")
    @SuppressLint("RtlHardcoded")
    public void openDrawer(View pView) {
        if (mConnected) {
            if (mDrawerLayout.getDrawerLockMode(Gravity.LEFT) != DrawerLayout.LOCK_MODE_LOCKED_CLOSED) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        } else {
            disconnect();
        }
    }

    @SuppressLint("RtlHardcoded")
    private void closeDrawer() {
        mDrawerLayout.closeDrawer(Gravity.LEFT);
    }

    @Override
    protected void onDestroy() {
        if (null != mCast) mCast.teardown();
        super.onDestroy();
    }

    public void showControl(Show pShow) {
        mCast.loadShow(pShow);
        mCast.show();
    }

    @Override
    protected void onActivityResult(int pRequestCode, int pResultCode, Intent pData) {
        if (pRequestCode == 1) {
            if (pResultCode == RESULT_OK) {
                if (null != pData && null != pData.getExtras()) {
                    int hasBeenPlayed = pData.getExtras().getInt("has_been_played", 0);
                    if (hasBeenPlayed == 1) {
                        String idShow = pData.getExtras().getString("id_show");
                        if (null != idShow) {
                            addReadList(idShow);
                            mTabsPagerAdapter.refreshAll();
                        }
                    }
                    int rate = pData.getExtras().getInt("new_rating", 0);
                    if (rate != 0) {
                        String idShow = pData.getExtras().getString("id_show");
                        if (null != idShow) {
                            addRatingList(idShow, rate);
                        }
                    }
                }
            }
        } else if (pRequestCode == 2) {
            if (pResultCode == RESULT_OK) {
                if (null != pData && null != pData.getExtras()) {
                    int old = mParentalControlLevel;
                    mParentalControlLevel = pData.getExtras().getInt("parental_control_level", 99);
                    if (old != mParentalControlLevel) {
                        if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
                            if (mParentalControlLevel != 99)
                                mParentalImage.setVisibility(View.VISIBLE);
                            else mParentalImage.setVisibility(View.GONE);
                        }
                        mTabsPagerAdapter.refreshAll();
                    }
                }
            }
        }
    }

    private void addReadList(String pId) {
        mReadList.add(pId);
    }

    public boolean isInReadList(String pId) {
        return mReadList.contains(pId);
    }

    private void addRatingList(String pId, Integer pRate) {
        mRatingList.put(pId, pRate);
    }

    public Integer getRatingList(String pId) {
        return mRatingList.get(pId);
    }

    private boolean search() {
        mSearch = mSearchText.getText().toString();
        if (mSearch.length() > 0) {
            try {
                mSearch = URLEncoder.encode(mSearch, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "UnsupportedEncodingException:" + e);
            }
            mCurrentPagination.put(CategoryType.SEARCH_INFO, 0);
            new ConnectNoco("GET", null, "Bearer", MainActivity.this, Constants.SEARCH + mSearch + "&" + Constants.SHOWS_PER_PAGE + 0, CategoryType.SEARCH);
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (null != imm) {
                imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
            }
            ShowArrayList searchList = mListShowMap.get(CategoryType.SEARCH_INFO);
            mTabsPagerAdapter.addTabOrRefresh(searchList, false, true, true, CategoryType.SEARCH_INFO, null);
            return true;
        }
        return false;
    }

    public boolean hasShowInPlayList(Show pShow) {
        ShowArrayList playlist = mListShowMap.get(CategoryType.USER_PLAYLIST_INFO);
        return playlist.contains(pShow);
    }

    public boolean hasShowInFavorites(Integer pId) {
        FamArrayList favorites = mListFamMap.get(CategoryType.FAVORITES_LIST);
        for (Family fam : favorites) {
            if (fam.getIdFamily().intValue() == pId) {
                return true;
            }
        }
        return false;
    }

    public void addOrDeletePlayList(Show pShow) {
        ShowArrayList playList = mListShowMap.get(CategoryType.USER_PLAYLIST_INFO);
        if (playList.contains(pShow)) {
            playList.remove(pShow);
        } else {
            playList.add(pShow);
        }
        mListShowMap.put(CategoryType.USER_PLAYLIST_INFO, playList);
        int size = playList.size();
        if (size > 0) {
            Map<String, String> paramMap = new HashMap<>();
            StringBuilder param = new StringBuilder("[");
            for (int i = 0; i < size; i++) {
                param.append(playList.get(i).getIdShow()).append((i + 1 < size) ? "," : "]");
            }
            paramMap.put("body", param.toString());
            new ConnectNoco("PUT", paramMap, "Bearer", this, Constants.USER_LIST, CategoryType.ADDDEL_USER_LIST);
        } else {
            new ConnectNoco("DELETE", null, "Bearer", this, Constants.USER_LIST, CategoryType.ADDDEL_USER_LIST);
        }
        mTabsPagerAdapter.addTabOrRefresh(playList, false, false, false, CategoryType.USER_PLAYLIST_INFO, null);
        mTabsPagerAdapter.refreshAll();
    }

    public void addOrDeleteFavorites(Integer pId) {
        FamArrayList favorites = mListFamMap.get(CategoryType.FAVORITES_LIST);
        FamArrayList families = mListFamMap.get(CategoryType.FAM_LIST);
        if (hasShowInFavorites(pId)) {
            for (Family fam : families) {
                if (fam.getIdFamily().intValue() == pId) {
                    favorites.remove(fam);
                }
            }
        } else {
            for (Family fam : families) {
                if (fam.getIdFamily().intValue() == pId) {
                    favorites.add(fam);
                }
            }
        }
        mListFamMap.put(CategoryType.FAVORITES_LIST, favorites);
        int count = favorites.size();
        if (count > 0) {
            Map<String, String> paramMap = new HashMap<>();
            StringBuilder param = new StringBuilder("[");
            for (int i = 0; i < count; i++) {
                param.append(favorites.get(i).getIdFamily()).append((i + 1 < count) ? "," : "]");
            }
            paramMap.put("body", param.toString());
            new ConnectNoco("PUT", paramMap, "Bearer", this, Constants.FAVORITES_LIST, CategoryType.ADDDEL_FAVORITES_LIST);
        } else {
            new ConnectNoco("DELETE", null, "Bearer", this, Constants.FAVORITES_LIST, CategoryType.ADDDEL_FAVORITES_LIST);
        }
        mTabsPagerAdapter.addTabOrRefresh(favorites, false, false, false, CategoryType.FAVORITES_LIST, null);
        mTabsPagerAdapter.refreshAll();
    }

    public String getTabName(int pCategoryType) {
        return getTabName(pCategoryType, null);
    }

    public String getTabName(int pCategoryType, String pKey) {
        if (pCategoryType == CategoryType.ABO_LIST) {
            for (Abo abo : mListAbo) {
                if (abo.getKey().equals(pKey)) {
                    return abo.getShortName();
                }
            }
        } else if (pCategoryType == CategoryType.FAM_LIST) {
            FamArrayList families = mListFamMap.get(CategoryType.FAM_LIST);
            for (Family fam : families) {
                if (fam.getFamilyKey().equals(pKey)) {
                    return fam.getFamilyTT();
                }
            }
        }
        return CategoryType.getTabName(pCategoryType, this);
    }

    private String getMenuName(int pCategoryType, String pKey) {
        switch (pCategoryType) {
            case CategoryType.ABO_VALUE:
                for (Abo abo : mListAbo) {
                    if (abo.getKey().equals(pKey)) {
                        return abo.getShortName();
                    }
                }
                break;
            case CategoryType.FAM_VALUE: {
                FamArrayList families = mListFamMap.get(CategoryType.FAM_LIST);
                for (Family fam : families) {
                    if (pKey.equals(fam.getFamilyKey())) {
                        return fam.getFamilyTT();
                    }
                }
                break;
            }
            case CategoryType.FAM_LIST: {
                FamArrayList families = mListFamMap.get(CategoryType.FAM_LIST);
                for (Family fam : families) {
                    if (pKey.equals(fam.getPartnerKey())) {
                        return fam.getPartnerShortname();
                    }
                }
                break;
            }
        }
        return CategoryType.getMenuName(pCategoryType, this);
    }

    private GroupItem createGroupItem(int pCategoryType) {
        return createGroupItem(pCategoryType, null, GroupItem.NOICON, null);
    }

    private GroupItem createGroupItem(int pCategoryType, int pType, ArrayList<SubGroupItem> pList) {
        return createGroupItem(pCategoryType, null, pType, pList);
    }

    private GroupItem createGroupItem(int pCategoryType, String pKey, int pType, ArrayList<SubGroupItem> pList) {
        return new GroupItem(getMenuName(pCategoryType, pKey), pCategoryType, pType, pList, pKey);
    }

    private SubGroupItem createSubGroupItem(int pCategoryType) {
        return createSubGroupItem(pCategoryType, null, 0);
    }

    private SubGroupItem createSubGroupItem(int pCategoryType, String pKey) {
        return createSubGroupItem(pCategoryType, pKey, 0);
    }

    private SubGroupItem createSubGroupItem(int pCategoryType, int pIcon) {
        return createSubGroupItem(pCategoryType, null, pIcon);
    }

    private SubGroupItem createSubGroupItem(int pCategoryType, String pKey, int pIcon) {
        return new SubGroupItem(getMenuName(pCategoryType, pKey), pCategoryType, pIcon, pKey);
    }

    private class DataUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context pContext, Intent pIntent) {
            if (null != mTabsPagerAdapter) {
                mTabsPagerAdapter.refreshRecorded();
            }
        }
    }

    private class CustomPageTransformer implements PageTransformer {
        private static final float MIN_SCALE = 0f;

        @Override
        public void transformPage(View pPage, float pPosition) {
            onTrans(pPage, pPosition);
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        void onTrans(View pPage, float pPosition) {
            GridView vGrid = pPage.findViewById(R.id.gridView);
            for (int i = 0; i < vGrid.getChildCount(); i++) {
                View nextChild = vGrid.getChildAt(i);
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(pPosition));
                nextChild.setScaleX(scaleFactor);
                nextChild.setScaleY(scaleFactor);
                nextChild.setAlpha(Math.abs(1 - Math.abs(pPosition)));
            }
        }
    }
}