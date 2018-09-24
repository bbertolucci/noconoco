package com.bbproject.noconoco.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.bbproject.noconoco.model.json.Show;

import java.util.ArrayList;

public class ShowArrayList extends ArrayList<Show> implements Parcelable {

    public static final Parcelable.Creator<ShowArrayList> CREATOR = new Parcelable.Creator<ShowArrayList>() {
        public ShowArrayList createFromParcel(Parcel in) {
            return new ShowArrayList(in);
        }

        public ShowArrayList[] newArray(int size) {
            return new ShowArrayList[size];
        }
    };

    public ShowArrayList() {
        super();
    }

    private ShowArrayList(Parcel in) {
        in.readTypedList(this, Show.CREATOR);
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
