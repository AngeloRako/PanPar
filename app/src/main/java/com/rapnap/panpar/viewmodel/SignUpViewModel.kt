package com.rapnap.panpar.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rapnap.panpar.model.Utente
import com.rapnap.panpar.repository.AuthRepository

class SignUpViewModel: ViewModel() {

    private val authRepository: AuthRepository = AuthRepository()
    private val _user = MutableLiveData<Utente>()

    val user: LiveData<Utente>
        get() = _user

    fun registerAs(tipologia: Utente.Tipologia, position: Location? = null){
        authRepository.firebaseCompleteSignUp(tipologia, position){
            _user.value = it
        }
    }

}