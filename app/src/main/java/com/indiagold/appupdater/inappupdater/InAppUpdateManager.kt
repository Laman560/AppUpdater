package com.indiagold.appupdater.inappupdater

import android.app.Activity
import androidx.annotation.IntDef
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import io.reactivex.Observable
import io.reactivex.disposables.Disposables


class InAppUpdateManager(
    private val activity: Activity,
    private val forceUpdateProvider: ForceUpdateProvider? = null
) {

    companion object {
        const val REQUEST_CODE_IN_APP_UPDATE = 1230
        const val UPDATE_TYPE_FLEXIBLE = AppUpdateType.FLEXIBLE
        const val UPDATE_TYPE_IMMEDIATE = AppUpdateType.IMMEDIATE
    }

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)

    private var currentInAppUpdateStatus = InAppUpdateStatus()

    /**
     * Observe the status of an in app update process
     * currentInAppUpdateStatus.appUpdateInfo  ... contains info about availability of an app update
     * currentInAppUpdateStatus.appUpdateState ... contains info about the current update process
     */
    fun observeInAppUpdateStatus(): Observable<InAppUpdateStatus> {
        return Observable.create { emitter ->
            val updateStateListener = InstallStateUpdatedListener { state ->
                if (currentInAppUpdateStatus.appUpdateState?.installStatus() != state.installStatus()) {
                    currentInAppUpdateStatus = currentInAppUpdateStatus.copy(appUpdateState = state)
                    emitter.onNext(currentInAppUpdateStatus)

                    if (state.installStatus() == InstallStatus.DOWNLOADED) {
                        emitter.onComplete()
                    }
                }
            }

            // register listener
            appUpdateManager.registerListener(updateStateListener)

            // unregister listener on dispose
            emitter.setDisposable(Disposables.fromAction {
                appUpdateManager.unregisterListener(
                    updateStateListener
                )
            })

            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                currentInAppUpdateStatus =
                    currentInAppUpdateStatus.copy(appUpdateInfo = appUpdateInfo)

                // handle a forced update
                forceUpdateProvider?.requestUpdateShouldBeImmediate(currentInAppUpdateStatus.availableVersionCode) {
                    startUpdate(UPDATE_TYPE_IMMEDIATE)
                }

                // if there already is an update progress in progress we just setup it to resume
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    //set state if app gets reopened with an update in progress
                    currentInAppUpdateStatus = currentInAppUpdateStatus.copy(
                        appUpdateState = InstallState.a(
                            appUpdateInfo.installStatus(),
                            0,
                            activity.packageName
                        )
                    )
                }
                emitter.onNext(currentInAppUpdateStatus)
            }
        }
    }



       // Use updateType to set the type of the in app update(for eg. Forced or Flexible Update)
    fun startUpdate(@InAppUpdateType updateType: Int = AppUpdateType.FLEXIBLE) {
        // refetch the update status before starting the update process
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            currentInAppUpdateStatus = currentInAppUpdateStatus.copy(appUpdateInfo = appUpdateInfo)

            appUpdateManager.startUpdateFlowForResult(
                currentInAppUpdateStatus.appUpdateInfo,
                updateType,
                activity,
                REQUEST_CODE_IN_APP_UPDATE
            )
        }
    }


     //When download is complete, start the installation with this method
    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int) {
        if (requestCode == REQUEST_CODE_IN_APP_UPDATE && resultCode == Activity.RESULT_CANCELED) {
            startUpdate(UPDATE_TYPE_IMMEDIATE)
        }
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(UPDATE_TYPE_FLEXIBLE, UPDATE_TYPE_IMMEDIATE)
    annotation class InAppUpdateType
}
