package com.bbproject.noconoco.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bbproject.noconoco.R;

/**
 * AboutActivity allows user to check the package version
 */
public class AboutActivity extends AppCompatActivity {

    private final static String TAG = "AboutActivity";

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);

        // Set the view
        setContentView(R.layout.activity_about);

        // Set the click action on close button
        ImageButton vCloseButton = findViewById(R.id.close);
        vCloseButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Close the Activity
                AboutActivity.this.finish();
            }
        });

        // Set the The Textview with Package Name and Version
        try {
            String versionName = "v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            TextView textView = findViewById(R.id.version);
            textView.setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "NameNotFoundException", e);
        }

        // Set a click action on the Logo ImageView
        ImageView logo = findViewById(R.id.logo);
        logo.setClickable(true);
        logo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Open the website in the default browser
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://noco.tv"));
                startActivity(browserIntent);
            }
        });
    }
}
