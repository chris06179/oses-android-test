package de.stm.oses.index.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import de.stm.oses.index.dao.ArbeitsauftragEntryDao;
import de.stm.oses.index.dao.FileSystemEntryDao;
import de.stm.oses.index.entities.ArbeitsauftragEntry;
import de.stm.oses.index.entities.FileSystemEntry;

@Database(entities = {FileSystemEntry.class, ArbeitsauftragEntry.class}, version = 3)
@TypeConverters({Converters.class})
public abstract class FileSystemDatabase extends RoomDatabase {

    private static final String DB_NAME = "oses_file_index";
    private static volatile FileSystemDatabase instance;

    public static synchronized FileSystemDatabase getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }

    private static FileSystemDatabase create(final Context context) {
        return Room.databaseBuilder(
                context,
                FileSystemDatabase.class,
                DB_NAME).fallbackToDestructiveMigration().build();
    }




    public abstract FileSystemEntryDao fileSystemEntryDao();
    public abstract ArbeitsauftragEntryDao arbeitsauftragEntryDao();



}
