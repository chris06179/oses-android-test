package de.stm.oses.arbeitsauftrag;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.stm.oses.R;
import de.stm.oses.verwendung.VerwendungClass;

public class ArbeitsauftragBuilder {

    public static final int TYPE_NONE = 0;
    public static final int TYPE_DILOC = 1;
    public static final int TYPE_CACHED = 2;
    public static final int TYPE_ONLINE = 3;

    private final Context context;

    private VerwendungClass verwendung;

    public class ArbeitsauftragNotFoundException extends Exception {
        @Override
        public String getMessage() {
            return "Es wurde in der bereitgestellten PDF-Datei kein passender Arbeitsauftrag gefunden!";
        }
    }

    private static int getResId(String resName, Class<?> c) {

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

        List<String> path;
        path = verwendung.getDilocSearchPath();

        if (path.isEmpty()) {

            int pathResource = getResId("diloc_" + verwendung.getEst(), R.string.class);


            if (pathResource == -1) {
                pathResource = getResId("diloc_" + verwendung.getEst(), R.array.class);

                if (pathResource == -1)
                    return null;

                path = Arrays.asList(context.getResources().getStringArray(pathResource));
            } else {
                path = new ArrayList<>();
                path.add(context.getResources().getString(pathResource));
            }
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

//    Object extractFromDilocSourceFile() {
//
//        try {
//
//            File pdf = getDilocSourceFile();
//
//            PDFBoxResourceLoader.init(context);
//            PDFTextStripper stripper = new PDFTextStripper();
//
//            PDDocument doc = PDDocument.load(pdf);
//
//            ArrayList<Integer> pages = new ArrayList<>();
//
//            int lastpageadded = 0;
//
//            for (int i = 0; i <= doc.getNumberOfPages()-1; i++) {
//
//                EventBus.getDefault().post(new ArbeitsauftragDilocIntentService.ArbeitsauftragProgressEvent(verwendung.getId(), doc.getNumberOfPages(), i+1));
//
//                PDDocument page = new PDDocument();
//                page.addPage(doc.getPage(i));
//                String text = stripper.getText(page);
//                page.close();
//
//                if (text.contains("Schicht: " + verwendung.getBezeichner()) || text.contains("Schicht:" + verwendung.getBezeichner())) {
//                    pages.add(i);
//                    lastpageadded = i;
//                } else {
//                    if (lastpageadded != 0 && lastpageadded < i)
//                        break;
//                }
//            }
//
//            if (pages.size() == 0) {
//                File cache = getExtractedCacheFile();
//                if (cache != null)
//                    return cache;
//                else
//                    return new ArbeitsauftragNotFoundException();
//            }
//
//            EventBus.getDefault().post(new ArbeitsauftragDilocIntentService.ArbeitsauftragProgressEvent(verwendung.getId(), doc.getNumberOfPages(), doc.getNumberOfPages()));
//
//            PDDocument auftrag = new PDDocument();
//
//            for (int page : pages)
//                auftrag.addPage(doc.getPage(page));
//
//            String path = Environment.getExternalStorageDirectory().getPath() + "/OSES/Dokumente/Arbeitsaufträge/" + verwendung.getDatumFormatted("yyyy/MM - MMMM") + "/Arbeitsauftrag_" + verwendung.getDatumFormatted("dd.MM.yyyy_EE").replaceAll(".$", "") + "_" + verwendung.getBezeichner() + ".pdf";
//
//            File file = new File(path);
//            file.getParentFile().mkdirs();
//
//            auftrag.save(file);
//            auftrag.close();
//            doc.close();
//
//            return file;
//
//        } catch (IOException e) {
//            FirebaseCrash.report(e);
//            return e;
//        }
//
//    }

    Object extractFromDilocSourceFile() {

        try {

            File pdf = getDilocSourceFile();

            PdfReader read;
            read = new PdfReader(new FileInputStream(pdf));
            PdfReaderContentParser parser;
            parser = new PdfReaderContentParser(read);
            TextExtractionStrategy strategy;

            ArrayList<Integer> pages = new ArrayList<>();

            int lastpageadded = 0;

            String bezeichner = Pattern.quote(verwendung.getBezeichner().replaceAll("[^0-9]", ""));
            Pattern p1 = Pattern.compile(".*Schicht:\\s*[VF]?\\s*" + bezeichner + "\\s*([(][VF]\\s*[0-9]*[)])?\\n.*", Pattern.DOTALL);
            Pattern p2 = Pattern.compile(".*Gültig am:\\n.*\\n.*\\n.*"+ Pattern.quote(verwendung.getDatumFormatted("dd.MM.yyyy")) +"\\n.*", Pattern.DOTALL);

            for (int i = 1; i <= read.getNumberOfPages(); i++) {

                EventBus.getDefault().post(new ArbeitsauftragDilocIntentService.ArbeitsauftragProgressEvent(verwendung.getId(), read.getNumberOfPages(), i));

                strategy = parser.processContent(i, new SimpleTextExtractionStrategy());
                String text = strategy.getResultantText();

                Log.d("AA", text);

                if (p1.matcher(text).matches() && p2.matcher(text).matches()) {
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
                    return new ArbeitsauftragNotFoundException();
            }

            EventBus.getDefault().post(new ArbeitsauftragDilocIntentService.ArbeitsauftragProgressEvent(verwendung.getId(), read.getNumberOfPages(), read.getNumberOfPages()));

            read.selectPages(pages);

            String path = Environment.getExternalStorageDirectory().getPath() + "/OSES/Dokumente/Arbeitsaufträge/" + verwendung.getDatumFormatted("yyyy/MM - MMMM") + "/Arbeitsauftrag_" + verwendung.getDatumFormatted("dd.MM.yyyy_EE").replaceAll(".$", "") + "_" + verwendung.getBezeichner().replaceAll("\\s", "_") + ".pdf";

            File file = new File(path);
            file.getParentFile().mkdirs();

            PdfStamper pdfStamper = new PdfStamper(read,
                    new FileOutputStream(path));
            pdfStamper.close();

            return file;

        } catch (IOException | DocumentException e) {
            FirebaseCrash.report(e);
            return e;
        }

    }

    public File getExtractedCacheFile() {

        File cache = new File(Environment.getExternalStorageDirectory().getPath() + "/OSES/Dokumente/Arbeitsaufträge/" + verwendung.getDatumFormatted("yyyy/MM - MMMM") + "/Arbeitsauftrag_" + verwendung.getDatumFormatted("dd.MM.yyyy_EE").replaceAll(".$", "") + "_" + verwendung.getBezeichner().replaceAll("\\s", "_") + ".pdf");

        if (cache.exists())
            return cache;
        else
            return null;

    }

}
