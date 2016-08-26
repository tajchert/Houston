# Houston - Notification Helper for Android
[![](https://jitpack.io/v/tajchert/Houston.svg)](https://jitpack.io/#tajchert/Houston)

![Mission Patch](https://raw.githubusercontent.com/tajchert/Houston/master/img/icon_library.jpg)

"Houston, we've had a problem here" - managing notifications on Android is a pain sometime. You need to store Ids for all of them if you wish to dismiss some of them. Creating proper managment is like inventing a wheel once more - you have done it dozens of times and involves a lot of same code.

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
    compile 'com.github.tajchert:Houston:0.1.1'
```

>"Persistence is very important" Elon Musk - SpaceX CEO
So we have:
```
houston.persistNotifications();//to save all notification currently available in getNotifications()
```

###WIP
This is very simple - categories and tags. But in most cases enought, any suggestions or Pull Request to make it more powerful are welcomed.
