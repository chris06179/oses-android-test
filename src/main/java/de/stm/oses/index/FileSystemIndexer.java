package de.stm.oses.index;

import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

import androidx.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.stm.oses.index.database.FileSystemDatabase;
import de.stm.oses.index.entities.ArbeitsauftragEntry;
import de.stm.oses.index.entities.FileSystemEntry;
import de.stm.oses.notification.NotificationHelper;

public class FileSystemIndexer {

    private final FileSystemDatabase mIndex;
    private final String mPath;
    private final FirebaseRemoteConfig mFirebaseRemoteConfig;
    private final NotificationHelper mNotificationHelper;
    private String mAppTitle;
    private boolean isStopped;

    FileSystemIndexer(String path, FileSystemDatabase database, NotificationHelper notificationHelper, String appTitle) {
        mIndex = database;
        mPath = path;
        mNotificationHelper = notificationHelper;
        mAppTitle = appTitle;
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    }

    void doStopCurrentWork() {
      isStopped = true;
    }

    private boolean isStopped() {
        return isStopped;
    }

    public void execute() {

        if (!isStopped())
            checkUpdatedFiles();
        if (!isStopped())
            addNewFiles();
        if (!isStopped())
            updateContentType();

        mNotificationHelper.removeIndexNotification();

    }

    private List<File> findFile(File aFile) {
        List<File> result = new ArrayList<>();
        if (aFile.isFile()) {
            result.add(aFile);
        } else if (aFile.isDirectory()) {
            for (File child : aFile.listFiles()) {
                result.addAll(findFile(child));
            }
        }
        return result;
    }

    private void checkUpdatedFiles() {

        for (FileSystemEntry databaseFile : mIndex.fileSystemEntryDao().getAll()) {
            File fsFile = new File(databaseFile.path + "/" + databaseFile.filename);
            if (!fsFile.exists() || (fsFile.lastModified() != databaseFile.lastModified)) {
                mIndex.fileSystemEntryDao().delete(databaseFile);
            }
        }

    }


    private void addNewFiles() {

        mNotificationHelper.showIndexNotification(mAppTitle,"Prüfe auf neue Dateien...", 0,0);

        File directory = new File(mPath);
        List<File> fsFiles = findFile(directory);

        for (File fsFile : fsFiles) {

            FileSystemEntry entry = new FileSystemEntry();

            entry.filename = fsFile.getName();
            entry.filetype = fsFile.getName().substring(fsFile.getName().lastIndexOf(".") + 1).toLowerCase();
            entry.path = fsFile.getParent();
            entry.lastModified = fsFile.lastModified();

            try {
                mIndex.fileSystemEntryDao().insert(entry);
            } catch (SQLiteConstraintException ignored) {

            }

        }

    }

    @FileSystemEntry.FileContent
    private int checkFileContent(FileSystemEntry databaseFile) {

        File file = databaseFile.getFile();

        if (!file.exists())
            return FileSystemEntry.FILECONTENT_OTHER;

        if (!file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase().equals("pdf"))
            return FileSystemEntry.FILECONTENT_OTHER;

        if (file.length() / (1024 * 1024) > FirebaseRemoteConfig.getInstance().getLong("indexer_max_filesize"))
            return FileSystemEntry.FILECONTENT_OTHER;

        try {

            PdfReader read = new PdfReader(new FileInputStream(file));
            PdfReaderContentParser parser = new PdfReaderContentParser(read);

            String isArbeitsauftragEDITH = mFirebaseRemoteConfig.getString("isArbeitsauftragEDITH");
            String isArbeitsauftragMBRAIL = mFirebaseRemoteConfig.getString("isArbeitsauftragMBRAIL");

            Pattern pIsArbeitsauftragEDITH = Pattern.compile(isArbeitsauftragEDITH, Pattern.DOTALL + Pattern.MULTILINE);
            Pattern pIsArbeitsauftragMBRAIL = Pattern.compile(isArbeitsauftragMBRAIL, Pattern.DOTALL + Pattern.MULTILINE);

            // Prüfe auf den ersten 4 Seiten ob es sich um ein Dokument mit Arbeitsaufträgen handelt
            for (int k = 1; k <= read.getNumberOfPages(); k++) {

                if (isStopped())
                    return FileSystemEntry.FILECONTENT_UNKNOWN;

                TextExtractionStrategy strategy = parser.processContent(k, new SimpleTextExtractionStrategy());
                String text = strategy.getResultantText();

                if (pIsArbeitsauftragEDITH.matcher(text).matches()) {
                    List<ArbeitsauftragEntry> result = scanEdith(read, parser, databaseFile);
                    if (isStopped())
                        return FileSystemEntry.FILECONTENT_UNKNOWN;
                    if (result.isEmpty()) {
                        return FileSystemEntry.FILECONTENT_OTHER;
                    } else {
                        mIndex.arbeitsauftragEntryDao().insertAll(result);
                        return FileSystemEntry.FILECONTENT_EDITH;
                    }
                }

                if (pIsArbeitsauftragMBRAIL.matcher(text).matches()) {
                    List<ArbeitsauftragEntry> result = scanMBRail(read, parser, databaseFile);
                    if (isStopped())
                        return FileSystemEntry.FILECONTENT_UNKNOWN;
                    if (result.isEmpty()) {
                        return FileSystemEntry.FILECONTENT_OTHER;
                    } else {
                        mIndex.arbeitsauftragEntryDao().insertAll(result);
                        return FileSystemEntry.FILECONTENT_MBRAIL;
                    }
                }

                if (k == 10) {
                    break;
                }

            }

        } catch (Exception | OutOfMemoryError | NoClassDefFoundError e) {
            Crashlytics.setString("File", file.getAbsolutePath());
            Crashlytics.logException(e);
            return FileSystemEntry.FILECONTENT_EXCEPTION;
        }

        return  FileSystemEntry.FILECONTENT_OTHER;

    }


