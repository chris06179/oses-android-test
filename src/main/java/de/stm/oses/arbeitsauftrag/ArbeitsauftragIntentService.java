package de.stm.oses.arbeitsauftrag;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.stm.oses.helper.OSESBase;
import de.stm.oses.helper.OSESRequest;
import de.stm.oses.verwendung.VerwendungClass;


public class ArbeitsauftragIntentService extends IntentService {

    private boolean alreadyStarted = false;

    public static class ArbeitsauftragProgressEvent {
        public int id;

        ArbeitsauftragProgressEvent(int id) {
            this.id = id;
        }
    }

    public static class ArbeitsauftragResultEvent {
        public VerwendungClass schicht;
        public File result;

        ArbeitsauftragResultEvent(File result, VerwendungClass schicht) {
            this.result = result;
            this.schicht = schicht;
        }
    }

    public static void tryStartService(Context context, OSESBase OSES, ArrayList<VerwendungClass> list) {

        if (!OSES.hasStoragePermission()) {
            return;
        }

        boolean bScanDiloc = OSES.getSession().getPreferences().getBoolean("scanDiloc", false);
        boolean bScanOSES = OSES.getSession().getPreferences().getBoolean("scanOSES", false);
        boolean bScanAgressive = OSES.getSession().getPreferences().getBoolean("scanAgressive", false);


        if ((OSES.getDilocStatus() == OSESBase.DILOC_STATUS_INSTALLED && bScanDiloc) || bScanOSES || bScanAgressive) {

            Intent serviceIntent = new Intent(context, ArbeitsauftragIntentService.class);
            serviceIntent.putExtra("scanDiloc", bScanDiloc);
            serviceIntent.putExtra("scanOSES", bScanOSES);
            serviceIntent.putExtra("scanAgressive", bScanAgressive);
            serviceIntent.putParcelableArrayListExtra("items", list);
            context.startService(serviceIntent);

        }

    }

    public ArbeitsauftragIntentService() {
        super("ArbeitsauftragIntentService");
    }

    public String getStringFile(File f) {
        InputStream inputStream;
        String encodedFile= "", lastVal;
        try {
            inputStream = new FileInputStream(f.getAbsolutePath());

            byte[] buffer = new byte[10240];//specify the size to allow
            int bytesRead;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Base64OutputStream output64 = new Base64OutputStream(output, Base64.DEFAULT);

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output64.write(buffer, 0, bytesRead);
            }
            output64.close();
            encodedFile =  output.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        lastVal = encodedFile;
        return lastVal;
    }



    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        // weitere Requests in der selben Instanz verhindern
        if (alreadyStarted)
            return;

        alreadyStarted = true;

        // Remote Config
        final FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // Verwendung initialisieren und aus Intent einlesen
        ArrayList<VerwendungClass> verwendung;

        boolean bScanDiloc = false;
        boolean bScanOSES = false;
        boolean bScanAgressive = false;

        if (intent != null) {
            verwendung = intent.getParcelableArrayListExtra("items");
            if (verwendung == null || verwendung.isEmpty())
                return;

            bScanDiloc = intent.getBooleanExtra("scanDiloc", false);
            bScanOSES = intent.getBooleanExtra("scanOSES", false);
            bScanAgressive = intent.getBooleanExtra("scanAgressive", false);

        } else {
            return;
        }

        for (VerwendungClass schicht : verwendung) {

            if (schicht.getKat() == null || !schicht.getKat().equals("S")) {
                continue;
            }

            // Nur bei Schichten die nicht mehr als 2 Tage zurück liegen und mehr als 8 Tage in der Zukunft liegen
            long timeDiff = schicht.getDatum() - System.currentTimeMillis()/1000;

            if (timeDiff < -172800 || timeDiff > 691200) {
                continue;
            }

            // Arbeitsauftrag erzeugen und Auswertung abwarten
            ArbeitsauftragBuilder auftrag = new ArbeitsauftragBuilder(schicht);
            File result = auftrag.extractSourceFile(bScanDiloc, bScanOSES, bScanAgressive);
            EventBus.getDefault().post(new ArbeitsauftragResultEvent(result, schicht));

            // mit Server synchronisieren, falls Arbeitsauftrag erfolgreich extrahiert
            if (result != null && mFirebaseRemoteConfig.getBoolean("allow_arbeitsauftrag_upload")) {

                OSESBase OSES = new OSESBase(this);

                String pdf64 = getStringFile(((File) result));
                if (pdf64.isEmpty())
                    return;

                Map<String, String> map = new HashMap<>();
                map.put("pdf", pdf64);

                OSESRequest upload = new OSESRequest(this);
                upload.setParams(map);
                upload.setUrl("https://oses.mobi/api.php?request=arbeitsauftrag&command=upload&id=" + schicht.getId() + "&session=" + OSES.getSession().getIdentifier());
                upload.execute();
            }

        }
    }
}
