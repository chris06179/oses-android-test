package de.stm.oses.index.entities;

import androidx.room.Embedded;
import androidx.room.Relation;

public class ArbeitsauftragWithFileEntry {

    @Embedded public ArbeitsauftragEntry arbeitsauftrag;

    @Relation(
            parentColumn = "fileId",
            entityColumn = "id"
    )
    public FileSystemEntry file;


}
