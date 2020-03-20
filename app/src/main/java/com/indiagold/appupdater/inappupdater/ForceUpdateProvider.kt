package com.indiagold.appupdater.inappupdater

// Use the interface for ForcedUpdates
interface ForceUpdateProvider {
    fun requestUpdateShouldBeImmediate(availableVersionCode: Int, doUpdate: () -> Unit)
}
