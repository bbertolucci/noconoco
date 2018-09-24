package com.bbproject.noconoco.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.bbproject.noconoco.R;

import java.util.Locale;

/**
 * HelpActivity allows user to check the help page
 */
public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);

        // Set the view
        setContentView(R.layout.activity_help);

        // Set the click action on close button
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                HelpActivity.this.finish();
            }
        });

        // Set the webview configuration and load the page locally
        WebView webview = findViewById(R.id.contenttext);
        webview.setBackgroundColor(0);
        webview.setVerticalScrollBarEnabled(true);
        webview.setVerticalScrollbarOverlay(true);
        String lang = Locale.getDefault().getLanguage();
        if (!"fr".equals(lang)) lang = "en";
        webview.loadUrl("file:///android_res/raw/help_" + lang + ".html");
        webview.setWebViewClient(new MyWebViewClient());
    }

    /**
     * Custom webview client
     */
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView pView, String pUrl) {
            if (pUrl.contains("conditions-generales") || pUrl.contains("forum.nolife-tv.com")) {
                // Leave webview and use browser
                return super.shouldOverrideUrlLoading(pView, pUrl);
            } else {
                // Stay within this webview and load url
                pView.loadUrl(pUrl);
                return true;
            }
        }
    }
}