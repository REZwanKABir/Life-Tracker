package com.WeTechDigital.lifeTracker.view

import android.Manifest
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AppOpsManager
import android.app.Dialog
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.WeTechDigital.lifeTracker.Model.ProminentModel.ProminentModel
import com.WeTechDigital.lifeTracker.RoomDataBase.OfflineDataBase_Entity.PersonalInformation_Entity
import com.WeTechDigital.lifeTracker.RoomDataBase.OfflineDataBase_Entity.SIMCard_Entity
import com.WeTechDigital.lifeTracker.controller.Contacts_RecyclerView
import com.WeTechDigital.lifeTracker.databinding.ActivityMainBinding
import com.WeTechDigital.lifeTracker.service.MotionDetectService
import com.WeTechDigital.lifeTracker.service.UIChange
import com.WeTechDigital.lifeTracker.service.broadcast_receiver.GpsLocationReceiver
import com.WeTechDigital.lifeTracker.utils.Constant.ACTION_START_SERVICE
import com.WeTechDigital.lifeTracker.utils.Constant.ACTION_STOP_SERVICE
import com.WeTechDigital.lifeTracker.utils.Constant.GPS_PERMISSION_CODE
import com.WeTechDigital.lifeTracker.utils.Constant.REQUESTED_PERMISSION_CODE
import com.WeTechDigital.lifeTracker.utils.Constant.TAG
import com.bumptech.glide.Glide
import com.WeTechDigital.lifeTracker.R
import com.WeTechDigital.lifeTracker.RoomDataBase.OfflineDataBase_Entity.BooleanState_Entity
import com.WeTechDigital.lifeTracker.controller.ProminentAdapter
import com.WeTechDigital.lifeTracker.utils.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.WeTechDigital.lifeTracker.viewModel.LocalDataBaseVM.LocalDataBaseViewModel
import com.WeTechDigital.lifeTracker.viewModel.firebaseViewModel.UserInfoViewModel
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import dmax.dialog.SpotsDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.relex.circleindicator.CircleIndicator3
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private lateinit var binding: ActivityMainBinding
    private lateinit var startDateValue: Date
    private lateinit var startDateValue2: Date
    private val simpleDateFormat: SimpleDateFormat = SimpleDateFormat("dd.MM.yyyy")
    private lateinit var endDateValue: Date
    private lateinit var endDateValue2: Date
    private var appsState = false//check apps was in resume state
    private var sosActivityState = false//this one used for checking if the user press turn on button bcoz by pressing turn of the
                                        // variable will be false and it will check permission of all that given in permission list
    private val calendar = Calendar.getInstance()
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private lateinit var mAdapter: Contacts_RecyclerView
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var localDataBaseViewModel: LocalDataBaseViewModel
    private lateinit var userActiveTime: String
    private lateinit var userInfoViewModel: UserInfoViewModel
    private var menuGlob: MenuItem? = null
    private var isInternetConnected = false
    private var internetDisposable: Disposable? = null
    private val firebaseAuth = Firebase.auth.currentUser

    private var callingProminentDisclosureOnce = true// avoid calling ProminentDisclosure dialog twice


    private var timeCountInMilliSeconds = (30 * 1000).toLong()
    private var countDownTimer: CountDownTimer? = null
    private var mSettingClient: SettingsClient? = null
    private var mLocationSettingRequest: LocationSettingsRequest? = null
    private var mLocationManager: LocationManager? = null
    private var mLocationRequest: LocationRequest? = null
    private var checkGPSObserveOnce = false//avoid calling checkGPS function once

    private lateinit var notificationManager: NotificationManager

    private var devicesName = ""
    private var permissionItem = mutableListOf<ProminentModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestPermission()
        initViewModel()
        initValue()
        setViewValue()
        setObservers()
        registerGPS()


