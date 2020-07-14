package com.rapnap.panpar.viewmodel

import android.content.ContentValues.TAG
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.GeoPoint
import com.rapnap.panpar.model.Paniere
import com.rapnap.panpar.repository.PaniereRepository
import com.rapnap.panpar.repository.UtenteRepository
import kotlinx.coroutines.launch


class ListaPanieriViewModel : ViewModel() {

    private val utenteRepository: UtenteRepository = UtenteRepository()
    private val paniereRepository: PaniereRepository = PaniereRepository()
    private val _listaPanieri = MutableLiveData<ArrayList<Paniere>>()
    val listaPanieri: LiveData<ArrayList<Paniere>>
        get() = _listaPanieri

    fun getPanieriPrenotabiliFromLocation(location: Location) {

        viewModelScope.launch {
            val utente = utenteRepository.getUser()
            try {
                Log.d(TAG, "Ho a disposizione: ${utente?.punteggio} punti!")

                _listaPanieri.value = paniereRepository.obtainPanieri(
                    points = utente?.punteggio!!,
                    userLocationAsGeopoint = GeoPoint(location.latitude, location.longitude)
                )
                Log.d(TAG, "Ho trovato: ${_listaPanieri.value?.count()} panieri!")
            } catch (e: Exception) {
                Log.d(TAG, "Errore durante la ricerca dei panieri: ${e}")
            }
        }
    }

    suspend fun updatePaniereFollowers(id: String): Boolean {

        val user = utenteRepository.getUser()
        try {
            return paniereRepository.updatePaniereFollowers(id, user?.punteggio!!.toInt())

        } catch (e: Exception) {
            Log.d(TAG, "Errore durante l'aggiornamento dei followers: ${e}")
            return false

        }
    }

}
