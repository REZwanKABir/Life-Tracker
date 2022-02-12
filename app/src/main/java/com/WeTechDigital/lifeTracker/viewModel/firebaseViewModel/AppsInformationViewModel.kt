package com.WeTechDigital.lifeTracker.viewModel.firebaseViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.WeTechDigital.lifeTracker.Model.AppsAdminDataModelPackage.AppsAdminModel
import com.WeTechDigital.lifeTracker.repository.firebaseRepository.AppsInformationRepository
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class AppsInformationViewModel : ViewModel() {

    private val appsInformationRepository: AppsInformationRepository = AppsInformationRepository()

    val getAppsInformationLiveData: MutableLiveData<AppsAdminModel?> =
        appsInformationRepository.getAppsInformation()


}