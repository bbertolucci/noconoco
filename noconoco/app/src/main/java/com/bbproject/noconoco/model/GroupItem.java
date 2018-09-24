package com.bbproject.noconoco.model;

import java.util.ArrayList;

public class GroupItem {
    public final static int NOICON = 0;
    public final static int ICON = 1;
    public final static int TWITCH = 2;
    private final int mCategoryType;
    private final ArrayList<SubGroupItem> mSubList;
    private final int mCount;
    private final String mName;
    private final int mType;
    private final String mKey;

    public GroupItem(String pName, int pCategoryType, int pType, ArrayList<SubGroupItem> pSubList, String pKey) {
        this.mName = pName;
        this.mCategoryType = pCategoryType;
        this.mType = pType;
        this.mSubList = pSubList;
        this.mCount = (null != pSubList) ? pSubList.size() : 0;
        this.mKey = pKey;
    }

    public String getName() {
        return mName;
    }

    public int getCategoryType() {
        return mCategoryType;
    }

    public int getType() {
        return mType;
    }

    public ArrayList<SubGroupItem> getSubList() {
        return mSubList;
    }

    public int getCount() {
        return mCount;
    }

    public String getKey() {
        return mKey;
    }
}
