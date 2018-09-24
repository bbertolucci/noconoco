package com.bbproject.noconoco.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.bbproject.noconoco.R;
import com.bbproject.noconoco.activities.SubActivity;
import com.bbproject.noconoco.constants.CategoryType;
import com.bbproject.noconoco.constants.SettingConstants;
import com.bbproject.noconoco.fragments.SubVideoGridFragment;
import com.bbproject.noconoco.model.json.Show;
import com.bbproject.noconoco.utils.ObjectSerializer;

import java.io.IOException;
import java.util.ArrayList;

public class SubTabsPagerAdapter {

    private static final String TAG = "SubTabsPagerAdapter";

    private final SubActivity mActivity;
    private final int screenWidth;
    private final int screenHeight;
    private final LayoutInflater mInflate;
    private LinearLayout mHorizontalTab;
    private int mScroll;
    private SubVideoGridFragment mTabFragment;

    @SuppressWarnings("unchecked")
    public SubTabsPagerAdapter(SubActivity pActivity) {
        mActivity = pActivity;

        DisplayMetrics displayMetrics = mActivity.getResources().getDisplayMetrics();
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

        mInflate = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (null == mInflate) {
            throw new AssertionError("LayoutInflater not found.");
        }

        mHorizontalTab = mActivity.findViewById(R.id.lineartab);

        ArrayList<Show> currentList = new ArrayList<>();
        try {
            currentList = (ArrayList<Show>) ObjectSerializer.deserialize(mActivity.getSettings().getString(SettingConstants.SHOW_LIST, ObjectSerializer.serialize(new ArrayList<Show>())));
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
        }
        addTab(currentList, CategoryType.RECORDED);

        mHorizontalTab.setPadding(0, 0, 0, 0);
        final HorizontalScrollView horizontal = mActivity.findViewById(R.id.hsv);

        horizontal.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    horizontal.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    horizontal.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                horizontal.scrollTo(mScroll, 0);
            }
        });
    }

    public void changeToList() {
        mTabFragment.changeToList();
    }

    public void changeToGrid() {
        mTabFragment.changeToGrid();
    }

    @SuppressWarnings("unchecked")
    public void refreshRecorded() {
        ArrayList<Show> currentList = new ArrayList<>();
        try {
            currentList = (ArrayList<Show>) ObjectSerializer.deserialize(mActivity.getSettings().getString(SettingConstants.SHOW_LIST, ObjectSerializer.serialize(new ArrayList<Show>())));
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
        }
        refreshList(currentList);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addTab(ArrayList<? extends Parcelable> pTabList, @SuppressWarnings("SameParameterValue") int pCategoryType) {

        String tabName = CategoryType.getTabName(pCategoryType, mActivity);

        LinearLayout linearLayout = (LinearLayout) mInflate.inflate(R.layout.view_tab, mHorizontalTab, false);
        linearLayout.measure(screenWidth, screenHeight);

        Button button = linearLayout.findViewById(R.id.id);
        //vButton.setTag(11110000);
        button.setText(tabName);

        View v = linearLayout.findViewById(R.id.tab_top);
        //v.setTag(11111000);
        View w = linearLayout.findViewById(R.id.tab_bottom);
        //w.setTag(11112000);
        linearLayout.measure(screenWidth, screenHeight);
        v.setVisibility(View.VISIBLE);
        w.setVisibility(View.VISIBLE);
        mScroll = (screenWidth - linearLayout.getMeasuredWidth()) / 2;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        mHorizontalTab.addView(linearLayout, params);

        Bundle paramBundle = new Bundle();
        paramBundle.putParcelableArrayList("ITEM", pTabList);
        paramBundle.putBoolean("ISGRID", mActivity.getSettings().getBoolean(SettingConstants.GRID_LIST, true));

        mTabFragment = new SubVideoGridFragment();
        mTabFragment.setArguments(paramBundle);
    }

    private void refreshList(ArrayList<? extends Parcelable> pGridItems) {
        mTabFragment.refreshList(pGridItems);
    }
}
