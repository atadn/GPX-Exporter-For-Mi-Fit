package com.fablapps.gpxexporterformifit.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;

import com.fablapps.gpxexporterformifit.R;
import com.fablapps.gpxexporterformifit.helpers.BillingHelper;
import com.fablapps.gpxexporterformifit.helpers.PrefManager;
import com.fablapps.gpxexporterformifit.util.Constants;
import com.google.firebase.analytics.FirebaseAnalytics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int DELAYMILLIS = 250;

    private final Handler mHandler = new Handler();
    private Runnable mStartActivityTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        new BillingHelper(MainActivity.this, Constants.LICENSE_KEY_BILLING, null, this::BillingCheck);

        new PrefManager(this);

        controlPermission();
    }

    private void BillingCheck() {

        if (BillingHelper.isPurchased(Constants.NOADS_BILLING_ID)) {
            PrefManager.putBoolean(Constants.NOADS_BILLING_ID, true);
        } else {
            PrefManager.putBoolean(Constants.NOADS_BILLING_ID, false);
            BillingHelper.consumePurchase(Constants.NOADS_BILLING_ID);
        }

        startActivity();
        startActivityDelay();
    }

    private boolean checkBillingService() {
        return BillingHelper.isIabServiceAvailable(this);
    }

    private void startActivity() {
        BillingHelper.bpInitialize();

        mStartActivityTask = () -> {
            startActivity(new Intent(getApplicationContext(), ActivitySummariesActivity.class));
            overridePendingTransition(R.anim.fadein_act, R.anim.fadeout_act);
        };

        if (!checkBillingService()) {
            BillingHelper.bpRelease();
            startActivityDelay();
        }
    }

    private void startActivityDelay() {
        mHandler.postDelayed(mStartActivityTask, DELAYMILLIS);
    }

    @SuppressLint("NewApi")
    private void controlPermission() {
        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startActivity();
        } else {
            requestPermissions(permissions, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity();
            } else {
                startActivity(new Intent(getApplicationContext(), PermActivity.class));
            }
        }
    }

    @Override
    public void onStop() {
        finish();
        super.onStop();
    }
}
