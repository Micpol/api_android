package pl.netigen.payments;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

class BillingPreferencesHelper {

    private static final String NETIGEN_BILLING_PREFERENCES_NAME = "NETIGEN_BILLING_PREFERENCES_NAME";
    private static final String WAS_CHECKED = "_WAS_CHECKED";
    private static BillingPreferencesHelper billingPreferencesHelper;
    private SharedPreferences sharedPreferences;

    private BillingPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(NETIGEN_BILLING_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    public static BillingPreferencesHelper getInstance(@NonNull Context context) {
        if (billingPreferencesHelper == null) {
            billingPreferencesHelper = new BillingPreferencesHelper(context);
        }
        return billingPreferencesHelper;
    }

    public boolean isSkuBought(String sku) {
        return sharedPreferences.getBoolean(sku, false);
    }

    public boolean wasSkuChecked(String sku) {
        return sharedPreferences.getBoolean(sku + WAS_CHECKED, false);
    }

    public void setSkuBought(String sku, boolean isBought) {
        sharedPreferences.edit().putBoolean(sku, isBought).apply();
    }

    public void setSkuChecked(String sku, boolean wasChecked) {
        sharedPreferences.edit().putBoolean(sku + WAS_CHECKED, wasChecked).apply();
    }

}
