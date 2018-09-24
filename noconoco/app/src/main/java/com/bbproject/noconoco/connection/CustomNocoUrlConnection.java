package com.bbproject.noconoco.connection;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.security.ProviderInstaller;

import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

class CustomNocoUrlConnection {
    private static final String TAG = "CustomNocoUrlConnection";
    private static final int CONNECT_TIMEOUT = 16000;
    private static final int READ_TIMEOUT = 16000;

    public static HttpURLConnection getUrlConnection(String pUrl, Context pContext) {
        HttpURLConnection urlConnection = null;
        try {
            System.setProperty("http.keepAlive", "false");
            System.setProperty("java.net.preferIPv4Stack", "true");

            System.setProperty("sun.net.client.defaultConnectTimeout", CONNECT_TIMEOUT + "");
            System.setProperty("sun.net.client.defaultReadTimeout", READ_TIMEOUT + "");

            URL urlObj = new URL(pUrl);
            if (pUrl.startsWith("https")) {
                SSLContext ctx = SSLContext.getInstance("TLSv1");
                try {
                    ProviderInstaller.installIfNeeded(pContext);
                } catch (Exception ignored) {
                }
                ctx.init(null, new TrustManager[]{new TrustEverythingManager()}, new SecureRandom());
                ctx.createSSLEngine();

                HttpsURLConnection httpsUrlConnection = (HttpsURLConnection) urlObj.openConnection();
                SSLSocketFactory noSSLv3Factory = new NoSSLv3SocketFactory(ctx.getSocketFactory());
                HttpsURLConnection.setDefaultSSLSocketFactory(noSSLv3Factory);
                httpsUrlConnection.setSSLSocketFactory(noSSLv3Factory);
                httpsUrlConnection.setHostnameVerifier(new DisabledHostnameVerifier());
                urlConnection = httpsUrlConnection;
            } else {
                urlConnection = (HttpURLConnection) urlObj.openConnection();
            }
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.89 Safari/537.1");
            urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
            urlConnection.setReadTimeout(READ_TIMEOUT);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Accept-Charset", "UTF-8");
            urlConnection.setRequestProperty("Connection", "close");

        } catch (Exception e) {
            Log.e(TAG, "An Exception occurred", e);
        }
        return urlConnection;
    }
}
