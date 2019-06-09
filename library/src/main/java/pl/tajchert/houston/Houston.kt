package pl.tajchert.houston

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import pl.tajchert.houston.HoustonLandReceiver.Companion.NOTIF_ID
import java.util.ArrayList

class Houston(
  private val sharedPreferences: SharedPreferences,
  private val notificationManager: NotificationManager
) : SharedPreferences.OnSharedPreferenceChangeListener {
  private val gson = GsonBuilder().create()
  var notifications = ArrayList<NotificationWrapper>()
    private set

  constructor(context: Context) : this(context.getSharedPreferences(context.applicationContext.packageName, MODE_PRIVATE), context)

  constructor(
    sharedPreferences: SharedPreferences,
    context: Context
  ) : this(sharedPreferences, context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)

  init {
    notifications = readNotificationListFromStorage(sharedPreferences)
    this.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
  }

  private fun readNotificationListFromStorage(sharedPreferences: SharedPreferences): ArrayList<NotificationWrapper> {
    var notificationFromStorage: ArrayList<NotificationWrapper>? = null
    try {
      notificationFromStorage = gson.fromJson<ArrayList<NotificationWrapper>>(
          this.sharedPreferences.getString(KEY_STORED, ""), object : TypeToken<List<NotificationWrapper>>() {

      }.type
      )
    } catch (e: Exception) {
      //JsonSyntaxException or NumberFormatExcetion and similar, hard to reproduce but in such case it is much better to drop data instead of throwing exception
      e.printStackTrace()
      sharedPreferences.edit()
          .remove(KEY_STORED)
          .apply()
    }

    if (notificationFromStorage == null) {
      notificationFromStorage = ArrayList()
    }
    return notificationFromStorage
  }

  fun saveNotification(notificationWrapper: NotificationWrapper) {
    if (notifications.contains(notificationWrapper)) {
      notifications.remove(notificationWrapper)
    }
    notifications.add(notificationWrapper)
    persistNotifications()
  }

  fun removeNotification(id: Int) {
    val notificationWrapper = getNotification(id)
    if (notificationWrapper != null) {
      notifications.remove(notificationWrapper)
    }
    persistNotifications()
  }

  fun getNotification(id: Int): NotificationWrapper? {
    for (notificationWrapper in notifications) {
      if (notificationWrapper.id == id) {
        return notificationWrapper
      }
    }
    return null
  }

  fun getNotifications(categoryName: String): ArrayList<NotificationWrapper> {
    val notificationWrappers = ArrayList<NotificationWrapper>()
    for (notificationWrapper in notifications) {
      if (notificationWrapper.category != null && categoryName == notificationWrapper.category!!.title) {
        notificationWrappers.add(notificationWrapper)
      }
    }
    return notificationWrappers
  }

  fun getNotifications(notificationCategory: NotificationCategory): ArrayList<NotificationWrapper> {
    val notificationWrappers = ArrayList<NotificationWrapper>()
    for (notificationWrapper in notifications) {
      if (notificationWrapper.category != null && notificationWrapper.category == notificationCategory) {
        notificationWrappers.add(notificationWrapper)
      }
    }
    return notificationWrappers
  }

  fun persistNotifications() {
    sharedPreferences.edit()
        .putString(KEY_STORED, gson.toJson(notifications))
        .apply()
  }

  private fun createNotificationWrapper(id: Int): NotificationWrapper {
    val notificationWrapper = NotificationWrapper()
    notificationWrapper.id = id
    notificationWrapper.showTime = System.currentTimeMillis()
    return notificationWrapper
  }

  fun addDismissListener(
    builder: Notification.Builder,
    id: Int,
    context: Context
  ): Notification.Builder {
    val intent = Intent(context, HoustonLandReceiver::class.java)
    val intentExtras = Bundle()
    intentExtras.putInt(NOTIF_ID, id)
    intent.putExtras(intentExtras)
    val pendingIntent = PendingIntent.getBroadcast(context.applicationContext, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    builder.setDeleteIntent(pendingIntent)
    return builder
  }

  fun addDismissListener(
    builder: NotificationCompat.Builder,
    id: Int,
    context: Context
  ): NotificationCompat.Builder {
    val intent = Intent(context, HoustonLandReceiver::class.java)
    val intentExtras = Bundle()
    intentExtras.putInt(NOTIF_ID, id)
    intent.putExtras(intentExtras)
    val pendingIntent = PendingIntent.getBroadcast(context.applicationContext, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    builder.setDeleteIntent(pendingIntent)
    return builder
  }

  fun launch(
    id: Int,
    notification: Notification
  ) {
    notificationManager.notify(id, notification)
    saveNotification(createNotificationWrapper(id))
  }

  fun launch(
    id: Int,
    notification: Notification,
    category: String
  ) {
    launch(id, notification)
    val notificationWrapper = createNotificationWrapper(id)
    notificationWrapper.category = NotificationCategory(category)
    saveNotification(notificationWrapper)
  }

  fun refreshList() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
      val activeNotifications = notificationManager.activeNotifications
      if (activeNotifications != null) {
        val notificationWrappers = ArrayList(notifications)
        for (notif in activeNotifications) {
          val notificationWrapper = getNotification(notif.id)
          if (notificationWrapper != null) {
            notificationWrappers.remove(notificationWrapper)
          }
        }
        for (notificationWrapper in notificationWrappers) {
          land(notificationWrapper.id)
        }
      } else {
        landAll()
      }
    }
  }

  fun land(id: Int) {
    notificationManager.cancel(id)
    val notificationWrapper = getNotification(id)
    notifications.remove(notificationWrapper)
    persistNotifications()
  }

  fun landAll(category: String) {
    val notifications = getNotifications(category)
    for (notificationWrapper in notifications) {
      notificationManager.cancel(notificationWrapper.id)
      this.notifications.remove(notificationWrapper)
    }
    persistNotifications()
  }

  fun landAll() {
    notificationManager.cancelAll()
    notifications.clear()
    persistNotifications()
  }

  override fun onSharedPreferenceChanged(
    sharedPreferences: SharedPreferences,
    s: String
  ) {
    if (KEY_STORED == s) {
      notifications = readNotificationListFromStorage(sharedPreferences)
    }
  }

  fun onDestroy() {
    sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
  }

  companion object {
    private val TAG = Houston::class.java!!.getCanonicalName()
    private val KEY_STORED = "houston_all_notifications_storage"
  }
}