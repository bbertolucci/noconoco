package com.bbproject.noconoco.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bbproject.noconoco.R;
import com.bbproject.noconoco.activities.MainActivity;
import com.bbproject.noconoco.activities.VideoActivity;
import com.bbproject.noconoco.adapters.VideoGridAdapter;
import com.bbproject.noconoco.constants.CategoryType;
import com.bbproject.noconoco.constants.Constants;
import com.bbproject.noconoco.constants.SettingConstants;
import com.bbproject.noconoco.model.json.Family;
import com.bbproject.noconoco.model.json.Show;
import com.bbproject.noconoco.connection.ConnectNoco;
import com.bbproject.noconoco.utils.ObjectSerializer;
import com.bbproject.noconoco.utils.SettingUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VideoGridFragment extends Fragment implements OnScrollListener {

    private static final String TAG = "VideoGridFragment";

    private GridView mGridView;
    private VideoGridAdapter mGridAdapter;
    private ArrayList<? extends Parcelable> mGridItems;
    private List<?> mFilteredGridItems;
    private boolean mRefreshCalled = false;
    private SettingUtils mSettings;
    private String mLocation;
    private boolean mCanRefreshOnScroll;
    private RelativeLayout mRView;
    private boolean mShowLoading;
    private boolean mRecorded;
    private boolean mIsGrid;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String mKey;
    private int mCategoryType;

    public VideoGridFragment() {
        if (null != getArguments()) {
            Bundle bundle = getArguments();
            mSettings = new SettingUtils(getActivity().getApplicationContext());
            mGridItems = bundle.getParcelableArrayList("ITEM");
            mLocation = mSettings.getString(SettingConstants.GEOLOC, "***");
            mCanRefreshOnScroll = bundle.getBoolean("REFRESHONSCROLL");
            mShowLoading = bundle.getBoolean("SHOWLOADING");
            mRecorded = bundle.getBoolean("RECORDED");
            mIsGrid = bundle.getBoolean("ISGRID");
            mKey = bundle.getString("KEY");
            mCategoryType = bundle.getInt("CATEGORYTYPE");

        }
    }

    @SuppressLint({"NewApi", "ObsoleteSdkInt", "ClickableViewAccessibility"})
    @Override
    public View onCreateView(LayoutInflater pInflater, ViewGroup pContainer, Bundle pSavedInstanceState) {
        Bundle bundle;
        if (null != pSavedInstanceState) bundle = pSavedInstanceState;
        else if (null != getArguments()) bundle = getArguments();
        else bundle = getActivity().getIntent().getExtras();
        if (null != bundle) {
            mSettings = new SettingUtils(getActivity().getApplicationContext());
            mGridItems = bundle.getParcelableArrayList("ITEM");
            mLocation = mSettings.getString(SettingConstants.GEOLOC, "***");
            mCanRefreshOnScroll = bundle.getBoolean("REFRESHONSCROLL");
            mShowLoading = bundle.getBoolean("SHOWLOADING");
            mRecorded = bundle.getBoolean("RECORDED");
            mIsGrid = bundle.getBoolean("ISGRID");
            mKey = bundle.getString("KEY");
            mCategoryType = bundle.getInt("CATEGORYTYPE");
        }
        View mainView = pInflater.inflate(R.layout.view_grid, pContainer, false);
        mGridView = mainView.findViewById(R.id.gridView);
        mSwipeRefreshLayout = mainView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setClipChildren(false);
        mSwipeRefreshLayout.setClipToPadding(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSwipeRefreshLayout.setClipToOutline(false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mSwipeRefreshLayout.setOnTouchListener(new OnTouchListener() {
                private float initY = -99999;
                private View mView;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (mSwipeRefreshLayout.getChildCount() > 1) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN || initY == -99999) {
                            mView = mSwipeRefreshLayout.getChildAt(1);
                            initY = mView.getY();
                        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                            int decY = Math.round((mView.getY() - initY) * 5 / 3);
                            v.setPadding(0, decY, 0, 0);
                        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                            v.setPadding(0, 0, 0, 0);
                        }
                    }
                    return false;
                }
            });
        }
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ((MainActivity) getActivity()).getTabsPagerAdapter().reloadPage();
            }
        });
        initColumn();
        mGridView.setOnScrollListener(this);
        mGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view,
                                    int position, long id) {
                onGridItemClick(position);
            }
        });
        mRView = mainView.findViewById(R.id.layout_refresh);
        if (null != mRView) {
            ImageView iv = mRView.findViewById(R.id.ic_refresh);
            if (null == iv.getAnimation()) {
                Animation rotation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
                rotation.setRepeatCount(Animation.INFINITE);
                iv.startAnimation(rotation);
            }
            if (mShowLoading) {
                mRView.setVisibility(View.VISIBLE);
            } else {
                mRView.setVisibility(View.GONE);
            }
        }

        init();
        return mainView;
    }

    private void init() {
        mFilteredGridItems = filterList(mGridItems);
        MainActivity mActivity = ((MainActivity) getActivity());
        mGridAdapter = new VideoGridAdapter(this, mFilteredGridItems, mActivity, mLocation, mRecorded);
        mGridView.setAdapter(mGridAdapter);
        mGridView.computeScroll();
    }

    @SuppressWarnings("unchecked")
    private void onGridItemClick(int pPosition) {
        MainActivity mActivity = ((MainActivity) getActivity());
        if (mFilteredGridItems.get(pPosition) instanceof Show) {
            Show show = (Show) mFilteredGridItems.get(pPosition);
            if (!mActivity.getModeSelectionPlaylist() && !mActivity.getModeSelectionFavorites()) {
                Intent intent = new Intent(mActivity, VideoActivity.class);
                if (mRecorded) {
                    try {
                        ArrayList<Show> currentList = (ArrayList<Show>) ObjectSerializer.deserialize(mSettings.getString(SettingConstants.SHOW_LIST, ObjectSerializer.serialize(new ArrayList<Show>())));
                        for (Show tmpShow : currentList) {
                            if (tmpShow.getIdShow().equals(show.getIdShow())) {
                                if (tmpShow.getRecordedStatus() != 2) {
                                    Toast.makeText(getActivity(), getString(R.string.video_inaccessible), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                break;
                            }
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "IOException", e);
                    }
                }
                if (!mActivity.getIsCasting()) {
                    if (null != mActivity.getRatingList(show.getIdShow())) {
                        show.setUserRating(mActivity.getRatingList(show.getIdShow()));
                    }
                    intent.putExtra("show", (Serializable) show);
                    intent.putExtra("recorded", mRecorded);
                    mActivity.startActivityForResult(intent, 1);
                } else {
                    mActivity.showControl(show);
                }
            }
            if (mActivity.getModeSelectionPlaylist()) {
                if ("*".equals(show.getGeoloc()) || !show.getGeoloc().contains(mLocation)) {
                    mActivity.addOrDeletePlayList(show);
                }
            } else if (mActivity.getModeSelectionFavorites()) {
                if ("*".equals(show.getGeoloc()) || !show.getGeoloc().contains(mLocation)) {
                    mActivity.addOrDeleteFavorites(show.getIdFamily());
                }
            }
        } else if (mFilteredGridItems.get(pPosition) instanceof Family) {
            Family family = (Family) mFilteredGridItems.get(pPosition);
            if (mActivity.getModeSelectionFavorites()) {
                mActivity.addOrDeleteFavorites(family.getIdFamily());
            } else {
                mActivity.getTabsPagerAdapter().addTabOrRefresh(family.getList(), false, true, true, CategoryType.FAM_LIST, family.getFamilyKey());
                new ConnectNoco("GET", null, "Bearer", mActivity, Constants.FIND_SHOWS + "0&" + Constants.BY_FAMILY_KEY + family.getFamilyKey(), CategoryType.SHOW_BY_FAMILY);
            }
        }

    }

    @Override
    public void onScroll(AbsListView pView, int pFirstVisibleItem, int pVisibleItemCount, int pTotalItemCount) {
        if (mCanRefreshOnScroll) {
            if (pFirstVisibleItem != 0 && pFirstVisibleItem + 2 * pVisibleItemCount > pTotalItemCount && !mRefreshCalled) {
                mRefreshCalled = true;
                mShowLoading = true;
                if (null != mRView) mRView.setVisibility(View.VISIBLE);
                ((MainActivity) getActivity()).getTabsPagerAdapter().nextPage();
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView pView, int pScrollState) {
    }

    public void refreshList(ArrayList<? extends Parcelable> pGridItems, boolean pCanRefreshOnScroll, boolean pShowLoading) {
        if (pGridItems == null) {
            return;
        }
        mGridItems = pGridItems;
        mFilteredGridItems = filterList(mGridItems);
        mCanRefreshOnScroll = pCanRefreshOnScroll;
        mShowLoading = pShowLoading;
        if (mShowLoading) {
            if (null != mRView) {
                mRView.setVisibility(View.VISIBLE);
            }
        } else {
            if (null != mRView) {
                mRView.setVisibility(View.GONE);
            }
        }
        if (null != mSwipeRefreshLayout && mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }

        if (null != mGridAdapter) {
            mGridAdapter.refreshList(mFilteredGridItems);
        }
        mRefreshCalled = false;

    }

    public boolean getIsGrid() {
        return mIsGrid;
    }

    public void changeToList() {
        mIsGrid = false;
        initColumn();
    }

    public void changeToGrid() {
        mIsGrid = true;
        initColumn();
    }

    private void initColumn() {
        if (null != mGridView) {
            if (mIsGrid) {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mGridView.setNumColumns(3);
                } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mGridView.setNumColumns(2);
                }
            } else {
                mGridView.setNumColumns(1);
            }
        }
    }

    public void refreshList() {
        refreshList(mGridItems, mCanRefreshOnScroll, mShowLoading);
    }

    @Override
    public void onConfigurationChanged(Configuration pNewConfig) {
        if (mIsGrid) {
            mGridView.setNumColumns(pNewConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? 3 : 2);
        } /*else {
			mGridView.setNumColumns(1);
		}*/
        super.onConfigurationChanged(pNewConfig);
    }

    private ArrayList<Show> filterList(ArrayList<? extends Parcelable> pArrayList) {
        ArrayList<Show> returnList = new ArrayList<>();
        for (Object object : pArrayList) {
            if (object instanceof Show) {
                Show show = (Show) object;
                if (!"".equals(show.getRatingFr())) {
                    int csa = Integer.parseInt(show.getRatingFr());
                    if (((MainActivity) getActivity()).getParentalControlLevel() > csa) {
                        returnList.add(show);
                    }
                } else {
                    returnList.add(show);
                }

            } else {
                break;
            }
        }
        return returnList;
    }

    @Override
    public void onSaveInstanceState(Bundle pSavedInstanceState) {
        pSavedInstanceState.putParcelableArrayList("ITEM", mGridItems);
        pSavedInstanceState.putBoolean("REFRESHONSCROLL", mCanRefreshOnScroll);
        pSavedInstanceState.putBoolean("SHOWLOADING", mShowLoading);
        pSavedInstanceState.putBoolean("RECORDED", mRecorded);
        pSavedInstanceState.putBoolean("ISGRID", mIsGrid);
        pSavedInstanceState.putString("KEY", mKey);
        pSavedInstanceState.putInt("CATEGORYTYPE", mCategoryType);
    }

    public String getKey() {
        return mKey;
    }

    public int getCategoryType() {
        return mCategoryType;
    }
}