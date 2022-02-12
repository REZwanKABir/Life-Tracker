package com.WeTechDigital.lifeTracker.view.Purchase

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.WeTechDigital.lifeTracker.Model.AppsAdminDataModelPackage.One_Month_Pack_Model
import com.WeTechDigital.lifeTracker.Model.AppsAdminDataModelPackage.Six_Month_Pack_Model
import com.WeTechDigital.lifeTracker.Model.AppsAdminDataModelPackage.Twelve_Month_Pack_Model
import com.WeTechDigital.lifeTracker.Model.PurchaseModel.PurchaseModel
import com.WeTechDigital.lifeTracker.R
import com.WeTechDigital.lifeTracker.RoomDataBase.OfflineDataBase_Entity.PersonalInformation_Entity
import com.WeTechDigital.lifeTracker.databinding.ActivityPurchasePackageBinding
import com.WeTechDigital.lifeTracker.utils.Constant.TAG
import com.WeTechDigital.lifeTracker.utils.LoadingDialog
import com.WeTechDigital.lifeTracker.view.MainActivity
import com.WeTechDigital.lifeTracker.viewModel.LocalDataBaseVM.LocalDataBaseViewModel
import com.WeTechDigital.lifeTracker.viewModel.firebaseViewModel.AppsInformationViewModel
import com.WeTechDigital.lifeTracker.viewModel.firebaseViewModel.Purchase.PurchaseViewModel
import com.WeTechDigital.lifeTracker.viewModel.firebaseViewModel.UserInfoViewModel
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*

class Purchase_Package : AppCompatActivity() {
    private val firebaseAuth = Firebase.auth.currentUser
    private lateinit var binding: ActivityPurchasePackageBinding
    private lateinit var appsInformationViewModel: AppsInformationViewModel
    private lateinit var purchaseViewModel: PurchaseViewModel
    private lateinit var localDataBaseViewModel: LocalDataBaseViewModel
    private lateinit var userInfoViewModel: UserInfoViewModel
    private var plan = ""
    private var cost = ""
    private var namePackage = ""
    private var intentPackageName = ""
    private var afterPackageDays = ""
    private var currentDate = ""
    private var currentDate2 = ""
    private val calendar = Calendar.getInstance()
    private val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy")
    private val simpleDateFormat2 = SimpleDateFormat("EEE, d MMM -HH:mm")
    private var broughtPack: String = "null"
    private var isInternetConnected = false
    private var internetDisposable: Disposable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPurchasePackageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViewModel()

        intentPackageName = intent.getStringExtra("Package").toString()
        setDetails(intentPackageName)

        binding.apply {
            appsInformationViewModel.getAppsInformationLiveData.observe(this@Purchase_Package,
                { app ->
                    purchaseBtn.setOnClickListener {
                        if (TextUtils.isEmpty(referenceNumber.text.toString())) {
                            referenceNumber.error = "Empty Field"
                            return@setOnClickListener
                        } else {

                            if (isInternetConnected) {
                                when (intentPackageName) {
                                    "OneMonth" -> {
                                        formattingDate(
                                            app?.oneMonthPackModel?.description.toString(),
                                            referenceNumber.text.toString()
                                        )

                                    }
                                    "SixMonth" -> {
                                        formattingDate(
                                            app?.sixMonthPackModel?.description.toString(),
                                            referenceNumber.text.toString()
                                        )

                                    }
                                    "TwelveMonth" -> {
                                        formattingDate(
                                            app?.twelveMonthPackModel?.description.toString(),
                                            referenceNumber.text.toString()
                                        )

                                    }
                                }

                            } else {
                                Toast.makeText(
                                    this@Purchase_Package,
                                    "You Cannot Purchase package Without Internet",
                                    Toast.LENGTH_SHORT
                                ).show()

                                return@setOnClickListener
                            }


                        }
                    }
                })
        }
    }


    private fun updateToLocalDataBase(
        description: String,
        personalinformationEntity: PersonalInformation_Entity
    ) {


        localDataBaseViewModel.addUserInfo(
            PersonalInformation_Entity(
                personalinformationEntity.id,
                personalinformationEntity.First_Name,
                personalinformationEntity.Last_Name,
                afterPackageDays,
                currentDate,
                description,
                broughtPack,
                "Pending",
                personalinformationEntity.User_Email,
                personalinformationEntity.Image
            )
        )

        LoadingDialog.loadingDialogStop()
        startActivity(Intent(this,MainActivity::class.java))
        localDataBaseViewModel.realAllUserInformation.removeObservers(this)


    }

    private fun updatePackageToDataBase(reference: String, description: String) {

        LoadingDialog.loadingDialogStart(this@Purchase_Package, R.style.Purchase)
        val data = PurchaseModel(
            firebaseAuth?.email.toString(),
            firebaseAuth?.uid.toString(),
            reference,
            "$namePackage $plan $cost"
        )
        purchaseViewModel.insertData(data, description)
        userInfoViewModel.updateUserPackageInfo(
            currentDate,
            broughtPack,
            afterPackageDays,
            description
        )
        LoadingDialog.loadingDialogStop()


    }

