package pl.tajchert.houston;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class HoustonLandReceiver extends BroadcastReceiver {
  public static final String NOTIF_ID = "pl.tajchert.houston_NOTIF_ID";

  @Override public void onReceive(Context context, Intent intent) {
    if (intent.hasExtra(NOTIF_ID)) {
      int id = intent.getIntExtra(NOTIF_ID, Integer.MAX_VALUE);
      if (id != Integer.MAX_VALUE) {
        Houston houston = new Houston(context);
        houston.land(id);
      }
    }
  }
}