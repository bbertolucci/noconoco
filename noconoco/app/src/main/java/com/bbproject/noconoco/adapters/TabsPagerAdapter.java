package com.bbproject.noconoco.adapters;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.bbproject.noconoco.R;
import com.bbproject.noconoco.activities.HelpActivity;
import com.bbproject.noconoco.activities.MainActivity;
import com.bbproject.noconoco.constants.CategoryType;
import com.bbproject.noconoco.constants.SettingConstants;
import com.bbproject.noconoco.fragments.VideoGridFragment;
import com.bbproject.noconoco.model.json.Show;
import com.bbproject.noconoco.utils.ObjectSerializer;
import com.bbproject.noconoco.utils.SettingUtils;

import java.io.IOException;
import java.util.ArrayList;

public class TabsPagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = "TabsPagerAdapter";

    private final MainActivity mActivity;
    private final ArrayList<String> mListTab = new ArrayList<>();
    private final ArrayList<VideoGridFragment> mTabFragmentList = new ArrayList<>();
    private final View mAboutTab;
    private final LayoutInflater mInflate;
    private final SparseArray<View[]> mTabSparceArray = new SparseArray<>();
    private int mScreenWidth;
    private int mScreenHeight;
    private int mLeftH = 0;
    private int mRightH = 0;
    private int mLeftV = 0;
    private int mRightV = 0;
    private LinearLayout mHorizontalTab;
    private int mTabCount;
    private int mCurrentPosition;
    private int mViewSeparatorWidthMeasure;
    private LinearLayout.LayoutParams mParams;
    private int mPos;

    @SuppressWarnings("unchecked")
    public TabsPagerAdapter(MainActivity pActivity, FragmentManager pFragment, boolean pShouldInit) {
        super(pFragment);
        mActivity = pActivity;

        DisplayMetrics displayMetrics = mActivity.getResources().getDisplayMetrics();
        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;

        mInflate = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (null == mInflate) {
            throw new AssertionError("LayoutInflater not found.");
        }
        mParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        mViewSeparatorWidthMeasure = SettingUtils.convertDpToPixel(2, mActivity);

        View about = mActivity.findViewById(R.id.about2);
        Animation anim = AnimationUtils.loadAnimation(mActivity, R.anim.rotate);
        anim.setRepeatCount(Animation.INFINITE);
        about.startAnimation(anim);
        mAboutTab = mActivity.findViewById(R.id.relativetab);
        Button aboutButton = mActivity.findViewById(R.id.about);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                Intent configIntent = new Intent(mActivity, HelpActivity.class);
                mActivity.startActivity(configIntent);
            }
        });
        mAboutTab.setVisibility(View.VISIBLE);

        mHorizontalTab = mActivity.findViewById(R.id.lineartab);
        if (pShouldInit) {
            if (mActivity.isConnected()) {
                addTab(mActivity.getLastShows(), true, false, false, false, CategoryType.LAST_NEWS, null);
            } else {
                addTab(mActivity.getFreeShows(), true, false, false, false, CategoryType.FREE_SHOWS, null);
            }
            ArrayList<Show> currentList = new ArrayList<>();
            try {
                currentList = (ArrayList<Show>) ObjectSerializer.deserialize(mActivity.getSettings().getString(SettingConstants.SHOW_LIST, ObjectSerializer.serialize(new ArrayList<Show>())));
            } catch (IOException e) {
                Log.e(TAG, "IOException : ", e);
            }
            addTab(currentList, false, false, false, true, CategoryType.RECORDED, null);
        } else {
            initRefreshTab();
        }
    }

    @Override
    public void destroyItem(ViewGroup pContainer, int pPosition, Object pObject) {
    }

    public void addTabOrRefresh(ArrayList<? extends Parcelable> pTabList, boolean pCanRefreshOnScroll, boolean pDisplay, boolean pShowLoading, int pCategoryType, String pKey) {
        String tabName = mActivity.getTabName(pCategoryType, pKey);
        if (!mListTab.contains(tabName)) {
            addTab(pTabList, pCanRefreshOnScroll, pDisplay, pShowLoading, false, pCategoryType, pKey);
        } else {
            refreshList(pTabList, pCanRefreshOnScroll, pDisplay, pShowLoading, pCategoryType, pKey);
        }
    }

    public void refreshAll() {
        for (VideoGridFragment fragment : mTabFragmentList) {
            fragment.refreshList();
        }
    }

    public void changeToList() {
        for (VideoGridFragment fragment : mTabFragmentList) {
            fragment.changeToList();
        }
    }

    public void changeToGrid() {
        for (VideoGridFragment fragment : mTabFragmentList) {
            fragment.changeToGrid();
        }
    }

    @SuppressWarnings("unchecked")
    public void refreshRecorded() {
        ArrayList<Show> currentList = new ArrayList<>();
        try {
            currentList = (ArrayList<Show>) ObjectSerializer.deserialize(mActivity.getSettings().getString(SettingConstants.SHOW_LIST, ObjectSerializer.serialize(new ArrayList<Show>())));
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
        }
        refreshList(currentList, false, false, false, CategoryType.RECORDED, null);
    }

    public void refreshRecordedAndSelect() {
        mPos = mListTab.indexOf(mActivity.getTabName(CategoryType.RECORDED));
        if (mPos != -1) {
            if (mCurrentPosition != mPos) {
                mActivity.getViewPager().setCurrentItem(mPos, true);
                setCurrentItem(mPos, 0);
            }
            refreshRecorded();
        }
    }

    private void initRefreshTab() {
        notifyDataSetChanged();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addTab(ArrayList<? extends Parcelable> pTabList, boolean pCanRefreshOnScroll, boolean pDisplay, boolean pShowLoading, boolean pRecorded, int pCategoryType, String pKey) {

        String tabName = mActivity.getTabName(pCategoryType, pKey);
        int position = mTabCount;

        LinearLayout linearLayout = (LinearLayout) mInflate.inflate(R.layout.view_tab, mHorizontalTab, false);

        Button button = linearLayout.findViewById(R.id.id);
        button.setText(tabName);

        button.setOnTouchListener(new CustomOnTouchListener(position));

        View v = linearLayout.findViewById(R.id.tab_top);
        View w = linearLayout.findViewById(R.id.tab_bottom);
        linearLayout.measure(mScreenWidth, mScreenHeight);
        mTabSparceArray.put(position, new View[]{v, w, linearLayout});
        if (mCurrentPosition == position) {
            v.setVisibility(View.VISIBLE);
            w.setVisibility(View.VISIBLE);
        } else {
            v.setVisibility(View.INVISIBLE);
            w.setVisibility(View.INVISIBLE);
        }
        if (position == 0) {
            linearLayout.findViewById(R.id.separator).setVisibility(View.GONE);
            linearLayout.measure(mScreenWidth, mScreenHeight);
            if (mScreenWidth < mScreenHeight) {
                mRightH = (mScreenWidth - linearLayout.getMeasuredWidth()) / 2;
                mRightV = (mScreenHeight - linearLayout.getMeasuredWidth()) / 2;
                mLeftH = mRightH - mAboutTab.getWidth();
                mLeftV = mRightV - mAboutTab.getWidth();
            } else {
                mRightV = (mScreenWidth - linearLayout.getMeasuredWidth()) / 2;
                mRightH = (mScreenHeight - linearLayout.getMeasuredWidth()) / 2;
                mLeftH = mRightH - mAboutTab.getWidth();
                mLeftV = mRightV - mAboutTab.getWidth();
            }
        } else {
            if (mScreenWidth < mScreenHeight) {
                mRightH = (mScreenWidth - linearLayout.getMeasuredWidth()) / 2;
                mRightV = (mScreenHeight - linearLayout.getMeasuredWidth()) / 2;
            } else {
                mRightV = (mScreenWidth - linearLayout.getMeasuredWidth()) / 2;
                mRightH = (mScreenHeight - linearLayout.getMeasuredWidth()) / 2;
            }
        }

        mListTab.add(tabName);
        mTabCount++;
        Bundle paramBundle = new Bundle();
        paramBundle.putParcelableArrayList("ITEM", pTabList);
        paramBundle.putBoolean("REFRESHONSCROLL", pCanRefreshOnScroll);
        paramBundle.putBoolean("SHOWLOADING", pShowLoading);
        paramBundle.putBoolean("RECORDED", pRecorded);
        paramBundle.putString("TAB", tabName);
        paramBundle.putString("KEY", pKey);
        paramBundle.putSerializable("CATEGORYTYPE", pCategoryType);
        paramBundle.putBoolean("ISGRID", mActivity.getSettings().getBoolean(SettingConstants.GRID_LIST, true));

        VideoGridFragment frag = new VideoGridFragment();
        frag.setArguments(paramBundle);
        mTabFragmentList.add(frag);

        int left, right;
        if (mScreenWidth < mScreenHeight) {
            left = mLeftH;
            right = mRightH;
        } else {
            left = mLeftV;
            right = mRightV;
        }
        mHorizontalTab.setPadding(left, 0, right, 0);

        mHorizontalTab.addView(linearLayout, mParams);

        notifyDataSetChanged();
        if (pDisplay) {
            mActivity.getViewPager().setCurrentItem(position, true);
            setCurrentItem(position, 0);
        }

    }

    @Override
    public Fragment getItem(int position) {
        return mTabFragmentList.get(position);
    }

    public void refreshList(ArrayList<? extends Parcelable> pGridItems, boolean pCanRefreshOnScroll, int pCategoryType, String pKey) {
        refreshList(pGridItems, pCanRefreshOnScroll, false, false, pCategoryType, pKey);
    }

    public void refreshList(ArrayList<? extends Parcelable> pGridItems, boolean pCanRefreshOnScroll, boolean pDisplay, boolean pShowLoading, int pCategoryType, String pKey) {
        mPos = mListTab.indexOf(mActivity.getTabName(pCategoryType, pKey));
        if (mPos != -1) {
            if (pDisplay && mCurrentPosition != mPos) {
                mActivity.getViewPager().setCurrentItem(mPos, true);
                setCurrentItem(mPos, 0);
            }
            mTabFragmentList.get(mPos).refreshList(pGridItems, pCanRefreshOnScroll, pShowLoading);
        }
    }

    public void setCurrentItem(int pPosition, int pOffsetx) {
        mCurrentPosition = pPosition;
        Log.d(TAG, "position:" + pPosition);
        horizontalScrollToCurrent(pOffsetx);
    }

    @SuppressLint("ObsoleteSdkInt")
    private void horizontalScrollToCurrent(int pOffsetx) {
        Log.d(TAG, "offset:" + pOffsetx);
        for (int i = 0; i < mTabCount; i++) {
            View[] viewTab = mTabSparceArray.get(i);
            if (i == mCurrentPosition) {
                viewTab[0].setVisibility(View.VISIBLE);
                viewTab[1].setVisibility(View.VISIBLE);
            } else {
                viewTab[0].setVisibility(View.INVISIBLE);
                viewTab[1].setVisibility(View.INVISIBLE);
            }
        }
        View[] view = mTabSparceArray.get(mCurrentPosition);
        int left = view[2].getLeft() - (mScreenWidth - view[2].getWidth() - mViewSeparatorWidthMeasure) / 2 + mAboutTab.getWidth() - mViewSeparatorWidthMeasure;
        if (mCurrentPosition == 0) {
            left += mViewSeparatorWidthMeasure;
        } else if (view[2].getLeft() == 0) {
            View[] view2 = mTabSparceArray.get(mCurrentPosition - 1);
            left = view2[2].getLeft() + view2[2].getWidth() - (mScreenWidth - view[2].getMeasuredWidth() - mViewSeparatorWidthMeasure) / 2 + mAboutTab.getWidth() - mViewSeparatorWidthMeasure;
        }
        HorizontalScrollView horizontal = mActivity.findViewById(R.id.hsv);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            ObjectAnimator animator = ObjectAnimator.ofInt(horizontal, "scrollX", left + pOffsetx);
            animator.setDuration(400);
            animator.start();
        } else {
            horizontal.smoothScrollTo(left + pOffsetx, 0);
        }
    }

    @Override
    public int getCount() {
        return mTabCount;
    }

    @Override
    public CharSequence getPageTitle(int pPosition) {
        return "◀ " + mListTab.get(pPosition) + " ▶";
    }

    public void nextPage() {
        VideoGridFragment fragment = mTabFragmentList.get(mCurrentPosition);
        mActivity.findShowNextPage(fragment.getCategoryType(), fragment.getKey());
    }

    public void reloadPage() {
        VideoGridFragment fragment = mTabFragmentList.get(mCurrentPosition);
        mActivity.findShowReloadPage(fragment.getCategoryType(), fragment.getKey());
    }

    public void recenter(Configuration pNewConfig) {
        int offsetx, left, right;
        if (pNewConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            left = mLeftH;
            right = mRightH;
        } else {
            left = mLeftV;
            right = mRightV;
        }
        offsetx = left - mHorizontalTab.getPaddingLeft();
        int tmp = mScreenWidth;
        //noinspection SuspiciousNameCombination
        mScreenWidth = mScreenHeight;
        mScreenHeight = tmp;
        mHorizontalTab.setPadding(left, 0, right, 0);
        horizontalScrollToCurrent(offsetx);
    }

    private class CustomOnTouchListener implements View.OnTouchListener {

        private final int mPosition;

        CustomOnTouchListener(int pPosition) {
            mPosition = pPosition;
        }

        @SuppressLint({"NewApi", "ClickableViewAccessibility"})
        @Override
        public boolean onTouch(View pView, MotionEvent pEvent) {
            switch (pEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_UP:
                    mActivity.getViewPager().setCurrentItem(mPosition, true);
                    setCurrentItem(mPosition, 0);
                    mActivity.getViewPager().requestLayout();
                    break;
            }
            return false;
        }
    }
}
