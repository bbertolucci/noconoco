package com.bbproject.noconoco.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bbproject.noconoco.R;
import com.bbproject.noconoco.activities.MainActivity;
import com.bbproject.noconoco.constants.SettingConstants;
import com.bbproject.noconoco.debug.DbgMode;
import com.bbproject.noconoco.fragments.VideoGridFragment;
import com.bbproject.noconoco.model.json.Family;
import com.bbproject.noconoco.model.json.Show;
import com.bbproject.noconoco.utils.ObjectSerializer;
import com.bbproject.noconoco.utils.SettingUtils;
import com.bbproject.noconoco.utils.TimeAgo;
import com.bbproject.noconoco.custom.view.smartimage.SmartImageTask.OnCompleteListener;
import com.bbproject.noconoco.custom.view.smartimage.SmartImageView;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.bbproject.noconoco.constants.Constants.DEMO_MODE;

public class VideoGridAdapter extends BaseAdapter {

    private static final String TAG = "VideoGridAdapter";
    private final VideoGridFragment mFragment;
    private final String mLocation;
    private final MainActivity mMainActivity;
    private final boolean mRecorded;
    private final SettingUtils mSettings;
    private final LayoutInflater mInflater;
    private final SimpleDateFormat mSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE);
    private final static Date NOW = new Date();
    private final SparseArray<GradientDrawable> mGradientMap = new SparseArray<>();
    private List<?> mGridItems;
    private int mGridCount;

    public VideoGridAdapter(VideoGridFragment pFragment, List<?> pGridItems, MainActivity pActivity, String pLocation, boolean pRecorded) {
        super();
        mFragment = pFragment;
        mGridItems = pGridItems;
        mGridCount = pGridItems.size();
        mLocation = pLocation;
        mMainActivity = pActivity;
        mRecorded = pRecorded;
        mSettings = new SettingUtils(mFragment.getActivity());
        mInflater = (LayoutInflater) mFragment.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (null == mInflater) {
            throw new AssertionError("LayoutInflater not found.");
        }
    }

    public void refreshList(List<?> pGridItems) {
        mGridItems = pGridItems;
        mGridCount = pGridItems.size();
        notifyDataSetChanged();
    }

    @SuppressWarnings("unchecked")
    @SuppressLint("NewApi")
    @Override
    public View getView(int pPosition, View pConvertView, ViewGroup pParent) {
        View convertView;
        ViewHolder holder;
        boolean gray = false;
        boolean hasToChange = false;
        if (pConvertView != null) {
            holder = (ViewHolder) pConvertView.getTag();
            hasToChange = (null != holder) && (holder.isGrid != mFragment.getIsGrid());
        }
        if ((pConvertView == null) || hasToChange) {

            if (mFragment.getIsGrid()) {
                convertView = mInflater.inflate(R.layout.view_grid_2column, pParent, false);
            } else {
                convertView = mInflater.inflate(R.layout.view_grid_1column, pParent, false);
            }
            holder = new ViewHolder();
            holder.smartImageView = convertView.findViewById(R.id.image1);
            holder.smartImageView.setAdjustHorizontal(true);
            holder.smartImageView.setAdjustSizeImage(512, 288);
            holder.textView = convertView.findViewById(R.id.title1);
            holder.total_time = convertView.findViewById(R.id.total_time);

            if (!mFragment.getIsGrid()) {
                holder.layout_stars = convertView.findViewById(R.id.layout_stars);
                holder.star1 = convertView.findViewById(R.id.star1);
                holder.star2 = convertView.findViewById(R.id.star2);
                holder.star3 = convertView.findViewById(R.id.star3);
                holder.star4 = convertView.findViewById(R.id.star4);
                holder.star5 = convertView.findViewById(R.id.star5);
                holder.resume = convertView.findViewById(R.id.resume);
                holder.published_time = convertView.findViewById(R.id.published_time);
            }
            holder.isGrid = mFragment.getIsGrid();
            if (mGridItems.get(pPosition) instanceof Show) {
                holder.show = (Show) mGridItems.get(pPosition);
            } else { //instanceof Family
                Family family = (Family) mGridItems.get(pPosition);
                holder.show = Show.buildShowFromFamily(family);
                if (!mFragment.getIsGrid()) {
                    holder.resume.setTextColor(mFragment.getResources().getColor(R.color.light_font));
                }
            }

            holder.read = convertView.findViewById(R.id.alreadyviewed);
            holder.noread = convertView.findViewById(R.id.cantbeviewed);
            holder.csa = convertView.findViewById(R.id.csadraw);
            holder.playlist = convertView.findViewById(R.id.imgplaylist);
            holder.favorites = convertView.findViewById(R.id.imgfavorites);
            holder.download = convertView.findViewById(R.id.imgdownload);
            holder.familyinfo = convertView.findViewById(R.id.familyinfo);
            holder.family = convertView.findViewById(R.id.family);

        } else {

            convertView = pConvertView;
            holder = (ViewHolder) pConvertView.getTag();

            if (mGridItems.get(pPosition) instanceof Show) {
                Show show = (Show) mGridItems.get(pPosition);
                if (holder.show.getIdShow().equals(show.getIdShow())) {

                    if (mMainActivity.hasShowInPlayList(holder.show)) {
                        holder.playlist.setVisibility(View.VISIBLE);
                    } else {
                        holder.playlist.setVisibility(View.GONE);
                    }
                    if (mMainActivity.hasShowInFavorites(holder.show.getIdFamily())) {
                        holder.favorites.setVisibility(View.VISIBLE);
                    } else {
                        holder.favorites.setVisibility(View.GONE);
                    }
                    return convertView;
                }
                holder.show = show;
            } else { //instance of Family
                Family family = (Family) mGridItems.get(pPosition);
                if (holder.show.getIdFamily().equals(family.getIdFamily())) {
                    if (mMainActivity.hasShowInFavorites(holder.show.getIdFamily())) {
                        holder.favorites.setVisibility(View.VISIBLE);
                    } else {
                        holder.favorites.setVisibility(View.GONE);
                    }
                    return convertView;
                }
                holder.show = Show.buildShowFromFamily(family);
            }
        }
        if (mGridItems.get(pPosition) instanceof Show) {
            long millis = holder.show.getDurationMs();
            long second = (millis / 1000) % 60;
            long minute = (millis / (1000 * 60)) % 60;
            long hour = (millis / (1000 * 60 * 60)) % 24;

            String time;
            if (hour > 0) {
                time = String.format(Locale.FRANCE, "%02d:%02d:%02d", hour, minute, second);
            } else {
                time = String.format(Locale.FRANCE, "%02d:%02d", minute, second);
            }
            holder.total_time.setText(time);

            if (!mFragment.getIsGrid()) {

                time = "";
                try {
                    String dateStr = holder.show.getOnlineDateStartUtc();
                    Date onlineDate = mSdf.parse(dateStr);
                    time = TimeAgo.toRelative(mFragment.getActivity(), onlineDate, NOW, 1);
                } catch (ParseException e) {
                    Log.e(TAG, "ParseException", e);
                }
                holder.published_time.setText(time);

                String resume = (!"null".equals(holder.show.getShowResume()) ? holder.show.getShowResume() : "");
                holder.resume.setText(resume);

                if (holder.show.getGlobalRating() > 0) {
                    holder.layout_stars.setVisibility(View.VISIBLE);
                    holder.star1.setSelected(true);
                    holder.star2.setSelected(holder.show.getGlobalRating() > 1);
                    holder.star3.setSelected(holder.show.getGlobalRating() > 2);
                    holder.star4.setSelected(holder.show.getGlobalRating() > 3);
                    holder.star5.setSelected(holder.show.getGlobalRating() > 4);
                } else {
                    holder.layout_stars.setVisibility(View.INVISIBLE);
                }
            }
        } else {
            if (!mFragment.getIsGrid()) {
                String resume = (!"null".equals(holder.show.getFamilyResume()) ? holder.show.getFamilyResume() : "");
                holder.resume.setText(resume);
                holder.layout_stars.setVisibility(View.INVISIBLE);
                holder.textView.setVisibility(View.INVISIBLE);
            }
        }

        if (!"*".equals(holder.show.getGeoloc()) && !holder.show.getGeoloc().toLowerCase(Locale.FRANCE).contains(mLocation.toLowerCase(Locale.FRANCE))) {
            gray = true;
        }
        switch (mMainActivity.getThumbnailQuality()) {
            case 0:
                holder.url = holder.show.getScreenshot128();
                break;
            case 1:
                holder.url = holder.show.getScreenshot256();
                break;
            case 2:
                holder.url = holder.show.getScreenshot512();
                break;
        }

        if (DEMO_MODE) {
            holder.smartImageView.setImageResource(DbgMode.getDemoImage());
            AlphaAnimation scaleAnim = new AlphaAnimation(0, 1);
            scaleAnim.setDuration(375);
            scaleAnim.initialize(convertView.getWidth(), convertView.getHeight(), convertView.getWidth(), convertView.getHeight());
            convertView.startAnimation(scaleAnim);
        } else {
            convertView.setVisibility(View.INVISIBLE);
            holder.smartImageView.setImageUrl(holder.url, gray, new CustomCompleteListener(convertView));
        }

        String textTitle = (!"null".equals(holder.show.getShowTT()) ? (holder.show.getShowTT()) : "");
        if (0 < holder.show.getEpisode() + holder.show.getSeason()) {
            if (!"".equals(textTitle)) textTitle += " - ";
            if (0 < holder.show.getSeason()) {
                textTitle += mMainActivity.getString(R.string.prefix_season) + holder.show.getSeason();
                if (0 < holder.show.getEpisode()) {
                    textTitle += mMainActivity.getString(R.string.prefix_episode) + holder.show.getEpisode() + mMainActivity.getString(R.string.suffix_episode);
                }
            } else if (0 < holder.show.getEpisode()) {
                textTitle += "#" + holder.show.getEpisode();
            }

        }
        holder.textView.setText(textTitle);
        if (gray) {
            holder.textView.setTextColor(Color.RED);
            holder.read.setVisibility(View.GONE);
            holder.noread.setVisibility(View.VISIBLE);
        } else {
            holder.textView.setTextColor(Color.WHITE);
            holder.noread.setVisibility(View.GONE);
            if (holder.show.getMarkRead() == 1) {
                holder.read.setVisibility(View.VISIBLE);
            } else {
                if (mMainActivity.isInReadList(holder.show.getIdShow())) {
                    holder.read.setVisibility(View.VISIBLE);
                } else {
                    holder.read.setVisibility(View.GONE);
                }
            }
        }
        if ("0".equals(holder.show.getRatingFr())) {
            holder.csa.setVisibility(View.GONE);
        } else {
            holder.csa.setVisibility(View.VISIBLE);
            switch (holder.show.getRatingFr()) {
                case "10":
                    holder.csa.setSelected(false);
                    holder.csa.setEnabled(false);
                    break;
                case "12":
                    holder.csa.setSelected(true);
                    holder.csa.setEnabled(false);
                    break;
                case "16":
                    holder.csa.setSelected(false);
                    holder.csa.setEnabled(true);
                    break;
                default:
                    holder.csa.setSelected(true);
                    holder.csa.setEnabled(true);
                    break;
            }
        }
        if (mMainActivity.hasShowInPlayList(holder.show)) {
            holder.playlist.setVisibility(View.VISIBLE);
        } else {
            holder.playlist.setVisibility(View.GONE);
        }
        if (mMainActivity.hasShowInFavorites(holder.show.getIdFamily())) {
            holder.favorites.setVisibility(View.VISIBLE);
        } else {
            holder.favorites.setVisibility(View.GONE);
        }
        if (mRecorded) {
            ArrayList<Show> currentList = new ArrayList<>();
            try {
                currentList = (ArrayList<Show>) ObjectSerializer.deserialize(mSettings.getString(SettingConstants.SHOW_LIST, ObjectSerializer.serialize(new ArrayList<Show>())));
            } catch (IOException e) {
                Log.e(TAG, "IOException", e);
            }
            boolean notFound = true;
            for (Show show : currentList) {
                if (show.getIdShow().equals(holder.show.getIdShow())) {
                    holder.download.setVisibility(View.VISIBLE);
                    switch (show.getRecordedStatus()) {
                        case 0:
                            holder.download.setSelected(true);
                            holder.download.setEnabled(false);
                            break;
                        case 1:
                            holder.download.setSelected(false);
                            holder.download.setEnabled(true);
                            break;
                        default:
                            holder.download.setSelected(true);
                            holder.download.setEnabled(true);
                            break;
                    }
                    notFound = false;
                }
            }
            if (notFound) holder.download.setVisibility(View.GONE);
        }
        GradientDrawable gd = getGradientBackground(holder.show.getIdFamily());

        if (Build.VERSION.SDK_INT >= 16) {
            holder.familyinfo.setBackground(gd);
        } else {
            holder.familyinfo.setBackgroundDrawable(gd);
        }
        holder.family.setText(holder.show.getFamilyTT());

        convertView.setTag(holder);
        return convertView;
    }

    private GradientDrawable getGradientBackground(Integer pFamilyId) {
        GradientDrawable gd = mGradientMap.get(pFamilyId);
        if (null == gd) {
            gd = new GradientDrawable(
                    GradientDrawable.Orientation.BL_TR,
                    Family.getColorsByFamilyId(pFamilyId));
            gd.setCornerRadius(0f);
        }
        return gd;
    }

    @Override
    public int getCount() {
        if (mGridItems != null) {
            return mGridCount;
        }
        return 0;
    }

    @Override
    public Object getItem(int pPosition) {
        if (mGridItems != null) {
            return mGridItems.get(pPosition);
        }
        return null;
    }

    @Override
    public long getItemId(int pPosition) {
        if (mGridItems != null) {
            if (mGridItems.get(pPosition) instanceof Show) {
                Show show = (Show) mGridItems.get(pPosition);
                return Long.parseLong(show.getIdShow());
            } else {
                Family family = (Family) mGridItems.get(pPosition);
                return family.getIdFamily();
            }
        }
        return 0;
    }

    private static class ViewHolder {
        private TextView textView;
        private SmartImageView smartImageView;
        private String url = "";
        private Show show;
        private RelativeLayout read;
        private RelativeLayout noread;
        private View csa;
        private View playlist;
        private View favorites;
        private View download;
        private RelativeLayout familyinfo;
        private TextView family;
        private View star1;
        private View star2;
        private View star3;
        private View star4;
        private View star5;
        private TextView total_time;
        private TextView resume;
        private TextView published_time;
        private View layout_stars;
        private boolean isGrid;
    }

    private class CustomCompleteListener extends OnCompleteListener {

        CustomCompleteListener(View view) {
            super(view);
        }

        @Override
        public void onComplete() {
            mView.setVisibility(View.VISIBLE);
            AlphaAnimation scaleAnim = new AlphaAnimation(0, 1);
            scaleAnim.setDuration(375);
            scaleAnim.initialize(mView.getWidth(), mView.getHeight(), mView.getWidth(), mView.getHeight());
            mView.startAnimation(scaleAnim);
        }
    }
}


