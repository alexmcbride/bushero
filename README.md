# Bus Hero
Android app to replace bus information screens at UK bus stops, written in Java. It figures out which stop you're standing at and then shows you up to date scheduling information.

# Secret.xml

The app uses [Transport API](http://www.transportapi.com) for data, in order to run you need to create a string resource file called `secret.xml` containing the following information from your account.

```XML
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <item name="apiKey" type="string"><API_KEY></item>
    <item name="appId" type="string"><APP_ID></item>
</resources>
```
