package de.stm.oses.index.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.Date;
import java.util.List;

import de.stm.oses.index.entities.ArbeitsauftragEntry;
import de.stm.oses.index.entities.ArbeitsauftragWithFileEntry;

@Dao
public interface ArbeitsauftragEntryDao {
    @Query("SELECT * FROM arbeitsauftrag")
    List<ArbeitsauftragEntry> getAll();

    @Transaction
    @Query("SELECT * FROM arbeitsauftrag WHERE bezeichner = :bezeichner AND (datum = :datum OR (datum_von <= :datum AND datum_bis >= :datum)) AND (est LIKE :est OR ort_start LIKE :est OR ort_ende LIKE :est) ORDER by datum_von, last_edit ASC LIMIT 1")
    ArbeitsauftragWithFileEntry getFileForVerwendung(String bezeichner, Date datum, String est);

    @Query("SELECT count(*) FROM arbeitsauftrag")
    long getCount();
    //@Query("SELECT * FROM arbeitsauftrag WHERE bezeichner = :bezeichner AND (datum = :datum OR (datum >= :datum AND datum <= :datum))")
    //List<ArbeitsauftragEntry> getArbeitsauftragFiles(String bezeichner, Date datum);

    /*@Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    User findByName(String first, String last);*/

    @Insert
    void insertAll(List<ArbeitsauftragEntry> arbeitsauftragEntries);

    @Insert
    void insert(ArbeitsauftragEntry arbeitsauftrag);

    @Query("DELETE FROM arbeitsauftrag")
    void clear();

    @Delete
    void delete(ArbeitsauftragEntry arbeitsauftrag);

    @Update
    void update(ArbeitsauftragEntry arbeitsauftrag);
}
