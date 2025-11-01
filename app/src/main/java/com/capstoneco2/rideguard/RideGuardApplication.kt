package com.capstoneco2.rideguard

import android.app.Application
import com.capstoneco2.rideguard.notification.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RideGuardApplication : Application() {
	override fun onCreate() {
		super.onCreate()

		// Create notification channels early so notifications work immediately
		NotificationHelper.createNotificationChannels(this)
	}
}