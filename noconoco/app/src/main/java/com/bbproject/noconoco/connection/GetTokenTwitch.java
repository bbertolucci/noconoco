package com.bbproject.noconoco.connection;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import com.bbproject.noconoco.constants.Constants;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.zip.GZIPInputStream;

public class GetTokenTwitch extends AsyncTask<String, Void, String> {

    private static final String TAG = "GetTokenTwitch";

    private final WeakReference<AsyncResponse> mActivity;
    private final int mNextStep;

    @SuppressWarnings("unchecked")
    public GetTokenTwitch(AsyncResponse pAsync, int pNextStep) {
        mActivity = new WeakReference<>(pAsync);
        mNextStep = pNextStep;
    }

    @SuppressLint("NewApi")
    protected String doInBackground(String... urls) {

        try {
            HttpURLConnection urlConnection = CustomTwichUrlConnection.getUrlConnection(Constants.POST_TWITCH_TOKEN_URL, mActivity.get().getContext());
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            urlConnection.setRequestProperty("Accept-Encoding", "gzip");
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(false);

            StringBuilder result_string = new StringBuilder();
            String str;
            String ret;

            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = null;
                try {
                    InputStream in = urlConnection.getInputStream();

                    if ("gzip".equals(urlConnection.getContentEncoding())) {
                        br = new BufferedReader(new InputStreamReader(new GZIPInputStream(in), "UTF-8"));
                    } else {
                        br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    }
                    while (null != (str = br.readLine())) {
                        result_string.append(str).append("\r\n");
                    }
                    in.close();

                    ret = result_string.toString();
                } finally {
                    if (null != br) {
                        br.close();
                    }
                }
            } else {
                BufferedReader br = null;
                try {
                    InputStream in = urlConnection.getErrorStream();

                    if ("gzip".equals(urlConnection.getContentEncoding())) {
                        br = new BufferedReader(new InputStreamReader(new GZIPInputStream(in), "UTF-8"));
                    } else {
                        br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    }
                    while (null != (str = br.readLine())) {
                        result_string.append(str).append("\r\n");
                    }
                    br.close();
                    in.close();
                    ret = "NG:::" + urlConnection.getResponseCode();
                    Log.d(TAG, ret + " " + result_string);
                } finally {
                    if (null != br) {
                        br.close();
                    }
                }
            }

            return ret;
        } catch (Exception e) {
            return "NG:::" + e.toString();
        }
    }

    protected void onPostExecute(String pReturn) {
        mActivity.get().processFinish(pReturn, mNextStep);
    }
}