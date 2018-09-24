package com.bbproject.noconoco.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.bbproject.noconoco.R;
import com.bbproject.noconoco.adapters.SubTabsPagerAdapter;
import com.bbproject.noconoco.constants.SettingConstants;
import com.bbproject.noconoco.utils.SettingUtils;

/**
 * SubActivity is the activity dedicated for the offline replay.
 * Recorded video are stored on device and are listed on this screen.
 */
public class SubActivity extends FragmentActivity {

    private final SettingUtils mSettings = new SettingUtils(this);
    private SubTabsPagerAdapter mTabsPagerAdapter;
    private int mThumbnailQuality = 0;
    private boolean mIsGrid = true;
    private int mParentalControlLevel = 99;

    /**
     * Get ParentalControl Level
     */
    public int getParentalControlLevel() {
        return mParentalControlLevel;
    }

    /**
     * Get Thumbnail Quality
     */
    public int getThumbnailQuality() {
        return mThumbnailQuality;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh recorded video list
        if (null != mTabsPagerAdapter) mTabsPagerAdapter.refreshRecorded();
    }

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);

        // Set the View
        setContentView(R.layout.activity_submain);

        // Get User setting : grid or list
        mIsGrid = mSettings.getBoolean(SettingConstants.GRID_LIST, true);

        // Get User setting : parental control
        mParentalControlLevel = mSettings.getInteger(SettingConstants.PARENTAL_CONTROL, 99);

        // Get User setting : Thumbnail quality
        mThumbnailQuality = mSettings.getInteger(SettingConstants.THUMB_QUALITY, 0);

        // Instantiate the fragment pager adapter
        mTabsPagerAdapter = new SubTabsPagerAdapter(this);

        // Set the action click to switch between list and grid view
        ImageButton btnList = findViewById(R.id.listic);
        btnList.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!v.isSelected()) {
                    mTabsPagerAdapter.changeToList();
                    v.setSelected(true);
                    mIsGrid = false;
                    mSettings.setBoolean(SettingConstants.GRID_LIST, false);
                } else {
                    mTabsPagerAdapter.changeToGrid();
                    v.setSelected(false);
                    mIsGrid = true;
                    mSettings.setBoolean(SettingConstants.GRID_LIST, true);
                }
            }
        });
        // Set current display grid or list
        btnList.setSelected(!mIsGrid);
    }

    public SettingUtils getSettings() {
        return mSettings;
    }
}