package de.stm.oses.index.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import de.stm.oses.index.dao.ArbeitsauftragEntryDao;
import de.stm.oses.index.dao.FileSystemEntryDao;
import de.stm.oses.index.entities.ArbeitsauftragEntry;
import de.stm.oses.index.entities.FileSystemEntry;

@Database(entities = {FileSystemEntry.class, ArbeitsauftragEntry.class}, version = 2)
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
                DB_NAME).addMigrations(MIGRATION_1_2).fallbackToDestructiveMigration().build();
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE arbeitsauftrag ADD COLUMN est TEXT;");
            database.execSQL("ALTER TABLE arbeitsauftrag ADD COLUMN est_db TEXT;");
            database.execSQL("ALTER TABLE arbeitsauftrag ADD COLUMN est_de TEXT;");
            database.execSQL("ALTER TABLE arbeitsauftrag ADD COLUMN beginn TEXT;");
            database.execSQL("ALTER TABLE arbeitsauftrag ADD COLUMN ende TEXT;");

            database.execSQL("DELETE FROM files WHERE content_type = 1 OR content_type = 2");
        }
    };


    public abstract FileSystemEntryDao fileSystemEntryDao();
    public abstract ArbeitsauftragEntryDao arbeitsauftragEntryDao();



}