//------------------------------------binding----------------------------------------//
        binding.apply {

            //Local Emergency button
            emergencyButton.setOnClickListener {
                startActivity(Intent(this@MainActivity, LocalAreaEmergency::class.java))
            }

            //GPS Broad Cast Receiver
            GpsLocationReceiver.GPSEnable.observe(this@MainActivity, {
                if (it == true) {
                    checkGPSObserveOnce = false
                } else {

                    if (!checkGPSObserveOnce) {
                        checkGPS()
                    }

                }
            })

            //controlling bottom nav
            bottomNavigationView.setOnItemSelectedListener { menu ->
                menuGlob = menu
                when (menu.itemId) {
                    R.id.notification -> {
                        binding.apply {
                            emergencyButton.visibility = View.GONE

                        }
                        BottomSheetBehavior.from(bottomSheet).state =
                            BottomSheetBehavior.STATE_HIDDEN
                        return@setOnItemSelectedListener NavigationUI.onNavDestinationSelected(
                            menu,
                            navController
                        )
                    }
                    R.id.mapsFragment -> {
                        binding.emergencyButton.visibility = View.VISIBLE
                        BottomSheetBehavior.from(bottomSheet).state =
                            BottomSheetBehavior.STATE_COLLAPSED
                        return@setOnItemSelectedListener NavigationUI.onNavDestinationSelected(
                            menu,
                            navController
                        )
                    }
                    else -> {
                        binding.apply {
                            emergencyButton.visibility = View.GONE

                        }
                        BottomSheetBehavior.from(bottomSheet).state =
                            BottomSheetBehavior.STATE_HIDDEN
                        return@setOnItemSelectedListener NavigationUI.onNavDestinationSelected(
                            menu,
                            navController
                        )
                    }
                }
            }

            //warning bottom sheet
            stopServicesBeforeCall.setOnClickListener {

                BottomSheetBehavior.from(bottomSheet2).state = BottomSheetBehavior.STATE_COLLAPSED
                bottomSheet2.visibility = View.GONE
                BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED

                notificationManager.cancelAll()
                serviceStart()
            }

            //controlling nav drawer
            drawerIndicator.setOnClickListener {
                drawerlayout.openDrawer(GravityCompat.START)
            }

            //sim selection
            simSelection.setOnClickListener {
                drawerlayout.closeDrawer(GravityCompat.START)
                selectSIMDialog()
            }
            //server start button
            AppTurnOn.setOnClickListener {
                BottomSheetBehavior.from(bottomSheet).apply {
                    state = BottomSheetBehavior.STATE_COLLAPSED
                }

                sosActivityState = false
                callingProminentDisclosureOnce = true
                requestPermission()

            }

            //server stop button
            turnOffServices.setOnClickListener {
                BottomSheetBehavior.from(bottomSheet).apply {
                    state = BottomSheetBehavior.STATE_COLLAPSED
                }

                Toast.makeText(
                    this@MainActivity,
                    "services stop",
                    Toast.LENGTH_SHORT
                )
                    .show()

                startService(
                    Intent(
                        this@MainActivity,
                        MotionDetectService::class.java
                    ).apply {
                        this.action = ACTION_STOP_SERVICE
                    })

            }

            //adding more trusted contacts
            addMoreContacts.setOnClickListener {

                startActivity(
                    Intent(
                        this@MainActivity,
                        TrustedNumberDetails::class.java
                    )
                )
            }

            //log out button
            navLogOut.setOnClickListener {

                Firebase.auth.signOut()
                googleSignInClient.signOut()
                startService(
                    Intent(
                        this@MainActivity,
                        MotionDetectService::class.java
                    ).apply {
                        this.action = ACTION_STOP_SERVICE
                    })
                finish()
                startActivity(Intent(this@MainActivity, SignIn::class.java).putExtra("Nuke", "YES"))
                finish()


            }
            //add new contact from navigation
            navAddContacts.setOnClickListener {


                startActivity(
                    Intent(
                        this@MainActivity,
                        TrustedNumberDetails::class.java
                    )
                )


            }

            //nav profile
            navProfile.setOnClickListener {
                try {
                    BottomSheetBehavior.from(bottomSheet).state =
                        BottomSheetBehavior.STATE_HIDDEN
                    emergencyButton.visibility = View.GONE
                    drawerlayout.closeDrawer(GravityCompat.START)
                    navController.navigate(R.id.profile)
                } catch (e: Exception) {
                    BottomSheetBehavior.from(bottomSheet).state =
                        BottomSheetBehavior.STATE_HIDDEN
                    emergencyButton.visibility = View.GONE
                    drawerlayout.closeDrawer(GravityCompat.START)
                    Log.d(TAG, "onCreate: exception happen $e")
                }
            }
            //nav emergency number
            navEmergency.setOnClickListener {
                try {
                    drawerlayout.closeDrawer(GravityCompat.START)
                    startActivity(Intent(this@MainActivity, LocalAreaEmergency::class.java))
                } catch (e: Exception) {
                    drawerlayout.closeDrawer(GravityCompat.START)
                    Log.d(TAG, "onCreate: exception happen $e")
                }
            }
            //pack purchase
            navPurchasePlan.setOnClickListener {
                try {
                    BottomSheetBehavior.from(bottomSheet).state =
                        BottomSheetBehavior.STATE_HIDDEN
                    emergencyButton.visibility = View.GONE
                    drawerlayout.closeDrawer(GravityCompat.START)
                    navController.navigate(R.id.promotion2)
                } catch (e: Exception) {
                    BottomSheetBehavior.from(bottomSheet).state =
                        BottomSheetBehavior.STATE_HIDDEN
                    emergencyButton.visibility = View.GONE
                    drawerlayout.closeDrawer(GravityCompat.START)
                    Log.d(TAG, "onCreate: exception happen $e")
                }
            }
            //package status
            navYourPlan.setOnClickListener {
                try {
                    BottomSheetBehavior.from(bottomSheet).state =
                        BottomSheetBehavior.STATE_HIDDEN
                    emergencyButton.visibility = View.GONE
                    drawerlayout.closeDrawer(GravityCompat.START)
                    navController.navigate(R.id.yourPlan2)
                } catch (e: Exception) {
                    BottomSheetBehavior.from(bottomSheet).state =
                        BottomSheetBehavior.STATE_HIDDEN
                    emergencyButton.visibility = View.GONE
                    drawerlayout.closeDrawer(GravityCompat.START)
                    Log.d(TAG, "onCreate: exception happen $e")
                }
            }
            //about page
            navAbout.setOnClickListener {
                try {
                    BottomSheetBehavior.from(bottomSheet).state =
                        BottomSheetBehavior.STATE_HIDDEN
                    emergencyButton.visibility = View.GONE
                    drawerlayout.closeDrawer(GravityCompat.START)
                    navController.navigate(R.id.information2)
                } catch (e: Exception) {
                    BottomSheetBehavior.from(bottomSheet).state =
                        BottomSheetBehavior.STATE_HIDDEN
                    emergencyButton.visibility = View.GONE
                    drawerlayout.closeDrawer(GravityCompat.START)
                    Log.d(TAG, "onCreate: exception happen $e")
                }
            }


        }


    }

    //navigate to fragment if trial or package date is over
    private fun navigateToFragmentIfNeeded() {
        binding.emergencyButton.visibility = View.GONE
        navHostFragment.findNavController().navigate(R.id.action_global_fragment)

    }


    //check gps
    private fun registerGPS() {
        val br: BroadcastReceiver = GpsLocationReceiver()
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(br, filter)
    }

