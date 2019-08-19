package com.fablapps.gpxexporterformifit.helpers;

import android.os.AsyncTask;

import java.io.File;

import eu.chainfire.libsuperuser.Shell;

public class GetDataAsyncTask extends AsyncTask<Void, Void, Integer> {

    public static final int PROCESS_FINISH = 0;
    public static final int NO_ROOT = 1;
    public static final int NO_DB = 2;

    private final AsyncResponse listener;
    private final File dir;

    public interface AsyncResponse {
        void processFinish(int output);
    }

    public GetDataAsyncTask(AsyncResponse listener, File dir) {
        this.listener = listener;
        this.dir = dir;
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        if (Shell.SU.available()) {
            try {
                getMiFitDB();
            } catch (Exception e)  {
                return NO_DB;
            }
            return PROCESS_FINISH;
        } else {
            return NO_ROOT;
        }
    }

    @Override
    protected void onPostExecute(Integer result) {
        listener.processFinish(result);
    }

    private void getMiFitDB() {
        Shell.SU.run("adb shell \n am force-stop com.xiaomi.hm.health");
        Shell.SU.run("cp " + Shell.SU.run("ls /data/data/com.xiaomi.hm.health/databases/origin_db_* | grep -v journal").get(0) + " " + dir + "/mifitdatabase");
    }
}