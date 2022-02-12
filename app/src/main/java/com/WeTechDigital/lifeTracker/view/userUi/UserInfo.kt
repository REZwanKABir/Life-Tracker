package com.WeTechDigital.lifeTracker.view.userUi

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.WeTechDigital.lifeTracker.Model.UserInfoModel
import com.WeTechDigital.lifeTracker.R
import com.WeTechDigital.lifeTracker.RoomDataBase.OfflineDataBase_Entity.PersonalInformation_Entity
import com.WeTechDigital.lifeTracker.RoomDataBase.OfflineDataBase_Entity.SIMCard_Entity
import com.WeTechDigital.lifeTracker.databinding.ActivityUserInfoBinding
import com.WeTechDigital.lifeTracker.utils.Constant.TAG
import com.WeTechDigital.lifeTracker.utils.ImageConverter
import com.WeTechDigital.lifeTracker.utils.LoadingDialog
import com.WeTechDigital.lifeTracker.view.TrustedNumberDetails
import com.WeTechDigital.lifeTracker.viewModel.LocalDataBaseVM.LocalDataBaseViewModel
import com.WeTechDigital.lifeTracker.viewModel.firebaseViewModel.*
import com.bumptech.glide.Glide
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class UserInfo : AppCompatActivity() {
    private lateinit var binding: ActivityUserInfoBinding
    @DelicateCoroutinesApi
    private lateinit var signInViewModel: SignInViewModel
    @DelicateCoroutinesApi
    private lateinit var userInfoViewModel: UserInfoViewModel

    @DelicateCoroutinesApi
    private lateinit var downloadXLViewModel: DownloadXLViewModel
    private var afterThirtyDays: String? = null
    private var currentDate: String? = null
    private var currentDate2: String? = null
    private val calendar = Calendar.getInstance()
    private val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy")
    private val simpleDateFormat2 = SimpleDateFormat("EEE, d MMM -HH:mm")
    private lateinit var localDataBaseViewModel: LocalDataBaseViewModel
    private var globalEmail: String = "null"
    private var globalImage: String = "null"
    private var subsPack: String = "Trail Version"
    private var broughtPack: String = "null"
    private var status: String = "On going"
    private var isInternetConnected = false
    private var internetDisposable: Disposable? = null
    private lateinit var simSlotViewModel: SimSlotViewModel
    private lateinit var startDateValue2: Date
    private lateinit var endDateValue2: Date
    private var userIsNotNull = false




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        initModel()
        formattingDate()
        setInfo()
        simSlot()


        binding.apply {
            next.setOnClickListener {

                if (TextUtils.isEmpty(userFirstName.text.toString()) || TextUtils.isEmpty(
                        userLastName.text.toString()
                    )
                ) {
                    userFirstName.error = "Empty Field"
                    userLastName.error = "Empty Field"
                    return@setOnClickListener
                } else {

                    if (isInternetConnected) {
                        insertValue(
                            userFirstName.text.toString(),
                            userLastName.text.toString(),

                            )
                        Log.d(TAG, "getInfo: " + userLastName.text)
                    } else {
                        Toast.makeText(
                            this@UserInfo,
                            "You need Internet Connect",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }

            }
        }


    }

    @DelicateCoroutinesApi
    private fun simSlot() {
        simSlotViewModel.getSelectedSimSlot.observe(this, { sim ->
            if (sim != null) {
                localDataBaseViewModel.addSIMSlot(
                    SIMCard_Entity(
                        0, sim.SELECTED_SIM_SLOT
                    )
                )
            } else {
                localDataBaseViewModel.addSIMSlot(
                    SIMCard_Entity(
                        0, "0"
                    )
                )
            }
        })
    }


    private fun formattingDate() {

        currentDate = simpleDateFormat.format(calendar.time)
        currentDate2 = simpleDateFormat2.format(calendar.time)
        broughtPack = currentDate2 as String
        calendar.add(Calendar.DATE, 30) // number of days to add

    }

    @DelicateCoroutinesApi
    private fun initModel() {
        localDataBaseViewModel = ViewModelProvider(this).get(LocalDataBaseViewModel::class.java)
        userInfoViewModel = ViewModelProvider(
            this
        )[UserInfoViewModel::class.java]
        signInViewModel = ViewModelProvider(
            this
        ).get(
            SignInViewModel::class.java
        )
        simSlotViewModel = ViewModelProvider(this)[SimSlotViewModel::class.java]

        downloadXLViewModel = ViewModelProvider(this)[DownloadXLViewModel::class.java]

        downloadXLViewModel.downloadXLSheet(this)

    }


    @DelicateCoroutinesApi
    private fun setInfo() {
        LoadingDialog.loadingDialogStart(this, R.style.LoadingUserInfo)
        signInViewModel.collectUserInfoLiveData.observe(this, {
            globalEmail = it.email
            userInfoViewModel.getUserInfoLiveData.observe(this, { user ->
                if (user != null) {
                    LoadingDialog.loadingDialogStop()
                    binding.apply {
                        userIsNotNull = true
                        val time = trailCalculation(user.deactivate_Time)
                        userFirstName.setText(user.first_Name)
                        userLastName.setText(user.last_Name)
                        globalImage = user.avatar_image
                        subsPack = if (time <= 0) {
                            "Trail Version"
                        } else {
                            user.subscription_pack
                        }
                        afterThirtyDays = user.deactivate_Time
                        broughtPack = user.brought_pack_time
                        status = if (time <= 0) {
                            "END"
                        } else {
                            user.status
                        }
                        currentDate = user.active_Time
                        userGmail.text = it.email
                        Glide.with(this@UserInfo).load(globalImage)
                            .centerCrop()
                            .placeholder(R.drawable.ic_team)
                            .into(UserImage)


                    }
                } else {
                    LoadingDialog.loadingDialogStop()
                    afterThirtyDays = simpleDateFormat.format(calendar.time)
                    globalImage = it.imageUrl
                    globalEmail = it.email
                    binding.apply {
                        userGmail.text = it.email
                        Glide.with(this@UserInfo).load(it.imageUrl)
                            .centerCrop()
                            .placeholder(R.drawable.ic_team)
                            .into(UserImage)

                    }
                }
            })
        })


    }

    @DelicateCoroutinesApi
    private fun insertValue(
        firstName: String,
        lastName: String
    ) {
        Log.d(TAG, "insertValue: is called")

        LoadingDialog.loadingDialogStart(this, R.style.LoadingUserInfo)
        val job = CoroutineScope(Dispatchers.IO).launch {
            localDataBaseUpload(
                ImageConverter.getBitmap(globalImage, this@UserInfo),
                firstName,
                lastName
            )
            if (!userIsNotNull){
                fireBaseUpload(
                    ImageConverter.getByte(
                        ImageConverter.getBitmap(
                            globalImage,
                            this@UserInfo
                        )
                    ), firstName, lastName
                )
            }


        }

        job.invokeOnCompletion {
            LoadingDialog.loadingDialogStop()
            startActivity(Intent(this@UserInfo, TrustedNumberDetails::class.java))
            finish()
        }


    }

    @DelicateCoroutinesApi
    private fun fireBaseUpload(image: ByteArray, firstName: String, lastName: String) {
        userInfoViewModel.insert(
            UserInfoModel(
                currentDate.toString(), "", broughtPack,
                afterThirtyDays.toString(), firstName, lastName, status, subsPack
            ),
            image
        )

    }

    private fun localDataBaseUpload(bitmap: Bitmap, firstName: String, lastName: String) {
        Log.d(TAG, "localDataBaseUpload: is caled")
        localDataBaseViewModel.addUserInfo(
            PersonalInformation_Entity(
                0,
                firstName,
                lastName,
                afterThirtyDays!!,
                currentDate!!,
                subsPack,
                broughtPack,
                status,
                globalEmail,
                bitmap
            )
        )

    }


    override fun onResume() {
        super.onResume()
        //checking device connection to internet
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


    private fun trailCalculation(deactivatedTime: String): Int {
        startDateValue2 =
            simpleDateFormat.parse(simpleDateFormat.format(Calendar.getInstance().time))
        endDateValue2 = simpleDateFormat.parse(deactivatedTime)
        val remain = (TimeUnit.DAYS.convert(
            (endDateValue2.time - startDateValue2.time),
            TimeUnit.MILLISECONDS
        )).toString()
        Log.e(TAG, "Trail Calculation: " + Integer.parseInt(remain))
        return Integer.parseInt(remain)


    }


}