//setting up navhostFragment, recycler adapter, bottomsheet and progress bar
    private fun initValue() {
        mAdapter = Contacts_RecyclerView(this)
        userActiveTime = simpleDateFormat.format(calendar.time)

        binding.apply {

            //bottomNavigationView.setupWithNavController(navController)
            setSupportActionBar(upperNav)
            navHostFragment =
                supportFragmentManager.findFragmentById(fragmentContainerView.id) as NavHostFragment
            navController = navHostFragment.navController
            NavigationUI.setupWithNavController(bottomNavigationView, navController)


            //recycler setup
            recyclerViewSheet.apply {
                adapter = mAdapter
                layoutManager = LinearLayoutManager(
                    this@MainActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                setHasFixedSize(true)
            }

            //setting up bottom sheet peek height
            BottomSheetBehavior.from(bottomSheet).apply {
                val tv = TypedValue()
                if (theme.resolveAttribute(R.attr.actionBarSize, tv, true)) {
                    val actionBarHeight = TypedValue.complexToDimensionPixelSize(
                        tv.data,
                        resources.displayMetrics
                    )
                    peekHeight = actionBarHeight / 2
                }

                state = BottomSheetBehavior.STATE_COLLAPSED
            }

            //warning bottom sheet behavior
            BottomSheetBehavior.from(bottomSheet2).apply {
                val tv = TypedValue()
                if (theme.resolveAttribute(R.attr.actionBarSize, tv, true)) {
                    val actionBarHeight = TypedValue.complexToDimensionPixelSize(
                        tv.data,
                        resources.displayMetrics
                    )
                    peekHeight = actionBarHeight / 2
                }

                state = BottomSheetBehavior.STATE_COLLAPSED
            }


        }

    }

//initialize viewmodel
    private fun initViewModel() {
    //getting manufacture name of devices
        devicesName = Build.MANUFACTURER
    //initialize notification
        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        userInfoViewModel = ViewModelProvider(this).get(UserInfoViewModel::class.java)

    //-----------------for gps permission------------
        mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        mSettingClient = LocationServices.getSettingsClient(this)
        mLocationRequest = LocationRequest.create().setInterval(5000L).setFastestInterval(5000L)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        if (mLocationManager != null) {
            mLocationSettingRequest =
                LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest!!).build()
        }
//----------end of gps permission

        //local database
        localDataBaseViewModel = ViewModelProvider(this).get(LocalDataBaseViewModel::class.java)

        //checking trail
        localDataBaseViewModel.realAllUserInformation.observe(this, {
            if (it != null) {
                trailCalculation(userActiveTime, it.Deactivate_Time, it.Subscription_Pack)

            } else {

                Toast.makeText(this, "reload the apps again", Toast.LENGTH_SHORT).show()
            }


        })


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id2))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)


    }

