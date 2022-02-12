package com.WeTechDigital.lifeTracker.service.broadcast_receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.WeTechDigital.lifeTracker.utils.Constant
import com.WeTechDigital.lifeTracker.utils.Constant.CANCEL_ACTION
import com.WeTechDigital.lifeTracker.utils.Constant.START_PHONE_SERVICES
import com.WeTechDigital.lifeTracker.utils.Constant.STOP_SERVICE_ACTION
import com.WeTechDigital.lifeTracker.utils.Constant.STOP_SERVICE_ACTION_CALL
import com.WeTechDigital.lifeTracker.utils.Constant.TAG
import com.WeTechDigital.lifeTracker.service.CallingServices
import com.WeTechDigital.lifeTracker.service.MotionDetectService

class SystemShakeAlert_broadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        when (intent?.action) {
            //cancel all the notification, service and start Motion services
            CANCEL_ACTION -> {
                Log.d(TAG, "onReceive: is called shake")
                Toast.makeText(context, "Serviced Canceled", Toast.LENGTH_SHORT).show()
                val notificationManager =
                    context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancelAll()
                context.startService(Intent(context, MotionDetectService::class.java).apply {
                    this.action = Constant.ACTION_START_SERVICE
                })

            }
            STOP_SERVICE_ACTION -> {
                context?.startService(Intent(context, MotionDetectService::class.java).apply {
                    this.action = Constant.ACTION_STOP_SERVICE
                })
            }
            //stop calling services and start motion services
            STOP_SERVICE_ACTION_CALL -> {
                context?.stopService(Intent(context, CallingServices::class.java))
                context?.startService(Intent(context, MotionDetectService::class.java).apply {
                    this.action = Constant.ACTION_START_SERVICE
                })
            }
            START_PHONE_SERVICES -> {
//                context?.stopService(Intent(context, MotionDetectService::class.java))
                context?.startService(Intent(context, CallingServices::class.java))
            }


        }

    }
}