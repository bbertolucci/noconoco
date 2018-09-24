package com.bbproject.noconoco.utils;

import android.content.Context;

import com.bbproject.noconoco.constants.Constants;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;

import java.util.List;

public class CastOptionsProvider implements OptionsProvider {

    @Override
    public CastOptions getCastOptions(Context pContext) {
        return new CastOptions.Builder()
                .setReceiverApplicationId(Constants.CHROMECAST_APP_ID)
                .build();
    }

    @Override
    public List<SessionProvider> getAdditionalSessionProviders(Context pContext) {
        return null;
    }
}