//THE value is set in bottom sheet contact
    private fun setViewValue() {

        binding.apply {

            val dialogue =
                SpotsDialog.Builder().setContext(this@MainActivity).setTheme(R.style.Custom)
                    .setCancelable(true).build()
            dialogue?.show()
            //setting trusted Contacts from local database
            localDataBaseViewModel.readAllContacts.observe(
                this@MainActivity,
                { contacts ->
                    dialogue?.dismiss()

                    if (contacts.isNotEmpty()) {
                        //setting user information
                        localDataBaseViewModel.realAllUserInformation.observe(
                            this@MainActivity,
                            { userInfo ->
                                if (userInfo != null) {
                                    mAdapter.setData(contacts)

                                    if (userInfo.Image != null) {
                                        Glide.with(this@MainActivity)
                                            .asBitmap()
                                            .load(userInfo.Image)
                                            .placeholder(R.drawable.ic_man)
                                            .into(navUserImage)
                                    }
                                    navuserGmail.text = userInfo.User_Email

                                }
                            })
                    }
                })


        }

    }

    //select sim fro calling and message in navigation drawer
    private fun selectSIMDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.sim_select_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        val cancel = dialog.findViewById<Button>(R.id.cancelDialog)
        val ok = dialog.findViewById<Button>(R.id.okDialog)
        val sim1Check = dialog.findViewById<RadioButton>(R.id.sim1)
        val sim2Check = dialog.findViewById<RadioButton>(R.id.sim2)

        localDataBaseViewModel.readAllSIMCardSlot.observe(this, { databaseSIM ->

            //this section is for selection sim if one select then other one will be diselect
            if (databaseSIM.isNotEmpty()) {
                //this section is for selection sim if one select then other one will be diselect
                if (databaseSIM[0].SELECTED_SIM_SLOT == "0") {
                    sim2Check.isChecked = false
                    sim1Check.isChecked = true
                } else {
                    sim1Check.isChecked = false
                    sim2Check.isChecked = true
                }
            } else {
                sim2Check.isChecked = false
                sim1Check.isChecked = false
            }

            sim1Check.setOnClickListener {
                sim2Check.isChecked = false
                sim1Check.isChecked = true
            }

            sim2Check.setOnClickListener {
                sim1Check.isChecked = false
                sim2Check.isChecked = true
            }

            //this will upload your selected sim in localdatabase
            ok.setOnClickListener {
                if (!sim1Check.isChecked && !sim2Check.isChecked) {

                    Toast.makeText(
                        this,
                        "Default SIM will make your Calls ",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (sim1Check.isChecked) {
                    localDataBaseViewModel.addSIMSlot(
                        SIMCard_Entity(
                            0, "0"
                        )
                    )

                } else if (sim2Check.isChecked) {
                    localDataBaseViewModel.addSIMSlot(
                        SIMCard_Entity(
                            0, "1"
                        )
                    )

                }
                dialog.dismiss()
            }

            cancel.setOnClickListener {
                Toast.makeText(this, "Default SIM will make your Calls ", Toast.LENGTH_SHORT)
                    .show()
                dialog.dismiss()
            }

        })

        dialog.show()

    }

//check trial calculation of apps
    private fun trailCalculation(userActiveTime: String, dueDate: String, pack: String) {
        startDateValue = simpleDateFormat.parse(userActiveTime)
        endDateValue = simpleDateFormat.parse(dueDate)

        val remain = (TimeUnit.DAYS.convert(
            (endDateValue.time - startDateValue.time),
            TimeUnit.MILLISECONDS
        )).toString()

    //show the progress bar with progress with corresponding package
        when (pack) {
            "30 days plan" -> {
                binding.apply {

                    trailProgress.max = 100
                    ObjectAnimator.ofInt(
                        trailProgress,
                        "Progress",
                        4 * (30 - Integer.parseInt(remain))
                    )
                        .setDuration(2000)
                        .start()
                    remainingDays.text = (Integer.parseInt(remain)).toString()
                    daysCounter.text = "/30 Days"

                }

            }
            "180 days plan" -> {
                binding.apply {
                    trailProgress.max = 100
                    ObjectAnimator.ofInt(
                        trailProgress,
                        "Progress",
                        4 * (180 - Integer.parseInt(remain))
                    )
                        .setDuration(2000)
                        .start()
                    remainingDays.text = (Integer.parseInt(remain)).toString()
                    daysCounter.text = "/180 Days"

                }

            }
            "365 days plan" -> {
                binding.apply {
                    trailProgress.max = 100
                    ObjectAnimator.ofInt(
                        trailProgress,
                        "Progress",
                        4 * (365 - Integer.parseInt(remain))
                    )
                        .setDuration(2000)
                        .start()
                    remainingDays.text = (Integer.parseInt(remain)).toString()
                    daysCounter.text = "/365 Days"

                }
            }
            "Trail Version" -> {
                binding.apply {
                    trailProgress.max = 100
                    ObjectAnimator.ofInt(
                        trailProgress,
                        "Progress",
                        4 * (30 - Integer.parseInt(remain))
                    )
                        .setDuration(2000)
                        .start()
                    remainingDays.text = (Integer.parseInt(remain)).toString()
                    daysCounter.text = "/30 Days"

                }
            }

        }


    }


    ////////////////////////////////////////////////Services Section \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    private fun serviceStart() {
        //check if the user has all permission
        if (Permission.hasLocationPermissions(this)) {
            //check trial or package days
            localDataBaseViewModel.realAllUserInformation.observe(this, {
                val time = trailCalculation(it.Deactivate_Time)
                if (time <= 0) {
                    Toast.makeText(this, "Your Package is Over", Toast.LENGTH_LONG).show()
                    //navigate to purchase fragment
                    navigateToFragmentIfNeeded()
                    localDataBaseViewModel.realAllUserInformation.removeObservers(this)
                } else {
                    if (it.status == "Pending") {
                        //it will check firebase if the admin update pending to on going
                        loadPackage()
                    } else {

                        startService(Intent(this, MotionDetectService::class.java).apply {
                            this.action = ACTION_START_SERVICE
                        })
                    }
                }
            })
        } else {
            callingProminentDisclosureOnce = true
            requestPermission()
        }

    }

    //check if the status is on going or not
    private fun loadPackage() {
        //checking internet connection
        if (isInternetConnected) {
            userInfoViewModel.getUserInfoLiveData.observe(this@MainActivity, { info ->


                if (info?.status == "Pending") {
                    Toast.makeText(
                        this@MainActivity,
                        "Your Package activation is on Pending",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    //if the status changed then the data will be update in local database for offline uses
                    lifecycleScope.launch(Dispatchers.IO) {
                        localDataBaseViewModel.addUserInfo(
                            PersonalInformation_Entity(
                                0,
                                info?.first_Name.toString(),
                                info?.last_Name.toString(),
                                info?.deactivate_Time.toString(),
                                info?.active_Time.toString(),
                                info?.subscription_pack.toString(),
                                info?.brought_pack_time.toString(),
                                "On going",
                                firebaseAuth?.email.toString(),
                                ImageConverter.getBitmap(
                                    info?.avatar_image.toString(),
                                    this@MainActivity
                                )
                            )
                        )
                    }
                }

            })
        } else {
            Toast.makeText(
                this@MainActivity,
                "check Your Internet Connectivity",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    //calculating active package date
    private fun trailCalculation(deactivateTime: String): Int {
        startDateValue2 = simpleDateFormat.parse(simpleDateFormat.format(calendar.time))
        endDateValue2 = simpleDateFormat.parse(deactivateTime)
        val remain = (TimeUnit.DAYS.convert(
            (endDateValue2.time - startDateValue2.time),
            TimeUnit.MILLISECONDS
        )).toString()

        return Integer.parseInt(remain)

    }

/* this method is used for observe in service
* 1. if the apps detect motion then in service it will change UIChange class for updating UI when motion is detected*/
    private fun setObservers() {
        MotionDetectService.uiChange.observe(this, {
            when (it) {
                is UIChange.START -> {
                    binding.apply {
                        start()
                        BottomSheetBehavior.from(bottomSheet).state =
                            BottomSheetBehavior.STATE_COLLAPSED
                        bottomSheet2.visibility = View.VISIBLE
                        BottomSheetBehavior.from(bottomSheet2).state =
                            BottomSheetBehavior.STATE_EXPANDED
                    }
                }
                is UIChange.END -> {
                    binding.apply {
                        bottomSheet.visibility = View.VISIBLE
                        bottomSheet2.visibility = View.GONE
                        progressBarCounter.clearAnimation()
                        countDownTimer?.cancel()
                        bottomSheet2.visibility = View.GONE
                    }
                }
            }
        })


    }
//this function is used for when motion is detected the calling bottom Sheet red circle Animation start
    private fun start() {
        setTimerValues()
        startCountDownTimer()
    }

    //setting timer for red circle animation
    private fun setTimerValues() {
        // assigning values after converting to milliseconds
        timeCountInMilliSeconds = (30 * 1000).toLong()
        setProgressBarValues()
    }

    //this is used for to determine the time to complete red circle animation
    private fun startCountDownTimer() {
        countDownTimer = object : CountDownTimer(timeCountInMilliSeconds, 50) {
            override fun onTick(millisUntilFinished: Long) {
                binding.progressBarCounter.progress = 600 - (millisUntilFinished / 50).toInt()
            }

            override fun onFinish() {
                binding.apply {
                    BottomSheetBehavior.from(bottomSheet2).state =
                        BottomSheetBehavior.STATE_COLLAPSED
                    bottomSheet2.visibility = View.GONE
                    BottomSheetBehavior.from(bottomSheet2).state =
                        BottomSheetBehavior.STATE_COLLAPSED
                    bottomSheet.visibility = View.VISIBLE
                }
                setProgressBarValues() // call to initialize the progress bar values
            }
        }.start()
    }

    //this is used for fill the color of red circle animation with smoothness animation
    private fun setProgressBarValues() {
        binding.progressBarCounter.max = timeCountInMilliSeconds.toInt() / 50
        binding.progressBarCounter.progress = timeCountInMilliSeconds.toInt() / 1000
    }


    /////////////////////////////End of services Section\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\


    //------------------------------------------------------------------------permission sector ------------------------------------------------------------


    //check gps
    private fun checkGPS() {
        checkGPSObserveOnce = true
        mSettingClient?.checkLocationSettings(mLocationSettingRequest!!)?.addOnSuccessListener {
        }?.addOnFailureListener { ex ->
            if ((ex as ApiException).statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {

                try {
                    val resolvableApiException = ex as ResolvableApiException
                    resolvableApiException.startResolutionForResult(this, GPS_PERMISSION_CODE)
                } catch (e: Exception) {

                }
            } else {
                if (ex.statusCode == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                    Toast.makeText(
                        this,
                        "GPS Enable Cannot be fixed here\nFix in Settings",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


    }

//is used for showing the dialog of sensitive permission for telling user why we are taking those sensitive permission
    private fun showProminentDisclosure() {
        callingProminentDisclosureOnce = false
        var permissionFlag = 0
        populateDataForProminentDisclosure()
        val permission = Dialog(this)
        permission.requestWindowFeature(Window.FEATURE_NO_TITLE)
        permission.setCancelable(false)
        permission.setContentView(R.layout.prominentdisclosure_all)
        val viewPager = permission.findViewById<ViewPager2>(R.id.viewPager2)
        val indicator = permission.findViewById<CircleIndicator3>(R.id.PermissionIndicator)
        val next = permission.findViewById<TextView>(R.id.dialogNextButton)
        val noThanks = permission.findViewById<TextView>(R.id.dialogNextButton2)
        viewPager.adapter = ProminentAdapter(permissionItem)
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        indicator.setViewPager(viewPager)

        next.setOnClickListener {
            viewPager.currentItem = viewPager.currentItem + 1
            if (permissionFlag > 0) {
                permission.dismiss()
                askForPermission()
                permissionFlag = 0
            }
            if (viewPager.currentItem == 3) {
                permissionFlag++
                next.text = "Turn on"
                noThanks.visibility = View.VISIBLE
            } else {
                next.text = "Next"
                noThanks.visibility = View.GONE
            }


        }

        noThanks.setOnClickListener {
            permission.dismiss()
            Toast.makeText(this, "This application will not work properly", Toast.LENGTH_SHORT)
                .show()
            permissionItem.clear()
        }




        permission.show()
        val window: Window? = permission.window
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

    }
//to populate dialog with information of sensetive data
    private fun populateDataForProminentDisclosure() {
        permissionItem.add(
            ProminentModel(
                R.drawable.ic_map,
                "Use your location",
                getString(R.string.Prominent_disclosure),
                getString(R.string.Prominent_disclosure2)
            )
        )
        permissionItem.add(
            ProminentModel(
                R.drawable.ic_permissioncall,
                "Use your Call and Call Logs",
                getString(R.string.permissionCallProminent),
                getString(R.string.permissionCallProminent2)
            )
        )
        permissionItem.add(
            ProminentModel(
                R.drawable.ic_baseline_sms_24,
                "Use your SMS system",
                getString(R.string.permissionSMSProminent1),
                getString(R.string.permissionSMSProminent2)
            )
        )
        permissionItem.add(
            ProminentModel(
                R.drawable.ic_baseline_folder_24,
                "Use your Internal Memory",
                getString(R.string.permissionReadWriteProminent1),
                getString(R.string.permissionReadWriteProminent2)
            )
        )

    }

//check permission written in manifest.xml
    private fun requestPermission() {
    //check if the use already give the permission
        if (Permission.hasLocationPermissions(this)) {
            callingProminentDisclosureOnce = false
            //check if the apps can start another activity when apps runs in background
            if (!Settings.canDrawOverlays(this)) {
                screenOverLayDialog()

            } else {
                //check if the user turn on gps or not
                if (mLocationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
                    //check if this devices can run service in background or not
                    if (canBackgroundStart(this)) {
                        //check if the manufacture of device samsung or not because samsung without permission allow noitification sound on
                        if (devicesName == "samsung") {
                            if (!sosActivityState) {
                                serviceStart()
                                appsState = true
                            }
                        } else {
                            //check if the device notification sound enable or not
                            soundCheckDialog()
                            if (!sosActivityState) {
                                serviceStart()
                                appsState = true
                            }
                        }


                    } else {
                        //start setting of background support enable of apps
                        navigateTOCorrespondingManufactureBackGroundSetting(Build.MANUFACTURER)


                    }

                } else {
                    //asking for gps permission
                    mLocationSettingRequest?.let {
                        mSettingClient?.checkLocationSettings(it)
                            ?.addOnSuccessListener {
                                if (canBackgroundStart(this)) {

                                    if (devicesName == "samsung") {
                                        if (!sosActivityState) {
                                            serviceStart()
                                            appsState = true
                                        }
                                    } else {
                                        soundCheckDialog()
                                        if (!sosActivityState) {
                                            serviceStart()
                                            appsState = true
                                        }
                                    }

                                } else {
                                    navigateTOCorrespondingManufactureBackGroundSetting(Build.MANUFACTURER)
                                }
                            }?.addOnFailureListener { ex ->
                                if ((ex as ApiException).statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {

                                    try {
                                        val resolvableApiException = ex as ResolvableApiException
                                        resolvableApiException.startResolutionForResult(
                                            this,
                                            GPS_PERMISSION_CODE
                                        )
                                    } catch (e: Exception) {
                                    }
                                } else {
                                    if (ex.statusCode == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                                        Toast.makeText(
                                            this,
                                            "GPS Enable Cannot be fixed here\nFix in Settings",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                    }
                }

            }
        } else {
            if (callingProminentDisclosureOnce) {
                showProminentDisclosure()
            }
        }


    }

    private fun askForPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept permissions to use this app.",
                REQUESTED_PERMISSION_CODE,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept permissions to use this app.",
                REQUESTED_PERMISSION_CODE,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        }
    }

//navigate TO Corresponding Manufacture BackGroundSetting
    private fun navigateTOCorrespondingManufactureBackGroundSetting(manufacture: String) {

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.request_permission_background)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        val cancel = dialog.findViewById<Button>(R.id.permissionCancel)
        val ok = dialog.findViewById<Button>(R.id.permissionOk)

        ok.setOnClickListener {
            when (manufacture) {
                "Huawei" -> {
                    ManufactureDevicesList.Huawei(this)
                }
                "Meizu" -> {
                    ManufactureDevicesList.Meizu(this)
                }
                "Sony" -> {
                    ManufactureDevicesList.Sony(this)
                }
                "OPPO" -> {
                    ManufactureDevicesList.OPPO(this)
                }
                "vivo" -> {
                    ManufactureDevicesList.Vivo(this)
                }
                "LENOVO" -> {
                    ManufactureDevicesList.Lenovo(this)
                }
                "Xiaomi" -> {
                    ManufactureDevicesList.Xiaomi(this)
                }
            }

            dialog.dismiss()
        }
        cancel.setOnClickListener {
            Toast.makeText(
                this,
                "This Application won't Work properly Background permission Permission",
                Toast.LENGTH_SHORT
            ).show()

            Handler(Looper.getMainLooper()).postDelayed({
                startService(
                    Intent(
                        this@MainActivity,
                        MotionDetectService::class.java
                    ).apply {
                        this.action = ACTION_STOP_SERVICE
                    })
                finishAndRemoveTask()

            }, 2000)

            dialog.dismiss()
        }
        dialog.show()

    }

    private fun defaultDialerDialog(){
        Log.d(TAG, "defaultDialerDialog: is called")
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.default_dialer_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        val ok = dialog.findViewById<Button>(R.id.defaultDialerOK)



        ok.setOnClickListener {
            localDataBaseViewModel.addBooleanState(BooleanState_Entity(0,"true"))
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun soundCheckDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.sound_permission_layout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        val cancel = dialog.findViewById<Button>(R.id.SpermissionCancel)
        val ok = dialog.findViewById<Button>(R.id.SpermissionOk)

        var state = ""

        localDataBaseViewModel.readAllState.observe(this,{
            state = it?.state ?: "false"

        })

        ok.setOnClickListener {
            when(state){
                "false"->{
                    allowNotificationSound()
                    defaultDialerDialog()
                    dialog.dismiss()
                }
                "true"->{
                    allowNotificationSound()
                    dialog.dismiss()
                }

            }


        }
        cancel.setOnClickListener {

            when(state){
                "false"->{
                    Snackbar.make(
                        findViewById(R.id.drawerlayout),
                        "The Sound feature of notification will not work properly! ",
                        Snackbar.LENGTH_LONG
                    ).apply {

                        animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE
                        setBackgroundTint(Color.RED)
                    }.show()

                    defaultDialerDialog()
                    dialog.dismiss()
                }
                "true"->{
                    Snackbar.make(
                        findViewById(R.id.drawerlayout),
                        "The Sound feature of notification will not work properly! ",
                        Snackbar.LENGTH_LONG
                    ).apply {

                        animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE
                        setBackgroundTint(Color.RED)
                    }.show()
                    dialog.dismiss()
                }

            }


        }
        dialog.show()

    }

//ask permission of screen overLay for start activity from services background
    private fun screenOverLayDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Permission!")
            .setMessage("Need Display Over Apps Permission")
            .setIcon(R.drawable.ic_baseline_error_24)
            .setPositiveButton("Yes") { _, _ ->
                screenOverLapRequest()
            }
            .setNegativeButton("NO") { _, _ ->
                Toast.makeText(
                    this,
                    "This Application won't Work properly without Display Over Apps Permission",
                    Toast.LENGTH_SHORT
                ).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    startService(
                        Intent(
                            this@MainActivity,
                            MotionDetectService::class.java
                        ).apply {
                            this.action = ACTION_STOP_SERVICE
                        })
                    finishAndRemoveTask()

                }, 2000)
            }.create()

        dialog.show()
    }

    private fun allowNotificationSound() {
        val settingsIntent: Intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        startActivity(settingsIntent)
    }

//setting of screen overlay activity
    private fun screenOverLapRequest() {
        //    Log.d(TAG, "screenOverLapRequest: is called")
        startActivity(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + this.packageName)
            )
        )
    }


    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(this).build().show()
            permissionItem.clear()
        } else {

            requestPermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        //check if the device allow screen overlay permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            !Settings.canDrawOverlays(this)
        ) {
           //navigate to screen overlay setting
            screenOverLayDialog()
            //       Log.d(TAG, "onPermissionsGranted: dialog")

        } else {
            //check if the user turn on gps or not
            if (mLocationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
                //check if the device run in background or not
                if (canBackgroundStart(this)) {
                    //check if the manufacture of device samsung or not because samsung without permission allow noitification sound on
                    if (devicesName == "samsung") {
                        if (!sosActivityState) {
                            serviceStart()
                            appsState = true
                        }
                    } else {
                        //check if the device notification sound enable or not
                        soundCheckDialog()
                        if (!sosActivityState) {
                            serviceStart()
                            appsState = true
                        }
                    }

                } else {
                    //start setting of background support enable of apps
                    navigateTOCorrespondingManufactureBackGroundSetting(Build.MANUFACTURER)


                }
            } else {
                //           Log.d(TAG, "onPermissionsGranted: second wall")
                mLocationSettingRequest?.let {
                    mSettingClient?.checkLocationSettings(it)
                        ?.addOnSuccessListener {
                            //                 Log.d(TAG, "requestPermission: GPS already Enable")
                            if (canBackgroundStart(this)) {
                                if (devicesName == "samsung") {
                                    if (!sosActivityState) {
                                        serviceStart()
                                        appsState = true
                                    }
                                } else {
                                    //check if the device notification sound enable or not
                                    soundCheckDialog()
                                    if (!sosActivityState) {
                                        serviceStart()
                                        appsState = true
                                    }
                                }

                            } else {
                                //start setting of background support enable of apps
                                navigateTOCorrespondingManufactureBackGroundSetting(Build.MANUFACTURER)

                            }
                        }?.addOnFailureListener { ex ->
                            if ((ex as ApiException).statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {

                                try {
                                    val resolvableApiException = ex as ResolvableApiException
                                    resolvableApiException.startResolutionForResult(
                                        this,
                                        GPS_PERMISSION_CODE
                                    )
                                } catch (e: Exception) {
                                    //                       Log.d(TAG, "requestPermission: exception $e")
                                }
                            } else {
                                if (ex.statusCode == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                                    Toast.makeText(
                                        this,
                                        "GPS Enable Cannot be fixed here\nFix in Settings",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                }
            }
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (resultCode == GPS_PERMISSION_CODE) {

            } else {
            }
        } else {
            checkGPSObserveOnce = false//avoid calling gps observe twice
            checkGPS()
        }
    }

    ///////////////////////////////////end of permission/////////////////////////////

    override fun onResume() {
        super.onResume()
        //checking device is connected to internet or not
        internetDisposable = ReactiveNetwork
            .observeInternetConnectivity()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { isConnectedToInternet ->
                isInternetConnected = isConnectedToInternet
            }
    }

    override fun onPause() {
        super.onPause()
        safelyDispose(internetDisposable)
    }

    private fun safelyDispose(disposable: Disposable?) {
        if (disposable != null && !disposable.isDisposed) {
            disposable.dispose()
        }
    }

    override fun onPostResume() {
        if (Settings.canDrawOverlays(this)) {
            if (!appsState) {
                requestPermission()
            }

        }
        super.onPostResume()
    }


    override fun onBackPressed() {
        binding.apply {
            BottomSheetBehavior.from(bottomSheet).state =
                BottomSheetBehavior.STATE_COLLAPSED
        }
        super.onBackPressed()
    }

    private fun canBackgroundStart(context: Context): Boolean {
        val ops = context.getSystemService(APP_OPS_SERVICE) as AppOpsManager
        return try {
            val op = 10021 // >= 23
            // ops.checkOpNoThrow(op, uid, packageName)
            val method: Method = ops.javaClass.getMethod(
                "checkOpNoThrow",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                String::class.java
            )
            val result = method.invoke(ops, op, Process.myUid(), context.packageName) as Int
            result == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            Log.e(TAG, "not support", e)
            true
        }
        return false
    }


}




