package com.bbproject.noconoco.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.bbproject.noconoco.constants.Constants;

//import android.util.Log;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;

//import java.util.ArrayList;
//import java.util.List;

public class SettingUtils {

    private final Context context;

    public SettingUtils(Context pContext) {
        this.context = pContext;
    }

    /*	public ArrayList<String> getStringList(String key)
        {
            String json_string = getString(key, "{\"array\": []}");
            try{
                JSONObject json = new JSONObject(json_string);
                JSONArray array = json.getJSONArray("array");
                int vSize = array.length();
                ArrayList<String> result = new ArrayList<>(vSize);
                for(int i = 0 ; i < vSize ; i++){
                    result.add(array.get(i).toString());
                }
                return result;
            }
            catch (JSONException e){
                Log.e("setting", "invalid json", e);
                return new ArrayList<String>();
            }
        }
        public void setStringList(String key, List<String> value)
        {
            try{
                JSONArray array = new JSONArray();
                for(String obj : value){
                    array.put(obj);
                }

                JSONObject json = new JSONObject();
                json.put("array", array);

                setString(key, json.toString());
            }
            catch (JSONException e){
                Log.e("setting", "invalid json", e);
            }
        }*/
    public static int convertDpToPixel(float dp, Context pContext) {
        Resources resources = pContext.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return Math.round(dp * (metrics.densityDpi / 160f));
    }

    private SharedPreferences openPreferences() {
        return context.getSharedPreferences(Constants.CLIENT_ID, Context.MODE_PRIVATE);
    }

    public String getString(String key, String def) {
        return openPreferences().getString(key, def);
    }

    public void setString(String key, String value) {
        SharedPreferences sp = openPreferences();
        Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public int getInteger(String key, Integer def) {
        return openPreferences().getInt(key, def);
    }

    public void setInteger(String key, Integer value) {
        SharedPreferences sp = openPreferences();
        Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key, Boolean value) {
        return openPreferences().getBoolean(key, value);
    }

    public void setBoolean(String key, Boolean value) {
        SharedPreferences sp = openPreferences();
        Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
}
