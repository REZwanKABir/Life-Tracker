package com.WeTechDigital.lifeTracker.viewModel.firebaseViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.WeTechDigital.lifeTracker.Model.SimSlotModel
import com.WeTechDigital.lifeTracker.repository.firebaseRepository.SimSlotRepository
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class SimSlotViewModel(application: Application) : AndroidViewModel(application) {

    private val simSlotRepo = SimSlotRepository()
    var insertSelectedSimSlot: LiveData<String>? = null
    var getSelectedSimSlot: MutableLiveData<SimSlotModel?> = simSlotRepo.getSelectedSimSlot()

    fun insertSimSlot(simSlotModel: SimSlotModel) {
        insertSelectedSimSlot = simSlotRepo.insertSIMSlotToDatabase(simSlotModel)
    }

}