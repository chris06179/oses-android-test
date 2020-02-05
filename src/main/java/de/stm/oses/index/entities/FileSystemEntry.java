package de.stm.oses.index.entities;

import androidx.annotation.IntDef;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.File;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;


@Entity(tableName = "files", indices = {@Index(value = {"path", "filename"}, unique = true)})
public class FileSystemEntry {

    @Retention(SOURCE)
    @IntDef({FILECONTENT_OTHER, FILECONTENT_EDITH, FILECONTENT_MBRAIL, FILECONTENT_UNKNOWN, FILECONTENT_EXCEPTION})
    public @interface FileContent {}

    public static final int FILECONTENT_UNKNOWN = 0;
    public static final int FILECONTENT_OTHER = 99;
    public static final int FILECONTENT_EXCEPTION = 100;

    public static final int FILECONTENT_EDITH = 1;
    public static final int FILECONTENT_MBRAIL = 2;

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "path")
    public String path;

    @ColumnInfo(name = "filename")
    public String filename;

    @ColumnInfo(name = "filetype")
    public String filetype;

    @FileContent
    @ColumnInfo(name = "content_type")
    public int contentType;

    @ColumnInfo(name = "lastModified")
    public long lastModified;

    public File getFile() {
       return new File(path + "/" + filename);
    }
}
