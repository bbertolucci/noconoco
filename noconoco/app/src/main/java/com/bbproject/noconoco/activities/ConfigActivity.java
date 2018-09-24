package com.bbproject.noconoco.activities;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;

import com.bbproject.noconoco.R;
import com.bbproject.noconoco.adapters.SpinnerBaseAdapter;
import com.bbproject.noconoco.constants.SettingConstants;
import com.bbproject.noconoco.utils.SettingUtils;
import com.bbproject.noconoco.custom.view.SquareLayout;

/**
 * ConfigActivity allows user to change the profile default value :
 * - Video quality
 * - Language
 * - Subtitle language
 */
public class ConfigActivity extends AppCompatActivity {

    private final SettingUtils mSettings = new SettingUtils(this);

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);

        // Set the View
        setContentView(R.layout.activity_config);

        // Set the click action on close button
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Close the Activity
                ConfigActivity.this.finish();
            }
        });

        // Define click action on the Quality Button
        final SquareLayout squareLQ = findViewById(R.id.LQ);
        final SquareLayout squareMQ = findViewById(R.id.MQ);
        final SquareLayout squareTV = findViewById(R.id.TV);
        final SquareLayout squareHD = findViewById(R.id.HD);
        final SquareLayout squareFHD = findViewById(R.id.FHD);

        squareLQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define new setting value
                mSettings.setString(SettingConstants.QUALITY_MOBILE, "LQ");
                squareLQ.setSelected(true);
                squareMQ.setSelected(false);
                squareTV.setSelected(false);
                squareHD.setSelected(false);
                squareFHD.setSelected(false);
            }
        });
        squareMQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define new setting value
                mSettings.setString(SettingConstants.QUALITY_MOBILE, "MQ");
                squareLQ.setSelected(false);
                squareMQ.setSelected(true);
                squareTV.setSelected(false);
                squareHD.setSelected(false);
                squareFHD.setSelected(false);
            }
        });
        squareTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define new setting value
                mSettings.setString(SettingConstants.QUALITY_MOBILE, "TV");
                squareLQ.setSelected(false);
                squareMQ.setSelected(false);
                squareTV.setSelected(true);
                squareHD.setSelected(false);
                squareFHD.setSelected(false);
            }
        });
        squareHD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define new setting value
                mSettings.setString(SettingConstants.QUALITY_MOBILE, "HD_720");
                squareLQ.setSelected(false);
                squareMQ.setSelected(false);
                squareTV.setSelected(false);
                squareHD.setSelected(true);
                squareFHD.setSelected(false);
            }
        });
        squareFHD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSettings.setString(SettingConstants.QUALITY_MOBILE, "HD_1080");
                squareLQ.setSelected(false);
                squareMQ.setSelected(false);
                squareTV.setSelected(false);
                squareHD.setSelected(false);
                squareFHD.setSelected(true);
            }
        });

        // Get Current setting for quality
        String quality = mSettings.getString(SettingConstants.QUALITY_MOBILE, "TV");

        // Highlight button depending on quality
        switch (quality) {
            case "LQ":
                squareLQ.setSelected(true);
                break;
            case "HQ":
                squareMQ.setSelected(true);
                break;
            case "TV":
                squareTV.setSelected(true);
                break;
            case "HD_720":
                squareHD.setSelected(true);
                break;
            case "HD_1080":
                squareFHD.setSelected(true);
                break;
        }

        // Get Current setting for lang
        String lang = mSettings.getString(SettingConstants.AUDIO_LANGUAGE, "fr");

        // Set textBtn value
        final TextView langText = findViewById(R.id.textLang);
        final String[] data = getResources().getStringArray(R.array.lang_array);
        final String[] value = getResources().getStringArray(R.array.lang_value_array);
        langText.setText(data[0]);
        int count = value.length;
        for (int i = 0; i < count; i++) {
            if (value[i].equals(lang)) {
                langText.setText(data[i]);
                break;
            }
        }

        findViewById(R.id.btnLang).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String lang = mSettings.getString(SettingConstants.AUDIO_LANGUAGE, "fr");

                // Get position for the current lang in the list
                int pos = 0;
                int count = value.length;
                for (int i = 0; i < count; i++) {
                    if (value[i].equals(lang)) {
                        pos = i;
                        break;
                    }
                }
                final SpinnerBaseAdapter vSpinnerAdapter = new SpinnerBaseAdapter(ConfigActivity.this, data, pos);

                final AlertDialog dialog = new AlertDialog.Builder(ConfigActivity.this).setAdapter(vSpinnerAdapter, null).create();
                if (null != dialog.getWindow())
                    dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
                dialog.getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
                dialog.getListView().setSelection(pos);
                dialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        vSpinnerAdapter.setSelectedPosition(position);
                        langText.setText(data[position]);
                        // Set the user setting value
                        mSettings.setString(SettingConstants.AUDIO_LANGUAGE, value[position]);

                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        // Get Current setting for subtitle
        String subtitle = mSettings.getString(SettingConstants.SUB_LANGUAGE, "");

        // Set textBtn value
        final TextView subtitleText = findViewById(R.id.textSubtitle);
        final String[] dataSub = getResources().getStringArray(R.array.subtitle_array);
        final String[] valueSub = getResources().getStringArray(R.array.subtitle_value_array);

        subtitleText.setText(dataSub[0]);
        count = valueSub.length;
        for (int i = 0; i < count; i++) {
            if (valueSub[i].equals(subtitle)) {
                subtitleText.setText(dataSub[i]);
                break;
            }
        }

        findViewById(R.id.btnSubtitle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String subtitle = mSettings.getString(SettingConstants.SUB_LANGUAGE, "");

                // Get position for the current lang in the list
                int pos = 0;
                int vCount = valueSub.length;
                for (int i = 0; i < vCount; i++) {
                    if (valueSub[i].equals(subtitle)) {
                        pos = i;
                        break;
                    }
                }
                final SpinnerBaseAdapter spinnerAdapter = new SpinnerBaseAdapter(ConfigActivity.this, dataSub, pos);

                final AlertDialog dialog = new AlertDialog.Builder(ConfigActivity.this).setAdapter(spinnerAdapter, null).create();
                if (null != dialog.getWindow())
                    dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
                dialog.getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
                dialog.getListView().setSelection(pos);
                dialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        spinnerAdapter.setSelectedPosition(position);
                        subtitleText.setText(dataSub[position]);
                        // Set the user setting value
                        mSettings.setString(SettingConstants.SUB_LANGUAGE, valueSub[position]);

                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
    }
}
