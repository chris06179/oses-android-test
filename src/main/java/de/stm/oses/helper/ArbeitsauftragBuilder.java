package de.stm.oses.helper;

import android.os.Environment;

import com.google.firebase.crash.FirebaseCrash;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import de.stm.oses.verwendung.VerwendungClass;

/**
 * Created by Christian on 30.01.2017.
 */

public class ArbeitsauftragBuilder {

    public static final int TYPE_NONE= 0;
    public static final int TYPE_DILOC = 1;
    public static final int TYPE_CACHED = 2;

    private VerwendungClass verwendung;

    public ArbeitsauftragBuilder(VerwendungClass verwendung) {
        this.verwendung = verwendung;
    }

    public File getDilocSourceFile() {

        String searchest;

        // Prototype
        switch (verwendung.getEst()) {
            case "NMER":
                searchest = "NNA";
                break;
            case "NGFG":
                searchest = "NNP";
                break;
            default:
                searchest = verwendung.getEst();
        }

        File directory = new File(Environment.getExternalStorageDirectory(),"/sync/data/Mitarbeiterhefte/Lokführer/08_Einsatzplanung_Tf/");

        if (!directory.exists())
            return null;

        File[] files = directory.listFiles();

        String nextdir = null;

        for (File file : files) {
            if (file.getName().contains(searchest)) {
                nextdir = file.getName();
                break;
            }
        }

        if (nextdir == null)
            return null;

        directory = new File(Environment.getExternalStorageDirectory(),"/sync/data/Mitarbeiterhefte/Lokführer/08_Einsatzplanung_Tf/"+nextdir+"/02_Arbeitsauftrag/");

        if (!directory.exists())
            return null;

        files = directory.listFiles();

        String filename = null;
        String searchdate = verwendung.getDatumFormatted("dd.MM.yyyy");
        String searchdate2 = verwendung.getDatumFormatted("dd.MM.yy");

        for (File file : files) {
            if (file.getName().contains(searchdate) || file.getName().contains(searchdate2)) {
                filename = file.getName();
                break;
            }
        }

        if (filename == null)
            return null;

        File pdf = new File(Environment.getExternalStorageDirectory(),"/sync/data/Mitarbeiterhefte/Lokführer/08_Einsatzplanung_Tf/"+nextdir+"/02_Arbeitsauftrag/"+filename);

        if (pdf.exists())
            return pdf;
        else
            return null;

    }

    public File extractFromDilocSourceFile() {

        try {

            File pdf = getDilocSourceFile();

            PdfReader read;
            read = new PdfReader(new FileInputStream(pdf));

            PdfReaderContentParser parser;
            parser = new PdfReaderContentParser(read);
            TextExtractionStrategy strategy;

            ArrayList<Integer> pages = new ArrayList<>();

            int lastpageadded = 0;
            for (int i = 1; i <= read.getNumberOfPages(); i++) {
                strategy = parser.processContent(i, new SimpleTextExtractionStrategy());
                String text = strategy.getResultantText();

                if (text.contains("Schicht: " + verwendung.getBezeichner())) {
                    pages.add(i);
                    lastpageadded = i;
                } else {
                    if (lastpageadded != 0 && lastpageadded < i)
                        break;
                }
            }

            if (pages.size() == 0) {
                File cache = getExtractedCacheFile();
                if (cache != null)
                    return cache;
                else
                    return null;
            }


            read.selectPages(pages);

            String path = Environment.getExternalStorageDirectory().getPath()+"/OSES/docs/Arbeitsaufträge/Arbeitsauftrag_"+verwendung.getBezeichner()+"_"+verwendung.getDatumFormatted("dd.MM.yyyy")+".pdf";

            File file = new File(path);
            file.getParentFile().mkdirs();

            PdfStamper pdfStamper = new PdfStamper(read,
                    new FileOutputStream(path));
            pdfStamper.close();

            return file;

        } catch (IOException | DocumentException e) {
            FirebaseCrash.report(e);
            return null;
        }

    }

    public File getExtractedCacheFile() {

        File cache = new File(Environment.getExternalStorageDirectory().getPath()+"/OSES/docs/Arbeitsaufträge/Arbeitsauftrag_"+verwendung.getBezeichner()+"_"+verwendung.getDatumFormatted("dd.MM.yyyy")+".pdf");

        if (cache.exists())
            return cache;
        else
            return null;

    }

}
