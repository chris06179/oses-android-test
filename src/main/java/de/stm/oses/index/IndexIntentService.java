package de.stm.oses.index;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;

import androidx.annotation.Nullable;

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

import de.stm.oses.arbeitsauftrag.ArbeitsauftragBuilder;
import de.stm.oses.helper.OSESBase;
import de.stm.oses.helper.OSESRequest;
import de.stm.oses.index.database.FileSystemDatabase;
import de.stm.oses.index.entities.ArbeitsauftragWithFileEntry;
import de.stm.oses.verwendung.VerwendungClass;


public class IndexIntentService extends IntentService {

    private boolean alreadyStarted = false;

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
        boolean bScanAgressive = OSES.getSession().getPreferences().getBoolean("scanAgressive", false);


        if ((OSES.getDilocStatus() == OSESBase.DILOC_STATUS_INSTALLED && bScanDiloc) || bScanAgressive) {

            Intent serviceIntent = new Intent(context, IndexIntentService.class);
            serviceIntent.putExtra("scanDiloc", bScanDiloc);
            serviceIntent.putExtra("scanAgressive", bScanAgressive);
            serviceIntent.putParcelableArrayListExtra("items", list);
            context.startService(serviceIntent);

        }

    }

    public IndexIntentService() {
        super("IndexIntentService");
    }

    public  String getStringFile(File f) {
        InputStream inputStream;
        String encodedFile = "", lastVal;
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
            encodedFile = output.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        lastVal = encodedFile;
        return lastVal;
    }

    @SuppressLint("WrongThread")
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        // weitere Requests in der selben Instanz verhindern
        if (alreadyStarted || intent == null)
            return;

        alreadyStarted = true;

        boolean bScanDiloc = intent.getBooleanExtra("scanDiloc", false);
        boolean bScanAgressive = intent.getBooleanExtra("scanAgressive", false);

       if (bScanAgressive) {
            new FileSystemIndexer(Environment.getExternalStorageDirectory()+"/", FileSystemDatabase.getInstance(getApplicationContext())).execute();
       } else if (bScanDiloc) {
           new FileSystemIndexer(Environment.getExternalStorageDirectory()+"/sync/fs-data/", FileSystemDatabase.getInstance(getApplicationContext())).execute();
       } else {
           return;
       }

       new ArbeitsauftragIndexer(FileSystemDatabase.getInstance(getApplicationContext())).execute();


        // Verwendung initialisieren und aus Intent einlesen
        ArrayList<VerwendungClass> verwendung;
        verwendung = intent.getParcelableArrayListExtra("items");
        if (verwendung == null || verwendung.isEmpty())
                return;

        for (VerwendungClass schicht : verwendung) {

            if (schicht.getKat() == null || !schicht.getKat().equals("S")) {
                continue;
            }

            // Nur bei Schichten die nicht mehr als 2 Tage zurück liegen und mehr als 8 Tage in der Zukunft liegen
            long timeDiff = schicht.getDatum() - System.currentTimeMillis()/1000;

            if (timeDiff < -172800 || timeDiff > 691200) {
                continue;
            }

            ArbeitsauftragWithFileEntry arbeitsauftragWithFile = FileSystemDatabase.getInstance(getApplicationContext()).arbeitsauftragEntryDao().getFileForVerwendung(schicht.getBezeichner().replaceAll("[^0-9]", ""), schicht.getDatumDate());

            if (arbeitsauftragWithFile == null) {
                continue;
            }
            // Arbeitsauftrag erzeugen und Auswertung abwarten
            ArbeitsauftragBuilder auftrag = new ArbeitsauftragBuilder(schicht);

            if (auftrag.getExtractedCacheFile() == null || auftrag.getExtractedCacheFile().lastModified() < arbeitsauftragWithFile.file.lastModified) {
                File result = auftrag.extractSourceFile(arbeitsauftragWithFile);

                Log.d("AA", "Arbeitsauftrag erstellt: "+schicht.getBezeichner()+" - "+schicht.getDatumFormatted("dd.MM.yyyy"));

                EventBus.getDefault().post(new ArbeitsauftragResultEvent(result, schicht));

                // Remote Config
                final FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
                // mit Server synchronisieren, falls Arbeitsauftrag erfolgreich extrahiert
                if (result != null && mFirebaseRemoteConfig.getBoolean("allow_arbeitsauftrag_upload")) {

                    OSESBase OSES = new OSESBase(this);

                    String pdf64 = getStringFile(result);
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
}
