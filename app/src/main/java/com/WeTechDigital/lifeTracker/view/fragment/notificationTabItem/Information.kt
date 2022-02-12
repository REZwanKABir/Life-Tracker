package com.WeTechDigital.lifeTracker.view.fragment.notificationTabItem

import android.content.Intent
import android.net.Uri
import android.os.Bundle

import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.WeTechDigital.lifeTracker.R
import com.WeTechDigital.lifeTracker.databinding.FragmentInformationBinding
import com.WeTechDigital.lifeTracker.utils.Constant
import com.WeTechDigital.lifeTracker.viewModel.firebaseViewModel.AppsInformationViewModel

class Information : Fragment(R.layout.fragment_information) {


    private lateinit var binding: FragmentInformationBinding
    private lateinit var appsInformationViewModel: AppsInformationViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInformationBinding.bind(view)
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)

        initViewModel()

        appsInformationViewModel.getAppsInformationLiveData.observe(viewLifecycleOwner, {
            binding.apply {

                helpCall.setOnClickListener {
                    callNumber("01886271808")
                }
                about.text = "Developed by "
                hyperlink.movementMethod = LinkMovementMethod.getInstance()


            }
        })
    }

    private fun callNumber(number: String) {
        val intent =
            Intent(Intent.ACTION_CALL).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.data = Uri.parse("tel:$number")
        startActivity(intent)
    }


    private fun initViewModel() {
        appsInformationViewModel = ViewModelProvider(this).get(AppsInformationViewModel::class.java)
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