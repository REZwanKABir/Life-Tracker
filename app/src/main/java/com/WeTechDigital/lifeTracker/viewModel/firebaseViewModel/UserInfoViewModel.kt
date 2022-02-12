package com.WeTechDigital.lifeTracker.viewModel.firebaseViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.WeTechDigital.lifeTracker.Model.UserInfoModel
import com.WeTechDigital.lifeTracker.repository.firebaseRepository.UserInfoRepository
import kotlinx.coroutines.DelicateCoroutinesApi


@DelicateCoroutinesApi
class UserInfoViewModel(application: Application) : AndroidViewModel(application) {

    private val userInfoRepository: UserInfoRepository = UserInfoRepository()
    var insertResultLiveData: LiveData<String>? = null
    var getUserInfoLiveData: MutableLiveData<UserInfoModel?> =
        userInfoRepository.getUserInfoFromDataBase()

    fun insert(userinfo: UserInfoModel, image: ByteArray?) {
        insertResultLiveData = userInfoRepository.insertUserInfoFirebase(userinfo, image)
    }

    fun insert(userinfo: UserInfoModel) {
        insertResultLiveData = userInfoRepository.insertUserInfoFirebase(userinfo)
    }

    fun updateProfileImage(image: ByteArray?){
        userInfoRepository.updateProfileImage(image)
    }

    fun updateUserPackageInfo(currentDate:String,broughtPack:String,afterPackageDays:String,description:String){
        userInfoRepository.updateUserPackageInfo(currentDate,broughtPack,afterPackageDays,description)
    }
    fun  updatePackageStatus(){
        userInfoRepository.updatePackageStatus()
    }



}