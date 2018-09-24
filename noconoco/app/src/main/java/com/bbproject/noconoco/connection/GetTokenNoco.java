package com.bbproject.noconoco.connection;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.bbproject.noconoco.constants.Constants;
import com.bbproject.noconoco.debug.DbgMode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.zip.GZIPInputStream;

import static com.bbproject.noconoco.constants.Constants.DEMO_MODE;

public class GetTokenNoco extends AsyncTask<String, Void, String> {

    private static final String TAG = "GetTokenNoco";
    private final WeakReference<AsyncResponse> mActivity;
    private final String mCode;
    private final boolean mRefresh;
    private final int mNextStep;

    public GetTokenNoco(String pCode, boolean pRefresh, AsyncResponse pAsync, int pNextStep) {
        mActivity = new WeakReference<>(pAsync);
        mRefresh = pRefresh;
        mCode = pCode;
        mNextStep = pNextStep;
    }

    @SuppressLint("NewApi")
    protected String doInBackground(String... urls) {
        if (DEMO_MODE) {
            return DbgMode.getToken(mActivity.get().getContext(), mNextStep);
        } else {
            try {

                HttpURLConnection urlConnection = CustomNocoUrlConnection.getUrlConnection(Constants.POST_TOKEN_URL, mActivity.get().getContext());
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                urlConnection.setRequestProperty("Accept-Encoding", "gzip");

                String base64EncodedCredentials = "Basic " + Base64.encodeToString(
                        (Constants.CLIENT_ID + ":" + Constants.CLIENT_SECRET).getBytes(),
                        Base64.NO_WRAP);
                urlConnection.setRequestProperty("Authorization", base64EncodedCredentials);
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                String postD = null;

                if (!"".equals(mCode)) {
                    if (!mRefresh) {
                        postD = "{\"grant_type\":\"authorization_code\",\"code\":\"" + mCode + "\"}";
                    } else {
                        postD = "{\"grant_type\":\"refresh_token\",\"refresh_token\":\"" + mCode + "\"}";
                    }
                } else {
                    postD = "{\"grant_type\":\"client_credentials\"}";
                }
                urlConnection.setRequestProperty("Content-Length", Integer.toString(postD.length()));
                OutputStream os = urlConnection.getOutputStream();
                OutputStreamWriter out = new OutputStreamWriter(os, "UTF-8");
                out.write(postD);
                out.flush();
                out.close();

                StringBuilder result_string = new StringBuilder();
                String str = null;
                String ret = null;

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
                        //{"code":"UNAUTHORIZED","error":"Unauthorized access","description":"OAuth2 is required."}
                        if ('{' == ret.charAt(0)) {
                            JSONObject root_object = new JSONObject(result_string.toString());
                            if (root_object.has("code")) {
                                String code = root_object.getString("code");
                                if ("UNAUTHORIZED".equals(code)) {
                                    Log.d(TAG, "NG:UnAuthorized Access:" + result_string);
                                }
                            }
                        }
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
                        in.close();
                        ret = "NG:Server Error [" + urlConnection.getResponseCode() + "] : " + result_string;
                        Log.d(TAG, ret);
                    } finally {
                        if (null != br) {
                            br.close();
                        }
                    }
                }

                return ret;
            } catch (JSONException e) {
                return "NG:JSONException:" + e.toString();
            } catch (IOException e) {
                return "NG:IOException:" + e.toString();
            }
        }
    }

    protected void onPostExecute(String pReturn) {
        mActivity.get().processFinish(pReturn, mNextStep);
    }
}