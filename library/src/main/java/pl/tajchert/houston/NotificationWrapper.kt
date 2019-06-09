package pl.tajchert.houston

class NotificationWrapper {
  var id: Int = 0
  var tag: String? = null
  var category: NotificationCategory? = null
  var showTime: Long? = null

  fun equals(tag: String?): Boolean {
    return if (tag != null) tag == this.tag else this.tag == null
  }

  fun equals(notificationCategory: NotificationCategory?): Boolean {
    return if (this.category == null) false else notificationCategory != null && category == notificationCategory
  }

  override fun equals(o: Any?): Boolean {
    if (this === o) return true
    if (o == null || javaClass != o.javaClass) return false
    val yawn = o as NotificationWrapper?
    return id == yawn!!.id
  }
}
