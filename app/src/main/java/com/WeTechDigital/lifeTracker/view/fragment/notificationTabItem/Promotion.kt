package com.WeTechDigital.lifeTracker.view.fragment.notificationTabItem

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.WeTechDigital.lifeTracker.Model.AppsAdminDataModelPackage.One_Month_Pack_Model
import com.WeTechDigital.lifeTracker.Model.AppsAdminDataModelPackage.Six_Month_Pack_Model
import com.WeTechDigital.lifeTracker.Model.AppsAdminDataModelPackage.Twelve_Month_Pack_Model
import com.WeTechDigital.lifeTracker.R
import com.WeTechDigital.lifeTracker.databinding.FragmentPromotionBinding
import com.WeTechDigital.lifeTracker.utils.Constant
import com.WeTechDigital.lifeTracker.view.Purchase.Purchase_Package
import com.WeTechDigital.lifeTracker.viewModel.LocalDataBaseVM.LocalDataBaseViewModel
import com.WeTechDigital.lifeTracker.viewModel.firebaseViewModel.AppsInformationViewModel


class Promotion : Fragment(R.layout.fragment_promotion) {

    private lateinit var appsInformationViewModel: AppsInformationViewModel
    private lateinit var localDataBaseViewModel: LocalDataBaseViewModel
    private lateinit var binding: FragmentPromotionBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPromotionBinding.bind(view)
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)
        initViewModel()
        setValue()



        binding.apply {
            firstPlan.setOnClickListener {
                checkSubPack("OneMonth")
            }

            secondPlan.setOnClickListener {
                checkSubPack("SixMonth")
            }
            thirdPlan.setOnClickListener {
                checkSubPack("TwelveMonth")

            }
        }

    }

    private fun checkSubPack(packName: String) {


        localDataBaseViewModel.realAllUserInformation.observe(viewLifecycleOwner, { subPack ->

            if (subPack.Subscription_Pack == "Trail Version") {
                requireContext().startActivity(
                    Intent(
                        requireContext(),
                        Purchase_Package::class.java
                    ).putExtra("Package", packName)
                )
            } else {
                Toast.makeText(
                    requireContext(),
                    "You already Brought ${subPack.Subscription_Pack}",
                    Toast.LENGTH_SHORT
                ).show()

            }


        })

    }

    private fun setValue() {
        appsInformationViewModel.getAppsInformationLiveData.observe(viewLifecycleOwner, { apps ->

            if (apps != null) {
                oneMonthPlane(apps.oneMonthPackModel)
                sixMonthPlane(apps.sixMonthPackModel)
                twelveMonthPlan(apps.twelveMonthPackModel)
            }

        })
    }

    private fun twelveMonthPlan(twelveMonthPackModel: Twelve_Month_Pack_Model?) {
        binding.apply {
            twelveMonthPlan.text = "${twelveMonthPackModel?.days} Days Plan"
            twelveMonthPlanCost.text = "BDT " + twelveMonthPackModel?.cost
        }
    }

    private fun sixMonthPlane(sixMonthPackModel: Six_Month_Pack_Model?) {
        binding.apply {
            sixMonthDays.text = sixMonthPackModel?.days + " Days Plan"
            sixMonthPlanCost.text = "BDT " + sixMonthPackModel?.cost
        }
    }

    private fun oneMonthPlane(oneMonthPackModel: One_Month_Pack_Model?) {
        binding.apply {
            oneMonth.text = oneMonthPackModel?.days + " Days Plan"
            oneMonthCost.text = "BDT " + oneMonthPackModel?.cost

        }
    }

    private fun initViewModel() {
        localDataBaseViewModel = ViewModelProvider(this).get(LocalDataBaseViewModel::class.java)
        appsInformationViewModel = ViewModelProvider(this)
            .get(AppsInformationViewModel::class.java)
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