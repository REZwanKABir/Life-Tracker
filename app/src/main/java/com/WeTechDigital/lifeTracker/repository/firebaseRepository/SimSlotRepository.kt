package com.WeTechDigital.lifeTracker.repository.firebaseRepository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.WeTechDigital.lifeTracker.Model.SimSlotModel
import com.WeTechDigital.lifeTracker.utils.Constant.TAG
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

@DelicateCoroutinesApi
class SimSlotRepository {

    private val firebaseAuth = Firebase.auth.currentUser
    private val firebaseFirestore = Firebase.firestore.collection("Client")
        .document(firebaseAuth!!.uid).collection("Client_PersonalInfo").document("DEFAULT_SIM")

    fun insertSIMSlotToDatabase(simSlot: SimSlotModel): MutableLiveData<String> {
        val insertResultLiveData = MutableLiveData<String>()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                firebaseFirestore.set(simSlot).await()
                withContext(Dispatchers.Main) {
                    insertResultLiveData.postValue("Successfully")
                }
            } catch (e: Exception) {
                Log.d(TAG, "insertSIMSlotToDatabase: exception happen ${e.message}")
                withContext(Dispatchers.Main) {
                    insertResultLiveData.postValue("Fail")
                }
            }
        }
        return insertResultLiveData

    }


    fun getSelectedSimSlot(): MutableLiveData<SimSlotModel?> {
        val getFireStoreMutableLiveData =
            MutableLiveData<SimSlotModel?>()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val data = firebaseFirestore.get().await().toObject(SimSlotModel::class.java)
                withContext(Dispatchers.Main) {
                    getFireStoreMutableLiveData.postValue(data)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "getSelectedSimSlot: exception happened ${e.message}")
                    getFireStoreMutableLiveData.postValue(SimSlotModel("0"))
                }
            }
        }
        return getFireStoreMutableLiveData


    }
}