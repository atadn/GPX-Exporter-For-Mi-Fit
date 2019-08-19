package com.fablapps.gpxexporterformifit.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fablapps.gpxexporterformifit.R;

public class DialogHelp {

    public static void openHelpDialog(final Activity act) {
        final ViewGroup parent = act.findViewById(R.id.dialog_help);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(act);
        View dialogView = LayoutInflater.from(act).inflate(R.layout.dialog_help, parent, false);

        dialogBuilder.setView(dialogView);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }
}
