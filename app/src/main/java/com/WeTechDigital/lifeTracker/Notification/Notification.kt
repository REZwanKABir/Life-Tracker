package com.WeTechDigital.lifeTracker.Notification

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.WeTechDigital.lifeTracker.R
import com.WeTechDigital.lifeTracker.utils.Constant.CHANNEL_ALERT_SYSTEM_ID_10_SECOND_ALERT_LOW
import com.WeTechDigital.lifeTracker.utils.Constant.CHANNEL_ALERT_SYSTEM_ID_10_SECOND_ALERT_HIGH
import com.WeTechDigital.lifeTracker.utils.Constant.CHANNEL_ID
import com.WeTechDigital.lifeTracker.utils.Constant.CHANNEL_ID_CALLING
import com.WeTechDigital.lifeTracker.utils.Constant.WOMEN_SAFETY_CHANNEL_ID


class Notification : Application() {
    override fun onCreate() {
        super.onCreate()
        MotionDetectService()
        MotionDetectHappenAlert_10Sec_Notification_BackGroundHighAlertService()
        MotionDetectHappenAlert_10Sec_Notification_BackGroundLowAlertService()
        womenSafetyAlert()
        createPhoneCallingService()
    }

    //calling service notification channel
    private fun createPhoneCallingService() {

        val serviceChannel = NotificationChannel(
            CHANNEL_ID_CALLING,
            "Calling",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(
            NotificationManager::class.java
        )
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
        serviceChannel.setSound(
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
            audioAttributes
        )
        serviceChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        manager.createNotificationChannel(serviceChannel)

    }
//women safety notification channel
    private fun womenSafetyAlert() {
        val serviceChannel = NotificationChannel(
            WOMEN_SAFETY_CHANNEL_ID,
            "Women Safety Alert",
            NotificationManager.IMPORTANCE_HIGH
        )
        serviceChannel.description = "5 second reminder"
        val manager = getSystemService(
            NotificationManager::class.java
        )
        serviceChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        serviceChannel.setSound(null, null)
        manager.createNotificationChannel(serviceChannel)
    }

    /*Motion detect service notification when app in foreground state here the apps notification will not show in display like pop up
    and can be destroyed by swiping*/
    private fun MotionDetectHappenAlert_10Sec_Notification_BackGroundLowAlertService() {

        val serviceChannel = NotificationChannel(
            CHANNEL_ALERT_SYSTEM_ID_10_SECOND_ALERT_LOW,
            "Alert System When app in foreground",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        serviceChannel.description = "30 second reminder"
        val manager = getSystemService(
            NotificationManager::class.java
        )

        val sound =
            Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + R.raw.siren)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()

        //vibration pattern
        serviceChannel.vibrationPattern = longArrayOf(
            1000,
            1000,
            1000,
            1000,
            1000,
            1000,
            1000,
            1000
        )
        serviceChannel.enableVibration(true)
        serviceChannel.shouldVibrate()
        serviceChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        serviceChannel.setSound(
            sound,
            audioAttributes
        )
        manager.createNotificationChannel(serviceChannel)
    }

    /*when Motion detect happen the alert notification show for 10 second, when app in background state here the apps notification will be show in display like pop up
and cannot be destroyed by swiping*/
    private fun MotionDetectHappenAlert_10Sec_Notification_BackGroundHighAlertService() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ALERT_SYSTEM_ID_10_SECOND_ALERT_HIGH,
            "Alert System When app in background",
            NotificationManager.IMPORTANCE_HIGH
        )
        serviceChannel.description = "30 second reminder"
        val manager = getSystemService(
            NotificationManager::class.java
        )
        val sound =
            Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + R.raw.siren)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()

        serviceChannel.vibrationPattern = longArrayOf(
            1000,
            1000,
            1000,
            1000,
            1000,
            1000,
            1000,
            1000
        )
        serviceChannel.enableVibration(true)
        serviceChannel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        serviceChannel.shouldVibrate()
        serviceChannel.setSound(
            sound,
            audioAttributes
        )
        manager.createNotificationChannel(serviceChannel)

    }
//regular notification that will make apps to perform task in background , this notification display when the apps start or start services button pressed
    private fun MotionDetectService() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Map and Sensor Notification",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(
            NotificationManager::class.java
        )
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
        serviceChannel.setSound(
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
            audioAttributes
        )
        serviceChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        manager.createNotificationChannel(serviceChannel)

    }
}