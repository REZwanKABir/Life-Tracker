package com.WeTechDigital.lifeTracker.view.fragment.notificationTabItem

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.WeTechDigital.lifeTracker.R

import com.WeTechDigital.lifeTracker.databinding.FragmentYourPlanBinding
import com.WeTechDigital.lifeTracker.utils.Constant
import com.WeTechDigital.lifeTracker.utils.LoadingDialog
import com.WeTechDigital.lifeTracker.viewModel.LocalDataBaseVM.LocalDataBaseViewModel
import com.WeTechDigital.lifeTracker.viewModel.firebaseViewModel.UserInfoViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class YourPlan : Fragment(R.layout.fragment_your_plan) {
    private val simpleDateFormat: SimpleDateFormat = SimpleDateFormat("EEE, d MMM yy")
    private val simpleDateFormat2 = SimpleDateFormat("dd.MM.yyyy")
    private lateinit var startDateValue2: Date
    private lateinit var endDateValue2: Date
    private lateinit var localDataBaseViewModel: LocalDataBaseViewModel
    private lateinit var userInfoViewModel: UserInfoViewModel
    private val calendar = Calendar.getInstance()
    private lateinit var binding: FragmentYourPlanBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentYourPlanBinding.bind(view)
        binding.CurrentDate.text = simpleDateFormat.format(calendar.time)
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)


        initViewModel()

        setValue()



    }

    private fun setValue() {
        binding.apply {
            LoadingDialog.loadingDialogStart(requireContext(),R.style.LoadingUserInfo)
            userInfoViewModel.getUserInfoLiveData.observe(viewLifecycleOwner,{ userInfo->

                if (userInfo != null){
                    LoadingDialog.loadingDialogStop()
                    boughtPack.text = userInfo.brought_pack_time
                    subscriptionPack.text = userInfo.subscription_pack
                    packageName.text = userInfo.subscription_pack
                    val time = trailCalculation(userInfo.deactivate_Time)
                    if (time <= 0){
                        status.text = "status: " + "END"
                        userInfoViewModel.updatePackageStatus()
                    }else{
                        status.text = "status: " + userInfo.status
                    }

                    packageDes.text = "You are using " + userInfo.subscription_pack
                    userPlaneActiveDate.text = userInfo.active_Time
                    userPlaneDeactivatedDate.text = userInfo.deactivate_Time
                }else{
                    LoadingDialog.loadingDialogStop()
                }




            })

        }
    }

    private fun trailCalculation(deactivatedTime: String): Int {
        startDateValue2 =
            simpleDateFormat2.parse(simpleDateFormat2.format(Calendar.getInstance().time))
        endDateValue2 = simpleDateFormat2.parse(deactivatedTime)
        val remain = (TimeUnit.DAYS.convert(
            (endDateValue2.time - startDateValue2.time),
            TimeUnit.MILLISECONDS
        )).toString()
        Log.e(Constant.TAG, "Trail Calculation: " + Integer.parseInt(remain))
        return Integer.parseInt(remain)


    }



    private fun initViewModel() {
        userInfoViewModel = ViewModelProvider(this).get(UserInfoViewModel::class.java)
        localDataBaseViewModel = ViewModelProvider(this).get(LocalDataBaseViewModel::class.java)
    }

    private val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                try {
                    findNavController().navigate(R.id.mapsFragment)
                } catch (e: Exception) {
                    Log.d(Constant.TAG, "handleOnBackPressed: $e")
                }
            }
        }
    override fun onDestroyView() {
        callback.isEnabled = false
        callback.remove()
        super.onDestroyView()
    }
}