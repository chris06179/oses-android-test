package de.stm.oses.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.stm.oses.R;
import de.stm.oses.verwendung.VerwendungClass;

/**
 * Created by Christian on 30.01.2017.
 */

public class ArbeitsauftragBuilder {

    public static final int TYPE_NONE = 0;
    public static final int TYPE_DILOC = 1;
    public static final int TYPE_CACHED = 2;
    private final Context context;

    private VerwendungClass verwendung;

    public interface OnProgressPublisher {
        void onPublishProgress(int progress, int found);
    }

    public static int getResId(String resName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public ArbeitsauftragBuilder(VerwendungClass verwendung, Context context) {
        this.context = context;
        this.verwendung = verwendung;
    }

    public File getDilocSourceFile() {

        int pathResource = getResId("diloc_" + verwendung.getEst(), R.string.class);

        List<String> path;

        if (pathResource == -1) {
            pathResource = getResId("diloc_" + verwendung.getEst(), R.array.class);

            if (pathResource == -1)
                return null;

            path = Arrays.asList(context.getResources().getStringArray(pathResource));
        } else {
            path = new ArrayList<>();
            path.add(context.getResources().getString(pathResource));
        }

        File pdf = null;

        search:
        {
            for (String dir : path) {

                File directory = new File(Environment.getExternalStorageDirectory(), dir);

                if (!directory.exists())
                    continue;

                File[] files = directory.listFiles();


                String searchdate = verwendung.getDatumFormatted("dd.MM.yyyy");
                String searchdate2 = verwendung.getDatumFormatted("dd.MM.yy");
                String searchdate3 = verwendung.getDatumFormatted("yyyyMMdd");
                String searchdate4 = verwendung.getDatumFormatted("yyMMdd");

                for (File file : files) {
                    if (file.getName().contains(searchdate) || file.getName().contains(searchdate2) || file.getName().contains(searchdate3) || file.getName().contains(searchdate4)) {
                        pdf = file;
                        break search;
                    }
                }

            }
        }

        if (pdf == null)
            return null;

        if (pdf.exists())
            return pdf;
        else
            return null;

    }

    public File extractFromDilocSourceFile(OnProgressPublisher listener, ProgressDialogFragment dialogFragment) {

        try {

            File pdf = getDilocSourceFile();

            PdfReader read;
            read = new PdfReader(new FileInputStream(pdf));

            dialogFragment.getDialog().setProgressNumberFormat("Seite %1d / %2d");
            dialogFragment.getDialog().setMax(read.getNumberOfPages());
            dialogFragment.getDialog().setIndeterminate(false);


            PdfReaderContentParser parser;
            parser = new PdfReaderContentParser(read);
            TextExtractionStrategy strategy;

            ArrayList<Integer> pages = new ArrayList<>();

            int lastpageadded = 0;
            for (int i = 1; i <= read.getNumberOfPages(); i++) {
                if (dialogFragment.getDialog() == null)
                    return null;

                listener.onPublishProgress(i, 0);

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

            listener.onPublishProgress(read.getNumberOfPages(), 1);

            read.selectPages(pages);

            String path = Environment.getExternalStorageDirectory().getPath() + "/OSES/docs/Arbeitsaufträge/Arbeitsauftrag_" + verwendung.getBezeichner() + "_" + verwendung.getDatumFormatted("dd.MM.yyyy") + ".pdf";

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

        File cache = new File(Environment.getExternalStorageDirectory().getPath() + "/OSES/docs/Arbeitsaufträge/Arbeitsauftrag_" + verwendung.getBezeichner() + "_" + verwendung.getDatumFormatted("dd.MM.yyyy") + ".pdf");

        if (cache.exists())
            return cache;
        else
            return null;

    }

}
