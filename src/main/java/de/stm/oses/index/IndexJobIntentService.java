package de.stm.oses.index;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.greenrobot.eventbus.EventBus;

import de.stm.oses.application.OsesApplication;
import de.stm.oses.helper.OSESBase;
import de.stm.oses.index.database.FileSystemDatabase;
import de.stm.oses.notification.NotificationHelper;


public class IndexJobIntentService extends JobIntentService {

    private boolean alreadyStarted = false;
    private FileSystemIndexer task;

    static final int JOB_ID = 1000;

    public static class IndexFinishedEvent {
        IndexFinishedEvent() {}
    }

    public static boolean enqueueWork(Context context, OSESBase OSES) {

        if (!OSES.hasStoragePermission()) {
            return false;
        }

        boolean bScanDiloc = (OSES.getSession().getPreferences().getBoolean("scanDiloc", true) && OSES.getDilocStatus() == OSESBase.STATUS_INSTALLED);
        boolean bScanFassi = (OSES.getSession().getPreferences().getBoolean("scanFassi", true) && OSES.getFassiStatus() == OSESBase.STATUS_INSTALLED);

        if (bScanDiloc || bScanFassi) {

            Intent serviceIntent = new Intent(context, IndexJobIntentService.class);
            serviceIntent.putExtra("scanDiloc", bScanDiloc);
            serviceIntent.putExtra("scanFassi", bScanFassi);
            enqueueWork(context, IndexJobIntentService.class, JOB_ID, serviceIntent);
            return true;

        }

        return false;
    }



    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        OsesApplication.getInstance().getLogger().log("INDEXER","Starte Work...");

        // weitere Requests in der selben Instanz verhindern
        if (alreadyStarted || isStopped())
            return;

        alreadyStarted = true;

        boolean bScanDiloc = intent.getBooleanExtra("scanDiloc", false);
        boolean bScanFassi = intent.getBooleanExtra("scanFassi", false);

        OsesApplication.getInstance().getLogger().log("INDEXER","Parameter - DiLoc "+bScanDiloc+" / FASSI "+bScanFassi);

        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(getApplicationContext());

        String dilocPath = Environment.getExternalStorageDirectory()+remoteConfig.getString("diloc_path");
        String fassiPath = Environment.getExternalStorageDirectory()+remoteConfig.getString("fassi_path");

        FileSystemDatabase database = FileSystemDatabase.getInstance(getApplicationContext());
        NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());

        if (bScanDiloc && !isStopped()) {
            task = new FileSystemIndexer(dilocPath, database, notificationHelper,"DiLoc|Sync");
            task.execute();
        }

        if (bScanFassi && !isStopped()) {
            task = new FileSystemIndexer(fassiPath, database, notificationHelper, "FASSI-MOVE");
            task.execute();
        }

        if (isStopped()) {
            analytics.logEvent("oses_file_index_force_stop", null);
            OsesApplication.getInstance().getLogger().log("INDEXER","Work durch System unterbrochen!");
            return;
        }

        long filesCount = database.fileSystemEntryDao().getCount();
        long arbeitsauftragCount = database.arbeitsauftragEntryDao().getCount();

        Bundle data = new Bundle();
        data.putLong("files", filesCount);
        data.putLong("arbeitsauftrag", arbeitsauftragCount);
        analytics.logEvent("oses_file_index_finished", data);

        EventBus.getDefault().post(new IndexFinishedEvent());

        OsesApplication.getInstance().getLogger().log("INDEXER","Work beendet! Dateien: "+filesCount+" - Arbeitsaufträge: "+arbeitsauftragCount);

    }

    @Override
    public boolean onStopCurrentWork() {
        if (task != null) {
            task.doStopCurrentWork();
        }
        return true;
    }
}
