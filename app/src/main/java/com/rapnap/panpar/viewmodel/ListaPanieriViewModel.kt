package com.rapnap.panpar.viewmodel

import android.content.ContentValues.TAG
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.GeoPoint
import com.rapnap.panpar.model.Paniere
import com.rapnap.panpar.model.Utente
import com.rapnap.panpar.repository.PaniereRepository
import com.rapnap.panpar.repository.UtenteRepository


class ListaPanieriViewModel : ViewModel() {

    private val utenteRepository: UtenteRepository = UtenteRepository()
    private val paniereRepository: PaniereRepository = PaniereRepository()
    private val _listaPanieri = MutableLiveData<ArrayList<Paniere>>()
    val listaPanieri: LiveData<ArrayList<Paniere>>
        get() = _listaPanieri

    private fun getUtente(onComplete: (Utente) -> Unit) {
        utenteRepository.getUser {
            onComplete(it)
        }
    }

    fun getPanieriPrenotabiliFromLocation(location: Location) {

        //val loc = Location("").apply{position(40.878437, 14.343430)}

        //Passare i valori giusti
        getUtente { utente ->

            Log.d(TAG, "Ho a disposizione: ${utente.punteggio} punti!")

            paniereRepository.obtainPanieri(
                points = utente.punteggio,
                userLocationAsGeopoint = GeoPoint(location.latitude, location.longitude)
                //userLocationAsGeopoint = GeoPoint(loc.latitude, loc.longitude)
            ) {
                _listaPanieri.value = it
                Log.d(TAG, "Ho trovato: ${it.count()} panieri!")
            }
        }
    }

    fun updatePaniereFollowers(id: String) {

        //TODO: prende i punti da authRepository?

        getUtente { user ->

            paniereRepository.updatePaniereFollowers(id, user.punteggio.toInt())

        }
    }

}
