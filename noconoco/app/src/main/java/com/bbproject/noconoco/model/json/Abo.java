package com.bbproject.noconoco.model.json;

import android.os.Parcel;
import android.os.Parcelable;

import com.bbproject.noconoco.model.ShowArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class Abo implements Parcelable, Serializable {

    public static final Parcelable.Creator<Abo> CREATOR = new Parcelable.Creator<Abo>() {
        public Abo createFromParcel(Parcel in) {
            return new Abo(in);
        }

        public Abo[] newArray(int size) {
            return new Abo[size];
        }
    };
    /**
     *
     */
    private static final long serialVersionUID = 5892950633977242195L;
    private String mShortName;
    private String mKey;
    private String mGeoloc;
    private ShowArrayList mList = new ShowArrayList();
    private int mCurrentPage = 0;

    private Abo() {
        super();
    }

    private Abo(Parcel in) {
        super();
        readFromParcel(in);
    }

    public static void decodeJSON(String pResponse, ArrayList<Abo> pList) throws JSONException {
        JSONArray root_object = new JSONArray(pResponse);
        int count = root_object.length();
        for (int i = 0; i < count; i++) {
            Abo abo = new Abo();
            JSONObject first = root_object.getJSONObject(i);
            abo.mShortName = first.getString("partner_shortname");
            abo.mKey = first.getString("partner_key");
            abo.mGeoloc = first.getString("geoloc");
            pList.add(abo);
        }
        //return root_object.length();
    }

    public int getCurrentPage() {
        return mCurrentPage;
    }

    public void setCurrentPage(int pCurrentPage) {
        this.mCurrentPage = pCurrentPage;
    }

    public String getShortName() {
        return mShortName;
    }

    public String getKey() {
        return mKey;
    }

    public ArrayList<Show> getList() {
        return mList;
    }

    public void setList(ShowArrayList pList) {
        this.mList = pList;
    }

    private void readFromParcel(Parcel in) {
        mShortName = in.readString();
        mKey = in.readString();
        mGeoloc = in.readString();
        mList = (ShowArrayList) in.readSerializable();
        mCurrentPage = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mShortName);
        dest.writeString(mKey);
        dest.writeString(mGeoloc);
        dest.writeSerializable(mList);
        dest.writeInt(mCurrentPage);
    }

}
