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
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static pl.tajchert.houston.HoustonLandReceiver.KEY_ID;

public class Houston implements SharedPreferences.OnSharedPreferenceChangeListener {
  private static final String TAG = Houston.class.getCanonicalName();
  private SharedPreferences sharedPreferences;
  private NotificationManager notificationManager;
  private Gson gson = new GsonBuilder().create();
  private static final String KEY_STORED = "houston_all_notifications_storage";
  private ArrayList<NotificationWrapper> allNotifications = new ArrayList<>();

  public Houston(Context context) {
    this.sharedPreferences = context.getSharedPreferences(context.getApplicationContext().getPackageName(), MODE_PRIVATE);
    this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    allNotifications = gson.fromJson(this.sharedPreferences.getString(KEY_STORED, ""), new TypeToken<List<NotificationWrapper>>() {
    }.getType());
    if (allNotifications == null) {
      allNotifications = new ArrayList<>();
    }
    this.sharedPreferences.registerOnSharedPreferenceChangeListener(this);
  }

  public Houston(SharedPreferences sharedPreferences, Context context) {
    this.sharedPreferences = sharedPreferences;
    this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    allNotifications = gson.fromJson(this.sharedPreferences.getString(KEY_STORED, ""), new TypeToken<List<NotificationWrapper>>() {
    }.getType());
    if (allNotifications == null) {
      allNotifications = new ArrayList<>();
    }
    this.sharedPreferences.registerOnSharedPreferenceChangeListener(this);
  }

  public Houston(SharedPreferences sharedPreferences, NotificationManager notificationManager) {
    this.sharedPreferences = sharedPreferences;
    this.notificationManager = notificationManager;
    allNotifications = gson.fromJson(this.sharedPreferences.getString(KEY_STORED, ""), new TypeToken<List<NotificationWrapper>>() {
    }.getType());
    if (allNotifications == null) {
      allNotifications = new ArrayList<>();
    }
    this.sharedPreferences.registerOnSharedPreferenceChangeListener(this);
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
    Bundle intentYawnBackActionExtras = new Bundle();
    intentYawnBackActionExtras.putInt(KEY_ID, id);
    intent.putExtras(intentYawnBackActionExtras);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    builder.setDeleteIntent(pendingIntent);
    return builder;
  }

  public NotificationCompat.Builder addDismissListener(NotificationCompat.Builder builder, int id, Context context) {
    Intent intent = new Intent(context, HoustonLandReceiver.class);
    Bundle intentYawnBackActionExtras = new Bundle();
    intentYawnBackActionExtras.putInt(KEY_ID, id);
    intent.putExtras(intentYawnBackActionExtras);
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
      if (activeNotifications != null && activeNotifications.length > 0) {
        ArrayList<NotificationWrapper> notificationWrappers = new ArrayList<>(allNotifications);
        for (StatusBarNotification notif : activeNotifications) {
          NotificationWrapper notificationWrapper = getNotification(notif.getId());
          if (notificationWrapper != null && notificationWrappers.contains(notificationWrapper)) {
            notificationWrappers.remove(notificationWrapper);
          }
        }
        if (notificationWrappers.size() > 0) {
          for (NotificationWrapper notificationWrapper : notificationWrappers) {
            land(notificationWrapper.id);
          }
        }
      } else {
        landAll();
      }
    }
  }

  public void land(int id) {
    Log.d(TAG, "Tranquility Base here. The Eagle has landed");
    notificationManager.cancel(id);
    NotificationWrapper notificationWrapper = getNotification(id);
    allNotifications.remove(notificationWrapper);
    persistNotifications();
  }

  public void landAll(String category) {
    Log.d(TAG, "Tranquility Base here. The Eagles had landed");
    ArrayList<NotificationWrapper> notifications = getNotifications(category);
    for (NotificationWrapper notificationWrapper : notifications) {
      notificationManager.cancel(notificationWrapper.id);
      allNotifications.remove(notificationWrapper);
    }
    persistNotifications();
  }

  public void landAll() {
    Log.d(TAG, "Tranquility Base here. All Eagles had landed");
    notificationManager.cancelAll();
    allNotifications.clear();
    persistNotifications();
  }

  @Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
    Log.d(TAG, "onSharedPreferenceChanged: ");
    if (KEY_STORED.equals(s)) {
      Log.d(TAG, "onSharedPreferenceChanged: ");
      allNotifications = gson.fromJson(this.sharedPreferences.getString(KEY_STORED, ""), new TypeToken<List<NotificationWrapper>>() {
      }.getType());
    }
    Log.d(TAG, "onSharedPreferenceChanged: ");
  }

  public void onDestroy() {
    sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
  }
}