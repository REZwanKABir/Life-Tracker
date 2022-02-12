package com.WeTechDigital.lifeTracker.service.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.location.LocationManager
import androidx.lifecycle.MutableLiveData


class GpsLocationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        //  Log.d(TAG, "onReceive: is called 1")

        intent?.action?.let { act ->
            //    Log.d(TAG, "onReceive: GPS is called")
            if (act.matches("android.location.PROVIDERS_CHANGED".toRegex())) {
                //  Log.d(TAG, "onReceive: Inside of GPS")
                val locationManager =
                    context!!.getSystemService(LOCATION_SERVICE) as LocationManager


                //Start your Activity if location was enabled:
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    //  Log.d(TAG, "onReceive: gps enable")
                    GPSEnable.postValue(true)
                } else {
                    //   Log.d(TAG, "onReceive: gps desable")
                    GPSEnable.postValue(false)
                }
            }
        }

    }

    companion object {
        val GPSEnable = MutableLiveData<Boolean>()
    }

}