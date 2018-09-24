package com.bbproject.noconoco.connection;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class ConnectTwitch {

    private static final String TAG = "ConnectTwitch";

    private final MyHandler mHandler;

    private ConnectTwitch(String pMethod, Map<String, String> pMapParam, String pTokenType, Context pContext, String pUrl, int pNextStep, int pRetry, AsyncResponse pAsync) {
        Log.d(TAG, "Request start" + pNextStep);
        Log.d(TAG, "url: " + pUrl);
        mHandler = new MyHandler(pAsync, pMethod, pMapParam, pTokenType, pContext, pUrl, pNextStep, pRetry);
    }

    public ConnectTwitch(String pMethod, Map<String, String> pMapParam, String pTokenType, Object pObject, String pUrl, int pNextStep, int pRetry, int pDelayed) {
        this(pMethod, pMapParam, pTokenType, (Context) pObject, pUrl, pNextStep, pRetry, (AsyncResponse) pObject);
        mHandler.sendMessageDelayed(Message.obtain(mHandler, 2, ""), pDelayed);
    }

    private static class MyHandler extends Handler {
        private final WeakReference<AsyncResponse> mRef;
        private final WeakReference<Context> mContext;

        private final String mTokenType;
        private final String mUrl;
        private final int mNextStep;
        private final String mMethod;
        private final Map<String, String> mMapParam;
        private final int mRetry;
        private boolean mError;

        private MyHandler(AsyncResponse pAsyncResponse, String pMethod, Map<String, String> pMapParam, String pTokenType, Context pContext, String pUrl, int pNextStep, int pRetry) {
            mRef = new WeakReference<>(pAsyncResponse);
            mTokenType = pTokenType;
            mContext = new WeakReference<>(pContext);
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
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String result_string;
                        Log.d(TAG, "Request start2" + mNextStep);
                        HttpURLConnection urlConnection = CustomTwichUrlConnection.getUrlConnection(mUrl, mContext.get());
                        urlConnection.setRequestMethod(mMethod);
                        urlConnection.setRequestProperty("Accept-Encoding", "gzip");
                        urlConnection.setRequestProperty("Content-Type", "application/json");
                        if ("PUT".equals(mMethod)) {
                            urlConnection.setDoOutput(true);
                            DataOutputStream outputStream = new DataOutputStream(urlConnection.getOutputStream());
                            outputStream.writeBytes(mMapParam.get("body"));
                            outputStream.flush();
                            outputStream.close();
                        }
                        if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            BufferedReader reader = null;
                            try {
                                InputStream in = urlConnection.getInputStream();
                                if (null != urlConnection.getContentEncoding() && "gzip".equalsIgnoreCase(urlConnection.getContentEncoding())) {
                                    reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(in), "ISO-8859-1"));
                                } else {
                                    reader = new BufferedReader(new InputStreamReader(in, "ISO-8859-1"));
                                }
                                StringBuilder sb = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    sb.append(line).append("\r\n");
                                }
                                result_string = sb.toString();
                                in.close();
                            } finally {
                                if (reader != null) {
                                    reader.close();
                                }
                            }

                            try {
                                if ('[' == result_string.charAt(0)) {
                                    JSONObject root_object = new JSONObject(result_string);
                                    if (root_object.has("code")) {
                                        String code = root_object.getString("code");
                                        if ("UNAUTHORIZED".equals(code)) {
                                            Log.e(TAG, "NG:::" + result_string);
                                            mError = true;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                //Pas forcement une erreur
                            }
                        } else {
                            mError = true;
                            result_string = "NG:::" + urlConnection.getResponseCode();
                            Log.d(TAG, result_string);
                        }
                        urlConnection.disconnect();
                        sendMessage(Message.obtain(MyHandler.this, 1, result_string));

                    } catch (Exception e) {
                        mError = true;
                        Log.e(TAG, "NG:::An Exception occured !", e);

                        sendMessage(Message.obtain(MyHandler.this, 1, "NG:::" + e.toString()));
                    }
                }
            });
            thread.start();
        }

        private void onPostExecute(String pReturn) {
            if (!mError) {
                mRef.get().processFinish(pReturn, mNextStep);
            } else {
                if (mRetry < 3) {
                    Log.d(TAG, "Retry request");
                    mRef.get().processRetry(mMethod, null, mTokenType, mUrl, mNextStep, mRetry + 1);
                } else {
                    Log.d(TAG, "Restart");
                    mRef.get().restart(mNextStep);
                }
            }
        }
    }
}