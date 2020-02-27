package de.stm.oses.index.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.Date;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "arbeitsauftrag", indices = {@Index(value = {"fileId"})}, foreignKeys = @ForeignKey(entity = FileSystemEntry.class,
                                                                    parentColumns = "id",
                                                                    childColumns = "fileId",
                                                                    onDelete = CASCADE))
public class ArbeitsauftragEntry {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "bezeichner")
    public String bezeichner;

    @ColumnInfo(name = "datum")
    public Date datum;

    @ColumnInfo(name = "datum_von")
    public Date datumVon;

    @ColumnInfo(name = "datum_bis")
    public Date datumBis;

    @ColumnInfo(name = "est")
    public String est;

    @ColumnInfo(name = "ort_start")
    public String ortStart;

    @ColumnInfo(name = "ort_ende")
    public String ortEnde;

    @ColumnInfo(name = "start")
    public String start;

    @ColumnInfo(name = "ende")
    public String ende;

    @ColumnInfo(name = "last_edit")
    public Date lastEdit;

    @ColumnInfo(name = "fileId")
    public long fileId;

    @ColumnInfo(name = "pages")
    public ArrayList<Integer> pages = new ArrayList<>();

}
