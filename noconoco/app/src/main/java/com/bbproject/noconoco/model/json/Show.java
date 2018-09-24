package com.bbproject.noconoco.model.json;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class Show implements Parcelable, Serializable {

    public static final Parcelable.Creator<Show> CREATOR = new Parcelable.Creator<Show>() {
        public Show createFromParcel(Parcel in) {
            return new Show(in);
        }

        public Show[] newArray(int size) {
            return new Show[size];
        }
    };
    /**
     *
     */
    private static final long serialVersionUID = 242866436796160557L;
    private Integer mMarkRead = 0;
    private String mTypeName;
    private String mThemeName;
    private String mGeoloc;
    private String mIdShow;
    private String mLinkComment;
    private Integer mDurationMs;
    private String mRatingFr;
    private Integer mDisplayRating;
    private String mShowTT;
    private String mShowOT;
    private String mFamilyTT;
    private String mFamilyOT;
    private String mAccessType;
    private String mGuestFree;
    private String mIdPartner;
    private Integer mIdFamily;
    private String mPartnerName;
    private String mPartnerKey;
    private String mPartnerShortname;
    private String mScreenshot128;
    private String mScreenshot256;
    private String mScreenshot512;
    private Integer mResumePlay;
    private Integer mRecordedStatus = 0;
    private String mStreamUrl;
    private String mPath;
    private String mTemplate;
    private Integer mSeason = 0;
    private Integer mEpisode = 0;
    private String mFamilyResume;
    private String mShowResume;
    private String mBanner;
    private String mType;
    private String mTheme;
    private String mFamilyKey;
    private Integer mNbShows;
    private Integer mGlobalRating;
    private Integer mUserRating;
    private Integer mPaid;
    private String mOnlineDateStartUtc;

    private Show() {
        super();
    }

    private Show(Parcel in) {
        super();
        readFromParcel(in);
    }

    public static int decodeJSON(String pResponse, ArrayList<Show> pList) throws JSONException {
        JSONArray root_object = new JSONArray(pResponse);
        int vCount = root_object.length();
        for (int i = 0; i < vCount; i++) {
            Show vShow = new Show();
            JSONObject first = root_object.getJSONObject(i);
            if (first.has("access_type")) {
                vShow.mAccessType = first.getString("access_type");
            } else if (first.has("access_error")) {
                vShow.mAccessType = first.getString("access_error");
            }
            vShow.mGuestFree = first.optString("guest_free", "0");
            vShow.mGeoloc = first.optString("geoloc", "fr");
            vShow.mUserRating = first.optInt("rating_user", 0);
            vShow.mGlobalRating = first.optInt("rated_all_time", 0);
            vShow.mMarkRead = first.optInt("icon_read", 0);
            vShow.mTypeName = first.optString("type_name", "");
            vShow.mThemeName = first.optString("theme_name", "");
            vShow.mDisplayRating = first.optInt("display_rating", 0);
            vShow.mDurationMs = first.optInt("duration_ms", 0);
            vShow.mPaid = first.optInt("show_paid", 0);
            vShow.mIdFamily = first.optInt("id_family", 0);
            vShow.mFamilyTT = first.optString("family_TT", "");
            vShow.mFamilyOT = first.optString("family_OT", "");
            vShow.mIdPartner = first.optString("id_partner", "1");
            vShow.mIdShow = first.optString("id_show", "0");
            vShow.mLinkComment = first.optString("link_comment", "");
            vShow.mPartnerKey = first.optString("partner_key", "NOL");
            vShow.mPartnerName = first.optString("partner_name", "Nolife");
            vShow.mPartnerShortname = first.optString("partner_shortname", "Nolife");
            vShow.mRatingFr = first.optString("rating_fr", "0");
            vShow.mShowTT = first.optString("show_TT", "");
            vShow.mShowOT = first.optString("show_OT", "");
            vShow.mScreenshot128 = first.optString("screenshot_128x72", "");
            vShow.mScreenshot256 = first.optString("screenshot_256x144", "");
            vShow.mScreenshot512 = first.optString("screenshot_512x288", "");
            vShow.mResumePlay = first.optInt("resume_play", 0);
            vShow.mSeason = first.optInt("season_number", 0);
            vShow.mEpisode = first.optInt("episode_number", 0);
            vShow.mShowResume = first.optString("show_resume", "");
            vShow.mFamilyResume = first.optString("family_resume", "");
            vShow.mBanner = first.optString("banner_family", "");
            vShow.mType = first.optString("type_name", "");
            vShow.mTheme = first.optString("theme_name", "");
            vShow.mFamilyKey = first.optString("family_key", "");
            vShow.mNbShows = first.optInt("nb_shows", 0);
            vShow.mOnlineDateStartUtc = first.optString("online_date_start_utc", "");
            pList.add(vShow);
        }
        return pList.size();
    }

    public static Show buildShowFromFamily(Family pFamily) {
        Show show = new Show();
        show.mFamilyKey = pFamily.getFamilyKey();
        show.mNbShows = pFamily.getNbShows();
        show.mFamilyTT = pFamily.getFamilyTT();
        show.mGeoloc = pFamily.getGeoloc();
        show.mIdFamily = pFamily.getIdFamily();
        show.mPartnerShortname = pFamily.getPartnerShortname();
        show.mScreenshot128 = pFamily.getScreenshot128();
        show.mScreenshot256 = pFamily.getScreenshot256();
        show.mScreenshot512 = pFamily.getScreenshot512();
        show.mFamilyResume = pFamily.getFamilyResume();
        return show;
    }

    public String getOnlineDateStartUtc() {
        return mOnlineDateStartUtc;
    }

    public Integer getShowPaid() {
        return mPaid;
    }

    public Integer getGlobalRating() {
        return mGlobalRating;
    }

    public Integer getUserRating() {
        return mUserRating;
    }

    public void setUserRating(Integer userRating) {
        this.mUserRating = userRating;
    }

    public Integer getResumePlay() {
        return mResumePlay;
    }

    public Integer getIdFamily() {
        return mIdFamily;
    }

    public Integer getSeason() {
        return mSeason;
    }

    public Integer getEpisode() {
        return mEpisode;
    }

    public String getFamilyResume() {
        return mFamilyResume;
    }

    public String getShowResume() {
        return mShowResume;
    }

    public Integer getRecordedStatus() {
        return mRecordedStatus;
    }

    public void setRecordedStatus(Integer recordedStatus) {
        this.mRecordedStatus = recordedStatus;
    }

    public String getStreamUrl() {
        return mStreamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this.mStreamUrl = streamUrl;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String path) {
        this.mPath = path;
    }

    public String getScreenshot128() {
        return mScreenshot128;
    }

    public String getScreenshot256() {
        return mScreenshot256;
    }

    public String getScreenshot512() {
        return mScreenshot512;
    }

    public Integer getMarkRead() {
        return mMarkRead;
    }

    public void setMarkRead(Integer markRead) {
        this.mMarkRead = markRead;
    }

    public String getGeoloc() {
        return mGeoloc;
    }

    public String getIdShow() {
        return mIdShow;
    }

    public Integer getDurationMs() {
        return mDurationMs;
    }

    public String getRatingFr() {
        return mRatingFr;
    }

    public String getShowTT() {
        return mShowTT;
    }

    public String getShowOT() {
        return mShowOT;
    }

    public String getFamilyTT() {
        return mFamilyTT;
    }

    public String getFamilyOT() {
        return mFamilyOT;
    }

    public String getPartnerName() {
        return mPartnerName;
    }

    public String getPartnerKey() {
        return mPartnerKey;
    }

    public String getFamilyKey() {
        return mFamilyKey;
    }

    public String getGuestFree() {
        return mGuestFree;
    }

    public String getBanner() {
        return mBanner;
    }

    public String toJSON() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("idPartner", mIdPartner);
            jsonObject.put("familyTT", mFamilyTT);
            jsonObject.put("showTT", !"null".equals(mShowTT) ? mShowTT : "");
            String vTextTitle = "";
            if (0 < mEpisode + mSeason) {
                if (0 < mSeason) {
                    vTextTitle += "Saison : " + mSeason;
                    if (0 < mEpisode) {
                        vTextTitle += " - Episode : ";
                        if (mEpisode < 10) vTextTitle += "0";
                        vTextTitle += mEpisode;
                    }
                } else if (0 < mEpisode) {
                    vTextTitle += "# : ";
                    if (mEpisode < 10) vTextTitle += "0";
                    vTextTitle += mEpisode;
                }


            }
            jsonObject.put("seasonepisode", vTextTitle);
            jsonObject.put("resume", !"null".equals(mShowResume) ? mShowResume : (!"null".equals(mFamilyResume) ? mFamilyResume : ""));
            jsonObject.put("banner", mBanner);
            jsonObject.put("ratingFr", mRatingFr);
            jsonObject.put("screenshot", mScreenshot512);
            jsonObject.put("duration", mDurationMs / 100);
            jsonObject.put("type", mType);
            jsonObject.put("theme", mTheme);

            return jsonObject.toString();
        } catch (JSONException e) {
            return "";
        }

    }

    private void readFromParcel(Parcel in) {

        mMarkRead = in.readInt();
        mTypeName = in.readString();
        mThemeName = in.readString();
        mGeoloc = in.readString();
        mIdShow = in.readString();
        mLinkComment = in.readString();
        mDurationMs = in.readInt();
        mRatingFr = in.readString();
        mDisplayRating = in.readInt();
        mShowTT = in.readString();
        mShowOT = in.readString();
        mFamilyTT = in.readString();
        mFamilyOT = in.readString();
        mAccessType = in.readString();
        mGuestFree = in.readString();
        mIdPartner = in.readString();
        mIdFamily = in.readInt();
        mPartnerName = in.readString();
        mPartnerKey = in.readString();
        mPartnerShortname = in.readString();
        mScreenshot128 = in.readString();
        mScreenshot256 = in.readString();
        mScreenshot512 = in.readString();
        mResumePlay = in.readInt();
        mRecordedStatus = in.readInt();
        mStreamUrl = in.readString();
        mPath = in.readString();
        mTemplate = in.readString();
        mSeason = in.readInt();
        mEpisode = in.readInt();
        mFamilyResume = in.readString();
        mShowResume = in.readString();
        mBanner = in.readString();
        mType = in.readString();
        mTheme = in.readString();
        mFamilyKey = in.readString();
        mNbShows = in.readInt();
        mGlobalRating = in.readInt();
        mUserRating = in.readInt();
        mPaid = in.readInt();
        mOnlineDateStartUtc = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mMarkRead);
        dest.writeString(mTypeName);
        dest.writeString(mThemeName);
        dest.writeString(mGeoloc);
        dest.writeString(mIdShow);
        dest.writeString(mLinkComment);
        dest.writeInt(mDurationMs);
        dest.writeString(mRatingFr);
        dest.writeInt(mDisplayRating);
        dest.writeString(mShowTT);
        dest.writeString(mShowOT);
        dest.writeString(mFamilyTT);
        dest.writeString(mFamilyOT);
        dest.writeString(mAccessType);
        dest.writeString(mGuestFree);
        dest.writeString(mIdPartner);
        dest.writeInt(mIdFamily);
        dest.writeString(mPartnerName);
        dest.writeString(mPartnerKey);
        dest.writeString(mPartnerShortname);
        dest.writeString(mScreenshot128);
        dest.writeString(mScreenshot256);
        dest.writeString(mScreenshot512);
        dest.writeInt(mResumePlay);
        dest.writeInt(mRecordedStatus);
        dest.writeString(mStreamUrl);
        dest.writeString(mPath);
        dest.writeString(mTemplate);
        dest.writeInt(mSeason);
        dest.writeInt(mEpisode);
        dest.writeString(mFamilyResume);
        dest.writeString(mShowResume);
        dest.writeString(mBanner);
        dest.writeString(mType);
        dest.writeString(mTheme);
        dest.writeString(mFamilyKey);
        dest.writeInt(mNbShows);
        dest.writeInt(mGlobalRating);
        dest.writeInt(mUserRating);
        dest.writeInt(mPaid);
        dest.writeString(mOnlineDateStartUtc);
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) return true;
        if (!(that instanceof Show)) return false;
        Show thatShow = (Show) that;
        return null != thatShow.getIdShow() && !thatShow.getIdShow().isEmpty() && thatShow.getIdShow().equals(this.mIdShow);
    }

}
