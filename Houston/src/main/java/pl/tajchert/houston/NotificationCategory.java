package pl.tajchert.houston;


public class NotificationCategory {
  public String title;

  public NotificationCategory(String title) {
    this.title = title;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    NotificationCategory notificationCategory = (NotificationCategory) o;

    return title != null ? title.equals(notificationCategory.title) : notificationCategory.title == null;
  }
}

