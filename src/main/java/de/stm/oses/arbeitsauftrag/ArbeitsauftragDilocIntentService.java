package de.stm.oses.arbeitsauftrag;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.stm.oses.helper.FileDownload;
import de.stm.oses.helper.OSESBase;
import de.stm.oses.helper.OSESRequest;
import de.stm.oses.verwendung.VerwendungClass;


public class ArbeitsauftragDilocIntentService extends IntentService {

    public static class ArbeitsauftragProgressEvent {
        public int id;
        public int max;
        public int progress;

        ArbeitsauftragProgressEvent(int id, int max, int progress) {
            this.id = id;
            this.max = max;
            this.progress = progress;
        }
    }

    public static class ArbeitsauftragResultEvent {
        public VerwendungClass schicht;
        public Object result;

        ArbeitsauftragResultEvent(Object result, VerwendungClass schicht) {
            this.result = result;
            this.schicht = schicht;
        }
    }

    public ArbeitsauftragDilocIntentService() {
        super("ArbeitsauftragDilocIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        VerwendungClass schicht;

        if (intent != null) {
            schicht = intent.getParcelableExtra("item");
            if (schicht == null)
                return;
        }
        else {
            return;
        }

        ArbeitsauftragBuilder auftrag = new ArbeitsauftragBuilder(schicht, getApplicationContext());
        Object result = auftrag.extractFromDilocSourceFile();
        EventBus.getDefault().post(new ArbeitsauftragResultEvent(result, schicht));


    }
}
