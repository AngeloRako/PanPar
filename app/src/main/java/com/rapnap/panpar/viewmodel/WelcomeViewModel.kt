package com.rapnap.panpar.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rapnap.panpar.model.Utente
import com.rapnap.panpar.repository.AuthRepository

class WelcomeViewModel: ViewModel() {

    private val authRepository: AuthRepository = AuthRepository()
    private val _user = MutableLiveData<Utente>()
    val user: LiveData<Utente>
        get() = _user

    fun login(onComplete: () -> Unit){
        authRepository.login(){
            _user.value = it
            onComplete()
        }

    }

    fun isLoggedIn(): Boolean {

        return authRepository.isLoggedIn()

    }

}