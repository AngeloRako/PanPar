package com.rapnap.panpar.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rapnap.panpar.model.Tipologia
import com.rapnap.panpar.model.Utente
import com.rapnap.panpar.repository.AuthRepository

class WelcomeViewModel: ViewModel() {

    private val authRepository: AuthRepository = AuthRepository()
    private lateinit var user: MutableLiveData<Utente>

    fun getUser(): LiveData<Utente> {
        return user
    }

    fun login(onComplete: () -> Unit){
        authRepository.login(){
            user = it
            onComplete()
        }

    }

    fun isLoggedIn(): Boolean {

        return authRepository.isLoggedIn()

    }

}