package com.WeTechDigital.lifeTracker.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.database.Cursor
import android.location.Location
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.CallLog
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.WeTechDigital.lifeTracker.R
import com.WeTechDigital.lifeTracker.service.broadcast_receiver.SystemShakeAlert_broadcastReceiver
import com.WeTechDigital.lifeTracker.utils.Constant
import com.WeTechDigital.lifeTracker.utils.Constant.TAG
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.WeTechDigital.lifeTracker.view.MainActivity
import com.WeTechDigital.lifeTracker.viewModel.LocalDataBaseVM.LocalDataBaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*

class CallingServices : LifecycleService() {

    private var count = 0
    private var count2 = 0
    private var calling = false
    private var callingFirst =
        true// when getting query all the call history will pop up but the apps need the recent call that

    //that has made by this app so when the first call finish then this flag will be true and the search queary
    //will execute
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var smsManager: SmsManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var SOSNumber2: String
    private lateinit var sms: String
    private val messagingNumber = ArrayList<String>()
    private val callingNumber = ArrayList<String>()
    private var duration: Int =
        0 //last call duration, if the user devices make a call and some one answer then the app must terminate call loop
    private var index = 0
    private var indexFlag = false
    private var simSlotFlag = false//check if the user has selected sim like sim1 or sim 2 or not
    private var simSlotNumber = 0 // sim slot present in devices
    private lateinit var localDataBaseViewModel: LocalDataBaseViewModel

    private var mPhoneStateListener: PhoneStateListener = object : PhoneStateListener() {

        /**checking state of phone call like
        is the phone not busy for calling, whether the calling number was called once or twice if twice then next to another number
         **/
        override fun onCallStateChanged(state: Int, number: String) {

            //  Log.d(Constant.TAG, "onCallStateChanged: is called ")
            when (state) {

                TelephonyManager.CALL_STATE_IDLE -> {//checking the call is idle or busy
                    if (!calling) {
                        makePhoneCall(callingNumber[count2])
                        calling = true

                    } else if (calling && duration <= 1 && count < callingNumber.size) {
                        callingFirst = false
                        // Log.d(Constant.TAG, "onCallStateChanged: count $count")
                        count2++

                        if (count2 == 2) {
                            count++
                            count2 = 0
                        }
                        if (count >= callingNumber.size) {
                            unregisterTelephoneListen()
                        } else {
                            makePhoneCall(callingNumber[count])
                        }

                    } else {
                        //  Log.d(Constant.TAG, "onCallStateChanged: unregister is called")
                        count = 0
                        count2 = 0
                        unregisterTelephoneListen()
                    }

                }


            }
            //   Log.d(Constant.TAG, "onCallStateChanged last call duration ---- else: $duration")
            duration = getLastCallDuration()

            // Toast.makeText(this@SOS, "Help", Toast.LENGTH_SHORT).show()

        }
    }

