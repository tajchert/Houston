package pl.tajchert.sample;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.tajchert.houston.Houston;
import pl.tajchert.houston.NotificationWrapper;

public class MainActivity extends AppCompatActivity {
  private Houston houston;
  private Random random = new Random();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    houston = new Houston(this);
  }

  @Override protected void onResume() {
    super.onResume();
    houston.refreshList();
  }

  @Override protected void onDestroy() {
    houston.onDestroy();
    super.onDestroy();
  }

  @OnClick(R.id.buttonSendOne) public void onClickSendOne() {
    showNotification("Notification #1", "categoryOne");
  }

  @OnClick(R.id.buttonSendTwo) public void onClickSendTwo() {
    showNotification("Notification #2", "categoryTwo");
  }

  @OnClick(R.id.buttonHideOne) public void onClickHideOne() {
    houston.landAll("categoryOne");
  }

  @OnClick(R.id.buttonHideTwo) public void onClickHideTwo() {
    houston.landAll("categoryTwo");
  }

  @OnClick(R.id.buttonCount) public void onClickCount() {
    houston.getNotifications("categoryOne");
    houston.getNotifications("categoryTwo");

    ArrayList<NotificationWrapper> notifications = houston.getNotifications();
    Toast.makeText(this, notifications.size() + " notifications", Toast.LENGTH_SHORT).show();
  }

  public void showNotification(String title) {
    int notificationId = random.nextInt();

    NotificationCompat.Builder notificationBuilder = buildTestNotification(title, notificationId);
    houston.addDismissListener(notificationBuilder, notificationId, MainActivity.this);
    houston.launch(notificationId, notificationBuilder.build());
  }

  public void showNotification(String title, String category) {
    int notificationId = random.nextInt();

    NotificationCompat.Builder notificationBuilder = buildTestNotification(title, notificationId);
    houston.addDismissListener(notificationBuilder, notificationId, MainActivity.this);
    houston.launch(notificationId, notificationBuilder.build(), category);
  }

  private NotificationCompat.Builder buildTestNotification(String title, int notificationId) {
    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
    TaskStackBuilder stackBuilder = TaskStackBuilder.create(MainActivity.this);
    stackBuilder.addParentStack(MainActivity.class);
    stackBuilder.addNextIntent(intent);
    PendingIntent pendingIntent = stackBuilder.getPendingIntent(1, PendingIntent.FLAG_CANCEL_CURRENT);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel channel = new NotificationChannel("channelId", "channel name", NotificationManager.IMPORTANCE_DEFAULT);
      channel.setDescription("channel description");
      NotificationManager notificationManager = getSystemService(NotificationManager.class);
      notificationManager.createNotificationChannel(channel);
    }

    return new NotificationCompat.Builder(MainActivity.this, "channelId").setSmallIcon(R.mipmap.ic_launcher)
        .setLargeIcon(BitmapFactory.decodeResource(MainActivity.this.getResources(), R.mipmap.ic_launcher))
        .setAutoCancel(true)
        .setContentTitle(title)
        .setContentText("Id: " + notificationId)
        .setContentIntent(pendingIntent)
        .setDefaults(Notification.DEFAULT_ALL);
  }
}
