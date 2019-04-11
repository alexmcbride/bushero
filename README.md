# Bus Hero
Android app to replace bus information screens at UK bus stops, written in Java. It figures out which stop you're standing at and then shows you up to date scheduling information.

## Screenshots

The main activity screen.

![alt tag](https://user-images.githubusercontent.com/1594088/35970685-3a8af054-0cc3-11e8-8d5e-507588f13409.png "BusHero main activity")

The favourites navigation drawer.

![alt tag](https://user-images.githubusercontent.com/1594088/35970687-3abe1d94-0cc3-11e8-903b-666919d7700d.png "BusHero favourites")

The route activity.

![alt tag](https://user-images.githubusercontent.com/1594088/35970686-3aa39c3a-0cc3-11e8-9706-ba8175417ac2.png "BusHero route")

## Secret.xml

The app uses [Transport API](http://www.transportapi.com) for data, in order to run you need to create a string resource file called `secret.xml` containing the following information from your account.

```XML
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <item name="apiKey" type="string">API_KEY</item>
    <item name="appId" type="string">APP_ID</item>
    <string name="google_maps_key" templateMergeStrategy="preserve" translatable="false">
        GOOGLE_MAPS_KEY
    </string>
</resources>
```