    //remove phone state listener
    private fun unregisterTelephoneListen() {
        //   Log.d(Constant.TAG, "unregisterTelephoneListen: is called")
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE)
        stopSelf()
        this.startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )

    }

    override fun onCreate() {
        super.onCreate()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager

    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ForeGroundStart()


        return super.onStartCommand(intent, flags, startId)
    }

    //starting calling service
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun ForeGroundStart() {
        localDataBaseViewModel = LocalDataBaseViewModel(this.application)
        arrangeArrayList()
        checkSimSlot()
        //MotionDetectService.isTracking.postValue(true)


        val stopService = PendingIntent.getBroadcast(
            this,
            Constant.BROADCAST_REQUEST_CODE,
            Intent(this, SystemShakeAlert_broadcastReceiver::class.java).also {
                it.action = Constant.STOP_SERVICE_ACTION_CALL
            },
            PendingIntent.FLAG_CANCEL_CURRENT
        )


        val notification = NotificationCompat.Builder(this, Constant.CHANNEL_ID_CALLING)
            .setContentTitle("Emergency Calling Services")
            .setContentText("Calling Services")
            .setSmallIcon(R.drawable.ic_baseline_call_24)
            .addAction(R.color.RED, "Stop Emergency Calling Services", stopService)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            // .setContentIntent(pendingIntent)
            .build()
        notification.visibility = Notification.VISIBILITY_PUBLIC
        startForeground(Constant.FOREGROUND_NOTIFICATION_ID_FROM_CALLING_SERVICES, notification)
    }

    //sorting number by it priority given by the user
    private fun arrangeArrayList() {
        localDataBaseViewModel.readAllContacts.observe(this, {

            val job = CoroutineScope(Dispatchers.Default).launch {
                var i = 0
                while (i < it.size) {
                    when (it[i].Priority) {
                        "First" -> {
                            Log.d(TAG, "initModelView: index $i is first")
                            if (callingNumber.isEmpty()) {
                                callingNumber.add(0, it[i].Phone)
                            } else {
                                val temp = callingNumber[0]
                                callingNumber[0] = it[i].Phone
                                callingNumber.add(i, temp)
                            }

                        }
                        "Second" -> {
                            Log.d(TAG, "initModelView: index $i is second")

                            when {
                                callingNumber.isEmpty() -> {
                                    SOSNumber2 = it[i].Phone
                                    Log.d(TAG, "initModelView: phone $SOSNumber2")
                                    callingNumber.add(i, it[i + 1].Phone)
                                    index = i
                                    //  Log.d(Constant.TAG, "initModelView: index --- > $index")
//                                Log.d(
//                                    Constant.TAG,
//                                    "initModelView: short array -- > $callingNumber"
//                                )
                                    indexFlag = true

                                }
                                callingNumber.size == 1 -> {
                                    callingNumber.add(i, it[i].Phone)
                                }
                                else -> {
                                    val temp = callingNumber[1]
                                    callingNumber[1] = it[i].Phone
                                    callingNumber.add(i, temp)
                                }
                            }


                        }
                        else -> {

                            callingNumber.add(i, it[i].Phone)
                        }
                    }
                    messagingNumber.add(i, it[i].Phone)

                    i++
                }

                if (indexFlag) {
                    //Log.d(Constant.TAG, "initModelView: flag is true")
                    callingNumber[index + 1] = SOSNumber2
                }

                Log.d(TAG, "initModelView: list number array $messagingNumber")
                Log.d(TAG, "initModelView: list number array sorted --->  $callingNumber")
            }
            job.invokeOnCompletion {
                getCurrentLocation()
            }


        })
    }

    //checking sim for calling and sending SMS
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkSimSlot() {

        localDataBaseViewModel.readAllSIMCardSlot.observe(this, {
            smsManager = if (it.isNotEmpty()) {
                simSlotFlag = true
                val subs =
                    getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

                val sim = subs.getSubscriptionIds(Integer.parseInt(it[0].SELECTED_SIM_SLOT))
                if (sim != null) {
                    simSlotNumber = Integer.parseInt(it[0].SELECTED_SIM_SLOT)
                    Log.d(TAG, "checkSimSlot: $simSlotNumber")
                    simSlotFlag = true
                    val simm = sim[0]
                    SmsManager.getSmsManagerForSubscriptionId(simm)
                } else {
                    simSlotFlag = false
                    SmsManager.getDefault()
                }

            } else {
                simSlotFlag = false
                SmsManager.getDefault()
            }
        })
    }


    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    sms =
                        "https://maps.google.com/?q=${location.latitude},${location.longitude}"
                    sendSms(sms)


                }
            }


    }

    private fun sendSms(s: String) {
        val job = CoroutineScope(Dispatchers.Default).launch {
            for (number in messagingNumber) {
                Log.d(TAG, "sendSms: is called with: $number")
                smsManager.sendTextMessage(
                    number,
                    null,
                    "The Person involved in an accident in -- > $s Please send help!!",
                    null,
                    null
                )
            }
        }
        job.invokeOnCompletion {
            telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        }


    }

    //() selecting sim module for all device and making call to corresponding sim that you give, different devices has difference way to make a call
    @SuppressLint("MissingPermission")
    private fun makePhoneCall(number: String) {

        if (simSlotFlag) {
            //  Log.e(Constant.TAG, "makePhoneCall: has sim slot")
            val phoneAccountHandleList: List<PhoneAccountHandle?>
            val item = simSlotNumber // 0 for sim1 & 1 for sim2

            val simSlotName = arrayOf(
                "extra_asus_dial_use_dualsim",
                "com.android.phone.extra.slot",
                "slot",
                "simslot",
                "sim_slot",
                "subscription",
                "Subscription",
                "phone",
                "com.android.phone.DialingMode",
                "simSlot",
                "slot_id",
                "simId",
                "simnum",
                "phone_type",
                "slotId",
                "slotIdx"
            )
            val telecomManager =
                this.getSystemService(TELECOM_SERVICE) as TelecomManager
            phoneAccountHandleList = telecomManager.callCapablePhoneAccounts
            val intent =
                Intent(Intent.ACTION_CALL).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.data = Uri.parse("tel:$number")
            intent.putExtra("c-om.android.phone.force.slot", true)
            intent.putExtra("Cdma_Supp", true)

            if (item == 0) { //for sim1
                for (s in simSlotName) {
                    intent.putExtra(s, 0) //0 or 1 according to sim.......
                }
                if (phoneAccountHandleList.isNotEmpty()) {
                    intent.putExtra(
                        "android.telecom.extra.PHONE_ACCOUNT_HANDLE",
                        phoneAccountHandleList[0]
                    )
                }
            } else { //for sim2
                for (s in simSlotName) {
                    intent.putExtra(s, 1) //0 or 1 according to sim.......
                }
                if (phoneAccountHandleList.isNotEmpty()) {
                    intent.putExtra(
                        "android.telecom.extra.PHONE_ACCOUNT_HANDLE",
                        phoneAccountHandleList[1]
                    )
                }
            }
            startActivity(intent)

        } else {
            startActivity(
                Intent(
                    Intent.ACTION_CALL,
                    Uri.parse("tel:$number")
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )

        }


    }

    //last call duration, if the user devices make a call and some one answer then the app must terminate call loop
    @SuppressLint("Range")
    private fun getLastCallDuration(): Int {
        val duration: Int
        val cur: Cursor? = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null, null, null, CallLog.Calls.DATE + " DESC limit 1;"
        )


        duration = if (cur != null && cur.moveToFirst()) {
            if (callingFirst) {
                0
            } else {
                Integer.parseInt(cur.getString(cur.getColumnIndex(CallLog.Calls.DURATION)))
            }
        } else {
            0

        }

        return duration
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: is called")
        duration = 0
        count = 0
        count2 = 0
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE)
        messagingNumber.clear()
        callingNumber.clear()
        super.onDestroy()
    }
}