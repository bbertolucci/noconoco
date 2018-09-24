package com.bbproject.noconoco.connection;

import android.annotation.SuppressLint;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

class DisabledHostnameVerifier implements HostnameVerifier {
    @SuppressLint("BadHostnameVerifier")
    @Override
    public boolean verify(String s, SSLSession sslSession) {
        return true;
    }
}
