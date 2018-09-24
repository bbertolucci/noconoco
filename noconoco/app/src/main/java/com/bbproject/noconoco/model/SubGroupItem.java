package com.bbproject.noconoco.model;

public class SubGroupItem {
    private final String mName;
    private final int mCategoryType;
    private final int mIconId;
    private final String mKey;

    public SubGroupItem(String pName, int pCategoryType, int pIconId, String pKey) {
        this.mName = pName;
        this.mCategoryType = pCategoryType;
        this.mIconId = pIconId;
        this.mKey = pKey;
    }

    public String getName() {
        return mName;
    }

    public int getCategoryType() {
        return mCategoryType;
    }

    public int getIconId() {
        return mIconId;
    }

    public String getKey() {
        return mKey;
    }
}
