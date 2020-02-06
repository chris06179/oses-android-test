package de.stm.oses.index;

import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import de.stm.oses.index.database.FileSystemDatabase;
import de.stm.oses.index.entities.FileSystemEntry;

public class FileSystemIndexer {

    private final FileSystemDatabase mIndex;
    private final String mPath;
    private final FirebaseRemoteConfig mFirebaseRemoteConfig;

    public FileSystemIndexer(String path, FileSystemDatabase database) {
        mIndex = database;
        mPath = path;

        // Remote Config
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    }

    public static class IndexProgressEvent {
        public int progress;
        public int max;
        public String message;
        public boolean show;
        public String subtitle;

        IndexProgressEvent(int progress, int max, @NonNull String message, @NonNull String subtitle, boolean show) {
            this.progress = progress;
            this.max = max;
            this.message = message;
            this.subtitle = subtitle;
            this.show = show;
        }
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

        if (mIndex.fileSystemEntryDao().getCount() == 0)
            EventBus.getDefault().postSticky(new IndexProgressEvent(0,0, "Prüfe auf neue Dateien...", "Der Index wird zum ersten mal erstellt, dies kann einige Minuten dauern...", true));

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
    private int checkFileContent(File file) {

        if (!file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase().equals("pdf"))
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

                TextExtractionStrategy strategy = parser.processContent(k, new SimpleTextExtractionStrategy());
                String text = strategy.getResultantText();


                if (pIsArbeitsauftragEDITH.matcher(text).matches()) {
                    return FileSystemEntry.FILECONTENT_EDITH;
                }

                if (pIsArbeitsauftragMBRAIL.matcher(text).matches()) {
                    return FileSystemEntry.FILECONTENT_MBRAIL;
                }

                if (k == 10) {
                    break;
                }

            }

        } catch (Exception | OutOfMemoryError | NoClassDefFoundError e) {
            return FileSystemEntry.FILECONTENT_EXCEPTION;
        }

        return  FileSystemEntry.FILECONTENT_OTHER;

    }


    private void updateContentType () {

        List<FileSystemEntry> all = mIndex.fileSystemEntryDao().getUnknown();

        for (int i = 0; i < all.size(); i++) {
            FileSystemEntry databaseFile = all.get(i);

            Log.d("INDEXER", i+1 + " / " + all.size() + ": " + databaseFile.filename);
            if (all.size() > 100)
                EventBus.getDefault().postSticky(new IndexProgressEvent(i, all.size()-1, "Ermittle Inhalt von PDF-Dateien - "+(i+1)+" / "+all.size()+" Dateien durchsucht...", "Es müssen viele neue Dateien geprüft werden, dies kann einige Minuten dauern...", true));

            databaseFile.contentType = checkFileContent(databaseFile.getFile());
            mIndex.fileSystemEntryDao().update(databaseFile);

        }

        if (all.size() > 100)
            EventBus.getDefault().postSticky(new IndexProgressEvent(0,0, "Erstelle Liste mit Arbeitsaufträgen - Vorbereitung", "Es müssen viele neue Dateien geprüft werden, dies kann einige Minuten dauern...", true));

    }


    public void execute() {

        checkUpdatedFiles();
        addNewFiles();
        updateContentType();

    }





}
