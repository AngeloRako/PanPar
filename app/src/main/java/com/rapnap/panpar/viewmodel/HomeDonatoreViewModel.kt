package com.rapnap.panpar.viewmodel

import androidx.lifecycle.ViewModel
import com.rapnap.panpar.model.Paniere
import com.rapnap.panpar.repository.PaniereRepository


class HomeDonatoreViewModel: ViewModel() {

    private val paniereRepository: PaniereRepository = PaniereRepository()

    fun donate(paniere: Paniere, onComplete: () -> Unit){

        paniereRepository.createNewPaniere(paniere){

            onComplete()

        }


    }



}