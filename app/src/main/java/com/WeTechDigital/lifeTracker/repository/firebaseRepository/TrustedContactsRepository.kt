package com.WeTechDigital.lifeTracker.repository.firebaseRepository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.WeTechDigital.lifeTracker.Model.TrustedContactsModel
import com.WeTechDigital.lifeTracker.utils.Constant.TAG
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.WeTechDigital.lifeTracker.RoomDataBase.OfflineDataBase_Entity.TrustedContacts_Entity
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.tasks.await

@DelicateCoroutinesApi
class TrustedContactsRepository {

    private val firebaseAuth = Firebase.auth.currentUser
    private val firebaseFirestore = Firebase.firestore.collection("Client")
        .document(firebaseAuth!!.uid).collection("Trusted_Contact")
    private val storageRef = Firebase.storage.reference.child("Images").child(firebaseAuth!!.uid)


    fun insertTrustedContactsInfo(trustedContactsModel: TrustedContactsModel): MutableLiveData<String> {

        val insertResultLiveData = MutableLiveData<String>()
        GlobalScope.launch(IO) {
            try {
                firebaseFirestore.document(trustedContactsModel.Phone).set(trustedContactsModel)
                    .await()
                withContext(Main) {
                    insertResultLiveData.postValue("Success")
                }
            } catch (e: Exception) {
                Log.d(TAG, "insertTrustedContactsInfo: exception happen ${e.message}")
                withContext(Main) {
                    insertResultLiveData.postValue("Failed")
                }
            }
        }
        return insertResultLiveData

    }


    fun getTrustedContactsInfo(): MutableLiveData<List<TrustedContactsModel>> {
        Log.d(TAG, "getTrustedContactsInfo: is called")
        val getFireStoreMutableLiveData =
            MutableLiveData<List<TrustedContactsModel>>()

        GlobalScope.launch(IO) {
            try {
                val data =
                    firebaseFirestore.get().await().toObjects(TrustedContactsModel::class.java)
                withContext(Main) {
                    getFireStoreMutableLiveData.postValue(data)
                }
            } catch (e: Exception) {
                withContext(Main) {
                    Log.d(TAG, "getTrustedContactsInfo: exception happened ${e.message} ")
                    getFireStoreMutableLiveData.postValue(emptyList())
                }
            }
        }
        return getFireStoreMutableLiveData


    }

    fun deleteTrustedContacts(deleteContact: String) {
        GlobalScope.launch(IO) {
            try {
                val result = firebaseFirestore.document(deleteContact).delete().await()
                storageRef.child(deleteContact).delete().await()
                Log.d(TAG, "deleteTrustedContacts: $result")

            } catch (e: Exception) {
                Log.d(TAG, "deleteTrustedContacts: exception happened ${e.message}")
            }
        }

    }

    fun updateDataTOFireBase(model: TrustedContacts_Entity, image: ByteArray?) {

        GlobalScope.launch(IO) {

            try {
                if (image != null) {
                    val result = storageRef.child(model.Phone).putBytes(image).await()
                    val uri = result.storage.downloadUrl.await().toString()
                    val trustedContactsInfo = hashMapOf(
                        "Image" to uri,
                        "Name" to model.Name,
                        "Phone" to model.Phone,
                        "Priority" to model.Priority,
                    )
                    firebaseFirestore.document(model.Phone).set(trustedContactsInfo).await()
                }
            } catch (e: Exception) {
                Log.d(TAG, "updateDataTOFireBase: exception happen ${e.message}")
            }

        }

    }





}