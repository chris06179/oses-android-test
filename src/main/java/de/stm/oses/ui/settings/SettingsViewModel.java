package de.stm.oses.ui.settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import de.stm.oses.index.database.FileSystemDatabase;
import de.stm.oses.index.entities.FileSystemStatus;

public class SettingsViewModel extends AndroidViewModel {

   private LiveData<FileSystemStatus> fileSystemStatus;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        fileSystemStatus = FileSystemDatabase.getInstance(getApplication()).fileSystemEntryDao().getStatus();
    }

    public LiveData<FileSystemStatus> getFileSystemStatus() {
        return fileSystemStatus;
    }

    public void resetIndex() {
        new Thread(() -> {
           FileSystemDatabase.getInstance(getApplication()).clearAllTables();
        }).start();
    }

}
