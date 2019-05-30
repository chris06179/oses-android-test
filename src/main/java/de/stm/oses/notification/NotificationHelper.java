package de.stm.oses.notification;

/*
* Copyright 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

import de.stm.oses.R;
import de.stm.oses.StartActivity;
import de.stm.oses.helper.OSESBase;

/**
 * Helper class to manage notification channels, and create notifications.
 */
public class NotificationHelper extends ContextWrapper {
    private NotificationManager manager;
    public static final String SDL = "sdl";
    public static final String AUSBLEIBE_FAHRAUSLAGEN = "ausbleibe_fahrauslagen";
    public static final String FAX = "fax";
    public static final String NEWS = "news";
    public static final String APPUPDATES = "appupdates";
    public static final String SONSTIGE = "sonstige";
    public static final String ADMIN_SECURITY = "admin_security";

    /**
     * Registers notification channels, which can be used later by individual notifications.
     *
     * @param ctx The application context
     */
    public NotificationHelper(Context ctx) {
        super(ctx);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            List<NotificationChannelGroup> notificationChannelGroups = new ArrayList<>();
            List<NotificationChannel> notificationChannels = new ArrayList<>();

            notificationChannelGroups.add(new NotificationChannelGroup("dokumente", "Dokumente"));
            notificationChannelGroups.add(new NotificationChannelGroup("system", "System"));

            //NotificationChannel sdl = new NotificationChannel(SDL, "Sonderleistungszettel", NotificationManager.IMPORTANCE_DEFAULT);
            //sdl.setDescription("Erinnerung an Sonderleistungszettel bei Schichtende");
            //sdl.setGroup("dokumente");
            //notificationChannels.add(sdl);

            //NotificationChannel ausbleibe_fahrauslagen = new NotificationChannel(AUSBLEIBE_FAHRAUSLAGEN, "Ausbleibezeiten & Fahrauslagen", NotificationManager.IMPORTANCE_DEFAULT);
            //ausbleibe_fahrauslagen.setDescription("Erinnerung an Ausbleibezeit & Fahrauslagen bei Monatsende");
            //ausbleibe_fahrauslagen.setGroup("dokumente");
            //notificationChannels.add(ausbleibe_fahrauslagen);


            NotificationChannel fax = new NotificationChannel(FAX, "Faxversand", NotificationManager.IMPORTANCE_MIN);
            fax.setDescription("Zeige Informationen über den Status des Faxversandes an");
            fax.setGroup("dokumente");
            notificationChannels.add(fax);

            NotificationChannel news = new NotificationChannel(NEWS, "Nachrichten", NotificationManager.IMPORTANCE_DEFAULT);
            news.setDescription("Über aktuelle Nachrichten zu OSES informieren");
            news.setGroup("system");
            notificationChannels.add(news);

            NotificationChannel updates = new NotificationChannel(APPUPDATES, "Aktualisierungen", NotificationManager.IMPORTANCE_DEFAULT);
            updates.setDescription("Über neue Version informieren");
            updates.setGroup("system");
            notificationChannels.add(updates);

            NotificationChannel sonstige = new NotificationChannel(SONSTIGE, "Sonstige", NotificationManager.IMPORTANCE_DEFAULT);
            sonstige.setGroup("system");
            notificationChannels.add(sonstige);


            OSESBase OSES = new OSESBase(ctx);

            if (OSES.getSession().getGroup() == 1) {
                notificationChannelGroups.add(new NotificationChannelGroup("admin", "Administration"));

                NotificationChannel admin_security = new NotificationChannel(ADMIN_SECURITY, "Systemsicherheit", NotificationManager.IMPORTANCE_HIGH);
                admin_security.setGroup("admin");
                notificationChannels.add(admin_security);
            }


            getManager().createNotificationChannelGroups(notificationChannelGroups);
            getManager().createNotificationChannels(notificationChannels);

        }
    }

    public NotificationCompat.Builder getAdminNotification(String title, String body, String longBody) {
        NotificationCompat.Builder mBuilder =  new NotificationCompat.Builder(this, ADMIN_SECURITY)
                        .setSmallIcon(R.drawable.ic_not_icon)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(longBody))
                        .setCategory(NotificationCompat.CATEGORY_STATUS)
                        .setOnlyAlertOnce(true)
                        .setAutoCancel(true)
                        .setLargeIcon(getLargeIcon());

        return mBuilder;
    }

    public NotificationCompat.Builder getNewsNotification(String title, String body) {
        NotificationCompat.Builder mBuilder =  new NotificationCompat.Builder(this, NEWS)
                .setSmallIcon(R.drawable.ic_not_icon)
                .setContentTitle(title)
                .setContentText(body)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setLargeIcon(getLargeIcon());

        Intent setintent = new Intent(this, StartActivity.class);
        setintent.putExtra("fragment", "browser");
        setintent.putExtra("type", "aktuell");

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, setintent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setLargeIcon(getLargeIcon());

        return mBuilder;
    }

    public NotificationCompat.Builder getUpdateNotification(String title, String body) {
        NotificationCompat.Builder mBuilder =  new NotificationCompat.Builder(this, APPUPDATES)
                .setSmallIcon(R.drawable.ic_not_icon)
                .setContentTitle(title)
                .setContentText(body)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setLargeIcon(getLargeIcon());

        Intent setintent = new Intent(this, StartActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, setintent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setLargeIcon(getLargeIcon());

        return mBuilder;
    }

    public NotificationCompat.Builder getFaxNotification(String title, String body, boolean progress) {
        NotificationCompat.Builder mBuilder =  new NotificationCompat.Builder(this, FAX)
                .setSmallIcon(R.drawable.ic_not_icon)
                .setContentTitle(title)
                .setContentText(body)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setLargeIcon(getLargeIcon());

        if (progress) mBuilder.setProgress(0,0, true);

        return mBuilder;
    }


    public void notify(int id, String tag, Notification notification) {
        getManager().notify(tag, id, notification);
    }

    private int getSmallIcon() {
        return android.R.drawable.stat_notify_chat;
    }

    private Bitmap getLargeIcon() {
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        return bm;
    }

    private NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }
}