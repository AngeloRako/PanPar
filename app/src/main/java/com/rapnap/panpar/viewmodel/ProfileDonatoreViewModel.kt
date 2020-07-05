package com.rapnap.panpar.viewmodel

import android.content.ContentValues
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.rapnap.panpar.model.Utente
import com.rapnap.panpar.repository.UtenteRepository
import java.net.URL


class ProfileDonatoreViewModel: ViewModel() {
    //private val paniereRepository: PaniereRepository = PaniereRepository()

    //Istanzio la repository affinché possa accedere ai suoi metodi
    private val utenteRepository: UtenteRepository = UtenteRepository()
    //Creo una variabile MutableLiveData affinché possa essere osservata
    private val userObserved = MutableLiveData<Utente>()

    /*  fun donate(paniere: Paniere, onComplete: () -> Unit){

          paniereRepository.createNewPaniere(paniere){

              onComplete()

          }


      }*/

    //Metodo che chiama la rispettiva funzione del repository per il cambio di tipologia
    fun changeRole(){
        utenteRepository.updateUserType("RICEVENTE")
    }

    //Metodo per prelevare l'utente dal repository. Lo snippet di codice presente nelle parentesi
    //graffe più interne, verrà eseguito al completamento della funzione getUser() di utenteRepository
    fun obtainRatingDonatore(): MutableLiveData<Utente> {
        utenteRepository.getUser() {
            userObserved.setValue(it)
        }
        return userObserved
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