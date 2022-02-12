package com.WeTechDigital.lifeTracker.repository.firebaseRepository

import android.content.Context
import android.util.Log
import com.WeTechDigital.lifeTracker.utils.Constant
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

@DelicateCoroutinesApi
class DownloadXLRepository {

    //getting reference of fireStore path
    private val allDir = Firebase.storage.reference.child("ListOfDivition")
    private val individualFile = Firebase.storage.reference.child("ListOfDivition")
    private var string = ""
    private lateinit var localFile: File

    //downloading xl sheet from firebase
    fun downloadXLSheet(context: Context) {
        val file = context.getExternalFilesDir("EXCEL")
        if (!file!!.exists()) {
            file.mkdir()
        }

        //fetching and storing data from fire base to phone storage
        GlobalScope.launch(Dispatchers.IO) {
            val result = allDir.listAll().await()
            for (item in result.items) {
                string = item.toString()
                val list = string.split("/")
                Log.d(Constant.TAG, "onCreate: ${list[list.size - 1]}")
                localFile = File(file, list[list.size - 1])
                individualFile.child(list[list.size - 1])
                    .getFile(localFile).await()
            }
        }

    }

}