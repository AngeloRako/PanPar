package com.rapnap.panpar.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.AuthCredential
import com.rapnap.panpar.model.Utente
import com.rapnap.panpar.repository.AuthRepository


open class SignInViewModel: ViewModel() {

    private val authRepository: AuthRepository = AuthRepository()
    private val _user = MutableLiveData<Utente>()

    val user: LiveData<Utente>
    get() = _user


    fun signInWithGoogle(googleAuthCredential: AuthCredential, onComplete: ()->Unit) {
        authRepository.firebaseSignInWithGoogle(googleAuthCredential){
            _user.value = it
            onComplete()
        }
    }


}