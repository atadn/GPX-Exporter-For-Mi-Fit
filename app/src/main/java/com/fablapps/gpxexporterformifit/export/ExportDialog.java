package com.fablapps.gpxexporterformifit.export;

import android.app.Activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fablapps.gpxexporterformifit.R;

import com.fablapps.gpxexporterformifit.helpers.ToolHelper;
import com.fablapps.gpxexporterformifit.util.Constants;

import androidx.appcompat.app.AlertDialog;

public class ExportDialog {

    public static void openActivityDetailDialog(final Activity act, String eventDateAndDis, String eventTime, String eventSavedTime, int trackId) {

        final ViewGroup parent = act.findViewById(R.id.activityDetailDialog);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(act);
        View dialogView = LayoutInflater.from(act).inflate(R.layout.dialog_activity_detail, parent, false);

        dialogBuilder.setView(dialogView);

        LinearLayout progressLayout = dialogView.findViewById(R.id.progress_layout);
        RelativeLayout mainLayout = dialogView.findViewById(R.id.main_layout);

        TextView dataEventDateAndDis = dialogView.findViewById(R.id.text_activity_type_distance);
        TextView textEventUsedTime = dialogView.findViewById(R.id.text_activity_used_time);
        TextView textEventSavedTime = dialogView.findViewById(R.id.text_activity_saved_time);

        Button buttonExport = dialogView.findViewById(R.id.button_export);
        Button buttonShare = dialogView.findViewById(R.id.button_share);

        dataEventDateAndDis.setText(eventDateAndDis);
        textEventUsedTime.setText(eventTime);
        textEventSavedTime.setText(eventSavedTime);

        AlertDialog alertDialog = dialogBuilder.create();

        buttonExport.setOnClickListener(v -> {
            mainLayout.setVisibility(View.INVISIBLE);
            progressLayout.setVisibility(View.VISIBLE);

            new GPXExporter(new GPXExporter.GPXExporterListener() {
                @Override
                public void onFailedToExport() {
                    alertDialog.cancel();

                    Toast.makeText(act, act.getResources().getString(R.string.export_failed), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFinishToExport(String fileName) {
                    alertDialog.cancel();

                    Toast.makeText(act, act.getResources().getString(R.string.export_gpx_saved) + Constants.FILE_PATH + fileName + Constants.FILE_TYPE, Toast.LENGTH_SHORT).show();
                }
            }, act.getApplicationContext(), trackId).execute();

        });

        buttonShare.setOnClickListener(v -> {
            mainLayout.setVisibility(View.INVISIBLE);
            progressLayout.setVisibility(View.VISIBLE);

            new GPXExporter(new GPXExporter.GPXExporterListener() {

                @Override
                public void onFailedToExport() {
                    alertDialog.cancel();

                    Toast.makeText(act, act.getResources().getString(R.string.export_failed), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFinishToExport(String fileName) {
                    alertDialog.cancel();

                    ToolHelper.openShareDialogGpx(act, fileName);
                }
            }, act.getApplicationContext(), trackId).execute();

        });

        alertDialog.show();
    }
}
