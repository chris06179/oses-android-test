package de.stm.oses.arbeitsauftrag;

import android.os.Environment;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import de.stm.oses.verwendung.VerwendungClass;

public class ArbeitsauftragBuilder {

    public static final int TYPE_NONE = 0;
    public static final int TYPE_DILOC = 1;
    public static final int TYPE_CACHED = 2;
    public static final int TYPE_ONLINE = 3;
    public static final int TYPE_EXTRACTING = 4;

    private static final int DOCTYPE_UNKNOWN = -1;
    private static final int DOCTYPE_EDITH = 0;
    private static final int DOCTYPE_SBAHN_A = 1;

    private VerwendungClass verwendung;

    public ArbeitsauftragBuilder(VerwendungClass verwendung) {
        this.verwendung = verwendung;
    }

    private List<File> findFile(File aFile, String[] toFind) {
        List<File> result = new ArrayList<>();
        if (aFile.isFile() && aFile.getName().endsWith(".pdf") && (aFile.length() / 1024 / 1024) < 25) {
            if (toFind != null) {
                String fileName = aFile.getAbsolutePath().replaceAll("[^A-Za-z0-9]", "");
                for (String s : toFind) {
                    if (fileName.contains(s)) {
                        result.add(aFile);
                        break;
                    }
                }
            } else {
                result.add(aFile);
            }
        } else if (aFile.isDirectory()) {
            for (File child : aFile.listFiles()) {
                result.addAll(findFile(child, toFind));
            }
        }
        return result;
    }


    private List<File> getDilocSourceFiles() {

        File cache = getExtractedCacheFile();

        // Alle PDF Dokumente mit Datumsbezug
        File dilocDirectory = new File(Environment.getExternalStorageDirectory(), "/sync/data/");
        String[] searchDates = new String[]{verwendung.getDatumFormatted("ddMMyyyy"), verwendung.getDatumFormatted("ddMMyy"), verwendung.getDatumFormatted("yyyyMMdd"), verwendung.getDatumFormatted("yyMMdd")};
        List<File> possibleFiles = findFile(dilocDirectory, searchDates);

        // Alle PDF Dokumente im Postfach (ohne Datumsbezug, Priorität)
        File userfileDirectory = new File(Environment.getExternalStorageDirectory(), "/sync/data/userfilebox/");
        possibleFiles.addAll(0, findFile(userfileDirectory, null));

        List<File> dilocArbeitsauftraege = new ArrayList<>();

        // Aussortieren wenn nicht verändert
        for (File file : possibleFiles) {
            if (cache == null || cache.lastModified() < file.lastModified()) {
                dilocArbeitsauftraege.add(file);
            }
        }

        return dilocArbeitsauftraege;

    }

    private List<File> getOSESSourceFiles() {

        File cache = getExtractedCacheFile();

        File searchDirectory = new File(Environment.getExternalStorageDirectory(), "/OSES/Dokumente/Grundschichten/");

        List<File> possibleFiles = findFile(searchDirectory, null);
        List<File> OSESArbeitsaufträge = new ArrayList<>();

        // Aussortieren, logische Reihenfolge
        for (File file : possibleFiles) {
            if (cache == null || cache.lastModified() < file.lastModified()) {
                if (file.getName().toLowerCase().contains(verwendung.getEst().toLowerCase()))
                    OSESArbeitsaufträge.add(0, file);
                else
                    OSESArbeitsaufträge.add(file);
            }
        }

        return OSESArbeitsaufträge;

    }

    private List<File> getDeviceSourceFiles() {

        File cache = getExtractedCacheFile();

        File searchDirectory = new File(Environment.getExternalStorageDirectory(), "/");

        List<File> possibleFiles = findFile(searchDirectory, null);
        List<File> DeviceArbeitsaufträge = new ArrayList<>();

        // Aussortieren, logische Reihenfolge
        for (File file : possibleFiles) {
            if ((cache == null || cache.lastModified() < file.lastModified()) && !file.getAbsolutePath().contains("/OSES/Dokumente/Arbeitsaufträge/")) {
                if (file.getName().toLowerCase().contains(verwendung.getEst().toLowerCase()) || file.getName().toLowerCase().contains("schicht") || file.getAbsolutePath().toLowerCase().contains("arbeitsauftr"))
                    DeviceArbeitsaufträge.add(0, file);
                else
                    DeviceArbeitsaufträge.add(file);
            }
        }

        return DeviceArbeitsaufträge;

    }

