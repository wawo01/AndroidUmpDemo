package com.gusdk.demo.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;
import com.ps.sdk.PSSDK;
import com.ps.sdk.entity.PrivacyAuthorizationResult;
import com.ps.sdk.tools.error.PrivacyAuthorizationException;
//import com.ps.sdk.PSSDK;
//import com.ps.sdk.entity.PrivacyAuthorizationResult;
//import com.ps.sdk.tools.error.PrivacyAuthorizationException;

//import java.util.concurrent.TimeUnit;
//
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;

public class MainActivity extends Activity {
    private static final String TAG = "UMP_DEMO";
    private String productId = "your pid ";
    private String gamerId = "gamer id";

    private ProgressBar progressBar;

    private void showProgressBar(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBar);
        showProgressBar(true);
        initUMP();
//
//        String isGdprUrl = "https://service.haloapps.com/policy/init?__pkg=xxxx&__os=android";
//        requestPrivacyApi(isGdprUrl, new ApiCallback() { // 获得是否是欧盟用户
//            @Override
//            public void onFinish(boolean result) {
//                MainActivity.this.runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        if (result) {  // 如果是欧盟用户
//                            // UMP操作
//                            initUMP();
//                        } else {
//                            // PSSDK操作,如果没有使用PSSDK，此处可忽略
////                            PSSDK.requestPrivacyAuthorization(MainActivity.this, productId, gamerId, new PSSDK.RequestPrivacyAuthorizationCallBack() {
////                                @Override
////                                public void onRequestSuccess(PrivacyAuthorizationResult privacyAuthorizationResult) {
////                                    // 其他操作 ，比如初始化tasdk，mssdk等
////                                }
////
////                                @Override
////                                public void onRequestFail(PrivacyAuthorizationException e) {
////                                    // 其他操作，比如初始化tasdk，mssdk等
////                                }
////                            });
//                        }
//                    }
//                });
//
//            }
//        });
    }


    private void initUMP() {
        // debugSettings 用于测试
        ConsentDebugSettings debugSettings = new ConsentDebugSettings.Builder(this)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId("6B268B4102A1961812FC511632EEAB9B")
                .build();

        ConsentRequestParameters params = new ConsentRequestParameters
                .Builder()
                .setConsentDebugSettings(debugSettings)
                .setTagForUnderAgeOfConsent(false) // 用户不是未成年人
                .build();

        ConsentInformation consentInformation = UserMessagingPlatform.getConsentInformation(this);
        consentInformation.reset(); // 重置状态,每次重置状态是为了方便测试
        consentInformation.requestConsentInfoUpdate(
                this,
                params,
                new ConsentInformation.OnConsentInfoUpdateSuccessListener() {
                    @Override
                    public void onConsentInfoUpdateSuccess() {
                        showProgressBar(false);
                        UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                                MainActivity.this,
                                (ConsentForm.OnConsentFormDismissedListener) loadAndShowError -> {
                                    if (loadAndShowError != null) {
                                        // Consent gathering failed.
                                        Log.w(TAG, String.format("%s: %s",
                                                loadAndShowError.getErrorCode(),
                                                loadAndShowError.getMessage()));
                                    }
                                    UMPConsentUtils utils = new UMPConsentUtils(MainActivity.this);
                                    if (!utils.isGDPR()) {
                                        //如果有PSSDK，
                                        PSSDK.requestPrivacyAuthorization(MainActivity.this, productId, gamerId, new PSSDK.RequestPrivacyAuthorizationCallBack() {
                                            @Override
                                            public void onRequestSuccess(PrivacyAuthorizationResult privacyAuthorizationResult) {
                                                // 其他操作 ，比如初始化tasdk，mssdk等
                                            }

                                            @Override
                                            public void onRequestFail(PrivacyAuthorizationException e) {
                                                // 其他操作，比如初始化tasdk，mssdk等
                                            }
                                        });
                                        // 如果没有pssdk，直接初始化其他sdk即可
                                        // 初始化TASDK,MSSDK等
                                    } else {
                                        // Consent has been gathered.
                                        if (consentInformation.canRequestAds()) {
                                            if (utils.canShowPersonalizedAds()) {
                                                // 初始化TASDK,MSSDK等
                                                Log.i(TAG, "onConsentInfoUpdateSuccess: can load ads");
                                            } else {
                                                // 先调用tasdk的disableAccessPrivacyInformation()方法，然后初始化TASDK
                                                Log.i(TAG, "onConsentInfoUpdateSuccess: need disableAccessPrivacyInformation");
                                            }
                                        } else {
                                            // 先调用tasdk的disableAccessPrivacyInformation()方法，然后初始化TASDK
                                            Log.i(TAG, "onConsentInfoUpdateSuccess: can't load ads");
                                        }
                                    }
                                }
                        );
                    }
                },
                new ConsentInformation.OnConsentInfoUpdateFailureListener() {
                    @Override
                    public void onConsentInfoUpdateFailure(FormError formError) {
                        // Handle the error.
                        Log.i(TAG, "onConsentInfoUpdateFailure: " + formError.getMessage());
                        //正常初始化TASDK,MSSDK等SDK
                    }
                });

    }

}