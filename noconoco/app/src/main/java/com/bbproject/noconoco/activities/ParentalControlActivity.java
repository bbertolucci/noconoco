package com.bbproject.noconoco.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.bbproject.noconoco.R;
import com.bbproject.noconoco.constants.SettingConstants;
import com.bbproject.noconoco.utils.SettingUtils;

/**
 * ParentalControlActivity allows user to add a code to protect kid for watching some inappropriate content
 */
public class ParentalControlActivity extends AppCompatActivity {

    private final SettingUtils mSettings = new SettingUtils(this);
    private boolean mCsa10 = false;
    private boolean mCsa12 = false;
    private boolean mCsa16 = false;
    private boolean mCsa18 = false;
    private ImageView mViewCsa10;
    private ImageView mViewCsa12;
    private ImageView mViewCsa16;
    private ImageView mViewCsa18;
    private EditText mEdit;
    private Button mOkButton;
    private Integer mCurrent = 99;
    private String mCurrentCode = "";
    private TextView mErrorMsg;

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);

        // Set the View
        setContentView(R.layout.activity_parental_control);

        // Set the click action on close button
        ImageButton closeButton = findViewById(R.id.close);
        closeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                hideKeyboard();
                // Return value to the parent activity
                Intent intent = new Intent();
                intent.putExtra("parental_control_level", mCurrent);
                setResult(RESULT_OK, intent);
                ParentalControlActivity.this.finish();
            }
        });

        // Set the CSA ImageView and click action
        mViewCsa10 = findViewById(R.id.csadraw1);
        mViewCsa12 = findViewById(R.id.csadraw2);
        mViewCsa16 = findViewById(R.id.csadraw3);
        mViewCsa18 = findViewById(R.id.csadraw4);

        mViewCsa10.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mCsa10) mCurrent = 99; // unset the parental filter
                else mCurrent = 10; // set the parental filter
                mCsa10 = !mCsa10;
                mCsa12 = mCsa10;
                mCsa16 = mCsa10;
                mCsa18 = mCsa10;
                okValidate();
                refreshButton();
            }
        });
        mViewCsa12.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mCsa12 && mCurrent == 12) {
                    // unset the parental filter
                    mCurrent = 99;
                    mCsa10 = false;
                    mCsa12 = false;
                    mCsa16 = false;
                    mCsa18 = false;
                } else {
                    // set the parental filter
                    mCurrent = 12;
                    mCsa10 = false;
                    mCsa12 = true;
                    mCsa16 = true;
                    mCsa18 = true;
                }
                okValidate();
                refreshButton();

            }
        });
        mViewCsa16.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mCsa16 && mCurrent == 16) {
                    // unset the parental filter
                    mCurrent = 99;
                    mCsa10 = false;
                    mCsa12 = false;
                    mCsa16 = false;
                    mCsa18 = false;
                } else {
                    // set the parental filter
                    mCurrent = 16;
                    mCsa10 = false;
                    mCsa12 = false;
                    mCsa16 = true;
                    mCsa18 = true;
                }
                okValidate();
                refreshButton();
            }
        });
        mViewCsa18.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mCsa18 && mCurrent == 18) {
                    // unset the parental filter
                    mCurrent = 99;
                    mCsa10 = false;
                    mCsa12 = false;
                    mCsa16 = false;
                    mCsa18 = false;
                } else {
                    // set the parental filter
                    mCurrent = 18;
                    mCsa10 = false;
                    mCsa12 = false;
                    mCsa16 = false;
                    mCsa18 = true;
                }
                okValidate();
                refreshButton();
            }
        });
        refreshButton();

        // Define a reference for the error_msg textview
        mErrorMsg = findViewById(R.id.error_msg);

        // Define action when user is typing a parental code
        mEdit = findViewById(R.id.parental_code);
        mEdit.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                okValidate();
                return false;
            }
        });

        // Define click action on the validate button
        mOkButton = findViewById(R.id.validateButton);
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!"".equals(mCurrentCode)) {
                    // If parental control is activated we compare the entered code with the registered one
                    if (mCurrentCode.equals(mEdit.getText().toString())) {
                        // Codes are identical, we disable the parental control
                        mCurrent = 99;
                        mSettings.setInteger(SettingConstants.PARENTAL_CONTROL, mCurrent);
                        mSettings.setString(SettingConstants.PARENTAL_CONTROL_PWD, "");
                        mErrorMsg.setVisibility(View.GONE);
                        mEdit.setText("");
                        mCurrentCode = "";
                    } else {
                        // Code entered was incorrect, we displayed an error messge
                        mErrorMsg.setVisibility(View.VISIBLE);
                    }
                } else {
                    // Activation of the parental control, all the video corresponding where age lower than mCurrent will be filtered on display
                    mSettings.setInteger(SettingConstants.PARENTAL_CONTROL, mCurrent);
                    mCurrentCode = mEdit.getText().toString();
                    mSettings.setString(SettingConstants.PARENTAL_CONTROL_PWD, mCurrentCode);
                    mEdit.setText("");
                }
                init();
            }
        });
        init();
    }

    /**
     * Data reset with default value
     */
    private void init() {

        mCsa10 = false;
        mCsa12 = false;
        mCsa16 = false;
        mCsa18 = false;

        mCurrent = mSettings.getInteger(SettingConstants.PARENTAL_CONTROL, 99);
        if (mCurrent != 99) {
            mCurrentCode = mSettings.getString(SettingConstants.PARENTAL_CONTROL_PWD, "-");
            if (mCurrent <= 10) mCsa10 = true;
            if (mCurrent <= 12) mCsa12 = true;
            if (mCurrent <= 16) mCsa16 = true;
            if (mCurrent <= 18) mCsa18 = true;
            mViewCsa10.setEnabled(false);
            mViewCsa12.setEnabled(false);
            mViewCsa16.setEnabled(false);
            mViewCsa18.setEnabled(false);
            mOkButton.setText(getString(R.string.disable));
        } else {
            mViewCsa10.setEnabled(true);
            mViewCsa12.setEnabled(true);
            mViewCsa16.setEnabled(true);
            mViewCsa18.setEnabled(true);
            mOkButton.setText(getString(R.string.enable));
        }
        refreshButton();
        okValidate();
    }

    /**
     * Set the button opacity for all the button
     */
    @SuppressLint("ObsoleteSdkInt")
    private void refreshButton() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mViewCsa10.setAlpha(mCsa10 ? 1f : 0.2f);
            mViewCsa12.setAlpha(mCsa12 ? 1f : 0.2f);
            mViewCsa16.setAlpha(mCsa16 ? 1f : 0.2f);
            mViewCsa18.setAlpha(mCsa18 ? 1f : 0.2f);
        } else {
            mViewCsa10.setAlpha(mCsa10 ? 255 : 50);
            mViewCsa12.setAlpha(mCsa12 ? 255 : 50);
            mViewCsa16.setAlpha(mCsa16 ? 255 : 50);
            mViewCsa18.setAlpha(mCsa18 ? 255 : 50);
        }
    }

    /**
     * Hide Keyboard
     */
    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (null != view) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (null != inputManager) {
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    /**
     * Enable or disable Ok Button
     */
    private void okValidate() {
        if (mEdit.getText().length() > 0 && mCurrent != 99) {
            mOkButton.setEnabled(true);
        } else {
            mOkButton.setEnabled(false);
        }
    }

    /**
     * Action done with Back Button.
     * Like closing activity.
     */
    @Override
    public void onBackPressed() {
        // Return value to the parent activity
        Intent intent = new Intent();
        intent.putExtra("parental_control_level", mCurrent);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }
}