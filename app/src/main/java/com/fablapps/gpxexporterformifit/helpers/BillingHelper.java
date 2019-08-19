package com.fablapps.gpxexporterformifit.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.anjlab.android.iab.v3.BillingProcessor;

import com.anjlab.android.iab.v3.TransactionDetails;

public class BillingHelper implements BillingProcessor.IBillingHandler {

    public interface billingListener {
        void onProductPurchased(String productId, TransactionDetails details);

        void onBillingError(int errorCode, Throwable error);
    }

    public interface billingInitialized {
        void onBillingInitialized();
    }

    private static BillingProcessor bp;
    private static billingListener listener;
    private static billingInitialized listenerI;
    private static boolean isReadyToPurchase = false;

    public BillingHelper(Context context, String licenseKey, billingListener listener, billingInitialized listenerI) {
        bp = BillingProcessor.newBillingProcessor(context, licenseKey, this);

        BillingHelper.listener = listener;
        BillingHelper.listenerI = listenerI;
    }

    public static void bpInitialize() {
        bp.initialize();
    }

    public static boolean isPurchased(String productId) {

        boolean purchaseResult = bp.loadOwnedPurchasesFromGoogle();
        if (purchaseResult) {
            TransactionDetails subscriptionTransactionDetails = bp.getPurchaseTransactionDetails(productId);
            return subscriptionTransactionDetails != null;
        }

        return false;
    }

    public static void consumePurchase(String productId) {
        bp.consumePurchase(productId);
    }

    public static boolean isReadyToPurchase() {
        return isReadyToPurchase;
    }

    public static boolean isIabServiceAvailable(Context context) {
        return BillingProcessor.isIabServiceAvailable(context);
    }

    public static void bpPurchase(Activity act, String productId) {
        bp.purchase(act, productId);
    }

    public static void bpRelease() {
        if (bp != null) {
            bp.release();
        }
    }

    public static boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        return bp.handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onProductPurchased(@NonNull String productId, TransactionDetails details) {
        listener.onProductPurchased(productId, details);
    }

    @Override
    public void onPurchaseHistoryRestored() {
        /* ignore */
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        listener.onBillingError(errorCode, error);
    }

    @Override
    public void onBillingInitialized() {
        if (listenerI != null) {
            listenerI.onBillingInitialized();
        }

        isReadyToPurchase = true;
    }
}
