package com.WeTechDigital.lifeTracker.viewModel.firebaseViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.WeTechDigital.lifeTracker.Model.SignInUser
import com.WeTechDigital.lifeTracker.repository.firebaseRepository.SignInRepository
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.DelicateCoroutinesApi


@DelicateCoroutinesApi
class SignInViewModel(application: Application) : AndroidViewModel(application) {
    private var signInRepository: SignInRepository = SignInRepository()

    var checkAuthenticateLiveData: LiveData<SignInUser>? = null
    var collectUserInfoLiveData: LiveData<SignInUser> = signInRepository.collectUserData()
    var authenticateUserLiveData: LiveData<String>? = null

    //firebase sign in with google
    fun signInWithGoogle(authCredential: AuthCredential?) {
        authenticateUserLiveData = signInRepository.firebaseSignInWithGoogle(authCredential)
    }

    fun checkAuth() {
        checkAuthenticateLiveData = signInRepository.checkAuthenticationInFirebase()
    }


}