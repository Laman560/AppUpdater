# AppUpdater

## Type of Updates Available 
There are two modes
* Flexible  - This is default  and the app User can use the app during update download, installation and restart needs to be triggered by user to be only used when adding a new feature

* Immediate - In Immediate update install user will be blocked until the app is updated to the current version of available Play Store version

## First Initialising the InAppUpdateManager
```Kotlin
    inAppUpdateManager = InAppUpdateManager(activity)
```

#### For Starting the Update Process
```Kotlin
    inAppUpdateManager.startUpdate(updateType)
```


## Forced updates
There are some reasons when an update is mandatory. For this case you can implement a provider interface to decide if an update is a forced update

```Kotlin
   class DemoForceUpdateProvider : ForceUpdateProvider {

    override fun requestUpdateShouldBeImmediate(availableVersionCode: Int, doUpdate: () -> Unit) {

        // You can place a Database Trigger here for eg you can trigger from backend that the version with 2.0 need to get Forced Updated immediately.
        // if a forced update is needed, just call doUpdate
        doUpdate()

    }
```


## Don't Forget to Override the Back Button
