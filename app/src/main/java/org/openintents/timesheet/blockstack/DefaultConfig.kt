package org.openintents.timesheet.blockstack

import org.blockstack.android.sdk.Scope
import org.blockstack.android.sdk.toBlockstackConfig
import org.openintents.timesheet.BuildConfig

val config = BuildConfig.APP_DOMAIN.toBlockstackConfig(arrayOf(Scope.StoreWrite, Scope.PublishData))