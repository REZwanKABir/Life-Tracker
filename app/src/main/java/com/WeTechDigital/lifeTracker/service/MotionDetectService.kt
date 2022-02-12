package com.WeTechDigital.lifeTracker.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.RingtoneManager
import android.net.Uri
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.*
import androidx.media.VolumeProviderCompat
import com.WeTechDigital.lifeTracker.R
import com.WeTechDigital.lifeTracker.service.broadcast_receiver.SystemShakeAlert_broadcastReceiver
import com.WeTechDigital.lifeTracker.utils.Constant.ACTION_START_SERVICE
import com.WeTechDigital.lifeTracker.utils.Constant.ACTION_STOP_SERVICE
import com.WeTechDigital.lifeTracker.utils.Constant.BROADCAST_REQUEST_CODE
import com.WeTechDigital.lifeTracker.utils.Constant.CANCEL_ACTION
import com.WeTechDigital.lifeTracker.utils.Constant.CHANNEL_ALERT_SYSTEM_ID_10_SECOND_ALERT_LOW
import com.WeTechDigital.lifeTracker.utils.Constant.CHANNEL_ALERT_SYSTEM_ID_10_SECOND_ALERT_HIGH
import com.WeTechDigital.lifeTracker.utils.Constant.CHANNEL_ID
import com.WeTechDigital.lifeTracker.utils.Constant.FOREGROUND_NOTIFICATION_ID_FROM_MOTION_DETECT_SERVICE
import com.WeTechDigital.lifeTracker.utils.Constant.MOTION_ALERT_SYSTEM_NOTIFICATION_ID_APPS_IN_FOREGROUND
import com.WeTechDigital.lifeTracker.utils.Constant.MOTION_ALERT_SYSTEM_NOTIFICATION_ID_APPS_IN_BACKGROUND
import com.WeTechDigital.lifeTracker.utils.Constant.START_PHONE_SERVICES
import com.WeTechDigital.lifeTracker.utils.Constant.STOP_SERVICE_ACTION
import com.WeTechDigital.lifeTracker.utils.Constant.TAG
import com.WeTechDigital.lifeTracker.utils.Constant.WOMEN_SAFETY_CHANNEL_ID
import com.WeTechDigital.lifeTracker.utils.Constant.WOMEN_SAFETY_NOTIFICATION_ID
import kotlin.math.roundToInt
import kotlin.math.sqrt


class MotionDetectService : LifecycleService(), SensorEventListener, LifecycleObserver {
    //detect apps forground and background state
    var wasInBackground = false


    //sensor ans notification
    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null
    private lateinit var notificationManager: NotificationManagerCompat


    private var accelerationX = 0.0
    private var accelerationY: Double = 0.0
    private var accelerationZ: Double = 0.0

    //accident threashold
    private val threshold = 72

    // Minimum acceleration needed to count as a shake movement
    private val MIN_SHAKE_ACCELERATION = 12

    // Minimum number of movements to register a shake
    private val MIN_MOVEMENTS = 1

    // Maximum time (in milliseconds) for the whole shake to occur
    private val MAX_SHAKE_DURATION = 500

    // Arrays to store gravity and linear acceleration values
    private val mGravity = floatArrayOf(0.0f, 0.0f, 0.0f)
    private val mLinearAcceleration = floatArrayOf(0.0f, 0.0f, 0.0f)
    private var Current = 0.0f

    // Indexes for x, y, and z values
    private val X = 0
    private val Y = 1
    private val Z = 2

    // Start time for the shake detection
    var startTime: Long = 0

    // Counter for shake movements
    var moveCount = 0
    //------------------sensor declatarion-----------///


    //----------------------googleMap------------------------//




    //   private var mediaSession: MediaSessionCompat? = null
    private var mediaSession: MediaSessionCompat? = null
    private var volumePressed = 0


    companion object {

        val uiChange = MutableLiveData<UIChange>()

    }

//initialize all the sensor and nessery attribute
    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        uiChange.postValue(UIChange.END)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        notificationManager = NotificationManagerCompat.from(this)
        volumeButtonPressed()


    }
