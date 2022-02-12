package com.WeTechDigital.lifeTracker.repository.firebaseRepository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.WeTechDigital.lifeTracker.Model.UserInfoModel
import com.WeTechDigital.lifeTracker.utils.Constant.TAG
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await


@DelicateCoroutinesApi
class UserInfoRepository {

    private val firebaseAuth = Firebase.auth.currentUser?.uid
    private val firebaseFirestore =
        Firebase.firestore.collection("Client").document(firebaseAuth.toString())
            .collection("Client_PersonalInfo").document("Info")

    private val storageRef =
        Firebase.storage.reference.child("Images").child(firebaseAuth.toString()).child("Avatar")

    fun updatePackageStatus(){
        GlobalScope.launch (Dispatchers.IO){
            try {
                firebaseFirestore.update(mapOf(
                    "status" to "END"
                ))

            }catch (e:Exception){
                Log.d(TAG, "updatePackageStatus: exception happen $e")
            }
        }
    }

    fun updateProfileImage(image: ByteArray?){

        GlobalScope.launch(Dispatchers.IO) {
            try {
                if (image!=null){
                    val result = storageRef.putBytes(image).await()
                    val uri = result.storage.downloadUrl.await().toString()
                    firebaseFirestore.update(mapOf(
                        "avatar_image" to uri
                    )).await()
                    Log.d(TAG, "updateProfileImage: done")
                }
            }catch (e:Exception){

                Log.d(TAG, "updateProfileImage: exception happen $e")

            }
        }
    }

    fun insertUserInfoFirebase(user: UserInfoModel, image: ByteArray?): MutableLiveData<String> {
        Log.d(TAG, "insertUserInfoFirebase: is called")
        val insertResultLiveData = MutableLiveData<String>()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "insertUserInfoFirebase: is called in background")
                if (image != null) {
                    val result = storageRef.putBytes(image).await()
                    val uri = result.storage.downloadUrl.await().toString()

                    val data = UserInfoModel(
                        user.active_Time,
                        uri,
                        user.brought_pack_time,
                        user.deactivate_Time,
                        user.first_Name,
                        user.last_Name,
                        user.status,
                        user.subscription_pack
                    )
                    firebaseFirestore.set(data).await()
                    withContext(Dispatchers.IO) {
                        Log.d(TAG, "insertUserInfoFirebase: is done")
                        insertResultLiveData.postValue("Insert Successfully")
                    }

                } else {
                    firebaseFirestore.set(user).await()
                    withContext(Dispatchers.IO) {
                        insertResultLiveData.postValue("Insert Successfully")
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "insertUserInfoFirebase: exception happen ${e.message}")
                    insertResultLiveData.postValue("failed")
                }
            }
        }
        return insertResultLiveData


    }

    fun updateUserPackageInfo(currentDate:String, broughtPack:String, afterPackageDays:String, description:String){

        GlobalScope.launch (Dispatchers.IO){
           try {
               firebaseFirestore.update(mapOf(
                   "active_Time" to currentDate,
                   "brought_pack_time" to broughtPack,
                   "deactivate_Time" to afterPackageDays,
                   "status" to "Pending",
                   "subscription_pack" to description
               )).await()
               Log.d(TAG, "updateUserPackageInfo: done")
           }catch (e:Exception){
               Log.d(TAG, "updateUserPackageInfo: exception happen $e")
           }
        }

    }

    fun insertUserInfoFirebase(user: UserInfoModel): MutableLiveData<String> {
        Log.d(TAG, "insertUserInfoFirebase: is called")
        val insertResultLiveData = MutableLiveData<String>()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "insertUserInfoFirebase: is called in background")
                val data = UserInfoModel(
                    user.active_Time,
                    user.avatar_image,
                    user.brought_pack_time,
                    user.deactivate_Time,
                    user.first_Name,
                    user.last_Name,
                    user.status,
                    user.subscription_pack
                )
                firebaseFirestore.set(data).await()
                withContext(Dispatchers.IO) {
                    Log.d(TAG, "insertUserInfoFirebase: is done")
                    insertResultLiveData.postValue("Insert Successfully")
                }


            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "insertUserInfoFirebase: exception happen ${e.message}")
                    insertResultLiveData.postValue("failed")
                }
            }
        }
        return insertResultLiveData


    }

    fun getUserInfoFromDataBase(): MutableLiveData<UserInfoModel?> {


        Log.d(TAG, "getUserInfoFromDataBase: uid $firebaseAuth")
        val getFireStoreMutableLiveData =
            MutableLiveData<UserInfoModel?>()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "getUserInfoFromDataBase: getting value")
                val data = firebaseFirestore.get().await().toObject(UserInfoModel::class.java)
                withContext(Dispatchers.IO) {
                    Log.d(TAG, "getUserInfoFromDataBase: $data")
                    getFireStoreMutableLiveData.postValue(data)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.IO) {
                    Log.d(TAG, "getUserInfoFromDataBase: exception happened ${e.message}")
                    getFireStoreMutableLiveData.postValue(
                        UserInfoModel(
                            "null", "null",
                            "null", "null",
                            "null", "null", "null", "null"
                        )
                    )
                }

            }

        }
        return getFireStoreMutableLiveData


    }
}