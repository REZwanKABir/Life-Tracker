package com.WeTechDigital.lifeTracker.viewModel.firebaseViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.WeTechDigital.lifeTracker.Model.TrustedContactsModel
import com.WeTechDigital.lifeTracker.RoomDataBase.OfflineDataBase_Entity.TrustedContacts_Entity
import com.WeTechDigital.lifeTracker.repository.firebaseRepository.TrustedContactsRepository
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class TrustedContactsViewModel : ViewModel() {

    private val trustedContactsRepository = TrustedContactsRepository()

    var getContactsLiveData =
        trustedContactsRepository.getTrustedContactsInfo()


    var getInsertedLiveData: MutableLiveData<String>? = null


    fun insertTrustedContactsInfo(trustedContactsModel: TrustedContactsModel) {
        getInsertedLiveData =
            trustedContactsRepository.insertTrustedContactsInfo(trustedContactsModel)
    }

    fun deleteContact(deleteContact: String) {
        trustedContactsRepository.deleteTrustedContacts(deleteContact)


    }
    fun updateDataTOFireBase(model: TrustedContacts_Entity, image: ByteArray?) {
        trustedContactsRepository.updateDataTOFireBase(model, image)
    }


}