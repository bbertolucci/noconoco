package com.bbproject.noconoco.debug;

import android.content.Context;
import android.util.Log;

import com.bbproject.noconoco.R;
import com.bbproject.noconoco.constants.CategoryType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Random;

public class DbgMode {
    private static final Random RANDOM = new Random();
    private static final String TAG = "DbgMode";

    public static String simulateAnswer(Context pContext, int pNextStep, String pUrl, String pMethod, Map<String, String> pMapParam) {
        Log.d(TAG, "Params:" + pNextStep + "," + pUrl + "," + pMethod + "," + (pMapParam != null ? pMapParam.toString() : null));
        String ret = "";
        switch (pNextStep) {
            case CategoryType.TOKEN:
            case CategoryType.REFRESH_TOKEN:
            case CategoryType.TOKEN_NO_USER: {
                ret = "{\"access_token\":\"fake_token\",\"refresh_token\":\"fake_token\"}";
                break;
            }
            case CategoryType.ABO_LIST: {
                ret = readDemoFile(pContext, R.raw.demo_partner);
                break;
            }
            case CategoryType.FAM_LIST: {
                ret = readDemoFile(pContext, R.raw.demo_fam);
                break;
            }
            case CategoryType.USERS_INIT: {
                ret = readDemoFile(pContext, R.raw.demo_user_init);
                break;
            }
            case CategoryType.LAST_NEWS: {
                ret = readDemoFile(pContext, R.raw.demo_show);
                break;
            }
            case CategoryType.FREE_SHOWS: {
                ret = readDemoFile(pContext, R.raw.demo_show);
                break;
            }
            case CategoryType.SHOW_BY_PARTNER_REFRESH:
            case CategoryType.SHOW_BY_PARTNER: {
                ret = readDemoFile(pContext, R.raw.demo_partner_show);
                String[] urlPart = pUrl.split("=");
                ret = ret.replaceAll("XXX", urlPart[urlPart.length - 1]);
                break;
            }
            case CategoryType.SHOW_BY_FAMILY_REFRESH:
            case CategoryType.SHOW_BY_FAMILY: {
                ret = readDemoFile(pContext, R.raw.demo_fam_show);
                String[] urlPart = pUrl.split("=");
                ret = ret.replaceAll("XXX", urlPart[urlPart.length - 1]);
                break;
            }
            case CategoryType.MOST_POPULAR: {
                ret = readDemoFile(pContext, R.raw.demo_show);
                break;
            }
            case CategoryType.UNREAD: {
                ret = readDemoFile(pContext, R.raw.demo_show);
                break;
            }
            case CategoryType.READ: {
                ret = readDemoFile(pContext, R.raw.demo_show);
                break;
            }
            case CategoryType.USER_LIST:
            case CategoryType.USER_LIST_BIS: {
                ret = readDemoFile(pContext, R.raw.demo_queue);
                break;
            }
            case CategoryType.FAVORITES_LIST:
            case CategoryType.FAVORITES_LIST_BIS: {
                ret = readDemoFile(pContext, R.raw.demo_favorites);
                break;
            }
            case CategoryType.ADDDEL_USER_LIST:
            case CategoryType.ADDDEL_FAVORITES_LIST: {
                break;
            }
            case CategoryType.USER_PLAYLIST_INFO: {
                break;
            }
            case CategoryType.RECOMMENDED_LIST: {
                ret = readDemoFile(pContext, R.raw.demo_show);
                break;
            }
            case CategoryType.PAID_LIST: {
                ret = readDemoFile(pContext, R.raw.demo_show);
                break;
            }
            case CategoryType.SEARCH_REFRESH:
            case CategoryType.SEARCH: {
                ret = readDemoFile(pContext, R.raw.demo_search);
                break;
            }
            case CategoryType.SEARCH_INFO: {
                ret = readDemoFile(pContext, R.raw.demo_show);
                break;
            }
            case CategoryType.SEARCH_INFO_REFRESH: {
                ret = readDemoFile(pContext, R.raw.demo_show);
                break;
            }
            case CategoryType.LAST_NEWS_REFRESH: {
                ret = readDemoFile(pContext, R.raw.demo_show);
                break;
            }
            case CategoryType.MOST_POPULAR_REFRESH: {
                ret = readDemoFile(pContext, R.raw.demo_show);
                break;
            }
            case CategoryType.FREE_SHOWS_REFRESH: {
                ret = readDemoFile(pContext, R.raw.demo_show);
                break;
            }
            case CategoryType.UNREAD_REFRESH: {
                ret = readDemoFile(pContext, R.raw.demo_show);
                break;
            }
            case CategoryType.READ_REFRESH: {
                ret = readDemoFile(pContext, R.raw.demo_show);
                break;
            }
            case CategoryType.RECOMMENDED_LIST_REFRESH: {
                ret = readDemoFile(pContext, R.raw.demo_show);
                break;
            }
            case CategoryType.PAID_LIST_REFRESH: {
                ret = readDemoFile(pContext, R.raw.demo_show);
                break;
            }
            case CategoryType.FROM_WEB: {
                break;
            }
            case CategoryType.MEDIAS: {
                ret = readDemoFile(pContext, R.raw.demo_medias);
                //vReturn = "{\"fr\":{\"audio_lang\":{\"fr\":"fran\\u00e7ais\",\"en\":\"French\"},\"video_list\":{\"none\":{\"sub_lang\":{\"fr\":\"none\",\"en\":\"none\"},\"quality_list\":{\"LQ\":{\"id_show\":39144,\"quality_key\":\"LQ\",\"videobitrate\":500000,\"audiobitrate\":96000,\"framerate\":25,\"framecount\":11118,\"res_width\":512,\"res_lines\":288,\"duration\":444720,\"filesize\":33251049,\"mosaique\":\"2017-02-10 18:45:28\",\"modified\":\"2017-02-10 18:45:28\"},\"HQ\":{\"id_show\":39144,\"quality_key\":\"HQ\",\"videobitrate\":1479000,\"audiobitrate\":127976,\"framerate\":50,\"framecount\":22238,\"res_width\":688,\"res_lines\":384,\"duration\":444760,\"filesize\":89661603,\"mosaique\":\"2017-02-10 18:45:28\",\"modified\":\"2017-02-10 18:45:28\"},\"TV\":{\"id_show\":39144,"quality_key":"TV","videobitrate":2434000,"audiobitrate":127976,"framerate":50,"framecount":22238,"res_width":1024,"res_lines":576,"duration":444760,"filesize":142908633,"mosaique":"2017-02-10 18:45:28","modified":"2017-02-10 18:45:28"},"HD_720":{"id_show":39144,"quality_key":"HD_720","videobitrate":3406000,"audiobitrate":127976,"framerate":50,"framecount":22238,"res_width":1280,"res_lines":720,"duration":444760,"filesize":197368802,"mosaique":"2017-02-10 18:45:28","modified":"2017-02-10 18:45:28"}}}}}}"
                break;
            }
            case CategoryType.PLAY: {
                //vReturn = "{\"access_token\":\"fake_token\",\"refresh_token\":\"fake_token\"}";
                ret = "{";
                ret += "\"file\": \"android.resource://" + pContext.getPackageName() + "/" + DbgMode.getDemoVideo() + "\",";
                ret += "\"quality_key\": \"HD_720\",";
                ret += "\"is_abo\": 1,";
                ret += "\"cross_access\": 0,";
                ret += "\"quotafr_free\": 0,";
                ret += "\"user_free\": 1,";
                ret += "\"guest_free\": 1,";
                ret += "\"code_reponse\": 1";
                ret += "}";
                break;
            }
            case CategoryType.MAY_ALSO_LIKE: {
                ret = readDemoFile(pContext, R.raw.demo_show);
                break;
            }
            case CategoryType.RANDOM: {
                ret = readDemoFile(pContext, R.raw.demo_show);
                break;
            }
        }
        return ret;
    }

