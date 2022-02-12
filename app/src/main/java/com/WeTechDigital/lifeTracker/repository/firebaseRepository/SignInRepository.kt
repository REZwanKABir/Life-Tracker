package com.WeTechDigital.lifeTracker.repository.firebaseRepository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.WeTechDigital.lifeTracker.Model.SignInUser
import com.WeTechDigital.lifeTracker.utils.Constant.TAG
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await


@DelicateCoroutinesApi
class SignInRepository {
    private val firebaseAuth = Firebase.auth

    private var check = true

    // check Authentication in firebase..........

    fun checkAuthenticationInFirebase(): MutableLiveData<SignInUser> {
        val isAuthenticateLiveData = MutableLiveData<SignInUser>()
        val currentUser = firebaseAuth.currentUser
        Log.d(TAG, "checkAuthenticationInFirebase: current user ${currentUser?.email}")
        if (currentUser == null) {
            val user = SignInUser("null", "null", "null", "null", false)
            Log.d(TAG, "checkAuthenticationInFirebase: in repository")
            check = false
            isAuthenticateLiveData.value = user
        } else {
            Log.d(TAG, "checkAuthenticationInFirebase: is called when user not null")
            val user = SignInUser("null", "null", "null", "null", true)
            check = true
            isAuthenticateLiveData.value = user
        }
        return isAuthenticateLiveData
    }

    //collect user info  from authentication..........

    fun collectUserData(): MutableLiveData<SignInUser> {
        val collectUserMutableLiveData = MutableLiveData<SignInUser>()
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            Log.d(TAG, "collectUserData: is called")
            val uId = currentUser.uid
            val name = currentUser.displayName
            val email = currentUser.email
            val getImageUrl = currentUser.photoUrl
            val imageUrl = getImageUrl.toString()
            val user = SignInUser(uId, name.toString(), email.toString(), imageUrl, true)
            collectUserMutableLiveData.value = user
            Log.d(TAG, "collectUserData: $user")
        }
        return collectUserMutableLiveData
    }



    //firebase sign in with google
    fun firebaseSignInWithGoogle(authCredential: AuthCredential?): MutableLiveData<String> {
        Log.d(TAG, "firebaseSignInWithGoogle: $authCredential")
        val authMutableLiveData = MutableLiveData<String>()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                if (authCredential != null) {
                    firebaseAuth.signInWithCredential(authCredential).await()
                    withContext(Dispatchers.Main){
                        authMutableLiveData.postValue("Successful")
                    }
                }
            }catch (e:Exception){
                withContext(Dispatchers.Main){
                    authMutableLiveData.postValue(e.toString())
                }
            }
        }

            return authMutableLiveData


    }


}