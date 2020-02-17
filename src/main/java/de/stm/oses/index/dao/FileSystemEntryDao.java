package de.stm.oses.index.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import de.stm.oses.index.entities.FileSystemArbeitsauftragEntry;
import de.stm.oses.index.entities.FileSystemEntry;
import de.stm.oses.index.entities.FileSystemStatus;

@Dao
public interface FileSystemEntryDao {
    @Query("SELECT * FROM files")
    List<FileSystemEntry> getAll();

    @Query("SELECT count(*) FROM files")
    long getCount();

    @Query("SELECT (SELECT count(*) FROM files) as fileSystemCount, (SELECT count(*) FROM arbeitsauftrag) as arbeitsauftragCount")
    LiveData<FileSystemStatus> getStatus();

    @Query("SELECT * FROM files WHERE content_type = " + FileSystemEntry.FILECONTENT_UNKNOWN)
    List<FileSystemEntry> getUnknown();

    @Query("SELECT files.id, path, filename, content_type, (SELECT count(*) FROM arbeitsauftrag WHERE arbeitsauftrag.fileId = files.id) as count FROM files WHERE (content_type = " + FileSystemEntry.FILECONTENT_EDITH +" OR content_type = " + FileSystemEntry.FILECONTENT_MBRAIL + ") AND count = 0 ORDER by lastModified ASC")
    List<FileSystemArbeitsauftragEntry> getUnindexedFilesWithArbeitsauftrag();

    @Query("SELECT * FROM files WHERE (content_type = " + FileSystemEntry.FILECONTENT_EDITH +" OR content_type = " + FileSystemEntry.FILECONTENT_MBRAIL + ") ORDER by lastModified ASC")
    List<FileSystemEntry> getFilesWithArbeitsauftrag();

    @Insert
    void insertAll(FileSystemEntry... file);

    @Insert
    void insert(FileSystemEntry file);

    @Query("DELETE FROM files")
    void clear();

    @Delete
    void delete(FileSystemEntry file);

    @Update
    void update(FileSystemEntry file);
}
