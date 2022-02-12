package com.WeTechDigital.lifeTracker.utils


object Constant {
    const val TAG = "TAG"


    const val ACTION_START_SERVICE = "ACTION_START_OR_RESUME_SERVICE"//For starting MotionDetectService forground services this action will be trigger from MainActivity
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"//For Stopping MotionDetectService forground services permanently this action will be trigger from MainActivity
    const val STOP_SERVICE_ACTION = "STOP_SERVICE_ACTION"// For Stopping MotionDetectService forground services permanently this action will be trigger from Notification
    const val STOP_SERVICE_ACTION_CALL = "STOP_SERVICE_ACTION_CALL"//stop call service from notification
    const val CANCEL_ACTION = "CANCEL_ACTION"//cancel al the service from notification of pressing stop services
    const val START_PHONE_SERVICES = "START_PHONE_SERVICES"//starting the calling service from motion services class

    const val REQUESTED_PERMISSION_CODE = 100
    const val REQUEST_STORAGE_READ_WRITE_CODE = 500


    const val GPS_PERMISSION_CODE = 2566
    const val BROADCAST_REQUEST_CODE = 200


    const val CHANNEL_ID = "Bike" // regular notification when apps start or start service(turn on) button push
    const val CHANNEL_ALERT_SYSTEM_ID_10_SECOND_ALERT_HIGH = "CHANNEL_ALERT_SYSTEM_ID_10_SECOND_ALERT_HIGH"//10 second alert notification when motion detect when apps in background
    const val CHANNEL_ALERT_SYSTEM_ID_10_SECOND_ALERT_LOW = "CHANNEL_ALERT_SYSTEM_ID_10_SECOND_ALERT_LOW"//10 second alert notification when motion detect when apps in foreground
    const val WOMEN_SAFETY_CHANNEL_ID = "WOMEN_SAFETY_CHANNEL_ID"
    const val CHANNEL_ID_CALLING = "CHANNEL_ID_CALLING"


    const val FOREGROUND_NOTIFICATION_ID_FROM_MOTION_DETECT_SERVICE = 1
    const val FOREGROUND_NOTIFICATION_ID_FROM_CALLING_SERVICES = 5
    const val MOTION_ALERT_SYSTEM_NOTIFICATION_ID_APPS_IN_FOREGROUND = 2
    const val MOTION_ALERT_SYSTEM_NOTIFICATION_ID_APPS_IN_BACKGROUND = 3
    const val WOMEN_SAFETY_NOTIFICATION_ID = 4


    const val ACCESS_LOCATION_REQUEST_CODE = 7800
}