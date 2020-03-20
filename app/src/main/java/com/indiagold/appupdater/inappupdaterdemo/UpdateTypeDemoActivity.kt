package com.indiagold.appupdater.inappupdaterdemo
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.indiagold.appupdater.R
import com.indiagold.appupdater.inappupdater.InAppUpdateManager
import com.indiagold.appupdater.inappupdater.InAppUpdateStatus
import io.reactivex.disposables.Disposables
import kotlinx.android.synthetic.main.activity_update_type.*

class UpdateTypeDemoActivity : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context) = Intent(context, UpdateTypeDemoActivity::class.java)
    }

    private lateinit var inAppUpdateManager: InAppUpdateManager
    private var inAppUpdateStatusDisposable = Disposables.empty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_type)

        tvPackageName.text = packageName

        val pInfo = applicationContext.packageManager.getPackageInfo(packageName, 0)
        var version = pInfo.versionCode

        tvCurrentVersionCode.text = version.toString()

        initInAppUpdate()

        btUpdate.setOnClickListener {
            inAppUpdateManager.startUpdate(getSelectedUpdateType())
        }

        btRestart.setOnClickListener {
            inAppUpdateManager.completeUpdate()
        }
    }

    @SuppressLint("WrongConstant")
    @InAppUpdateManager.InAppUpdateType
    private fun getSelectedUpdateType() =
        if (updateTypeGroup.checkedButtonId == R.id.btImmediate) InAppUpdateManager.UPDATE_TYPE_IMMEDIATE else InAppUpdateManager.UPDATE_TYPE_FLEXIBLE

    private fun initInAppUpdate() {
        inAppUpdateManager = InAppUpdateManager(this)
        inAppUpdateStatusDisposable = inAppUpdateManager.observeInAppUpdateStatus()
            .subscribe { currentStatus ->
                if (currentStatus.isUpdatePending()) {
                    inAppUpdateManager.startUpdate()
                }
                updateUI(currentStatus)
            }
    }

    private fun updateUI(currentStatus: InAppUpdateStatus) {
        if (currentStatus.isUpdateInProgress) {
            showUpdateInProgressState(currentStatus)
        } else {
            tvAvailableAppVersion.text = if (currentStatus.availableVersionCode == 0) {
                "App should be on Play Store"
            } else {
                currentStatus.availableVersionCode.toString()
            }
            if (currentStatus.isUpdateAvailable()) {
                tvUpdateAvailable.text = "YES"
                showUpdateAvailableState()
            } else {
                tvUpdateAvailable.text = "NO"
            }
        }
    }
    private fun showUpdateAvailableState() {
        btUpdate.visibility = View.VISIBLE
        vgUpdateInProgress.visibility = View.GONE
        vgUpdateAvailable.visibility = View.VISIBLE
        vgUpdateFinished.visibility = View.GONE
    }
    private fun showUpdateInProgressState(currentStatus: InAppUpdateStatus) {
        if (currentStatus.isDownloading) {
            vgUpdateInProgress.visibility = View.VISIBLE
            vgUpdateAvailable.visibility = View.GONE
            vgUpdateFinished.visibility = View.GONE
        } else if (currentStatus.isDownloaded) {
            vgUpdateInProgress.visibility = View.GONE
            vgUpdateAvailable.visibility = View.GONE
            vgUpdateFinished.visibility = View.VISIBLE
        }
    }
    override fun onDestroy() {
        if (!inAppUpdateStatusDisposable.isDisposed)
            inAppUpdateStatusDisposable.dispose()
        super.onDestroy()
    }
}
