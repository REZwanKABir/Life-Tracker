package com.WeTechDigital.lifeTracker.viewModel.LocalDataBaseVM

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.WeTechDigital.lifeTracker.RoomDataBase.api.OfflineDataBase
import com.WeTechDigital.lifeTracker.RoomDataBase.OfflineDataBase_Entity.*
import com.WeTechDigital.lifeTracker.utils.Constant.TAG
import com.rex.lifetracker.repository.LocalDataBaseRepo.LocalDataBaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocalDataBaseViewModel(application: Application) : AndroidViewModel(application) {
    val readAllContacts: LiveData<List<TrustedContacts_Entity>>
    val realAllUserInformation: LiveData<PersonalInformation_Entity>
    val readAllSIMCardSlot: LiveData<List<SIMCard_Entity>>
    val readAllCache: LiveData<List<DeleteCache_Entity>>
    val readAllState:LiveData<BooleanState_Entity>
    private val databaseRepository: LocalDataBaseRepository

    init {
        val userContactsDao = OfflineDataBase.getDatabase(application).userContactsDao()
        databaseRepository = LocalDataBaseRepository(userContactsDao)
        readAllContacts = databaseRepository.readAllContacts
        realAllUserInformation = databaseRepository.readAllUserInformation
        readAllSIMCardSlot = databaseRepository.readAllSIMCardSlot
        readAllCache = databaseRepository.readAllCache
        readAllState = databaseRepository.readAllState
    }

    fun addContacts(TrustedContactsEntity: TrustedContacts_Entity) {
        Log.d(TAG, "addContacts: is called")
        viewModelScope.launch(Dispatchers.IO) {

            databaseRepository.addContacts(TrustedContactsEntity)
        }
    }

    fun addUserInfo(personalInformationEntity: PersonalInformation_Entity) {
        viewModelScope.launch(Dispatchers.IO) {

            databaseRepository.addUserInfo(personalInformationEntity)
        }
    }

    fun addSIMSlot(simCardEntity: SIMCard_Entity) {
        Log.d(TAG, "addContacts: is called")
        viewModelScope.launch(Dispatchers.IO) {

            databaseRepository.addSIMSlot(simCardEntity)
        }
    }

    fun addCache(DeleteCache_Entity: DeleteCache_Entity) {
        Log.d(TAG, "Cache")
        viewModelScope.launch(Dispatchers.IO) {

            databaseRepository.addCache(DeleteCache_Entity)
        }
    }

    fun updateOnlyImageAndName(image:Bitmap,name:String,PhoneNumber:String){
        viewModelScope.launch (Dispatchers.IO){
            databaseRepository.updateOnlyImageAndName(image,name,PhoneNumber)
        }
    }

    fun updateWithOutImage(name:String,phone:String){
        viewModelScope.launch (Dispatchers.IO){
            databaseRepository.updateWithoutImage(name,phone)
        }
    }

    fun updateContacts(TrustedContactsEntity: TrustedContacts_Entity) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseRepository.updateContacts(TrustedContactsEntity)
        }
    }

    fun updateUserInfo(personalInformationEntity: PersonalInformation_Entity) {
        Log.d(TAG, "addContacts: is called")
        viewModelScope.launch(Dispatchers.IO) {

            databaseRepository.updateUserInfo(personalInformationEntity)
        }
    }

    fun updateSIMSlot(simCardEntity: SIMCard_Entity) {
        Log.d(TAG, "addContacts: is called")
        viewModelScope.launch(Dispatchers.IO) {

            databaseRepository.updateSIMSlot(simCardEntity)
        }
    }

    fun deleteContacts(TrustedContactsEntity: TrustedContacts_Entity) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseRepository.deleteContacts(TrustedContactsEntity)
        }
    }

    fun deleteUserInfo(personalInformationEntity: PersonalInformation_Entity) {
        Log.d(TAG, "addContacts: is called")
        viewModelScope.launch(Dispatchers.IO) {

            databaseRepository.deleteUserInfo(personalInformationEntity)
        }
    }

    fun deleteSIMSlot(simCardEntity: SIMCard_Entity) {
        Log.d(TAG, "addContacts: is called")
        viewModelScope.launch(Dispatchers.IO) {

            databaseRepository.deleteSIMSlot(simCardEntity)
        }
    }

    fun deleteCache(DeleteCache_Entity: DeleteCache_Entity) {
        Log.d(TAG, "Cache")
        viewModelScope.launch(Dispatchers.IO) {

            databaseRepository.deleteCache(DeleteCache_Entity)
        }
    }

    fun nukeTable() {
        viewModelScope.launch(Dispatchers.IO) {
            databaseRepository.nukeTable()
        }
    }

    fun addBooleanState(booleanStateEntity: BooleanState_Entity){
        viewModelScope.launch (Dispatchers.IO){
            databaseRepository.addBooleanState(booleanStateEntity)
        }
    }
}