    private static String readDemoFile(Context pContext, int pResources) {
        String ret;
        try {
            InputStream stream = pContext.getResources().openRawResource(pResources);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            StringBuilder result_string = new StringBuilder();
            String str;
            while (null != (str = reader.readLine())) {
                result_string.append(str);
            }
            stream.close();
            ret = result_string.toString();
        } catch (IOException e) {
            ret = "{\"code\":\"ERROR\",\"error\":\"Error demo\",\"description\":\"IOException\"}";
        }
        return ret;
    }

    public static String getToken(Context pContext, int pNextStep) {
        return simulateAnswer(pContext, pNextStep, null, null, null);
    }

    public static int getDemoImage() {
        int rand = RANDOM.nextInt(22);
        switch (rand) {
            case 0:
                return R.raw.demo1;
            case 1:
                return R.raw.demo2;
            case 2:
                return R.raw.demo3;
            case 3:
                return R.raw.demo4;
            case 4:
                return R.raw.demo5;
            case 5:
                return R.raw.demo6;
            case 6:
                return R.raw.demo7;
            case 7:
                return R.raw.demo8;
            case 8:
                return R.raw.demo9;
            case 9:
                return R.raw.demo10;
            case 10:
                return R.raw.demo11;
            case 11:
                return R.raw.demo12;
            case 12:
                return R.raw.demo13;
            case 13:
                return R.raw.demo14;
            case 14:
                return R.raw.demo15;
            case 15:
                return R.raw.demo16;
            case 16:
                return R.raw.demo17;
            case 17:
                return R.raw.demo18;
            case 18:
                return R.raw.demo19;
            case 19:
                return R.raw.demo20;
            case 20:
                return R.raw.demo21;
            case 21:
                return R.raw.demo22;
        }
        return 0;
    }

    private static int getDemoVideo() {
        int rand = RANDOM.nextInt(12);
        switch (rand) {
            case 0:
                return R.raw.demov1;
            case 1:
                return R.raw.demov2;
            case 2:
                return R.raw.demov3;
            case 3:
                return R.raw.demov4;
            case 4:
                return R.raw.demov5;
            case 5:
                return R.raw.demov6;
            case 6:
                return R.raw.demov7;
            case 7:
                return R.raw.demov8;
            case 8:
                return R.raw.demov9;
            case 9:
                return R.raw.demov10;
            case 10:
                return R.raw.demov11;
            case 11:
                return R.raw.demov12;
        }
        return 0;
    }
}
