package com.bbproject.noconoco.connection;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bbproject.noconoco.constants.SettingConstants;
import com.bbproject.noconoco.debug.DbgMode;
import com.bbproject.noconoco.utils.SettingUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static com.bbproject.noconoco.constants.Constants.DEMO_MODE;

public class ConnectNoco {

    private static final String TAG = "ConnectNoco";

    private final MyHandler mHandler;

    private ConnectNoco(String pMethod, Map<String, String> pMapParam, String pTokenType, AsyncResponse pAsync, String pUrl, int pNextStep, int pRetry) {
        Log.d(TAG, "Request start" + pNextStep);
        Log.d(TAG, "url: " + pUrl);
        mHandler = new MyHandler(pAsync, pMethod, pMapParam, pTokenType, pUrl, pNextStep, pRetry);
    }

    public ConnectNoco(String pMethod, Map<String, String> pMapParam, String pTokenType, AsyncResponse pAsync, String pUrl, int pNextStep, int pRetry, int pDelayed) {
        this(pMethod, pMapParam, pTokenType, pAsync, pUrl, pNextStep, pRetry);
        mHandler.sendMessageDelayed(Message.obtain(mHandler, 2, ""), pDelayed);
    }

    public ConnectNoco(String pMethod, Map<String, String> pMapParam, String pTokenType, AsyncResponse pAsync, String pUrl, int pNextStep) {
        this(pMethod, pMapParam, pTokenType, pAsync, pUrl, pNextStep, 0);
        mHandler.sendMessage(Message.obtain(mHandler, 2, ""));
    }

    private static class MyHandler extends Handler {
        private final WeakReference<AsyncResponse> mRef;

        private final String mTokenType;
        private final String mUrl;
        private final int mNextStep;
        private final String mMethod;
        private final Map<String, String> mMapParam;
        private final int mRetry;
        private boolean mError;

        private MyHandler(AsyncResponse pAsyncResponse, String pMethod, Map<String, String> pMapParam, String pTokenType, /*Context pContext,*/ String pUrl, int pNextStep, int pRetry) {
            mRef = new WeakReference<>(pAsyncResponse);
            mTokenType = pTokenType;
            mUrl = pUrl;
            mNextStep = pNextStep;
            mRetry = pRetry;
            mMethod = pMethod;
            mMapParam = pMapParam;
            mError = false;
        }

        @Override
        public void handleMessage(Message pMsg) {
            switch (pMsg.what) {
                case 1:
                    String is = (String) pMsg.obj;
                    onPostExecute(is);
                    break;
                case 2:
                    startConnect();
                    break;
            }
        }

        private void startConnect() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (DEMO_MODE) {
                        String result_string = DbgMode.simulateAnswer(mRef.get().getContext(), mNextStep, mUrl, mMethod, mMapParam);
                        sendMessage(Message.obtain(MyHandler.this, 1, result_string));
                    } else {
                        try {

                            HttpURLConnection urlConnection = CustomNocoUrlConnection.getUrlConnection(mUrl, mRef.get().getContext());
                            urlConnection.setRequestMethod(mMethod);
                            urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                            urlConnection.setRequestProperty("Accept-Encoding", "gzip");

                            StringBuilder result_string = new StringBuilder();

                            SettingUtils settings = new SettingUtils(mRef.get().getContext());
                            String accessToken = settings.getString(SettingConstants.TOKEN, "");
                            urlConnection.setRequestProperty("Authorization", "Bearer " + accessToken);
                            urlConnection.setChunkedStreamingMode(0);
                            urlConnection.setDoInput(true);
                            if ("PUT".equals(mMethod)) {
                                urlConnection.setDoOutput(true);
                                OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
                                out.write(mMapParam.get("body"));
                                out.flush();
                                out.close();
                            }

                            BufferedReader reader = null;
                            String str = null;

                            try {

                                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                    InputStream in = urlConnection.getInputStream();
                                    if (null != urlConnection.getContentEncoding() && "gzip".equals(urlConnection.getContentEncoding())) {
                                        reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(in), "ISO-8859-1"));//"UTF-8"
                                    } else {
                                        reader = new BufferedReader(new InputStreamReader(in, "ISO-8859-1"));//"UTF-8"
                                    }
                                    while (null != (str = reader.readLine())) {
                                        result_string.append(str).append("\r\n");
                                    }
                                    in.close();

                                    //{"code":"UNAUTHORIZED","error":"Unauthorized access","description":"OAuth2 is required."}
                                    if ('{' == result_string.charAt(0)) {
                                        JSONObject root_object = new JSONObject(result_string.toString());
                                        if (root_object.has("code")) {
                                            String code = root_object.getString("code");
                                            if ("UNAUTHORIZED".equals(code)) {
                                                Log.e(TAG, "NG:UnAuthorized Access:" + result_string);
                                                mError = true;
                                            }
                                        }
                                    }
                                } else {
                                    InputStream in = urlConnection.getErrorStream();
                                    if ("gzip".equals(urlConnection.getContentEncoding())) {
                                        reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(in), "UTF-8"));
                                    } else {
                                        reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                                    }
                                    while (null != (str = reader.readLine())) {
                                        result_string.append(str).append("\r\n");
                                    }
                                    in.close();
                                    mError = true;
                                    Log.e(TAG, "NG:Server Error [" + urlConnection.getResponseCode() + "] : " + result_string);

                                }
                            } finally {
                                if (reader != null) {
                                    reader.close();
                                }
                            }

                            sendMessage(Message.obtain(MyHandler.this, 1, result_string.toString()));
                        } catch (JSONException e) {
                            mError = true;
                            Log.e(TAG, "NG:JSONException:", e);

                            sendMessage(Message.obtain(MyHandler.this, 1, "NG:JSONException:" + e.toString()));

                        } catch (IOException e) {
                            mError = true;
                            Log.e(TAG, "NG:IOException:", e);

                            sendMessage(Message.obtain(MyHandler.this, 1, "NG:IOException:" + e.toString()));

                        }
                    }
                }
            }).start();
        }

        private void onPostExecute(String pReturn) {
            if (!mError) {
                Log.d(TAG, "Request end :" + mNextStep);
                mRef.get().processFinish(pReturn, mNextStep);
            } else {
                if (mRetry < 3) {
                    Log.d(TAG, "Retry [" + (mRetry + 1) + "] request :" + mNextStep);
                    mRef.get().processRetry(mMethod, null, mTokenType, mUrl, mNextStep, mRetry + 1);
                } else {
                    Log.d(TAG, "Restart due to multiple error on :" + mNextStep);
                    mRef.get().restart(mNextStep);
                }
            }
        }
    }
}
