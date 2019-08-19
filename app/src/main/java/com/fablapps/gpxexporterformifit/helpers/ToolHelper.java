package com.fablapps.gpxexporterformifit.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.fablapps.gpxexporterformifit.R;
import com.fablapps.gpxexporterformifit.util.Constants;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ToolHelper {

    private static double convertMtrToKm(double meter) {
        double km = meter / 1000;

        BigDecimal bd = new BigDecimal(km);
        bd = bd.setScale(2, RoundingMode.HALF_EVEN);

        return bd.doubleValue();
    }

    public static String getEventDistance(double meter) {
        return convertMtrToKm(meter) + "km";
    }

    public static String getSavedTimeHours(Context context, long trackId) {

        if (DateFormat.is24HourFormat(context)) {
            return getDate("hh:mm", trackId);
        } else {
            return getDate("hh:mm a", trackId);
        }

    }

    public static String getSavedTime(Context context, long trackId) {

        if (DateFormat.is24HourFormat(context)) {
            return getDate("yyyy-MM-dd hh:mm:ss", trackId);
        } else {
            return getDate("yyyy-MM-dd hh:mm:ss a", trackId);
        }

    }

    private static String getDate(String pattern, long trackId) {
        Date date = new Date((trackId * 1000));
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(date);
    }

    public static String getActivityUsedTime(int trackId, int endTime) {
        int lengthTime = endTime - trackId;
        int hours = lengthTime / 3600;
        int minutes = (lengthTime - (hours * 3600)) / 60;
        int seconds = (lengthTime - (hours * 3600)) - (minutes * 60);

        String hoursS = Integer.toString(hours);
        String minutesS = Integer.toString(minutes);
        String secondsS = Integer.toString(seconds);

        if (minutesS.length() == 1) {
            minutesS = "0" + minutesS;
        }

        if (secondsS.length() == 1) {
            secondsS = "0" + secondsS;
        }

        if (hoursS.equals("0")) {
            hoursS = "00";
        }

        return hoursS + ":" + minutesS + ":" + secondsS;
    }

    public static String getEventTypeString(Context context, int type) {
        switch (type) {
            case 1:
                return context.getResources().getString(R.string.activity_type_run);
            case 6:
                return context.getResources().getString(R.string.activity_type_walk);
            case 7:
                return context.getResources().getString(R.string.activity_type_trail);
            case 9:
                return context.getResources().getString(R.string.activity_type_cycling);
            default:
                return context.getResources().getString(R.string.activity_type_other);
        }
    }

    public static int getEventTypeImage(int type) {
        switch (type) {
            case 1:
                return R.drawable.ic_run;
            case 6:
                return R.drawable.ic_walk;
            case 7:
                return R.drawable.ic_walk;
            case 9:
                return R.drawable.ic_bike;
            default:
                return R.drawable.ic_accessibility;
        }
    }

    public static void openMail(Context context, String email, String subject, String title) {
        Intent Email = new Intent(Intent.ACTION_SEND);
        Email.setType("text/email");
        Email.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        Email.putExtra(Intent.EXTRA_SUBJECT, subject);
        context.startActivity(Intent.createChooser(Email, title));
    }

    public static void openShareDialogGpx(Activity act, String fileName) {
        File file = new File(Constants.FILE_PATH, fileName + Constants.FILE_TYPE);
        Uri contentUri = FileProvider.getUriForFile(act.getApplicationContext(), Constants.PACKAGE, file);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.setType(Constants.EXPORT_TYPE);
        act.startActivity(Intent.createChooser(shareIntent, act.getResources().getString(R.string.action_dialog_share)));
    }

    public static void openShareDialog(Context context) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, context.getResources().getString(R.string.app_name) + " \n" + context.getResources().getString(R.string.app_play_store_link));
        context.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.title_share)));
    }

    public static void openRate(Context context) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("market://details?id=" + context.getPackageName()));
            context.startActivity(i);
        } catch (Exception err) {
            Toast.makeText(context, context.getResources().getString(R.string.message_install_gp), Toast.LENGTH_SHORT).show();
        }
    }

    public static void openAboutDialog(final Activity act) {

        final ViewGroup parent = act.findViewById(R.id.dialog_about);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(act);
        View dialogView = LayoutInflater.from(act).inflate(R.layout.dialog_about, parent, false);

        dialogBuilder.setView(dialogView);

        dialogBuilder.setPositiveButton(act.getResources().getString(R.string.warning_dialog_about_positive),
                (dialog, id) -> dialog.cancel());

        TextView editText = dialogView.findViewById(R.id.version_text);
        try {
            editText.setText(act.getPackageManager().getPackageInfo(act.getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        TextView privacyPolicyText = dialogView.findViewById(R.id.privacy_policy_text);
        privacyPolicyText.setOnClickListener(view -> act.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(act.getResources().getString(R.string.app_privacy_link)))));

        TextView openSourceLT = dialogView.findViewById(R.id.open_source_licenses_text);
        openSourceLT.setOnClickListener(view -> {
            final AlertDialog.Builder dialogBuilderOSLT = new AlertDialog.Builder(act);
            View dialogView1 = LayoutInflater.from(act).inflate(R.layout.activity_open_sources, parent);
            dialogBuilderOSLT.setView(dialogView1);
            dialogBuilderOSLT.setTitle(act.getResources().getString(R.string.warning_dialog_about_osl));
            dialogBuilderOSLT.setPositiveButton(act.getResources().getString(R.string.warning_dialog_about_positive), null);

            TextView popupText = dialogView1.findViewById(R.id.popup_text);
            InputStream openRawResource = act.getResources().openRawResource(R.raw.open_source_licenses);
            byte[] bArr;

            try {
                bArr = new byte[openRawResource.available()];
                //noinspection ResultOfMethodCallIgnored
                openRawResource.read(bArr);
                popupText.setText(new String(bArr));
            } catch (IOException e) {
                e.printStackTrace();
            }

            AlertDialog alertDialog = dialogBuilderOSLT.create();
            alertDialog.show();
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    public static void openBuyDialog(Activity act) {
        final ViewGroup parent = act.findViewById(R.id.dialog_probuy);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(act);
        View dialogView = LayoutInflater.from(act).inflate(R.layout.dialog_probuy, parent, false);

        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        Button btnBuy = dialogView.findViewById(R.id.btn_buy);
        btnBuy.setOnClickListener(view -> {
            EventBus.getDefault().post("btn_buy");
            alertDialog.cancel();
        });
        alertDialog.show();
    }
}
