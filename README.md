# Houston - Notification Helper for Android
[![](https://jitpack.io/v/tajchert/Houston.svg)](https://jitpack.io/#tajchert/Houston)

<img src="img/icon_library.jpg" width="200" height="200" alt="Mission Patch"/>

"Houston, we've had a problem here" - managing notifications on Android is a pain sometime. You need to store Ids for all of them if you wish to dismiss some of them.

Example: in news app you have categories and you notify user about new articles in each category. But you would like to easily dismiss notifications about particular category when user opens it, or particular one when it was clicked without storing all Ids and managing them.

###Houston to the rescue!

Houston aim is to allow easy show, hide and track all notifications displayed without hassle of Ids.



To display notification:
```
houston.launch(id, notification);
//or
houston.launch(id, notification, "anyCategory")
```

To dismiss all notifications:

```
houston.land(id); //hides only notifications with particular Id
//or
houston.landAll("anyCategory"); //hides only notifications with particular category
//or
houston.landAll();
```

Also you can get list of notifications:
```
houston.getNotification(id);
houston.getNotifications("anyCategory");
houston.getNotifications();
```

To track dismissed notifications:
```
//create notification
houston.addDismissListener(notificationBuilder, id, context);
//here goes houston.launch(...);
```

To track clicked notifications:
```
houston.removeNotification(id);
```


Refresh (not obligatory, works only on API >= 23):
```
houston.refreshList();
//In such case calling removeNotification() on opened activity is not needed
```

###How to add?

Gradle depedency:
Add Jitpack in your root build.gradle at the end of repositories:
```gradle
	allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
	}
```
Add the dependency itself:
```gradle
    compile 'com.github.tajchert:Houston:0.1.4'
```



Proguard:
```
-keep class pl.tajchert.houston.NotificationWrapper
-keep class pl.tajchert.houston.NotificationCategory

-keepclassmembers class pl.tajchert.houston.NotificationWrapper { *; }
-keepclassmembers class pl.tajchert.houston.NotificationCategory { *; }
```

>"Persistence is very important" Elon Musk - SpaceX CEO

So we have:
```
houston.persistNotifications();//to save all notification currently available in getNotifications()
```
:)

###ToDo

It is on launch sequence list but any Pull Request with those features are more than welcomed.

* Special filter for ongoing notifications
* Persist order of notifications (priority?)
* Allow to group notifications (by category?) show new group notification and hide previous ones


###WIP
This is very simple - categories and tags. But in most cases enought, any suggestions or Pull Request to make it more powerful are welcomed.
