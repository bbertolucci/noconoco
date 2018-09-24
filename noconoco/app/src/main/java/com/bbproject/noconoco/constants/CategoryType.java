package com.bbproject.noconoco.constants;

import android.content.Context;

import com.bbproject.noconoco.R;

public class CategoryType {
    //public static final int NONE                        = 0;// (null,null),
    public static final int LAST_NEWS = 1;//(R.string.last_video,R.string.list_group_most_recent),
    public static final int FREE_SHOWS = 2;//(R.string.free_video,null),
    public static final int USER_PLAYLIST_INFO = 3;//(R.string.playlist,R.string.list_group_my_playlist),
    public static final int ABO_LIST = 4;//(null,R.string.list_group_my_subscriptions),
    public static final int FAM_LIST = 5;//(null,null),
    public static final int MOST_POPULAR = 6;//(R.string.most_popular,R.string.list_group_most_popular),
    public static final int UNREAD = 7;//(R.string.unread,R.string.list_subgroup_not_watched),
    public static final int READ = 8;//(R.string.read,R.string.list_subgroup_watched),
    public static final int RECOMMENDED_LIST = 9;//(R.string.recommended,R.string.list_group_recommended),
    public static final int PAID_LIST = 10;//(R.string.bought,R.string.list_group_my_video_bought),
    public static final int SEARCH_INFO = 11;//(R.string.search,null),
    public static final int FAVORITES_LIST = 12;//(R.string.favorites,R.string.list_group_my_favorites),
    public static final int FAVORITES_LIST_BIS = 13;//(null,null),
    public static final int TOKEN_NO_USER = 14;//(null,null),
    public static final int TOKEN = 15;//(null,null),
    public static final int REFRESH_TOKEN = 16;//(null,null),
    public static final int USERS_INIT = 17;//(null,null),
    public static final int SHOW_BY_PARTNER_REFRESH = 18;//(null,null),
    public static final int SHOW_BY_PARTNER = 19;//(null,null),
    public static final int SHOW_BY_FAMILY_REFRESH = 20;//(null,null),
    public static final int SHOW_BY_FAMILY = 21;//(null,null),
    public static final int USER_LIST = 22;//(null,null),
    public static final int USER_LIST_BIS = 23;//(null,null),
    public static final int ADDDEL_USER_LIST = 24;//(null,null),
    public static final int ADDDEL_FAVORITES_LIST = 25;//(null,null),
    public static final int SEARCH_REFRESH = 26;//(null,null),
    public static final int SEARCH = 27;//(null,null),
    public static final int SEARCH_INFO_REFRESH = 28;//(null,null),
    public static final int PAID_LIST_REFRESH = 29;//(null,null),
    public static final int RECOMMENDED_LIST_REFRESH = 30;//(null,null),
    public static final int READ_REFRESH = 31;//(null,null),
    public static final int UNREAD_REFRESH = 32;//(null,null),
    public static final int FREE_SHOWS_REFRESH = 33;//(null,null),
    public static final int MOST_POPULAR_REFRESH = 34;//(null,null),
    public static final int LAST_NEWS_REFRESH = 35;//(null,null),
    public static final int RETRY = 36;//(null,null),
    public static final int FROM_WEB = 37;//(null,null),
    public static final int MEDIAS = 38;//(null,null),
    public static final int PLAY = 39;//(null,null),
    public static final int MUGEN = 40;//(null,R.string.list_group_nolife_mugen),
    public static final int RECORDED = 41;//(R.string.recorded_video,R.string.list_subgroup_recorded),
    public static final int MY_VIDEO = 42;//(null,R.string.list_group_my_videos),
    public static final int THUMB_QUALITY = 43;//(null,R.string.list_subgroup_thumbs_quality),
    public static final int VIDEO_QUALITY = 44;//(null,R.string.list_subgroup_videos_quality),
    public static final int PARENTAL = 45;//(null,R.string.list_subgroup_parental_control),
    public static final int MY_OPTION = 46;//(null,R.string.list_group_options),
    public static final int ADD_PLAYLIST = 47;//(null,R.string.list_subgroup_playlist_add),
    public static final int MY_PLAYLIST = 48;//(null,R.string.list_subgroup_playlist),
    public static final int ADD_FAVORITES = 49;//(null,R.string.list_subgroup_favorites_add),
    public static final int MY_FAVORITES = 50;//(null,R.string.list_subgroup_favorites),
    public static final int MAY_ALSO_LIKE = 51;//(null,null),
    public static final int RATING_SEND = 52;//(null,null),
    public static final int COMMENT = 53;//(null,null),
    public static final int RANDOM = 54;//(null,null),
    public static final int RECORD = 55;//(null,null),
    public static final int ABO_VALUE = 56;//(null,null),
    public static final int FAM_VALUE = 57;//(null,null);

