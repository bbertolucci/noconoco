package com.bbproject.noconoco.connection;

import android.annotation.SuppressLint;

//import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

class TrustEverythingManager implements X509TrustManager {
    @SuppressLint("TrustAllX509TrustManager")
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {// throws CertificateException {

    }

    @SuppressLint("TrustAllX509TrustManager")
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {// throws CertificateException {

    }

    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
