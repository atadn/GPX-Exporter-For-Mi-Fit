package com.fablapps.gpxexporterformifit.util;

import android.os.Environment;

import java.io.File;

public class Constants {
    public static final String FILE_PATH = Environment.getExternalStorageDirectory() + File.separator + "Exported GPX" + File.separator;
    public static final String FILE_TYPE = ".gpx";
    public static final String CSN = "UTF-8";
    public static final String EXPORT_TYPE = "application/gpx+xml";

    public static final String PACKAGE = "com.fablapps.gpxexporterformifit";

    public static final String DATABASE_PATH = "/mifitdatabase";

    public static final Integer TYPE_RUN = 1;
    public static final Integer TYPE_WALK = 6;
    public static final Integer TYPE_TRAIL = 7;
    public static final Integer TYPE_CYCLING = 9;
    public static final Integer TYPE_OTHER = 8;

    public static final String LICENSE_KEY_BILLING = "MIIBIjAyxpr7l++Hc0qXecmdtDyJmOw0FwMxJsXx/U0tSPx2ljE5TTjFpiq5Z7k+jQIouZ68mQaG7wpYCNPfuar9afO1LWlcJSL8Q8X0cHYXBLmFYsxN7V7y0R4DW//emfdRuxQU3fp3JlavTPwsYdVL1ZZFI4ZTITmHR84YwSkkQ2P2N/LvgkBwpCiYVtehbqdaatXqg/I4nQIDAQAB";
    public static final String NOADS_BILLING_ID = "item_noads";

    public static final String ADMOB_GENERAL_ID = "ca-app-pub-9651595789067045~28300176341";
    public static final String ADMOB_AD_ID = "ca-app-pub-9651595789067045/38340042926";
}
