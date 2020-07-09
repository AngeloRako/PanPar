package com.rapnap.panpar.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rapnap.panpar.model.Paniere
import com.rapnap.panpar.model.Utente
import com.rapnap.panpar.repository.PaniereRepository
import com.rapnap.panpar.repository.UtenteRepository
import kotlinx.coroutines.launch

class ProfileRiceventeViewModel: ViewModel() {

    private val utenteRepository: UtenteRepository = UtenteRepository()
    private val paniereRepository: PaniereRepository = PaniereRepository()

    private val _ricevente = MutableLiveData<Utente>()
    val ricevente: LiveData<Utente>
        get() = _ricevente

    private val _panieriRicevente = MutableLiveData<ArrayList<Paniere>>()
    val panieriRicevente: LiveData<ArrayList<Paniere>>
        get() = _panieriRicevente


    fun obtainPanieri(){
        paniereRepository.getListaPanieriPerTipologia("ricevente"){
            _panieriRicevente.setValue(it)
        }
    }

    //Metodo che chiama la rispettiva funzione del repository per il cambio di tipologia
    fun changeRole(){
        utenteRepository.updateUserType("DONATORE")
    }

    //Metodo per prelevare l'utente dal repository. Lo snippet di codice presente nelle parentesi
    //graffe più interne, verrà eseguito al completamento della funzione getUser() di utenteRepository
    fun obtainPuntiRicevente(){

        viewModelScope.launch {
            _ricevente.setValue(utenteRepository.getUser())
        }
    }
}