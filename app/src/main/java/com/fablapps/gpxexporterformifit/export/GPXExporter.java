package com.fablapps.gpxexporterformifit.export;

import android.annotation.SuppressLint;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;

import com.fablapps.gpxexporterformifit.helpers.DatabaseHelper;
import com.fablapps.gpxexporterformifit.helpers.ToolHelper;
import com.fablapps.gpxexporterformifit.util.Constants;

import java.io.File;
import java.io.PrintWriter;

class GPXExporter extends AsyncTask<Void, Void, String> {

    @SuppressLint("StaticFieldLeak")
    private final Context context;

    private static float[] latitudes, longitudes, altitudes;
    private static int[] timestamps, HR;

    private final int trackId;

    private final GPXExporterListener listener;

    public interface GPXExporterListener {
        void onFailedToExport();

        void onFinishToExport(String fileName);
    }

    GPXExporter(GPXExporterListener listener, Context context,int trackId) {
        this.listener = listener;
        this.trackId = trackId;
        this.context = context.getApplicationContext();
    }

    private static Boolean createFolder() {
        File file = new File(Constants.FILE_PATH);
        if (!file.exists()) {
            return file.mkdirs();
        }
        return false;
    }

    private static void createGpxFile(String fileName) {
        if (createFolder()) {
            new File(Constants.FILE_PATH, fileName + Constants.FILE_TYPE);
        }
    }

    private static void getActivityData(int trackId) {
        latitudes = DatabaseHelper.getlatitudes(trackId);
        longitudes = DatabaseHelper.getLongitudes(trackId);
        altitudes = DatabaseHelper.getAltitudes(trackId);
        timestamps = DatabaseHelper.getTimeStamps(trackId);
        HR = DatabaseHelper.getHR(trackId);
    }

    private String exportGpx(final int trackId) throws Exception {

        String fileName = ToolHelper.getSavedTime(context,trackId);

        createGpxFile(fileName);
        getActivityData(trackId);

        PrintWriter pW = new PrintWriter(Constants.FILE_PATH + File.separator + fileName + Constants.FILE_TYPE, Constants.CSN);

        pW.println("<?xml version='1.0' encoding='UTF-8'?>");
        pW.println("<gpx version='1.1' creator='GPX Exporter For Mi Fit'");
        pW.println(" xsi:schemaLocation='http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd'");
        pW.println(" xmlns='http://www.topografix.com/GPX/1/1' \n xmlns:gpxtpx='http://www.garmin.com/xmlschemas/TrackPointExtension/v1' \n xmlns:gpxx='http://www.garmin.com/xmlschemas/GpxExtensions/v3' \n xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>");

        pW.println("  <metadata>");
        pW.println("    <link href='fablapps.com'>");
        pW.println("      <text>GPX Exporter For Mi Fit</text>");
        pW.println("    </link>");
        pW.println("     <time>2019-08-04T23:22:51.000Z</time>");
        pW.println("  </metadata>");

        pW.println("  <trk>");
        pW.println("   <trkseg>");


        for (int i = 0; i < timestamps.length; i++) {

            java.sql.Date dateFormatPoint = new java.sql.Date(((long) timestamps[i]) * 1000);

            String yearPoint = new SimpleDateFormat("yyyy").format(dateFormatPoint);
            String monthPoint = new SimpleDateFormat("MM").format(dateFormatPoint);
            String dayPoint = new SimpleDateFormat("dd").format(dateFormatPoint);
            String hourPoint = new SimpleDateFormat("HH").format(dateFormatPoint);
            String minutePoint = new SimpleDateFormat("mm").format(dateFormatPoint);
            String secondPoint = new SimpleDateFormat("ss").format(dateFormatPoint);

            String allTime = yearPoint + "-" + monthPoint + "-" + dayPoint + "T" + hourPoint + ":" + minutePoint + ":" + secondPoint + ".000Z";

            pW.println("   <trkpt lon='" + longitudes[i] + "' lat='" + latitudes[i] + "'>");

            if (((double) altitudes[i]) != -200000.0d) {
                pW.println("    <ele>" + (altitudes[i] / 10.0f) + "</ele>");
            }

            pW.println("    <time>" + allTime + "</time>");
            pW.println("    <extensions>");
            pW.println("     <gpxtpx:TrackPointExtension>");

            if (i < HR.length) {
                pW.println(" \t <gpxtpx:hr>" + HR[i] + "</gpxtpx:hr>");
            }

            pW.println("     </gpxtpx:TrackPointExtension>");
            pW.println("    </extensions>");
            pW.println("   </trkpt>");
        }
        pW.println("  </trkseg>");
        pW.println(" </trk>");
        pW.println(" </gpx>");

        pW.flush();
        pW.close();

        return ToolHelper.getSavedTime(context,trackId);
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            return exportGpx(trackId);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            listener.onFinishToExport(result);
        } else {
            listener.onFailedToExport();
        }
    }
}
