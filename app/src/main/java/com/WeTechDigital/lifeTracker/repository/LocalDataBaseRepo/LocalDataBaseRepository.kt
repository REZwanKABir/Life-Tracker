package com.rex.lifetracker.repository.LocalDataBaseRepo

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import com.WeTechDigital.lifeTracker.RoomDataBase.api.OfflineDataBaseDao
import com.WeTechDigital.lifeTracker.RoomDataBase.OfflineDataBase_Entity.*

class LocalDataBaseRepository(private val offlineDataBaseDao: OfflineDataBaseDao) {

    val readAllContacts: LiveData<List<TrustedContacts_Entity>> = offlineDataBaseDao.readAllContacts()
    val readAllUserInformation: LiveData<PersonalInformation_Entity> = offlineDataBaseDao.readAllUserInfo()
    val readAllSIMCardSlot: LiveData<List<SIMCard_Entity>> = offlineDataBaseDao.readAllSIMSlot()
    val readAllCache: LiveData<List<DeleteCache_Entity>> = offlineDataBaseDao.readAllCache()
    val readAllState:LiveData<BooleanState_Entity> = offlineDataBaseDao.readAllState()


    suspend fun addContacts(TrustedContactsEntity: TrustedContacts_Entity) {
        offlineDataBaseDao.addContacts(TrustedContactsEntity)
    }
    suspend fun addUserInfo(personalInformationEntity: PersonalInformation_Entity) {
        offlineDataBaseDao.addUserInfo(personalInformationEntity)
    }
    suspend fun addSIMSlot(simCardEntity: SIMCard_Entity) {
        offlineDataBaseDao.addSIMSlot(simCardEntity)
    }
    suspend fun addCache(DeleteCache_Entity: DeleteCache_Entity) {
        offlineDataBaseDao.addCache(DeleteCache_Entity)
    }

   suspend fun updateOnlyImageAndName(image:Bitmap?, name:String, PhoneNumber: String){
        offlineDataBaseDao.updateOnlyImageAndName(image,name,PhoneNumber)
    }

    suspend fun updateWithoutImage(name:String,Phone:String){
        offlineDataBaseDao.updateWithoutImage(name,Phone)
    }

    suspend fun updateContacts(TrustedContactsEntity: TrustedContacts_Entity) {
        offlineDataBaseDao.updateContacts(TrustedContactsEntity)
    }
    suspend fun updateUserInfo(personalInformationEntity: PersonalInformation_Entity) {
        offlineDataBaseDao.updateUserInfo(personalInformationEntity)
    }
    suspend fun updateSIMSlot(simCardEntity: SIMCard_Entity) {
        offlineDataBaseDao.updateSIMSlot(simCardEntity)
    }

    suspend fun deleteContacts(TrustedContactsEntity: TrustedContacts_Entity){
        offlineDataBaseDao.deleteContacts(TrustedContactsEntity)
    }
    suspend fun deleteUserInfo(personalInformationEntity: PersonalInformation_Entity){
        offlineDataBaseDao.deleteUserInfo(personalInformationEntity)
    }
    suspend fun deleteSIMSlot(simCardEntity: SIMCard_Entity){
        offlineDataBaseDao.deleteSIMSlot(simCardEntity)
    }

    suspend fun deleteCache(DeleteCache_Entity: DeleteCache_Entity){
        offlineDataBaseDao.deleteCache(DeleteCache_Entity)
    }

    suspend fun nukeTable(){
        offlineDataBaseDao.nukeTableContacts()
      //  offlineDataBaseDao.nukeTablePersonalInfo()
        offlineDataBaseDao.nukeTableSim()
        offlineDataBaseDao.nukeTabledCache()
    }

    suspend fun addBooleanState(booleanStateEntity: BooleanState_Entity){
        offlineDataBaseDao.addBooleanState(booleanStateEntity)
    }




}