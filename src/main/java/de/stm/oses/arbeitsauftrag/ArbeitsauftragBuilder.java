package de.stm.oses.arbeitsauftrag;

import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import de.stm.oses.index.entities.ArbeitsauftragWithFileEntry;
import de.stm.oses.verwendung.VerwendungClass;

public class ArbeitsauftragBuilder {

    public static final int TYPE_NONE = 0;
    public static final int TYPE_DILOC = 1;
    public static final int TYPE_CACHED = 2;
    public static final int TYPE_ONLINE = 3;
    public static final int TYPE_EXTRACTING = 4;

    private final VerwendungClass verwendung;
    private final FirebaseCrashlytics mCrashlytics;

    public ArbeitsauftragBuilder(VerwendungClass verwendung) {
        this.verwendung = verwendung;
        mCrashlytics = FirebaseCrashlytics.getInstance();
    }

    private void cleanPdfFile(PdfReader read) throws IOException {

        // Aufräumen - PDF Splitter - eingebettete Dateien
        PdfDictionary root = read.getCatalog();
        PdfDictionary names = root.getAsDict(PdfName.NAMES);
        if (names != null)
            names.remove(PdfName.EMBEDDEDFILES);

        // Aufräumen - Wasserzeichen entfernen
        for (int i = 1; i <= read.getNumberOfPages(); i++) {
            PdfDictionary dict = read.getPageN(i);
            PdfArray contents = dict.getAsArray(PdfName.CONTENTS);

            if (contents == null) {

                PdfObject content = dict.getAsIndirectObject(PdfName.CONTENTS);

                if (content == null)
                    continue;

                contents = new PdfArray();
                contents.add(content);

            }


            for (int k = 0; k < contents.size(); k++) {
                PdfObject object = contents.getDirectObject(k);

                if (object instanceof PRStream) {
                    PRStream stream = (PRStream) object;

                    String data = new String(PdfReader.getStreamBytes(stream), "ISO-8859-2");

                    if (data.contains("www.A-PDF.com")) {
                        contents.remove(k);
                        Log.d("AA", "Wasserzeichen entfernt!");
                    }
                }
            }
        }

        read.removeUnusedObjects();
    }

    public File extractSourceFile(@NonNull ArbeitsauftragWithFileEntry arbeitsauftragWithFile) {

        try {

            PdfReader read = new PdfReader(new FileInputStream(arbeitsauftragWithFile.file.getFile()));
            PdfReader.unethicalreading = true;
            read.selectPages(arbeitsauftragWithFile.arbeitsauftrag.pages);

            try {
                cleanPdfFile(read);
            } catch (IOException ignored) {

            }

            String path = Environment.getExternalStorageDirectory() + "/OSES/Dokumente/Arbeitsaufträge/" + verwendung.getDatumFormatted("yyyy/MM - MMMM") + "/Arbeitsauftrag_" + verwendung.getDatumFormatted("dd.MM.yyyy_EE").replaceAll(".$", "") + "_" + verwendung.getBezeichner().replaceAll("[^A-Za-z0-9]", "_") + ".pdf";

            File file = new File(path);
            file.getParentFile().mkdirs();

            PdfStamper pdfStamper = new PdfStamper(read, new FileOutputStream(path));
            pdfStamper.close();

            return file;

        } catch (OutOfMemoryError | NoClassDefFoundError | DocumentException | IOException e) {
            mCrashlytics.setCustomKey("Bezeichner", verwendung.getBezeichner());
            mCrashlytics.setCustomKey("Datum", verwendung.getDatumFormatted("dd.MM.yyyy"));
            mCrashlytics.setCustomKey("Est", verwendung.getEst());
            mCrashlytics.setCustomKey("File", arbeitsauftragWithFile.file.getFile().getAbsolutePath());
            mCrashlytics.recordException(e);
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
