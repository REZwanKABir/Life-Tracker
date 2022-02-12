package com.WeTechDigital.lifeTracker.repository.firebaseRepository

import android.util.Log
import com.WeTechDigital.lifeTracker.Model.PurchaseModel.PurchaseModel
import com.WeTechDigital.lifeTracker.utils.Constant
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@DelicateCoroutinesApi
class PurchaseRepository {
    private val uid = Firebase.auth.currentUser?.uid.toString()
    private val firebaseFirestore = Firebase.firestore.collection("Purchase")

    fun insertData(
        purchaseModel: PurchaseModel,
        packageName: String
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                firebaseFirestore.document(packageName).collection(uid).document(packageName).set(purchaseModel).await()

            } catch (e: Exception) {
                Log.d(Constant.TAG, "insertData: exception happen ${e.message}")
            }
        }
    }
}