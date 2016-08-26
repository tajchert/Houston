package pl.tajchert.houston;


public class NotificationWrapper {
  public int id;
  public String tag;
  public NotificationCategory category;
  public Long showTime;

  public boolean equals(String tag) {
    return tag != null ? tag.equals(this.tag) : this.tag == null;
  }

  public boolean equals(NotificationCategory notificationCategory) {
    if (this.category == null) return false;
    return notificationCategory != null && category.equals(notificationCategory);
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NotificationWrapper yawn = (NotificationWrapper) o;
    return id == (yawn.id);
  }
}
