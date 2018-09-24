package com.bbproject.noconoco.connection;

import android.content.Context;

import java.util.Map;

public interface AsyncResponse {
    void processFinish(String pResponse, int pNextStep);

    void processRetry(String pMethod, Map<String, String> pMapParam, String pTokenType, /*Object pObject,*/ String pUrl, int pNextStep, int pRetry);

    void restart(int pNextStep);

    Context getContext();

}
