package de.stm.oses.arbeitsauftrag;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;

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
