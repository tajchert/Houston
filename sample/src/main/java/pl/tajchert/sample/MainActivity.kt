package pl.tajchert.sample

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import kotlinx.android.synthetic.main.activity_main.buttonCount
import kotlinx.android.synthetic.main.activity_main.buttonHideOne
import kotlinx.android.synthetic.main.activity_main.buttonHideTwo
import kotlinx.android.synthetic.main.activity_main.buttonSendOne
import kotlinx.android.synthetic.main.activity_main.buttonSendTwo
import pl.tajchert.houston.Houston
import java.util.Random

class MainActivity : AppCompatActivity() {
  private var houston: Houston? = null
  private val random = Random()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    houston = Houston(this)
    setClickListeners()
  }

  private fun setClickListeners() {
    buttonSendOne.setOnClickListener {
      showNotification("Notification #1", "categoryOne")
    }
    buttonSendTwo.setOnClickListener {
      showNotification("Notification #2", "categoryTwo")
    }
    buttonHideOne.setOnClickListener {
      houston!!.landAll("categoryOne")
    }
    buttonHideTwo.setOnClickListener {
      houston!!.landAll("categoryTwo")
    }
    buttonCount.setOnClickListener {
      houston!!.getNotifications("categoryOne")
      houston!!.getNotifications("categoryTwo")
      val notifications = houston!!.notifications
      Toast.makeText(this, notifications.size.toString() + " notifications", Toast.LENGTH_SHORT)
          .show()
    }
  }

  override fun onResume() {
    super.onResume()
    houston!!.refreshList()
  }

  override fun onDestroy() {
    houston!!.onDestroy()
    super.onDestroy()
  }

  fun showNotification(title: String) {
    val notificationId = random.nextInt()

    val notificationBuilder = buildTestNotification(title, notificationId)
    houston!!.addDismissListener(notificationBuilder, notificationId, this@MainActivity)
    houston!!.launch(notificationId, notificationBuilder.build())
  }

  fun showNotification(
    title: String,
    category: String
  ) {
    val notificationId = random.nextInt()

    val notificationBuilder = buildTestNotification(title, notificationId)
    houston!!.addDismissListener(notificationBuilder, notificationId, this@MainActivity)
    houston!!.launch(notificationId, notificationBuilder.build(), category)
  }

  private fun buildTestNotification(
    title: String,
    notificationId: Int
  ): NotificationCompat.Builder {
    val intent = Intent(this@MainActivity, DetailActivity::class.java)
    val stackBuilder = TaskStackBuilder.create(this@MainActivity)
    stackBuilder.addParentStack(MainActivity::class.java)
    stackBuilder.addNextIntent(intent)
    val pendingIntent = stackBuilder.getPendingIntent(1, PendingIntent.FLAG_CANCEL_CURRENT)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel("channelId", "channel name", NotificationManager.IMPORTANCE_DEFAULT)
      channel.description = "channel description"
      val notificationManager = getSystemService(NotificationManager::class.java)
      notificationManager.createNotificationChannel(channel)
    }

    return NotificationCompat.Builder(this@MainActivity, "channelId")
        .setSmallIcon(R.mipmap.ic_launcher)
        .setLargeIcon(BitmapFactory.decodeResource(this@MainActivity.resources, R.mipmap.ic_launcher))
        .setAutoCancel(true)
        .setContentTitle(title)
        .setContentText("Id: $notificationId")
        .setContentIntent(pendingIntent)
        .setDefaults(Notification.DEFAULT_ALL)
  }
}
