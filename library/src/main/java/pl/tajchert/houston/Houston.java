package pl.tajchert.houston;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static pl.tajchert.houston.HoustonLandReceiver.NOTIF_ID;

public class Houston implements SharedPreferences.OnSharedPreferenceChangeListener {
  private static final String TAG = Houston.class.getCanonicalName();
  private SharedPreferences sharedPreferences;
  private NotificationManager notificationManager;
  private Gson gson = new GsonBuilder().create();
  private static final String KEY_STORED = "houston_all_notifications_storage";
  private ArrayList<NotificationWrapper> allNotifications = new ArrayList<>();

  public Houston(Context context) {
    this(context.getSharedPreferences(context.getApplicationContext().getPackageName(), MODE_PRIVATE), context);
  }

  public Houston(SharedPreferences sharedPreferences, Context context) {
    this(sharedPreferences, (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
  }

  public Houston(SharedPreferences sharedPreferences, NotificationManager notificationManager) {
    this.sharedPreferences = sharedPreferences;
    this.notificationManager = notificationManager;
    allNotifications = readNotificationListFromStorage(sharedPreferences);
    this.sharedPreferences.registerOnSharedPreferenceChangeListener(this);
  }

  private ArrayList<NotificationWrapper> readNotificationListFromStorage(SharedPreferences sharedPreferences) {
    ArrayList<NotificationWrapper> notificationFromStorage = null;
    try {
      notificationFromStorage = gson.fromJson(this.sharedPreferences.getString(KEY_STORED, ""), new TypeToken<List<NotificationWrapper>>() {}.getType());
    } catch (Exception e) {
      //JsonSyntaxException or NumberFormatExcetion and similar, hard to reproduce but in such case it is much better to drop data instead of throwing exception
      e.printStackTrace();
      sharedPreferences.edit().remove(KEY_STORED).apply();
    }
    if (notificationFromStorage == null) {
      notificationFromStorage = new ArrayList<>();
    }
    return notificationFromStorage;
  }

  public void saveNotification(NotificationWrapper notificationWrapper) {
    if (allNotifications.contains(notificationWrapper)) {
      allNotifications.remove(notificationWrapper);
    }
    allNotifications.add(notificationWrapper);
    persistNotifications();
  }

  public void removeNotification(int id) {
    NotificationWrapper notificationWrapper = getNotification(id);
    if (notificationWrapper != null) {
      allNotifications.remove(notificationWrapper);
    }
    persistNotifications();
  }

  public NotificationWrapper getNotification(int id) {
    for (NotificationWrapper notificationWrapper : allNotifications) {
      if (notificationWrapper.id == id) {
        return notificationWrapper;
      }
    }
    return null;
  }

  public ArrayList<NotificationWrapper> getNotifications(String categoryName) {
    ArrayList<NotificationWrapper> notificationWrappers = new ArrayList<>();
    for (NotificationWrapper notificationWrapper : allNotifications) {
      if (notificationWrapper.category != null && categoryName.equals(notificationWrapper.category.title)) {
        notificationWrappers.add(notificationWrapper);
      }
    }
    return notificationWrappers;
  }

  public ArrayList<NotificationWrapper> getNotifications(NotificationCategory notificationCategory) {
    ArrayList<NotificationWrapper> notificationWrappers = new ArrayList<>();
    for (NotificationWrapper notificationWrapper : allNotifications) {
      if (notificationWrapper.category != null && notificationWrapper.category.equals(notificationCategory)) {
        notificationWrappers.add(notificationWrapper);
      }
    }
    return notificationWrappers;
  }

  public ArrayList<NotificationWrapper> getNotifications() {
    return allNotifications;
  }

  public void persistNotifications() {
    sharedPreferences.edit().putString(KEY_STORED, gson.toJson(allNotifications)).apply();
  }

  @NonNull private NotificationWrapper createNotificationWrapper(int id) {
    NotificationWrapper notificationWrapper = new NotificationWrapper();
    notificationWrapper.id = id;
    notificationWrapper.showTime = System.currentTimeMillis();
    return notificationWrapper;
  }

  public Notification.Builder addDismissListener(Notification.Builder builder, int id, Context context) {
    Intent intent = new Intent(context, HoustonLandReceiver.class);
    Bundle intentExtras = new Bundle();
    intentExtras.putInt(NOTIF_ID, id);
    intent.putExtras(intentExtras);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    builder.setDeleteIntent(pendingIntent);
    return builder;
  }

  public NotificationCompat.Builder addDismissListener(NotificationCompat.Builder builder, int id, Context context) {
    Intent intent = new Intent(context, HoustonLandReceiver.class);
    Bundle intentExtras = new Bundle();
    intentExtras.putInt(NOTIF_ID, id);
    intent.putExtras(intentExtras);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    builder.setDeleteIntent(pendingIntent);
    return builder;
  }

  public void launch(int id, Notification notification) {
    notificationManager.notify(id, notification);
    saveNotification(createNotificationWrapper(id));
  }

  public void launch(int id, Notification notification, String category) {
    launch(id, notification);
    NotificationWrapper notificationWrapper = createNotificationWrapper(id);
    notificationWrapper.category = new NotificationCategory(category);
    saveNotification(notificationWrapper);
  }

  public void refreshList() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
      StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();
      if (activeNotifications != null) {
        ArrayList<NotificationWrapper> notificationWrappers = new ArrayList<>(allNotifications);
        for (StatusBarNotification notif : activeNotifications) {
          NotificationWrapper notificationWrapper = getNotification(notif.getId());
          if (notificationWrapper != null) {
            notificationWrappers.remove(notificationWrapper);
          }
        }
        for (NotificationWrapper notificationWrapper : notificationWrappers) {
          land(notificationWrapper.id);
        }
      } else {
        landAll();
      }
    }
  }

  public void land(int id) {
    notificationManager.cancel(id);
    NotificationWrapper notificationWrapper = getNotification(id);
    allNotifications.remove(notificationWrapper);
    persistNotifications();
  }

  public void landAll(String category) {
    ArrayList<NotificationWrapper> notifications = getNotifications(category);
    for (NotificationWrapper notificationWrapper : notifications) {
      notificationManager.cancel(notificationWrapper.id);
      allNotifications.remove(notificationWrapper);
    }
    persistNotifications();
  }

  public void landAll() {
    notificationManager.cancelAll();
    allNotifications.clear();
    persistNotifications();
  }

  @Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
    if (KEY_STORED.equals(s)) {
      allNotifications = readNotificationListFromStorage(sharedPreferences);
    }
  }

  public void onDestroy() {
    sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
  }
}