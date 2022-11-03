package de.stm.oses.arbeitsauftrag;

import android.content.Context;

import java.io.File;

import de.stm.oses.verwendung.VerwendungClass;

public class ArbeitsauftragBuilder {

    public static final int TYPE_NONE = 0;
    public static final int TYPE_CACHED = 2;
    public static final int TYPE_ONLINE = 3;

    private final VerwendungClass verwendung;

    public ArbeitsauftragBuilder(VerwendungClass verwendung) {
        this.verwendung = verwendung;
    }

    public File getExtractedCacheFile(Context context) {

        File cache = new File(context.getCacheDir(), "/Dokumente/Arbeitsaufträge/" + verwendung.getDatumFormatted("yyyy/MM - MMMM") + "/Arbeitsauftrag_" + verwendung.getDatumFormatted("dd.MM.yyyy_EE").replaceAll(".$", "") + "_" + verwendung.getBezeichner().replaceAll("[^A-Za-z0-9]", "_") + ".pdf");

        if (cache.exists())
            return cache;
        else
            return null;

    }

}
