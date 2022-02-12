package com.WeTechDigital.lifeTracker.viewModel.firebaseViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.WeTechDigital.lifeTracker.repository.firebaseRepository.DownloadXLRepository
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class DownloadXLViewModel : ViewModel() {
    private val repository = DownloadXLRepository()

    fun downloadXLSheet(context: Context) {
        repository.downloadXLSheet(context)
    }
}