package com.rapnap.panpar.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.GeoPoint
import com.rapnap.panpar.model.Paniere
import com.rapnap.panpar.repository.AuthRepository
import com.rapnap.panpar.repository.PaniereRepository


class ListaPanieriViewModel: ViewModel() {

    private val authRepository: AuthRepository = AuthRepository()
    private val paniereRepository : PaniereRepository = PaniereRepository()
    private lateinit var listaPanieri : MutableLiveData<ArrayList<Paniere>>

    fun getListaPanieri() : LiveData<ArrayList<Paniere>> {
        return listaPanieri
    }

    fun getUtente(onComplete: () -> Unit) {
        TODO("Aspetto Ozegar")
    }

    fun getPanieriByScore(onComplete: () -> Unit) {
        paniereRepository.getListaPanieri(GeoPoint(77.0, 77.0), 1000) {
            listaPanieri = it
            onComplete()
        }
    }

}
