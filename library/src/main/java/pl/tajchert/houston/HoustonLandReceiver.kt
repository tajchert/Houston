package pl.tajchert.houston

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class HoustonLandReceiver : BroadcastReceiver() {

  override fun onReceive(
    context: Context,
    intent: Intent
  ) {
    if (intent.hasExtra(NOTIF_ID)) {
      val id = intent.getIntExtra(NOTIF_ID, Integer.MAX_VALUE)
      if (id != Integer.MAX_VALUE) {
        val houston = Houston(context)
        houston.land(id)
      }
    }
  }

  companion object {
    val NOTIF_ID = "pl.tajchert.houston_NOTIF_ID"
  }
}