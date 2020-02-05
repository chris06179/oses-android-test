package de.stm.oses.index;

import android.annotation.SuppressLint;
import android.util.Log;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

import org.greenrobot.eventbus.EventBus;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.stm.oses.index.database.FileSystemDatabase;
import de.stm.oses.index.entities.ArbeitsauftragEntry;
import de.stm.oses.index.entities.FileSystemArbeitsauftragEntry;
import de.stm.oses.index.entities.FileSystemEntry;

public class ArbeitsauftragIndexer {

    private final FileSystemDatabase mIndex;
    private final FirebaseRemoteConfig mFirebaseRemoteConfig;

    ArbeitsauftragIndexer(FileSystemDatabase database) {
        mIndex = database;
        // Remote Config
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

    }


    @SuppressLint("SwitchIntDef")
    public void execute() {


        List<FileSystemArbeitsauftragEntry> filesWithArbeitsauftrag = mIndex.fileSystemEntryDao().getUnindexedFilesWithArbeitsauftrag();
        for (int i = 0; i < filesWithArbeitsauftrag.size(); i++) {
            if (filesWithArbeitsauftrag.size() > 20)
                EventBus.getDefault().postSticky(new FileSystemIndexer.IndexProgressEvent(i, filesWithArbeitsauftrag.size()-1, "Erstelle Liste mit Arbeitsaufträgen - " + (i+1) + " / " + filesWithArbeitsauftrag.size() + " Dateien durchsucht...", "Es müssen viele neue Dateien überprüft werden, dies kann einige Minuten dauern...", true));

            FileSystemArbeitsauftragEntry databaseFile = filesWithArbeitsauftrag.get(i);
            switch (databaseFile.contentType) {
                case FileSystemEntry.FILECONTENT_EDITH:
                    scanEdith(databaseFile);
                    break;
                case FileSystemEntry.FILECONTENT_MBRAIL:
                    scanMBRAIL(databaseFile);
                    break;
            }

        }

        EventBus.getDefault().postSticky(new FileSystemIndexer.IndexProgressEvent(0, 0, "", "", false));

    }

