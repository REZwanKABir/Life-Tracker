package com.WeTechDigital.lifeTracker.RoomDataBase.api

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.room.*
import com.WeTechDigital.lifeTracker.RoomDataBase.OfflineDataBase_Entity.*

@Dao
interface OfflineDataBaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addContacts(TrustedContactsEntity: TrustedContacts_Entity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBooleanState(booleanStateEntity: BooleanState_Entity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUserInfo(personalInformationEntity: PersonalInformation_Entity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSIMSlot(simCardEntity: SIMCard_Entity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addCache(DeleteCache_Entity: DeleteCache_Entity)

    @Update
    suspend fun updateContacts(TrustedContactsEntity: TrustedContacts_Entity)

    @Query("UPDATE user_trusted_contacts SET Name = :name, Image = :image WHERE Phone = :phoneNumber")
    suspend fun updateOnlyImageAndName(image: Bitmap?, name: String,phoneNumber:String)

    @Query("UPDATE user_trusted_contacts SET Name = :name WHERE Phone = :phoneNumber")
    suspend fun  updateWithoutImage(name:String,phoneNumber:String)

    @Update
    suspend fun updateUserInfo(personalInformationEntity: PersonalInformation_Entity)

    @Update
    suspend fun updateSIMSlot(simCardEntity: SIMCard_Entity)

    @Delete
    suspend fun deleteContacts(TrustedContactsEntity: TrustedContacts_Entity)

    @Delete
    suspend fun deleteUserInfo(personalInformationEntity: PersonalInformation_Entity)

    @Delete
    suspend fun deleteSIMSlot(simCardEntity: SIMCard_Entity)

    @Delete
    suspend fun deleteCache(DeleteCache_Entity: DeleteCache_Entity)

    @Query("SELECT * FROM user_trusted_contacts")
    fun readAllContacts(): LiveData<List<TrustedContacts_Entity>>

    @Query("SELECT * FROM booleanState")
    fun readAllState():LiveData<BooleanState_Entity>

    @Query("SELECT * FROM personal_info")
    fun readAllUserInfo(): LiveData<PersonalInformation_Entity>

    @Query("SELECT * FROM sim_table")
    fun readAllSIMSlot(): LiveData<List<SIMCard_Entity>>

    @Query("SELECT * FROM deleteCache")
    fun readAllCache(): LiveData<List<DeleteCache_Entity>>


    @Query("DELETE FROM user_trusted_contacts")
    suspend fun nukeTableContacts()

    @Query("DELETE FROM personal_info")
    suspend fun nukeTablePersonalInfo()

    @Query("DELETE FROM sim_table")
    suspend fun nukeTableSim()

    @Query("DELETE FROM deleteCache")
    suspend fun nukeTabledCache()
}