    File extractSourceFile(boolean bScanDiloc, boolean bScanOSES, boolean bScanAgressive) {

        List<File> files = new ArrayList<>();

        if (bScanAgressive) {
            files.addAll(getDeviceSourceFiles());
        } else {
            if (bScanDiloc) {
                files.addAll(getDilocSourceFiles());
            }
            if (bScanOSES) {
                files.addAll(getOSESSourceFiles());
            }
        }

        int count = 1;

        Log.d("AA", "Start: " + verwendung.getBezeichner());
        for (File pdf : files) {
            try {

                int docType = DOCTYPE_UNKNOWN;

                Log.d("AA", String.valueOf(count) + " / " + String.valueOf(files.size()) + ": " + pdf.getName());
                count++;

                EventBus.getDefault().post(new ArbeitsauftragIntentService.ArbeitsauftragProgressEvent(verwendung.getId()));

                PdfReader read;
                read = new PdfReader(new FileInputStream(pdf));
                PdfReaderContentParser parser;
                parser = new PdfReaderContentParser(read);
                TextExtractionStrategy strategy;

                ArrayList<Integer> pages = new ArrayList<>();

                int lastpageadded = -1;

                String bezeichner = Pattern.quote(verwendung.getBezeichner().replaceAll("[^0-9]", ""));
                Pattern pIsArbeitsauftragEDITH = Pattern.compile(".*Schicht:\\s*[VF]?\\s*[0-9]*\\s*([(][VF]\\s*[0-9]*[)])?$.*", Pattern.DOTALL + Pattern.MULTILINE);
                Pattern pIsArbeitsauftragSBAHNA = Pattern.compile(".*Schicht\\n[0-9]+$.*", Pattern.DOTALL + Pattern.MULTILINE);

                Pattern pSchicht = null;
                Pattern pDatum = null;
                Pattern pDatumsbereich = null;


                for (int i = 1; i <= read.getNumberOfPages(); i++) {

                    strategy = parser.processContent(i, new SimpleTextExtractionStrategy());
                    String text = strategy.getResultantText();

                    if (docType == DOCTYPE_UNKNOWN) { // Prüfe auf den ersten Seiten ob es sich um ein Dokument mit Arbeitsaufträgen handelt
                        if (pIsArbeitsauftragEDITH.matcher(text).matches())
                            docType = DOCTYPE_EDITH;
                        if (pIsArbeitsauftragSBAHNA.matcher(text).matches())
                            docType = DOCTYPE_SBAHN_A;
                    }

                    Log.d("AA", "Seite: "+String.valueOf(i)+" - docType: "+String.valueOf(docType));

                    if (docType == DOCTYPE_UNKNOWN) {
                        if (i < 4) {
                            continue;
                        } else {
                            break;
                        }
                    }





                    switch (docType) {
                        case DOCTYPE_EDITH:
                            pSchicht = Pattern.compile(".*Schicht:\\s*[VF]?\\s*" + bezeichner + "\\s*([(][VF]\\s*[0-9]*[)])?$.*", Pattern.DOTALL + Pattern.MULTILINE);
                            pDatum = Pattern.compile(".*^Gültig am:$.*^.*" + Pattern.quote(verwendung.getDatumFormatted("dd.MM.yyyy")) + "$.*", Pattern.DOTALL + Pattern.MULTILINE);
                            pDatumsbereich = Pattern.compile(".*^\\d{2}\\.\\d{2}\\.\\d{4}\\s-\\s\\d{2}\\.\\d{2}\\.\\d{4}$.*", Pattern.DOTALL + Pattern.MULTILINE);
                            break;
                        case DOCTYPE_SBAHN_A:
                            pSchicht = Pattern.compile(".*Schicht\\n" +bezeichner+ "$.*", Pattern.DOTALL + Pattern.MULTILINE);
                            pDatum = Pattern.compile(".*[A-Za-z]{2},\\sden\\s" + Pattern.quote(verwendung.getDatumFormatted("dd.MM.yyyy")) + "$.*", Pattern.DOTALL + Pattern.MULTILINE);
                            pDatumsbereich = Pattern.compile(".*^\\d{2}\\.\\d{2}\\.\\d{4}\\s-\\s\\d{2}\\.\\d{2}\\.\\d{4}$.*", Pattern.DOTALL + Pattern.MULTILINE); // TODO
                    }

                    // Auf Schichtbezeichnung und Datum prüfen
                    // bei Datumsbereich -> Datum ignorieren und ersten Treffer extrahieren

                    if (pSchicht.matcher(text).matches() && (pDatum.matcher(text).matches() || pDatumsbereich.matcher(text).matches())) {
                        pages.add(i);
                        lastpageadded = i;
                    } else {
                        if (lastpageadded != -1 && lastpageadded < i)
                            break;
                    }
                }

                if (pages.size() == 0) {
                    continue;
                }

                read.selectPages(pages);

                String path = Environment.getExternalStorageDirectory().getPath() + "/OSES/Dokumente/Arbeitsaufträge/" + verwendung.getDatumFormatted("yyyy/MM - MMMM") + "/Arbeitsauftrag_" + verwendung.getDatumFormatted("dd.MM.yyyy_EE").replaceAll(".$", "") + "_" + verwendung.getBezeichner().replaceAll("[^A-Za-z0-9]", "_") + ".pdf";

                File file = new File(path);
                file.getParentFile().mkdirs();

                PdfStamper pdfStamper = new PdfStamper(read,
                        new FileOutputStream(path));
                pdfStamper.close();

                System.gc();

                return file;

            } catch (Exception | OutOfMemoryError | NoClassDefFoundError e) {
                System.gc();
                Crashlytics.setString("Bezeichner", verwendung.getBezeichner());
                Crashlytics.setString("Datum", verwendung.getDatumFormatted("dd.MM.yyyy"));
                Crashlytics.setString("Est", verwendung.getEst());
                Crashlytics.setString("File", pdf.getAbsolutePath());
                Crashlytics.logException(e);
            }
        }

        return null;

    }

    public File getExtractedCacheFile() {

        File cache = new File(Environment.getExternalStorageDirectory().getPath() + "/OSES/Dokumente/Arbeitsaufträge/" + verwendung.getDatumFormatted("yyyy/MM - MMMM") + "/Arbeitsauftrag_" + verwendung.getDatumFormatted("dd.MM.yyyy_EE").replaceAll(".$", "") + "_" + verwendung.getBezeichner().replaceAll("[^A-Za-z0-9]", "_") + ".pdf");

        if (cache.exists())
            return cache;
        else
            return null;

    }

}
