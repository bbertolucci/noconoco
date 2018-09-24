package com.bbproject.noconoco.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.bbproject.noconoco.model.json.Family;

import java.util.ArrayList;

public class FamArrayList extends ArrayList<Family> implements Parcelable {

    public static final Creator<FamArrayList> CREATOR = new Creator<FamArrayList>() {
        public FamArrayList createFromParcel(Parcel in) {
            return new FamArrayList(in);
        }

        public FamArrayList[] newArray(int size) {
            return new FamArrayList[size];
        }
    };

    public FamArrayList() {
        super();
    }

    private FamArrayList(Parcel pIn) {
        pIn.readTypedList(this, Family.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel pParcel, int pI) {
        pParcel.writeTypedList(this);
    }
}
