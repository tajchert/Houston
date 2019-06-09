package pl.tajchert.houston

class NotificationCategory(var title: String?) {

  override fun equals(o: Any?): Boolean {
    if (this === o) return true
    if (o == null || javaClass != o.javaClass) return false

    val notificationCategory = o as NotificationCategory?

    return if (title != null) title == notificationCategory!!.title else notificationCategory!!.title == null
  }
}

