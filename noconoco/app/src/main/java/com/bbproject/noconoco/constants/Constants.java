package com.bbproject.noconoco.constants;

@SuppressWarnings("WeakerAccess")
public class Constants {

    public static final boolean DEMO_MODE = true;

    public static final String CHROMECAST_APP_ID = "0E6E6FBE";
    public static final String CLIENT_ID = "noconoco";
    public static final String CLIENT_SECRET = "528cefd07d9eb2c196f47d516f537129";
    public static final String API_VERSION = "1.1";
    public static final String CHANNEL_ID = "Noconoco";
    public static final String CHANNEL_NAME = "Noconoco Channel";
    public static final String CHANNEL_DESCRIPTIOM = "Channel used for noconoco";
    public static final int SHOWS_PER_PAGE = 40;
    public static final int MAX_OFFLINE_FILE = 10;
    public static final String ELEMENT_PER_PAGE = "elements_per_page=" + SHOWS_PER_PAGE + "&page=";

    public static final String WEB_LOGIN_URL = "https://api.noco.tv/" + API_VERSION + "/OAuth2/authorize.php?response_type=code&client_id=" + CLIENT_ID + "&state=STATE ";
    public static final String POST_TOKEN_URL = "https://api.noco.tv/" + API_VERSION + "/OAuth2/token.php";
    public static final String POST_TWITCH_TOKEN_URL = "https://api.twitch.tv/api/channels/nolife/access_token";

    public static final String GET_FAM_LIST = "https://api.noco.tv:443/" + API_VERSION + "/families/subscribed?page=0&elements_per_page=400";
    public static final String GET_ABO_LIST = "https://api.noco.tv:443/" + API_VERSION + "/partners";
    public static final String GET_USERS_INIT = "https://api.noco.tv:443/" + API_VERSION + "/users/init";
    //public static final String FIND_USER_HISTORY = "https://api.noco.tv:443/" + API_VERSION + "/users/history?" + ELEMENT_PER_PAGE;

    public static final String FIND_SHOWS = "https://api.noco.tv:443/" + API_VERSION + "/shows?" + ELEMENT_PER_PAGE;
    public static final String FREE_SHOWS = "https://api.noco.tv:443/" + API_VERSION + "/shows?guest_free=1&" + ELEMENT_PER_PAGE;
    public static final String BY_PARTNER_KEY = "partner_key=";
    public static final String BY_FAMILY_KEY = "family_key=";
    public static final String GET_SHOWS = "https://api.noco.tv:443/" + API_VERSION + "/shows/";
    public static final String GET_SHOWS_MEDIAS = "/medias";
    //public static final String GET_SHOWS_QUALITIES = "/qualities";
    //public static final String GET_SHOWS_LANGUAGES = "/languages";
    public static final String GET_SHOWS_VIDEO = "/video/";
    public static final String RATE = "/rate/";
    public static final String MAY_ALSO_LIKE = "https://api.noco.tv:443/" + API_VERSION + "/shows/you_may_also_like/";
    public static final String MAL_ELM_PER_PAGE = "elements_per_page=4&page=0";
    public static final String GET_SHOWS_RANDOM = "https://api.noco.tv:443/" + API_VERSION + "/shows/rand?page=0&elements_per_page=";
    //public static final String MARK_READ = "/mark_read";
    public static final String FIND_SHOWS_BY_ID = GET_SHOWS + "by_id/";
    //public static final String GET_SHOWS_BEFORE = "https://api.noco.tv:443/" + API_VERSION + "/shows/before/";

    //public static final String GET_FAMILIES = "https://api.noco.tv:443/" + API_VERSION + "/families/";
    //public static final String FIND_FAMILIES_BY_ID = GET_FAMILIES + "by_id/";

    public static final String GET_SHOWS_UNREAD_ONLY = GET_SHOWS + "unread_only?" + ELEMENT_PER_PAGE;
    public static final String GET_SHOWS_READ_ONLY = GET_SHOWS + "read_only?" + ELEMENT_PER_PAGE;
    public static final String FIND_SHOWS_RECOMMENDED = GET_SHOWS + "user_recommendations?" + ELEMENT_PER_PAGE;
    public static final String FIND_MOST_POPULAR = GET_SHOWS + "most_popular?period=";
    public static final String FIND_SHOWS_PAID = GET_SHOWS + "paid?" + ELEMENT_PER_PAGE;

    public static final String SEARCH = "https://api.noco.tv:443/" + API_VERSION + "/search?query=";

    public static final String USER_LIST = "https://api.noco.tv:443/" + API_VERSION + "/users/queue_list";
    public static final String FAVORITES_LIST = "https://api.noco.tv:443/" + API_VERSION + "/users/favorites";
    public static final String WEB_OFF = "file:///android_asset/off.html";
    public static final String SHARE_URL = "https://noco.tv/emission/";
    public static final String NOCONOCO_URL = "https://play.google.com/store/apps/details?id=com.bbproject.noconoco";

    public static final String TWITCH_URL = "http://usher.justin.tv/api/channel/hls/nolife.m3u8?allow_source=true&segment_preference=4&player=twitchweb&p=9864018&token=";
    public static final String TWITCH_LOGIN_URL = "http://www.twitchapps.com/tmi/";
    public static final String TWITCH_SERVER_URL = "irc.twitch.tv";
    public static final String TWITCH_CHANNEL = "#nolife";
    public static final int TWITCH_PORT = 6667;
}
