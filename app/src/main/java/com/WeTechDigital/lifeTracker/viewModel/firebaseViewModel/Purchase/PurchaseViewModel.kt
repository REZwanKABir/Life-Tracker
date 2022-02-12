package com.WeTechDigital.lifeTracker.viewModel.firebaseViewModel.Purchase

import androidx.lifecycle.ViewModel
import com.WeTechDigital.lifeTracker.Model.PurchaseModel.PurchaseModel
import com.WeTechDigital.lifeTracker.repository.firebaseRepository.PurchaseRepository
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class PurchaseViewModel : ViewModel() {
    private val purchaseRepository = PurchaseRepository()

    fun insertData(
        purchaseModel: PurchaseModel, packageName: String
    ) {
        purchaseRepository.insertData(
            purchaseModel, packageName
        )
    }
}