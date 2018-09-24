package com.bbproject.noconoco.fragments;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.bbproject.noconoco.R;
import com.bbproject.noconoco.activities.SubActivity;
import com.bbproject.noconoco.activities.VideoActivity;
import com.bbproject.noconoco.adapters.SubVideoGridAdapter;
import com.bbproject.noconoco.constants.SettingConstants;
import com.bbproject.noconoco.model.json.Show;
import com.bbproject.noconoco.utils.ObjectSerializer;
import com.bbproject.noconoco.utils.SettingUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SubVideoGridFragment extends Fragment {

    private static final String TAG = "SubVideoGridFragment";

    private GridView mGridView;
    private SubVideoGridAdapter mGridAdapter;
    private ArrayList<? extends Parcelable> mGridItems;
    private List<Show> mFilteredGridItems;
    private SettingUtils mSettings;
    private String mLocation;
    private boolean mIsGrid;

    public SubVideoGridFragment() {
        if (null != getArguments()) {
            Bundle bundle = getArguments();
            mSettings = new SettingUtils(getActivity().getApplicationContext());
            mGridItems = bundle.getParcelableArrayList("ITEM");
            mLocation = mSettings.getString(SettingConstants.GEOLOC, "***");
            mIsGrid = bundle.getBoolean("ISGRID");
        }
    }

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
            mIsGrid = bundle.getBoolean("ISGRID");
        }

        View vMainVew = pInflater.inflate(R.layout.view_grid, pContainer, false);
        mGridView = vMainVew.findViewById(R.id.gridView);
        initColumn();

        mGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view,
                                    int position, long id) {
                onGridItemClick(position);
            }
        });
        init();
        return vMainVew;
    }

    private void init() {
        mFilteredGridItems = filterList(mGridItems);
        mGridAdapter = new SubVideoGridAdapter(this, mFilteredGridItems, (SubActivity) getActivity(), mLocation);
        mGridView.setAdapter(mGridAdapter);
        mGridView.computeScroll();
    }

    @SuppressWarnings("unchecked")
    private void onGridItemClick(int pPosition) {
        Intent intent = new Intent(getActivity(), VideoActivity.class);
        try {
            ArrayList<Show> currentList = (ArrayList<Show>) ObjectSerializer.deserialize(mSettings.getString(SettingConstants.SHOW_LIST, ObjectSerializer.serialize(new ArrayList<Show>())));
            for (Show tmpShow : currentList) {
                if (tmpShow.getIdShow().equals(mFilteredGridItems.get(pPosition).getIdShow())) {
                    if (tmpShow.getRecordedStatus() != 2) {
                        Toast.makeText(getActivity(), "Video non telechargee", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    break;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "An IOException occured", e);
        }
        intent.putExtra("show", (Serializable) mFilteredGridItems.get(pPosition));
        intent.putExtra("recorded", true);
        intent.putExtra("is_offline", true);
        startActivityForResult(intent, 1);
    }

    public void refreshList(ArrayList<? extends Parcelable> pGridItems) {
        mGridItems = pGridItems;
        mFilteredGridItems = filterList(mGridItems);

        if (null != mGridAdapter) {
            mGridAdapter.refreshList(mFilteredGridItems);
        }
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

/*    public void refreshList() {
        refreshList(mGridItems, mCanRefreshOnScroll, mShowLoading);
    }*/

    @Override
    public void onConfigurationChanged(Configuration pNewConfig) {
        if (mIsGrid) {
            mGridView.setNumColumns(pNewConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? 3 : 2);
        } /*else {
            mGridView.setNumColumns(1);
        }*/
        super.onConfigurationChanged(pNewConfig);
    }

    private ArrayList<Show> filterList(List<? extends Parcelable> pArrayList) {
        ArrayList<Show> returnList = new ArrayList<>();
        for (Object object : pArrayList) {
            if (object instanceof Show) {
                Show show = (Show) object;
                if (!"".equals(show.getRatingFr())) {
                    int vCsa = Integer.parseInt(show.getRatingFr());
                    if (((SubActivity) getActivity()).getParentalControlLevel() > vCsa) {
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
        pSavedInstanceState.putBoolean("ISGRID", mIsGrid);
    }
}