    private void updateContentType () {

        List<FileSystemEntry> all = mIndex.fileSystemEntryDao().getUnknown();

        for (int i = 0; i < all.size(); i++) {
            FileSystemEntry databaseFile = all.get(i);

            Log.d("INDEXER", i+1 + " / " + all.size() + ": " + databaseFile.filename);

            mNotificationHelper.showIndexNotification(mAppTitle,(i+1)+" / "+all.size()+" Dateien durchsucht", all.size()-1,i);

            databaseFile.contentType = checkFileContent(databaseFile);
            mIndex.fileSystemEntryDao().update(databaseFile);

            if (isStopped())
                return;

        }

    }

    @NonNull
    private ArrayList<ArbeitsauftragEntry> scanEdith(PdfReader read, PdfReaderContentParser parser, FileSystemEntry databaseFile) {

        ArrayList<ArbeitsauftragEntry> arbeitsauftragList = new ArrayList<>();

        try {

            FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
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


            String lastBezeichner = "";

            for (int i = 1; i <= read.getNumberOfPages(); i++) {

                if (isStopped())
                    return new ArrayList<>();

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
                    Crashlytics.setString("File", databaseFile.getFile().getAbsolutePath());
                    Crashlytics.logException(e);
                }

            }

            Log.d("INDEXER", String.valueOf(arbeitsauftragList.size()));

        } catch (Exception | OutOfMemoryError | NoClassDefFoundError e) {
            Crashlytics.setString("File", databaseFile.getFile().getAbsolutePath());
            Crashlytics.logException(e);
        }

        return arbeitsauftragList;
    }

    @NonNull
    private ArrayList<ArbeitsauftragEntry> scanMBRail(PdfReader read, PdfReaderContentParser parser, FileSystemEntry databaseFile) {

        ArrayList<ArbeitsauftragEntry> arbeitsauftragList = new ArrayList<>();

        try {

            FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
            String schichtMBRAIL = mFirebaseRemoteConfig.getString("schichtMBRAIL");
            String datumMBRAIL = mFirebaseRemoteConfig.getString("datumMBRAIL");
            String letzteBearbeitungMBRAIL = mFirebaseRemoteConfig.getString("letzteBearbeitungMBRAIL");

            Pattern pSchicht = Pattern.compile(schichtMBRAIL, Pattern.MULTILINE);
            Pattern pDatum = Pattern.compile(datumMBRAIL, Pattern.MULTILINE);
            Pattern pLetzteBearbeitung = Pattern.compile(letzteBearbeitungMBRAIL, Pattern.MULTILINE);

            Log.d("AAINDEXER", databaseFile.filename);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

            String lastBezeichner = "";

            for (int i = 1; i <= read.getNumberOfPages(); i++) {

                if (isStopped())
                    return new ArrayList<>();

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
                    Crashlytics.setString("File", databaseFile.getFile().getAbsolutePath());
                    Crashlytics.logException(e);
                }

            }

            Log.d("AAINDEXER", String.valueOf(arbeitsauftragList.size()));


        } catch (Exception | OutOfMemoryError | NoClassDefFoundError e) {
            Crashlytics.setString("File", databaseFile.getFile().getAbsolutePath());
            Crashlytics.logException(e);
        }

        return  arbeitsauftragList;

    }





}
