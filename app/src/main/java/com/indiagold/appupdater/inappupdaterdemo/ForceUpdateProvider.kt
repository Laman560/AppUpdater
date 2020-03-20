package com.indiagold.appupdater.inappupdaterdemo

import com.indiagold.appupdater.inappupdater.ForceUpdateProvider


class ForceUpdateProvider :
    ForceUpdateProvider {

    override fun requestUpdateShouldBeImmediate(availableVersionCode: Int, doUpdate: () -> Unit) {

        // update l
        // if a forced update is needed, just call doUpdate
        doUpdate()

    }
}
