package com.gusdk.demo.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

public class UMPConsentUtils {
    private Context applicationContext;
    public UMPConsentUtils(Context context) {
        this.applicationContext = context;
    }
    // 所在地区是否收到GDPR政策约束
    public boolean isGDPR() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        int gdpr = prefs.getInt("IABTCF_gdprApplies", 0);
        return gdpr == 1;
    }
    // 是否可以展示广告
    public boolean canShowAds() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext);

        String purposeConsent = prefs.getString("IABTCF_PurposeConsents", "");
        String vendorConsent = prefs.getString("IABTCF_VendorConsents", "");
        String vendorLI = prefs.getString("IABTCF_VendorLegitimateInterests", "");
        String purposeLI = prefs.getString("IABTCF_PurposeLegitimateInterests", "");

        int googleId = 755;
        boolean hasGoogleVendorConsent = hasAttribute(vendorConsent, googleId);
        boolean hasGoogleVendorLI = hasAttribute(vendorLI, googleId);

        // Minimum required for at least non-personalized ads
        return hasConsentFor(Arrays.asList(1), purposeConsent, hasGoogleVendorConsent)
                && hasConsentOrLegitimateInterestFor(Arrays.asList(2, 7, 9, 10), purposeConsent, purposeLI, hasGoogleVendorConsent, hasGoogleVendorLI);
    }
    // 是否可以展示个性化广告
    public boolean canShowPersonalizedAds() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext);

        String purposeConsent = prefs.getString("IABTCF_PurposeConsents", "");
        String vendorConsent = prefs.getString("IABTCF_VendorConsents", "");
        String vendorLI = prefs.getString("IABTCF_VendorLegitimateInterests", "");
        String purposeLI = prefs.getString("IABTCF_PurposeLegitimateInterests", "");

        int googleId = 755;
        boolean hasGoogleVendorConsent = hasAttribute(vendorConsent, googleId);
        boolean hasGoogleVendorLI = hasAttribute(vendorLI, googleId);

        return hasConsentFor(Arrays.asList(1, 3, 4), purposeConsent, hasGoogleVendorConsent)
                && hasConsentOrLegitimateInterestFor(Arrays.asList(2, 7, 9, 10), purposeConsent, purposeLI, hasGoogleVendorConsent, hasGoogleVendorLI);
    }

    private boolean hasAttribute(String input, int index) {
        return input != null && input.length() >= index && input.charAt(index - 1) == '1';
    }

    private boolean hasConsentFor(List<Integer> purposes, String purposeConsent, boolean hasVendorConsent) {
        for (Integer p : purposes) {
            if (!hasAttribute(purposeConsent, p)) {
                return false;
            }
        }
        return hasVendorConsent;
    }

    private boolean hasConsentOrLegitimateInterestFor(List<Integer> purposes, String purposeConsent, String purposeLI, boolean hasVendorConsent, boolean hasVendorLI) {
        for (Integer p : purposes) {
            boolean hasConsent = hasAttribute(purposeConsent, p) && hasVendorConsent;
            boolean hasLegitimateInterest = hasAttribute(purposeLI, p) && hasVendorLI;
            if (!(hasConsent || hasLegitimateInterest)) {
                return false;
            }
        }
        return true;
    }
}
