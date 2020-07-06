package com.rapnap.panpar.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.GeoPoint
import com.rapnap.panpar.model.Paniere
import com.rapnap.panpar.model.Utente
import com.rapnap.panpar.repository.PaniereRepository
import com.rapnap.panpar.repository.UtenteRepository


class ListaPanieriViewModel: ViewModel() {

    private val utenteRepository: UtenteRepository = UtenteRepository()
    private val paniereRepository : PaniereRepository = PaniereRepository()
    private lateinit var listaPanieri : MutableLiveData<ArrayList<Paniere>>

    fun getListaPanieri() : LiveData<ArrayList<Paniere>> {
        return listaPanieri
    }

    private fun getUtente(onComplete: (Utente) -> Unit) {
        utenteRepository.getUser {
            onComplete(it)
        }
    }

    fun getPanieriByScore(onComplete: () -> Unit) {
        //Passare i valori giusti
        paniereRepository.getListaPanieri(GeoPoint(40.878437, 14.343430), 1000) {
            listaPanieri = it
            onComplete()
        }
    }

    fun updatePaniereFollowers(id: String){

        //TODO: prende i punti da authRepository?

        getUtente{ user->

            paniereRepository.updatePaniereFollowers(id, user.punteggio.toInt())

        }

    }




}
