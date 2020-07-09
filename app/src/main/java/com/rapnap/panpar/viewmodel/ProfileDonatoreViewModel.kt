package com.rapnap.panpar.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.rapnap.panpar.model.Paniere
import com.rapnap.panpar.model.Utente
import com.rapnap.panpar.repository.PaniereRepository
import com.rapnap.panpar.repository.UtenteRepository
import kotlinx.coroutines.launch


class ProfileDonatoreViewModel: ViewModel() {
    //private val paniereRepository: PaniereRepository = PaniereRepository()

    //Istanzio la repository affinché possa accedere ai suoi metodi
    private val utenteRepository: UtenteRepository = UtenteRepository()
    private val panieriRepository: PaniereRepository = PaniereRepository()
    //Creo una variabile MutableLiveData affinché possa essere osservata

    private val _donatore = MutableLiveData<Utente>()
    val donatore: LiveData<Utente>
        get(): LiveData<Utente> = _donatore

    private val _panieriDonatore = MutableLiveData<ArrayList<Paniere>>()
    val panieriDonatore: LiveData<ArrayList<Paniere>>
        get(): LiveData<ArrayList<Paniere>> = _panieriDonatore


    fun obtainPanieri(){
        panieriRepository.getListaPanieriPerTipologia("donatore"){

            _panieriDonatore.value = it

        }
    }

    //Metodo che chiama la rispettiva funzione del repository per il cambio di tipologia
    fun changeRole(){
        utenteRepository.updateUserType("RICEVENTE")
    }

    //Metodo per prelevare l'utente dal repository. Lo snippet di codice presente nelle parentesi
    //graffe più interne, verrà eseguito al completamento della funzione getUser() di utenteRepository
    fun obtainDonatore() {
        viewModelScope.launch {
            _donatore.setValue(utenteRepository.getUser())
        }
    }

    //Metodo per ottenere nome e cognome utente.
    fun obtainNameFromGoogle(account: GoogleSignInAccount): String? {
        return account.displayName
    }

    //Metodo per ottenere l'immagine del profilo dell'utente
    fun obtainImageFromGoogle(account: GoogleSignInAccount): Uri? {
        return account.photoUrl
    }

}