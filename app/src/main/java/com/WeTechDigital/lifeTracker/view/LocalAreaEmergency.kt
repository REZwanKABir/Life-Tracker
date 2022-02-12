package com.WeTechDigital.lifeTracker.view

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.WeTechDigital.lifeTracker.R
import com.WeTechDigital.lifeTracker.controller.LocalEmergency_RecyclerView
import com.WeTechDigital.lifeTracker.databinding.ActivityLocalAreaEmergencyBinding
import com.WeTechDigital.lifeTracker.utils.Constant.TAG
import com.WeTechDigital.lifeTracker.utils.ExcelReader
import com.google.android.gms.location.*
import java.util.*





class LocalAreaEmergency : AppCompatActivity() {
    private lateinit var binding: ActivityLocalAreaEmergencyBinding
    private lateinit var mAdapter: LocalEmergency_RecyclerView
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private var state: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocalAreaEmergencyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)



        inti()

        binding.apply {
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    mAdapter.clear()
                    Log.d(
                        TAG,
                        "onItemSelected: place name ${
                            parent?.getItemAtPosition(position).toString()
                        }"
                    )
                    setDataInAdapter(state, parent?.getItemAtPosition(position).toString())
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }
        }

    }

    private fun inti() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().setInterval(5000L).setFastestInterval(5000L)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        mAdapter = LocalEmergency_RecyclerView(this)

        binding.apply {
            localEmergencyList.apply {
                layoutManager = LinearLayoutManager(this@LocalAreaEmergency)
                setHasFixedSize(true)
                adapter = mAdapter

            }
        }
    }


    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        locationRequest?.let {
            fusedLocationProviderClient!!.requestLocationUpdates(
                it,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
    }

    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            getAddress(locationResult.lastLocation)

        }
    }

    fun getAddress(lastLocation: Location) {
        Log.d(TAG, "getAddress: is called")
        try {
            val addresses: List<Address>
            val geocoder = Geocoder(this, Locale.getDefault())
            addresses = geocoder.getFromLocation(
                lastLocation.latitude,
                lastLocation.longitude,
                1
            )
            val city = addresses[0].locality
            val Division = addresses[0].adminArea // Only if available else return NULL
            Log.d(
                TAG,
                "getAddress: City: $city,\nstate: $Division"

            )
            fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
            initializeSpinnerAdapter(Division.split(" ".toRegex()).toTypedArray()[0])
            state = Division


        } catch (e: Exception) {
            Log.d(TAG, "getAddress: exception $e")
            fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
        }


    }

    private fun setDataInAdapter(state: String, thana: String) {
        Log.d(TAG, "setDataInAdapter: is called")
        when (state.split(" ".toRegex()).toTypedArray()[0]) {
            "ঢাকা" -> {
                mAdapter.setData(
                    ExcelReader.readxl(
                        this, "Dhaka",
                        thana
                    )
                )
            }
            "রাজশাহী" -> {
                mAdapter.setData(
                    ExcelReader.readxl(
                        this, "Rajshahi",
                        thana
                    )
                )
            }
            "সিলেট" -> {
                mAdapter.setData(
                    ExcelReader.readxl(
                        this, "Sylhet",
                        thana
                    )
                )
            }
            "রংপুর" -> {
                mAdapter.setData(
                    ExcelReader.readxl(
                        this, "Rangpur",
                        thana
                    )
                )
            }
            "ময়মনসিংহ" -> {
                mAdapter.setData(
                    ExcelReader.readxl(
                        this, "Mymensingh",
                        thana
                    )
                )
            }
            "বরিশাল" -> {
                mAdapter.setData(
                    ExcelReader.readxl(
                        this, "Barisal",
                        thana
                    )
                )
            }
            "খুলনা" -> {
                mAdapter.setData(
                    ExcelReader.readxl(
                        this, "Khulna",
                        thana
                    )
                )
            }
            else -> {
                mAdapter.setData(
                    ExcelReader.readxl(
                        this, state.split(" ".toRegex()).toTypedArray()[0],
                        thana
                    )
                )
            }
        }


    }

    private fun initializeSpinnerAdapter(state: String) {

        when (state) {
            "Rajshahi" -> {
//                adapter = ArrayAdapter(
//                    this,
//                    android.R.layout.simple_spinner_item,
//                    resources.getStringArray(R.array.Rajshahi)
//
//                )
                setAdapter(resources.getStringArray(R.array.Rajshahi))
//                adapter = ArrayAdapter.createFromResource(this,R.array.Rajshahi,R.layout.spinner_layout_format)
//                setSpinnerAdapter(adapter)
            }
            "রাজশাহী" -> {
//                adapter = ArrayAdapter(
//                    this,
//                    android.R.layout.simple_spinner_item,
//                    resources.getStringArray(R.array.Rajshahi)
//
//                )
                setAdapter(resources.getStringArray(R.array.Rajshahi))
//                adapter = ArrayAdapter.createFromResource(this,R.array.Rajshahi,R.layout.spinner_layout_format)
//                setSpinnerAdapter(adapter)
            }
            "Sylhet" -> {
//                adapter = ArrayAdapter(
//                    this,
//                    android.R.layout.simple_spinner_item,
//                    resources.getStringArray(R.array.Sylhet)
//
//                )
                setAdapter(resources.getStringArray(R.array.Sylhet))
//                adapter = ArrayAdapter.createFromResource(this,R.array.Sylhet,R.layout.spinner_layout_format)
//                setSpinnerAdapter(adapter)
            }
            "সিলেট" -> {
//                adapter = ArrayAdapter(
//                    this,
//                    android.R.layout.simple_spinner_item,
//                    resources.getStringArray(R.array.Sylhet)
//
//                )
                setAdapter(resources.getStringArray(R.array.Sylhet))
//                adapter = ArrayAdapter.createFromResource(this,R.array.Sylhet,R.layout.spinner_layout_format)
//                setSpinnerAdapter(adapter)
            }
            "Rangpur" -> {
//                adapter = ArrayAdapter(
//                    this,
//                    android.R.layout.simple_spinner_item,
//                    resources.getStringArray(R.array.Rangpur)
//
//                )
                setAdapter(resources.getStringArray(R.array.Rangpur))
//                adapter = ArrayAdapter.createFromResource(this,R.array.Rangpur,R.layout.spinner_layout_format)
//                setSpinnerAdapter(adapter)

            }
            "রংপুর" -> {
//                adapter = ArrayAdapter(
//                    this,
//                    android.R.layout.simple_spinner_item,
//                    resources.getStringArray(R.array.Rangpur)
//
//                )
                setAdapter(resources.getStringArray(R.array.Rangpur))
//                adapter = ArrayAdapter.createFromResource(this,R.array.Rangpur,R.layout.spinner_layout_format)
//                setSpinnerAdapter(adapter)
            }
            "Mymensingh" -> {
//                adapter = ArrayAdapter(
//                    this,
//                    android.R.layout.simple_spinner_item,
//                    resources.getStringArray(R.array.Mymensingh)
//
//                )
                setAdapter(resources.getStringArray(R.array.Mymensingh))
//                adapter = ArrayAdapter.createFromResource(this,R.array.Mymensingh,R.layout.spinner_layout_format)
//                setSpinnerAdapter(adapter)
            }
            "ময়মনসিংহ" -> {
//                adapter = ArrayAdapter(
//                    this,
//                    android.R.layout.simple_spinner_item,
//                    resources.getStringArray(R.array.Mymensingh)
//
//                )
                setAdapter(resources.getStringArray(R.array.Mymensingh))
//                adapter = ArrayAdapter.createFromResource(this,R.array.Mymensingh,R.layout.spinner_layout_format)
//                setSpinnerAdapter(adapter)
            }
            "Barisal" -> {
//                adapter = ArrayAdapter(
//                    this,
//                    android.R.layout.simple_spinner_item,
//                    resources.getStringArray(R.array.Barisal)
//
//                )
                setAdapter(resources.getStringArray(R.array.Barisal))
//                adapter = ArrayAdapter.createFromResource(this,R.array.Barisal,R.layout.spinner_layout_format)
//                setSpinnerAdapter(adapter)
            }
            "বরিশাল" -> {
//                adapter = ArrayAdapter(
//                    this,
//                    android.R.layout.simple_spinner_item,
//                    resources.getStringArray(R.array.Barisal)
//
//                )
                setAdapter(resources.getStringArray(R.array.Barisal))
//                adapter = ArrayAdapter.createFromResource(this,R.array.Barisal,R.layout.spinner_layout_format)
//                setSpinnerAdapter(adapter)
            }
            "Dhaka" -> {
//                adapter = ArrayAdapter(
//                    this,
//                    android.R.layout.simple_spinner_item,
//                    resources.getStringArray(R.array.Dhaka)
//
//                )

                setAdapter(resources.getStringArray(R.array.Dhaka))


            }
            "ঢাকা" -> {
//                adapter = ArrayAdapter(
//                    this,
//                    android.R.layout.simple_spinner_item,
//                    resources.getStringArray(R.array.Dhaka)
//
//                )
//                adapter = ArrayAdapter.createFromResource(this,R.array.Dhaka,R.layout.spinner_layout_format)
//                setSpinnerAdapter(adapter)
                setAdapter(resources.getStringArray(R.array.Dhaka))
            }
            "Khulna" -> {
//                adapter = ArrayAdapter(
//                    this,
//                    android.R.layout.simple_spinner_item,
//                    resources.getStringArray(R.array.Khulna)
//
//                )
                setAdapter(resources.getStringArray(R.array.Khulna))
//                adapter = ArrayAdapter.createFromResource(this,R.array.Khulna,R.layout.spinner_layout_format)
//
//                setSpinnerAdapter(adapter)
            }
            "খুলনা" -> {
//                adapter = ArrayAdapter(
//                    this,
//                    android.R.layout.simple_spinner_item,
//                    resources.getStringArray(R.array.Khulna)
//
//                )
                setAdapter(resources.getStringArray(R.array.Khulna))
//                adapter = ArrayAdapter.createFromResource(this,R.array.Khulna,R.layout.spinner_layout_format)
//
//                setSpinnerAdapter(adapter)
            }
            else -> {
//                adapter = ArrayAdapter(
//                    this,
//                    android.R.layout.simple_spinner_item,
//                    resources.getStringArray(R.array.DISTRICT)
//
//                )
                setAdapter(resources.getStringArray(R.array.DISTRICT))
//                adapter = ArrayAdapter.createFromResource(this,R.array.DISTRICT,R.layout.spinner_layout_format)
//                setSpinnerAdapter(adapter)
            }
        }

    }
    private fun setAdapter(stringArray: Array<String>){
        val adapter = object: ArrayAdapter<Any>(this, R.layout.spinner_layout_format,stringArray) {
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                return super.getDropDownView(position, convertView, parent).also { view ->
                    if(position == binding.spinner.selectedItemPosition){
                        view.setBackgroundColor(Color.rgb(204, 255, 255))
                    }else{
                        view.setBackgroundColor(getColor(R.color.selectedSpinner))
                    }

                }
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        setSpinnerAdapter(adapter)
    }

    private fun setSpinnerAdapter(adapterValue: ArrayAdapter<Any>) {
        binding.apply {
            spinner.adapter = adapterValue

        }
    }

    override fun onStart() {
        super.onStart()
        startLocationUpdates()
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }
}