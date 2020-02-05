package de.stm.oses.index.entities;

import androidx.room.ColumnInfo;

import java.io.File;

public class FileSystemArbeitsauftragEntry {

    public long id;

    @ColumnInfo(name = "path")
    public String path;

    @ColumnInfo(name = "filename")
    public String filename;

    @FileSystemEntry.FileContent
    @ColumnInfo(name = "content_type")
    public int contentType;

    @ColumnInfo(name = "count")
    public int count;

    public File getFile() {
       return new File(path + "/" + filename);
    }

}
