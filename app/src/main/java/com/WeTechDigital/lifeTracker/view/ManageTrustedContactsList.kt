package com.WeTechDigital.lifeTracker.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.WeTechDigital.lifeTracker.R
import com.WeTechDigital.lifeTracker.RoomDataBase.OfflineDataBase_Entity.TrustedContacts_Entity
import com.WeTechDigital.lifeTracker.controller.TrustedContactsManageAdapter
import com.WeTechDigital.lifeTracker.databinding.ActivityManageTrustedContactsListBinding
import com.WeTechDigital.lifeTracker.utils.Constant.TAG
import com.WeTechDigital.lifeTracker.utils.ImageConverter
import com.WeTechDigital.lifeTracker.utils.LoadingDialog
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.WeTechDigital.lifeTracker.viewModel.LocalDataBaseVM.LocalDataBaseViewModel
import com.WeTechDigital.lifeTracker.viewModel.firebaseViewModel.TrustedContactsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ManageTrustedContactsList : AppCompatActivity() {
    private lateinit var trustedContactsViewModel: TrustedContactsViewModel
    private lateinit var binding: ActivityManageTrustedContactsListBinding
    private lateinit var mAdapter: TrustedContactsManageAdapter
    private lateinit var localDataBaseViewModel: LocalDataBaseViewModel
    private var isInternetConnected = false
    private var internetDisposable: Disposable? = null
    private lateinit var priorityList: ArrayList<TrustedContacts_Entity>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageTrustedContactsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewModel()
        setValue()


        //recycler initialize
        binding.apply {
            manageTrustedPeople.apply {
                layoutManager = LinearLayoutManager(this@ManageTrustedContactsList)
                setHasFixedSize(true)
                adapter = mAdapter

            }
        }

        //saving button
        binding.SaveManageBtn.setOnClickListener {
            //not to restart setValue

            Log.d(TAG, "onCreate: button pressed is called")

            priorityList = mAdapter.getSelectedValue()
            Log.d(TAG, "onCreate: listof prior: $priorityList")
            if (priorityList.isNotEmpty()) {
                for (list in priorityList) {
                    localDataBaseViewModel.addContacts(
                        TrustedContacts_Entity(

                            list.Phone, list.Priority, list.Name, list.Image
                        )
                    )
                }
                if (isInternetConnected) {
                    uploadDataToFireBase(priorityList)
                } else {

                    startActivity(
                        Intent(this, MainActivity::class.java).putExtra(
                            "Service",
                            "NO"
                        )
                    )
                    finish()
                }
            }

        }
    }


    private fun setValue() {
        LoadingDialog.loadingDialogStart(this, R.style.Custom)
        localDataBaseViewModel.readAllContacts.observe(this, {
            LoadingDialog.loadingDialogStop()
            mAdapter.setValue(it)
            Log.e(TAG, "setValue: is called")

        })


    }

    private fun initViewModel() {
        trustedContactsViewModel = ViewModelProvider(this).get(TrustedContactsViewModel::class.java)
        mAdapter = TrustedContactsManageAdapter(this)

        localDataBaseViewModel = ViewModelProvider(this).get(LocalDataBaseViewModel::class.java)


    }


    //--------------------upload firebase-------------------//

    private fun uploadDataToFireBase(numberList: List<TrustedContacts_Entity>?) {

        LoadingDialog.loadingDialogStart(this, R.style.Custom)

        val mainJob = CoroutineScope(Dispatchers.IO).launch {
             updateWithoutNullPicture(numberList)

        }
        mainJob.invokeOnCompletion {
            LoadingDialog.loadingDialogStop()
            Log.d(TAG, "uploadDataToFireBase: done")
            startActivity(
                Intent(this, MainActivity::class.java).putExtra(
                    "Service",
                    "NO"
                )
            )
            finish()

        }

    }

    private fun updateWithoutNullPicture(numberList: List<TrustedContacts_Entity>?) {
        if (numberList!!.isNotEmpty()) {
            for (number in numberList) {
                trustedContactsViewModel.updateDataTOFireBase(
                    number, ImageConverter.getByte(number.Image!!)
                )
            }

        }

    }


    //---------------Network-----------------//
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