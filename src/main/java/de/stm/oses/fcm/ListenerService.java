package de.stm.oses.fcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;

import de.stm.oses.OSESActivity;
import de.stm.oses.R;
import de.stm.oses.StartActivity;
import de.stm.oses.fax.FaxActivity;
import de.stm.oses.helper.OSESBase;

public class ListenerService extends FirebaseMessagingService {

    public static class RefreshVerwendungEvent {
    }

    @Override
    public void onMessageReceived(RemoteMessage message){
        Map<String, String> data = message.getData();
        String type = data.get("type");

        if (type == null)
            return;

        SharedPreferences osesprefs = getSharedPreferences("OSESPrefs", 0);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (osesprefs.getString("SessionIdentifier", "").equals("")) {
            return;
        }

        if (!settings.getBoolean("allowNotification", false)) {
            return;
        }

        if (type.equals("refresh_verwendung")) {
            EventBus.getDefault().post(new RefreshVerwendungEvent());
            return;
        }

        int SessionGroup = osesprefs.getInt("SessionGruppe", 0);

        if (type.equals("admin") && SessionGroup == 1) {
            String title = data.get("title");
            String text = data.get("text");
            String subtext = data.get("subtext");
            String tag = data.get("tag");
            generateNotificationAdmin(getApplicationContext(), text, title, subtext, "ADMIN" + tag, 0);
            return;
        }

        if (type.equals("force")) {
            generateNotification(this, data.get("message"), data.get("titel"), R.mipmap.ic_launcher,  "force", Integer.parseInt(data.get("id")), true, false);
            return;
        }

        if (type.equals("sdl") && settings.getBoolean("sdlNotification", true)) {
            Intent setintent = new Intent(this, FaxActivity.class);
            setintent.putExtra("type", "SDL");
            setintent.putExtra("id", data.get("id"));
            setintent.putExtra("date", data.get("date"));

            generateNotificationIntent(this, data.get("message"),data.get("titel"), "sdl", Integer.parseInt(data.get("id")), setintent);
            return;
        }

        if (type.equals("news") && settings.getBoolean("newsNotification", true)) {
            Intent setintent = new Intent(this, StartActivity.class);
            setintent.putExtra("fragment", "browser");
            setintent.putExtra("type", "aktuell");
            generateNotificationIntent(this, data.get("message"), data.get("titel"), "news", 0, setintent);
            return;
        }

        if (type.equals("update") && settings.getBoolean("updateNotification", true)) {
            generateNotification(this, data.get("message"), data.get("titel"), R.mipmap.ic_launcher, "update", 0, true, false);
            return;
        }

        if (type.equals("text") && settings.getBoolean("otherNotification", true)) {
            generateNotification(this, data.get("message"), data.get("titel"), R.mipmap.ic_launcher, "text", Integer.parseInt(data.get("id")), true, false);
            return;
        }

        if (type.equals("fax") && settings.getBoolean("faxNotification", true)) {
            boolean iprogress = false;
            boolean sound = false;

            if (data.containsKey("sound"))
             sound = data.get("sound").equals("true");

            if (data.containsKey("iprogress"))
              iprogress = data.get("iprogress").equals("true");

            generateNotification(this, data.get("message"), data.get("titel"), R.mipmap.ic_launcher, "FAX_"+data.get("FaxID"), 0, sound, iprogress );
            return;
        }

        if (type.equals("unwetter")) {
            generateNotification(this, data.get("message"), data.get("titel"), R.drawable.icon_warning, "unwetter",Integer.parseInt(data.get("id")), true, false);
            return;
        }

    }


    private void generateNotificationAdmin(Context context, String text, String title, String subText, String mTag, Integer mId) {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        Uri notificationsound = Uri.parse(settings.getString("ringtoneOnNotification", Settings.System.DEFAULT_NOTIFICATION_URI.toString()));
        boolean vibrate = settings.getBoolean("vibrateOnNotification", true);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_not_icon)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(subText))
                        .setTicker(text)
                        .setSound(notificationsound)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setCategory(Notification.CATEGORY_STATUS)
                        .setOnlyAlertOnce(true);


        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        mBuilder.setLargeIcon(bm);

        if (vibrate)
            mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);


        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(mTag, mId, mBuilder.build());

    }

    private void generateNotification(Context context, String text, String title, int icon, String mTag,  Integer mId, boolean sound, boolean iprogress) {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        Uri notificationsound = Uri.parse(settings.getString("ringtoneOnNotification", Settings.System.DEFAULT_NOTIFICATION_URI.toString()));
        boolean vibrate = settings.getBoolean("vibrateOnNotification", true);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_not_icon)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setAutoCancel(true)
                        .setTicker(text)
                        .setPriority(Notification.PRIORITY_DEFAULT);

        if (sound) mBuilder.setSound(notificationsound);
        if (iprogress) mBuilder.setProgress(0,0, true);

        Bitmap bm = BitmapFactory.decodeResource(getResources(), icon);
        mBuilder.setLargeIcon(bm);

        if (vibrate)
            mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);

        Intent notificationIntent = new Intent(context, OSESActivity.class);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.setAction(Intent.ACTION_MAIN);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(mTag, mId, mBuilder.build());

    }

    private void generateNotificationIntent(Context context, String text, String title, String mTag, Integer mId, Intent notificationIntent) {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        Uri notificationsound = Uri.parse(settings.getString("ringtoneOnNotification", Settings.System.DEFAULT_NOTIFICATION_URI.toString()));
        boolean vibrate = settings.getBoolean("vibrateOnNotification", true);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_not_icon)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setAutoCancel(true)
                        .setTicker(text)
                        .setPriority(Notification.PRIORITY_DEFAULT)
                        .setSound(notificationsound);

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        mBuilder.setLargeIcon(bm);

        if (vibrate)
            mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);

        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.setAction(Intent.ACTION_MAIN);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(mTag, mId, mBuilder.build());

    }


}
