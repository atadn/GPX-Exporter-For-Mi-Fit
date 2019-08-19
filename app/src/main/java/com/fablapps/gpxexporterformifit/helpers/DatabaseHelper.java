package com.fablapps.gpxexporterformifit.helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static String DATABASE_PATH;
    private static SQLiteDatabase db;

    public DatabaseHelper(Context context, String databaseName) {
        super(context, databaseName, null, 1);
        DATABASE_PATH = databaseName;
    }

    public void onCreate(SQLiteDatabase db) {

    }

    public void opendb() {
        db = SQLiteDatabase.openOrCreateDatabase(DATABASE_PATH, null);
    }

    public void closeDb() {
        if (db != null) {
            if (db.isOpen()) {
                db.close();
            }
        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private static Cursor getData(String query) {
        return db.rawQuery(query, null);
    }

    public ArrayList<HashMap<String, String>> allData() {
        String selectQuery = "SELECT _id,TYPE,DATE,TRACKID,DISTANCE,COSTTIME,ENDTIME FROM TRACKRECORD order by TRACKID DESC";
        Cursor cursor = db.rawQuery(selectQuery, null);
        ArrayList<HashMap<String, String>> dataList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<>();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    map.put(cursor.getColumnName(i), cursor.getString(i));
                }
                dataList.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return dataList;
    }

    public static float[] getlatitudes(int trackid) {
        Cursor res = getData("select BULKLL from TRACKDATA where TRACKID=" + trackid + "");

        res.moveToFirst();

        String[] latlongs = res.getString(res.getColumnIndex("BULKLL")).split(";");
        float[] latlist = new float[latlongs.length];
        int j = 0;
        for (int i = 0; i < latlongs.length; i++) {
            String[] latlongs_sub = latlongs[i].split(",");
            if (i == 0) {
                latlist[j] = Float.parseFloat(latlongs_sub[0]) / 1.0E8f;
            } else {
                latlist[j] = latlist[j - 1] + (Float.parseFloat(latlongs_sub[0]) / 1.0E8f);
            }
            j++;
        }
        res.close();
        return latlist;
    }

    public static float[] getLongitudes(int trackid) {
        Cursor res = getData("select BULKLL from TRACKDATA where TRACKID=" + trackid + "");
        res.moveToFirst();
        String[] latLongs = res.getString(res.getColumnIndex("BULKLL")).split(";");
        float[] longList = new float[latLongs.length];
        int j = 0;
        for (int i = 0; i < latLongs.length; i++) {
            String[] latlongs_sub = latLongs[i].split(",");
            if (i == 0) {
                longList[j] = Float.parseFloat(latlongs_sub[1]) / 1.0E8f;
            } else {
                longList[j] = longList[j - 1] + (Float.parseFloat(latlongs_sub[1]) / 1.0E8f);
            }
            j++;
        }
        res.close();
        return longList;
    }

    public static float[] getAltitudes(int trackid) {
        Cursor res = getData("select BULKAL from TRACKDATA where TRACKID=" + trackid + "");
        res.moveToFirst();
        String[] alts = res.getString(res.getColumnIndex("BULKAL")).split(";");
        float[] altList = new float[alts.length];
        for (int i = 0; i < alts.length; i++) {
            altList[i] = Float.parseFloat(alts[i]) / 10.0f;
        }
        res.close();
        return altList;
    }

    public static int[] getTimeStamps(int trackid) {
        Cursor res = getData("select BULKTIME from TRACKDATA where TRACKID=" + trackid + "");
        res.moveToFirst();
        String[] timeStamps = res.getString(res.getColumnIndex("BULKTIME")).split(";");
        int[] timeStampsList = new int[timeStamps.length];
        for (int i = 0; i < timeStamps.length; i++) {
            if (i == 0) {
                timeStampsList[i] = Integer.parseInt(timeStamps[i]) + trackid;
            } else {
                timeStampsList[i] = timeStampsList[i - 1] + Integer.parseInt(timeStamps[i]);
            }
        }
        res.close();
        return timeStampsList;
    }

    public static int[] getHR(int trackid) {
        Cursor res = getData("select BULKHR from TRACKDATA where TRACKID=" + trackid + "");

        res.moveToFirst();
        String[] hrcompost = res.getString(res.getColumnIndex("BULKHR")).split(";");
        int[] hrlist = new int[hrcompost.length];
        int j = 0;
        for (int i = 0; i < hrcompost.length; i++) {
            String[] hrlist_sub = hrcompost[i].split(",");
            if (i == 0) {
                hrlist[j] = Integer.parseInt(hrlist_sub[1]);
            } else {
                hrlist[j] = hrlist[j - 1] + Integer.parseInt(hrlist_sub[1]);
            }
            j++;
        }
        res.close();
        return hrlist;
    }
}
