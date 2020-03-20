package com.indiagold.appupdater.inappupdater

import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

const val NO_UPDATE = -1

data class InAppUpdateStatus(
    val appUpdateInfo: AppUpdateInfo? = null,
    val appUpdateState: InstallState? = null
) {

    val availableVersionCode: Int
        get() = appUpdateInfo?.availableVersionCode() ?: NO_UPDATE

    //Call this to check if update is in progress
    val isUpdateInProgress: Boolean
        get() = appUpdateState != null

    val isDownloading: Boolean
        get() = appUpdateState?.installStatus() == InstallStatus.DOWNLOADING

    val isDownloaded: Boolean
        get() = appUpdateState?.installStatus() == InstallStatus.DOWNLOADED

    //For checking if update is available
    fun isUpdateAvailable() =
        appUpdateInfo?.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE

    //For checking if update is availabe but not installed
    fun isUpdatePending() =
        appUpdateInfo?.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
}