    public static String getTabName(int pCategory, Context pContext) {
        switch (pCategory) {
            case LAST_NEWS:
                return pContext.getString(R.string.last_video);
            case FREE_SHOWS:
                return pContext.getString(R.string.free_video);
            case USER_PLAYLIST_INFO:
                return pContext.getString(R.string.playlist);
            case MOST_POPULAR:
                return pContext.getString(R.string.most_popular);
            case UNREAD:
                return pContext.getString(R.string.unread);
            case READ:
                return pContext.getString(R.string.read);
            case RECOMMENDED_LIST:
                return pContext.getString(R.string.recommended);
            case PAID_LIST:
                return pContext.getString(R.string.bought);
            case SEARCH_INFO:
                return pContext.getString(R.string.search);
            case FAVORITES_LIST:
                return pContext.getString(R.string.favorites);
            case RECORDED:
                return pContext.getString(R.string.recorded_video);
        }
        return "";
    }

    public static String getMenuName(int pCategory, Context pContext) {
        switch (pCategory) {
            case LAST_NEWS:
                return pContext.getString(R.string.list_group_most_recent);
            case USER_PLAYLIST_INFO:
                return pContext.getString(R.string.list_group_my_playlist);
            case ABO_LIST:
                return pContext.getString(R.string.list_group_my_subscriptions);
            case MOST_POPULAR:
                return pContext.getString(R.string.list_group_most_popular);
            case UNREAD:
                return pContext.getString(R.string.list_subgroup_not_watched);
            case READ:
                return pContext.getString(R.string.list_subgroup_watched);
            case RECOMMENDED_LIST:
                return pContext.getString(R.string.list_group_recommended);
            case PAID_LIST:
                return pContext.getString(R.string.list_group_my_video_bought);
            case FAVORITES_LIST:
                return pContext.getString(R.string.list_group_my_favorites);
            case MUGEN:
                return pContext.getString(R.string.list_group_nolife_mugen);
            case RECORDED:
                return pContext.getString(R.string.list_subgroup_recorded);
            case MY_VIDEO:
                return pContext.getString(R.string.list_group_my_videos);
            case THUMB_QUALITY:
                return pContext.getString(R.string.list_subgroup_thumbs_quality);
            case VIDEO_QUALITY:
                return pContext.getString(R.string.list_subgroup_videos_quality);
            case PARENTAL:
                return pContext.getString(R.string.list_subgroup_parental_control);
            case MY_OPTION:
                return pContext.getString(R.string.list_group_options);
            case ADD_PLAYLIST:
                return pContext.getString(R.string.list_subgroup_playlist_add);
            case MY_PLAYLIST:
                return pContext.getString(R.string.list_subgroup_playlist);
            case ADD_FAVORITES:
                return pContext.getString(R.string.list_subgroup_favorites_add);
            case MY_FAVORITES:
                return pContext.getString(R.string.list_subgroup_favorites);
        }
        return "";
    }
}
