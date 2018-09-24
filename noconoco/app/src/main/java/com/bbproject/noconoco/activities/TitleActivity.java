package com.bbproject.noconoco.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.bbproject.noconoco.R;
import com.bbproject.noconoco.constants.SettingConstants;
import com.bbproject.noconoco.utils.SettingUtils;

/**
 * TitleActivity is the start screen
 */
public class TitleActivity extends AppCompatActivity {

    private static final String TAG = "TitleActivity";
    private final SettingUtils mSettings = new SettingUtils(this);

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);

        // Check for a refresh token
        String refreshToken = mSettings.getString(SettingConstants.REFRESH_TOKEN, "");
        if (!"".equals(refreshToken)) {
            Log.d(TAG, "Refresh token:" + refreshToken);
            // Refresh token exist so we start the main activity and end this one
            Intent intent = new Intent(TitleActivity.this, MainActivity.class);
            intent.putExtra("connected", true);
            startActivity(intent);
            finish();
        } else {
            // No refresh token found
            // Set the view
            setContentView(R.layout.activity_title);

            // Define click action on connect button
            findViewById(R.id.connect).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Start the main activity and end this one
                    Intent intent = new Intent(TitleActivity.this, MainActivity.class);
                    intent.putExtra("connected", true);
                    startActivity(intent);
                    finish();
                }
            });

            // Define click action on register button
            findViewById(R.id.abo).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Launch an external browser to create an account on the official website
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://noco.tv/abonnement"));
                    startActivity(browserIntent);
                }
            });

            // Define click action on discover button
            findViewById(R.id.discover).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Start the main activity with a non required connection argument and end this one
                    Intent intent = new Intent(TitleActivity.this, MainActivity.class);
                    intent.putExtra("connected", false);
                    startActivity(intent);
                    finish();
                }
            });

            // Display the Package Version and add a click action to redirect to Google play store.
            try {
                String versionName = "v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                TextView textView = findViewById(R.id.version);
                textView.setText(versionName);
                textView.setClickable(true);
                textView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    }
                });
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Package Name not found : " + getPackageName());
            }

            // Define a click action on the about button
            findViewById(R.id.about).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Open the AboutActivity over TitleActivity (Should be a popup like)
                    Intent configIntent = new Intent(TitleActivity.this, AboutActivity.class);
                    startActivity(configIntent);
                }
            });
        }
    }
}
