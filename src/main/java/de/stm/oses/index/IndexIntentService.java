package de.stm.oses.index;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.Nullable;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.greenrobot.eventbus.EventBus;

import de.stm.oses.helper.OSESBase;
import de.stm.oses.index.database.FileSystemDatabase;
import de.stm.oses.notification.NotificationHelper;


public class IndexIntentService extends IntentService {

    private boolean alreadyStarted = false;

    public static class IndexFinishedEvent {
        IndexFinishedEvent() {}
    }

    public static boolean startService(Context context, OSESBase OSES) {

        if (!OSES.hasStoragePermission()) {
            return false;
        }

        boolean bScanDiloc = (OSES.getSession().getPreferences().getBoolean("scanDiloc", true) && OSES.getDilocStatus() == OSESBase.STATUS_INSTALLED);
        boolean bScanFassi = (OSES.getSession().getPreferences().getBoolean("scanFassi", true) && OSES.getFassiStatus() == OSESBase.STATUS_INSTALLED);

        if (bScanDiloc || bScanFassi) {

            Intent serviceIntent = new Intent(context, IndexIntentService.class);
            serviceIntent.putExtra("scanDiloc", bScanDiloc);
            serviceIntent.putExtra("scanFassi", bScanFassi);
            context.startService(serviceIntent);
            return true;

        }

        return false;
    }

    public IndexIntentService() {
        super("IndexIntentService");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        // weitere Requests in der selben Instanz verhindern
        if (alreadyStarted || intent == null)
            return;

        alreadyStarted = true;

        boolean bScanDiloc = intent.getBooleanExtra("scanDiloc", false);
        boolean bScanFassi = intent.getBooleanExtra("scanFassi", false);

        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

        String dilocPath = Environment.getExternalStorageDirectory()+remoteConfig.getString("dilocPath");
        String fassiPath = Environment.getExternalStorageDirectory()+remoteConfig.getString("fassiPath");

        FileSystemDatabase database = FileSystemDatabase.getInstance(getApplicationContext());
        NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());

        if (bScanDiloc)
            new FileSystemIndexer(dilocPath, database, notificationHelper, "DiLoc|Sync").execute();
        if (bScanFassi)
            new FileSystemIndexer(fassiPath, database, notificationHelper, "FASSI-MOVE").execute();

        EventBus.getDefault().post(new IndexFinishedEvent());

        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(getApplicationContext());
        Bundle data = new Bundle();
        data.putLong("files", database.fileSystemEntryDao().getCount());
        data.putLong("arbeitsauftrag", database.arbeitsauftragEntryDao().getCount());
        analytics.logEvent("oses_file_index_finished", data);

    }
}
