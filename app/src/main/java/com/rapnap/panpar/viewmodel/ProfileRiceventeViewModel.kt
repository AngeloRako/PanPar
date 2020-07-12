package com.rapnap.panpar.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import com.rapnap.panpar.model.Abbinamento
import com.rapnap.panpar.model.Paniere
import com.rapnap.panpar.model.Utente
import com.rapnap.panpar.repository.AbbinamentoRepository
import com.rapnap.panpar.repository.PaniereRepository
import com.rapnap.panpar.repository.UtenteRepository
import kotlinx.coroutines.launch

class ProfileRiceventeViewModel: ViewModel() {

    private lateinit var registration: ListenerRegistration

    private val utenteRepository: UtenteRepository = UtenteRepository()
    private val paniereRepository: PaniereRepository = PaniereRepository()
    private val abbinamentoRepository: AbbinamentoRepository = AbbinamentoRepository()

    private val _ricevente = MutableLiveData<Utente>()
    val ricevente: LiveData<Utente>
        get() = _ricevente

    private val _abbinamenti = MutableLiveData<ArrayList<Abbinamento>>()
    val abbinamenti: LiveData<ArrayList<Abbinamento>>
        get() = _abbinamenti

    private val _panieriRicevente = MutableLiveData<ArrayList<Paniere>>()
    val panieriRicevente: LiveData<ArrayList<Paniere>>
        get() = _panieriRicevente

    fun obtainPanieri(){
        registration = paniereRepository.getListaPanieriPerTipologia("ricevente"){ panieri ->
            getAbbinamenti() {
                _panieriRicevente.setValue(panieri)
            }
        }!!
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

    override fun onCleared() {
        registration.remove()
        super.onCleared()
    }

    fun userId(): String{
        return Firebase.auth.currentUser?.uid!!
    }

    fun getAbbinamenti(onComplete: (ArrayList<Abbinamento>) -> Unit) {
        abbinamentoRepository.getAbbinamenti() {
            _abbinamenti.value = it
            onComplete(it)
        }
    }


}