    private void scanEdith(FileSystemArbeitsauftragEntry databaseFile) {

        try {

            PdfReader read = new PdfReader(new FileInputStream(databaseFile.getFile()));
            PdfReaderContentParser parser = new PdfReaderContentParser(read);

            String schichtEDITH = mFirebaseRemoteConfig.getString("schichtEDITH");
            String datumEDITH = mFirebaseRemoteConfig.getString("datumEDITH");
            String datumsbereichEDITH = mFirebaseRemoteConfig.getString("datumsbereichEDITH");
            String letzteBearbeitungEDITH = mFirebaseRemoteConfig.getString("letzteBearbeitungEDITH");

            Pattern pSchicht = Pattern.compile(schichtEDITH, Pattern.MULTILINE);
            Pattern pDatum = Pattern.compile(datumEDITH, Pattern.DOTALL + Pattern.MULTILINE);
            Pattern pDatumsbereich = Pattern.compile(datumsbereichEDITH, Pattern.MULTILINE);
            Pattern pLetzteBearbeitung = Pattern.compile(letzteBearbeitungEDITH, Pattern.MULTILINE);

            Log.d("AAINDEXER", databaseFile.filename);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.GERMAN);

            ArrayList<ArbeitsauftragEntry> arbeitsauftragList = new ArrayList<>();
            String lastBezeichner = "";

            for (int i = 1; i <= read.getNumberOfPages(); i++) {

                try {

                    TextExtractionStrategy strategy = parser.processContent(i, new SimpleTextExtractionStrategy());
                    String text = strategy.getResultantText();

                    String bezeichner = null;

                    // Bezeichner
                    Matcher bezeichnerMatcher = pSchicht.matcher(text);
                    if (bezeichnerMatcher.find() && bezeichnerMatcher.groupCount() == 1) {
                        bezeichner = bezeichnerMatcher.group(1);
                        Log.d("AAINDEXER Bezeichner", bezeichner != null ? bezeichner : "");
                    }

                    if (bezeichner == null) {
                        continue;
                    }

                    if (!lastBezeichner.equals(bezeichner)) {

                        ArbeitsauftragEntry entry = new ArbeitsauftragEntry();
                        lastBezeichner = bezeichner;

                        entry.bezeichner = bezeichner;
                        entry.fileId = databaseFile.id;

                        // Datum
                        Matcher datumMatcher = pDatum.matcher(text);
                        if (datumMatcher.find() && datumMatcher.groupCount() == 1) {
                            String datum = datumMatcher.group(1);
                            if (datum != null) {
                                entry.datum = dateFormat.parse(datum);
                            }
                        }

                        // Letzte Bearbeitung
                        Matcher letzteBearbeitungMatcher = pLetzteBearbeitung.matcher(text);
                        if (letzteBearbeitungMatcher.find() && letzteBearbeitungMatcher.groupCount() == 1) {
                            String letzteBearbeitung = letzteBearbeitungMatcher.group(1);
                            if (letzteBearbeitung != null) {
                                entry.lastEdit = dateTimeFormat.parse(letzteBearbeitung);
                            }
                        }

                        // Datumsbereich
                        Matcher datumsbereichMatcher = pDatumsbereich.matcher(text);
                        if (datumsbereichMatcher.find() && datumsbereichMatcher.groupCount() == 2) {
                            String datumStart = datumsbereichMatcher.group(1);
                            String datumEnd = datumsbereichMatcher.group(2);
                            if (datumStart != null && datumEnd != null) {
                                entry.datumVon = dateFormat.parse(datumStart);
                                entry.datumBis = dateFormat.parse(datumEnd);
                            }
                        }

                        entry.pages.add(i);

                        arbeitsauftragList.add(entry);

                    } else {

                        // Letzte Bearbeitung
                        Matcher letzteBearbeitungMatcher = pLetzteBearbeitung.matcher(text);
                        if (letzteBearbeitungMatcher.find() && letzteBearbeitungMatcher.groupCount() == 1) {
                            String letzteBearbeitung = letzteBearbeitungMatcher.group(1);
                            if (letzteBearbeitung != null) {
                                arbeitsauftragList.get(arbeitsauftragList.size() - 1).lastEdit = dateTimeFormat.parse(letzteBearbeitung);
                            }
                        }
                        arbeitsauftragList.get(arbeitsauftragList.size() - 1).pages.add(i);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            mIndex.arbeitsauftragEntryDao().insertAll(arbeitsauftragList);

            Log.d("AAINDEXER", String.valueOf(arbeitsauftragList.size()));


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void scanMBRAIL(FileSystemArbeitsauftragEntry databaseFile) {

        try {

            PdfReader read = new PdfReader(new FileInputStream(databaseFile.getFile()));
            PdfReaderContentParser parser = new PdfReaderContentParser(read);

            String schichtMBRAIL = mFirebaseRemoteConfig.getString("schichtMBRAIL");
            String datumMBRAIL = mFirebaseRemoteConfig.getString("datumMBRAIL");
            String letzteBearbeitungMBRAIL = mFirebaseRemoteConfig.getString("letzteBearbeitungMBRAIL");

            Pattern pSchicht = Pattern.compile(schichtMBRAIL, Pattern.MULTILINE);
            Pattern pDatum = Pattern.compile(datumMBRAIL, Pattern.MULTILINE);
            Pattern pLetzteBearbeitung = Pattern.compile(letzteBearbeitungMBRAIL, Pattern.MULTILINE);

            Log.d("AAINDEXER", databaseFile.filename);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

            ArrayList<ArbeitsauftragEntry> arbeitsauftragList = new ArrayList<>();
            String lastBezeichner = "";

            for (int i = 1; i <= read.getNumberOfPages(); i++) {

                try {

                    TextExtractionStrategy strategy = parser.processContent(i, new SimpleTextExtractionStrategy());
                    String text = strategy.getResultantText();

                    String bezeichner = null;

                    // Bezeichner
                    Matcher bezeichnerMatcher = pSchicht.matcher(text);
                    if (bezeichnerMatcher.find() && bezeichnerMatcher.groupCount() == 1) {
                        bezeichner = bezeichnerMatcher.group(1);
                        Log.d("AAINDEXER Bezeichner", bezeichner != null ? bezeichner : "");
                    }

                    if (bezeichner == null) {
                        continue;
                    }

                    if (!lastBezeichner.equals(bezeichner)) {

                        ArbeitsauftragEntry entry = new ArbeitsauftragEntry();
                        lastBezeichner = bezeichner;

                        entry.bezeichner = bezeichner;
                        entry.fileId = databaseFile.id;

                        // Datum
                        Matcher datumMatcher = pDatum.matcher(text);
                        if (datumMatcher.find() && datumMatcher.groupCount() == 1) {
                            String datum = datumMatcher.group(1);
                            if (datum != null) {
                                entry.datum = dateFormat.parse(datum);
                            }
                        }

                        // Letzte Bearbeitung
                        Matcher letzteBearbeitungMatcher = pLetzteBearbeitung.matcher(text);
                        if (letzteBearbeitungMatcher.find() && letzteBearbeitungMatcher.groupCount() == 1) {
                            String letzteBearbeitung = letzteBearbeitungMatcher.group(1);
                            if (letzteBearbeitung != null) {
                                entry.lastEdit = dateFormat.parse(letzteBearbeitung);
                            }
                        }

                        entry.pages.add(i);

                        arbeitsauftragList.add(entry);

                    } else {

                        // Letzte Bearbeitung
                        Matcher letzteBearbeitungMatcher = pLetzteBearbeitung.matcher(text);
                        if (letzteBearbeitungMatcher.find() && letzteBearbeitungMatcher.groupCount() == 1) {
                            String letzteBearbeitung = letzteBearbeitungMatcher.group(1);
                            if (letzteBearbeitung != null) {
                                arbeitsauftragList.get(arbeitsauftragList.size() - 1).lastEdit = dateFormat.parse(letzteBearbeitung);
                            }
                        }
                        arbeitsauftragList.get(arbeitsauftragList.size() - 1).pages.add(i);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            mIndex.arbeitsauftragEntryDao().insertAll(arbeitsauftragList);

            Log.d("AAINDEXER", String.valueOf(arbeitsauftragList.size()));


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