//used to detect volume press while running the service in background
    private fun volumeButtonPressed() {
        mediaSession = MediaSessionCompat(this, "PlayerService")
        mediaSession!!.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        )
        mediaSession!!.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(
                    PlaybackStateCompat.STATE_PLAYING,
                    0,
                    0f
                ) //you simulate a player which plays something.
                .build()
        )
        val myVolumeProvider = object : VolumeProviderCompat(
            VOLUME_CONTROL_RELATIVE, /*max volume*/
            100, /*initial volume level*/
            50
        ) {
            @SuppressLint("InvalidWakeLockTag")
            override fun onAdjustVolume(direction: Int) {
                Log.e("ACTION", direction.toString())
                Log.d(TAG, "onAdjustVolume: $direction")
                Log.d(TAG, "onAdjustVolume: :count $volumePressed")
                volumePressed++
                if (volumePressed >= 4) {//checking volume button pressed twice or not {press one the value increment 2}
                    womenSafety()
                    Log.e(TAG, "onAdjustVolume: Working")
                }

            }
        }
        mediaSession!!.setPlaybackToRemote(myVolumeProvider)
        mediaSession!!.isActive = true
    }


    //-----------sensor--------------------------------------------//

    override fun onSensorChanged(event: SensorEvent?) {


        accelerationX = (event!!.values[0] * 1000).roundToInt() / 1000.0
        // Log.d(TAG, "onSensorChanged: acceleration X -------> $accelerationX")
        accelerationY = (event.values[1] * 1000).roundToInt() / 1000.0
        //Log.d(TAG, "onSensorChanged: acceleration Y -------> $accelerationY")
        accelerationZ = (event.values[2] * 1000).roundToInt() / 1000.0
        // Log.d(TAG, "onSensorChanged: acceleration Z -------> $accelerationZ")

        /* Detect Accident */
        if (accelerationX > threshold || accelerationY > threshold || accelerationZ > threshold) {

            executeAction()
        }

        // This method will be called when the accelerometer detects a change.

        // Call a helper method that wraps code from the Android developer site
        setCurrentAcceleration(event)

        // Get the max linear acceleration in any direction
        val maxLinearAcceleration = getMaxCurrentLinearAcceleration()

        // Check if the acceleration is greater than our minimum threshold
        if (maxLinearAcceleration > MIN_SHAKE_ACCELERATION) {
            val now = System.currentTimeMillis()

            // Set the startTime if it was reset to zero
            if (startTime == 0L) {
                startTime = now
            }
            val elapsedTime = now - startTime

            // Check if we're still in the shake window we defined
            if (elapsedTime > MAX_SHAKE_DURATION) {
                // Too much time has passed. Start over!
                resetShakeDetection()
            } else {
                // Keep track of all the movements
                moveCount++

                // Check if enough movements have been made to qualify as a shake
                if (moveCount > MIN_MOVEMENTS) {

                    // Reset for the next one!
                    resetShakeDetection()

                }
            }
        }

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }


    private fun setCurrentAcceleration(event: SensorEvent) {
        /*
         *  BEGIN SECTION from Android developer site. This code accounts for
         *  gravity using a high-pass filter
         */

        // alpha is calculated as t / (t + dT)
        // with t, the low-pass filter's time-constant
        // and dT, the event delivery rate
        val alpha = 0.8f

        // Gravity components of x, y, and z acceleration
        mGravity[X] =
            alpha * mGravity[X] + (1 - alpha) * event.values[X]
        mGravity[Y] =
            alpha * mGravity[Y] + (1 - alpha) * event.values[Y]
        mGravity[Z] =
            alpha * mGravity[Z] + (1 - alpha) * event.values[Z]

        // Linear acceleration along the x, y, and z axes (gravity effects removed)
        mLinearAcceleration[X] =
            event.values[X] - mGravity[X]
        mLinearAcceleration[Y] =
            event.values[Y] - mGravity[Y]
        mLinearAcceleration[Z] =
            event.values[Z] - mGravity[Z]

        /*
         *  END SECTION from Android developer site
         */
//        Log.d(TAG, "===============================================================")
//        Log.d(TAG, "===============================================================")
        Current =
            sqrt(
                (mLinearAcceleration[X] * mLinearAcceleration[X] + mLinearAcceleration[Y] *
                        mLinearAcceleration[Y] + mLinearAcceleration[Z] * mLinearAcceleration[Z]).toDouble()
            ).roundToInt()
                .toFloat()
        //  Log.d(TAG, "setCurrentAcceleration: current ---- $Current")
    }

    private fun getMaxCurrentLinearAcceleration(): Float {
        // Start by setting the value to the x value

        // Return the greatest value
        return Current
    }

    private fun resetShakeDetection() {
        startTime = 0
        moveCount = 0
    }

    private fun executeAction() {

        createAlertNotification()
        stopSelf()
    }
    //--------------------sensor-------------------------//


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when (it.action) {
                ACTION_START_SERVICE -> {
                    ForeGroundStart()
                    uiChange.postValue(UIChange.END)//used for updating Main activity ui when motion detected
                }
                ACTION_STOP_SERVICE -> {
                    uiChange.postValue(UIChange.END)//used for updating Main activity ui when motion detected
                    sensorManager?.unregisterListener(this)
                    stopForeground(true)
                    stopSelf()
                }

            }
        }



        return super.onStartCommand(intent, flags, startId)
    }