//is used for setting package details of the package that user is clicked for purchage
    private fun setDetails(packageName: String) {
        LoadingDialog.loadingDialogStart(this, R.style.LoadingList)
        appsInformationViewModel.getAppsInformationLiveData.observe(this, { data ->
            binding.apply {
                when (packageName) {
                    "OneMonth" -> {
                        LoadingDialog.loadingDialogStop()
                        oneMonthPlane(data?.oneMonthPackModel)
                    }
                    "SixMonth" -> {
                        LoadingDialog.loadingDialogStop()
                        sixMonthPlane(data?.sixMonthPackModel)
                    }
                    "TwelveMonth" -> {
                        LoadingDialog.loadingDialogStop()
                        twelveMonthPlan(data?.twelveMonthPackModel)
                    }
                    else -> {
                        LoadingDialog.loadingDialogStop()
                    }
                }
            }
        })
    }

    private fun oneMonthPlane(oneMonthPackModel: One_Month_Pack_Model?) {
        binding.apply {
            // plan ="30 Days Plan"
            cost = "BDT " + oneMonthPackModel?.cost
            namePackage = "30 days Plan Package"
            PackageDetails.text = "$namePackage\n$cost"

        }
    }

    private fun sixMonthPlane(sixMonthPackModel: Six_Month_Pack_Model?) {
        binding.apply {
            // plan = sixMonthPackModel?.days + " Days Plan"
            cost = "BDT " + sixMonthPackModel?.cost
            namePackage = "180 days Plan Package"
            PackageDetails.text = "$namePackage\n$cost"
        }
    }

    private fun twelveMonthPlan(twelveMonthPackModel: Twelve_Month_Pack_Model?) {
        binding.apply {
            //  plan = twelveMonthPackModel?.days + " Days Plan"
            cost = "BDT " + twelveMonthPackModel?.cost
            namePackage = "365 days Plan Package"
            PackageDetails.text = "$namePackage\n$cost"
        }
    }

    private fun initViewModel() {
        userInfoViewModel = ViewModelProvider(
            this
        ).get(
            UserInfoViewModel::class.java
        )
        localDataBaseViewModel = ViewModelProvider(this).get(LocalDataBaseViewModel::class.java)
        purchaseViewModel = ViewModelProvider(this).get(PurchaseViewModel::class.java)

        appsInformationViewModel = ViewModelProvider(this)
            .get(AppsInformationViewModel::class.java)
    }

    //formating date with corresponding packages
    private fun formattingDate(description: String, reference: String) {
        Log.d(TAG, "formattingDate: $description")

        when (description) {
            "One Month Pack" -> {
                currentDate = simpleDateFormat.format(calendar.time)
                currentDate2 = simpleDateFormat2.format(calendar.time)
                broughtPack = currentDate2
                calendar.add(Calendar.DATE, 30) // number of days to add
                afterPackageDays = simpleDateFormat.format(calendar.time)

                updatePackageToDataBase(
                    reference,
                    "30 days plan"
                )

                localDataBaseViewModel.realAllUserInformation.observe(this,{
                    updateToLocalDataBase("30 days plan",it)
                })


            }
            "Six Month Pack" -> {
                currentDate = simpleDateFormat.format(calendar.time)
                currentDate2 = simpleDateFormat2.format(calendar.time)
                broughtPack = currentDate2
                calendar.add(Calendar.DATE, 180) // number of days to add
                afterPackageDays = simpleDateFormat.format(calendar.time)
                updatePackageToDataBase(
                    reference,
                    "180 days plan"
                )
                localDataBaseViewModel.realAllUserInformation.observe(this,{
                    updateToLocalDataBase("180 days plan",it)
                })
            }
            "Twelve Month Pack" -> {
                currentDate = simpleDateFormat.format(calendar.time)
                currentDate2 = simpleDateFormat2.format(calendar.time)
                broughtPack = currentDate2
                calendar.add(Calendar.DATE, 365) // number of days to add
                afterPackageDays = simpleDateFormat.format(calendar.time)
                updatePackageToDataBase(
                    reference,
                    "365 days plan"
                )
                localDataBaseViewModel.realAllUserInformation.observe(this,{
                    updateToLocalDataBase("365 days plan",it)
                })

            }

        }


    }

    override fun onBackPressed() {
       startActivity(Intent(this,MainActivity::class.java))
        localDataBaseViewModel.realAllUserInformation.removeObservers(this)
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

}