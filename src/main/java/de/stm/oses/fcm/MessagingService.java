package de.stm.oses.fcm;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;

import de.stm.oses.helper.OSESBase;
import de.stm.oses.notification.NotificationHelper;

public class MessagingService extends FirebaseMessagingService {

    public static class RefreshVerwendungEvent {
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {

        NotificationHelper mNotificationHelper = new NotificationHelper(this);

        Map<String, String> data = message.getData();
        String type = data.get("type");

        if (type == null)
            return;

        OSESBase OSES = new OSESBase(this);
        SharedPreferences notificationSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (OSES.getSession().getIdentifier().isEmpty()) {
            return;
        }

        if (type.equals("refresh_verwendung")) {
            EventBus.getDefault().post(new RefreshVerwendungEvent());
            return;
        }

        if (!notificationSettings.getBoolean("allowNotification", true) && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            return;
        }


        if (type.equals("admin") && OSES.getSession().getGroup() == 1) {
            String title = data.get("title");
            String text = data.get("text");
            String subtext = data.get("subtext");
            String tag = data.get("tag");
            Notification notification = mNotificationHelper.getAdminNotification(title, text, subtext).build();
            mNotificationHelper.notify(0, "ADMIN" + tag, notification);
            return;
        }

        if (type.equals("news")) {
            Notification notification = mNotificationHelper.getNewsNotification(data.get("titel"), data.get("message")).build();
            mNotificationHelper.notify(0, "news", notification);
            return;
        }

        if (type.equals("update")) {
            Notification notification = mNotificationHelper.getUpdateNotification(data.get("titel"), data.get("message")).build();
            mNotificationHelper.notify(0, "update", notification);
            return;
        }

        if (type.equals("fax")) {
            boolean progress = false;

            if (data.containsKey("iprogress"))
                progress = data.get("iprogress").equals("true");

            Notification notification = mNotificationHelper.getFaxNotification(data.get("titel"), data.get("message"), progress).build();
            mNotificationHelper.notify(0, "FAX_" + data.get("FaxID"), notification);
            return;
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {

        SharedPreferences prefs = getSharedPreferences("OSESPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("SessionFcmInstanceId", token);
        editor.apply();

    }
}
