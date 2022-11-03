package de.stm.oses.application;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Logger {

    private boolean useFileLogging;

    Logger(Context context) {
        useFileLogging = (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("useFileLogging", false) && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    public void setUseFileLogging(boolean useFileLogging) {
        this.useFileLogging = useFileLogging;
    }

    public void log(String tag, String text)
    {
        Log.d(tag, text);

        if (!useFileLogging)
            return;

        SimpleDateFormat dayformat = new SimpleDateFormat("yyyy_MM_dd", Locale.GERMAN);
        SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm:ss", Locale.GERMAN);

        File logFile = new File(Environment.getExternalStorageDirectory()+"/OSES/logs/"+dayformat.format(new Date())+".log");
        if (!logFile.exists())
        {
            try
            {
                logFile.getParentFile().mkdirs();
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(timeformat.format(new Date())).append(" -> ").append(tag).append(": ").append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