//this method to pop when volume button pressed twice to show 5 second notification to give alert that the would be making call
    private fun womenSafety() {
        mediaSession!!.isActive = false
        sensorManager?.unregisterListener(this)

        stopSelf()

        val pendingIntentCancel2 = PendingIntent.getBroadcast(
            this,
            BROADCAST_REQUEST_CODE,
            Intent(this, SystemShakeAlert_broadcastReceiver::class.java),
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        val cancelButtonInWomenSafetyNotification = PendingIntent.getBroadcast(
            this,
            BROADCAST_REQUEST_CODE,
            Intent(this, SystemShakeAlert_broadcastReceiver::class.java).also {
                it.action = CANCEL_ACTION
            },
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        val startCallingServicesFromWomenSafetyNotification = PendingIntent.getBroadcast(
            this,
            BROADCAST_REQUEST_CODE,
            Intent(this, SystemShakeAlert_broadcastReceiver::class.java).also {
                it.action = START_PHONE_SERVICES
            },
            PendingIntent.FLAG_CANCEL_CURRENT
        )


        val notification = NotificationCompat.Builder(this, WOMEN_SAFETY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_add_alert_24)
            .setContentTitle("Danger Detected")
            .setContentText("After 10 Second the service will make calls")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .addAction(R.color.RED, "Cancel", cancelButtonInWomenSafetyNotification)
            .setDeleteIntent(startCallingServicesFromWomenSafetyNotification)
            .setWhen(System.currentTimeMillis())
            .setUsesChronometer(true)
            .setFullScreenIntent(pendingIntentCancel2, true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(true)
            .setTimeoutAfter(10000)
            .build()
        notification.flags = notification.flags or Notification.FLAG_NO_CLEAR
        notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT
        notificationManager.notify(WOMEN_SAFETY_NOTIFICATION_ID, notification)
    }

   // starting actual apps service of life tracker
    private fun ForeGroundStart() {
        sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)

        val stopService = PendingIntent.getBroadcast(
            this,
            BROADCAST_REQUEST_CODE,
            Intent(this, SystemShakeAlert_broadcastReceiver::class.java).also {
                it.action = STOP_SERVICE_ACTION
            },
            PendingIntent.FLAG_CANCEL_CURRENT
        )


        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Life Tracker is Activated")
            .setContentText("Drive Safe, Keep your eyes on the road")
            .setSmallIcon(R.drawable.ic_baseline_directions_bike_24)
            .addAction(R.color.RED, "Stop Life Tracking", stopService)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .build()
        notification.visibility = Notification.VISIBILITY_PUBLIC
        startForeground(FOREGROUND_NOTIFICATION_ID_FROM_MOTION_DETECT_SERVICE, notification)
    }

/**creating notification button press functionality
 * and check what is the state of the app while motion detected if it is in background
 * then sound alert and long pop notification will appear
 * if not then notification will appear normal**/
    private fun createAlertNotification() {
        uiChange.postValue(UIChange.START)
        sensorManager?.unregisterListener(this)

        val pendingIntentCancel = PendingIntent.getBroadcast(
            this,
            BROADCAST_REQUEST_CODE,
            Intent(this, SystemShakeAlert_broadcastReceiver::class.java).also {
                it.action = CANCEL_ACTION
            },
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        val pendingIntentCancel2 = PendingIntent.getBroadcast(
            this,
            BROADCAST_REQUEST_CODE,
            Intent(this, SystemShakeAlert_broadcastReceiver::class.java),
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        val executeWhenNotificationDisappearAutomatically = PendingIntent.getBroadcast(
            this,
            BROADCAST_REQUEST_CODE,
            Intent(this, SystemShakeAlert_broadcastReceiver::class.java).also {
                it.action = START_PHONE_SERVICES
            },
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        if (!wasInBackground) {
            Log.d(TAG, "createAlertNotification: background Yes")
            val notification = NotificationCompat.Builder(this, CHANNEL_ALERT_SYSTEM_ID_10_SECOND_ALERT_HIGH)
                .setSmallIcon(R.drawable.ic_baseline_add_alert_24)
                .setContentTitle("Motion Detected")
                .setContentText("After 30 Second the service will make calls")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                //.setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .addAction(R.color.RED, "Cancel", pendingIntentCancel)
                .setFullScreenIntent(pendingIntentCancel2, true)
                .setDeleteIntent(executeWhenNotificationDisappearAutomatically)
                .setWhen(System.currentTimeMillis())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setUsesChronometer(true)
                .setSound(
                    Uri.parse(
                        "android.resource://"
                                + packageName + "/" + R.raw.siren
                    )
                )
                .setTimeoutAfter(30000)
                .build()
            notification.flags = notification.flags or Notification.FLAG_INSISTENT
            notification.flags = notification.flags or Notification.FLAG_NO_CLEAR
            notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT
            notification.visibility = Notification.VISIBILITY_PUBLIC
            notificationManager.notify(MOTION_ALERT_SYSTEM_NOTIFICATION_ID_APPS_IN_FOREGROUND, notification)
        } else {
            Log.d(TAG, "createAlertNotification: background No")
            val notification = NotificationCompat.Builder(this, CHANNEL_ALERT_SYSTEM_ID_10_SECOND_ALERT_LOW)
                .setSmallIcon(R.drawable.ic_baseline_add_alert_24)
                .setContentTitle("Motion Detected")
                .setContentText("After 30 Second the service will make calls")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                //.setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .addAction(R.color.RED, "Cancel", pendingIntentCancel)
                .setFullScreenIntent(pendingIntentCancel2, true)
                .setDeleteIntent(executeWhenNotificationDisappearAutomatically)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setWhen(System.currentTimeMillis())
                .setUsesChronometer(true)
                .setSound(
                    Uri.parse(
                        "android.resource://"
                                + packageName + "/" + R.raw.siren
                    )
                )
                .setTimeoutAfter(30000)
                .build()


            // notification.flags= Notification.FLAG_ONGOING_EVENT
            notification.flags = notification.flags or Notification.FLAG_INSISTENT
            notification.flags = notification.flags or Notification.FLAG_NO_CLEAR
            notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT
            notification.visibility = Notification.VISIBILITY_PUBLIC
            notificationManager.notify(MOTION_ALERT_SYSTEM_NOTIFICATION_ID_APPS_IN_BACKGROUND, notification)
        }


    }

//checking apps state
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        // app moved to foreground
        //  Log.d(TAG, "onMoveToForeground: in forground")
        wasInBackground = true
    }

    //checking apps state
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        // app moved to background
        // Log.d(TAG, "onMoveToBackground: in background")
        wasInBackground = false
    }

    override fun onDestroy() {
        sensorManager?.unregisterListener(this)
        mediaSession!!.isActive = false

        super.onDestroy()

    }


}