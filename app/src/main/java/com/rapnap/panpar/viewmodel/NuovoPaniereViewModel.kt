package com.rapnap.panpar.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rapnap.panpar.model.Paniere
import com.rapnap.panpar.model.PuntoRitiro
import com.rapnap.panpar.repository.PaniereRepository
import com.rapnap.panpar.repository.PuntiRitiroRepository

class NuovoPaniereViewModel: ViewModel() {

    private val puntiRitiroRep = PuntiRitiroRepository() //Forse ho bisogno di un factory per il VM per evitare memory leaks?
    private val paniereRep = PaniereRepository()

    private val _nuovoPaniereInCreazione = MutableLiveData<Paniere>()
    val nuovoPaniereInCreazione: LiveData<Paniere>
        get() = _nuovoPaniereInCreazione

    private val _puntiRitiro = MutableLiveData<ArrayList<PuntoRitiro>>()
    val puntiRitiro: LiveData<ArrayList<PuntoRitiro>>
        get() = _puntiRitiro

    // Il paniere scelto dall'utente per la donazione
    private val _puntoRitiroScelto = MutableLiveData<PuntoRitiro>()
    val puntoRitiroScelto: LiveData<PuntoRitiro>
        get() = _puntoRitiroScelto

    // Il paniere attualmente visualizzato dall'utente (potrebbe non essere quello scelto)
    private val _puntoRitiroVisualizzato = MutableLiveData<PuntoRitiro>()
    val puntoRitiroVisualizzato: LiveData<PuntoRitiro>
        get() = _puntoRitiroVisualizzato


    //Ottieni i punti di ritiro a una certa distanza massima da una location (async)
    fun getPuntiDiRitiro(location: Location, maxDistance: Double, onComplete: () -> Unit ) {

        puntiRitiroRep.getPuntiDiRitiro(location, maxDistance){

            _puntiRitiro.value = it

            onComplete()
        }

    }


    fun setPuntoRitiroScelto(punto: PuntoRitiro){

        //Imposta notifica per gli interessati alla crezione paniere se questo è presente
        _nuovoPaniereInCreazione.value?.let{
            it.puntoRitiro   = punto
        }

        //Imposta notifica per gli interessati al solo puntoRitiro
        _puntoRitiroScelto.value = punto

    }

    fun setPuntoRitiroVisualizzato(punto: PuntoRitiro){

        //Imposta notifica per gli interessati al solo puntoRitiro visualizzato
        _puntoRitiroVisualizzato.value = punto

    }

    //Crea un nuovo paniere
    fun creaNuovoPaniere(onComplete: () -> Unit ) {

        paniereRep.createNewPaniere(_nuovoPaniereInCreazione.value!!){
            onComplete()
        }

    }

    fun setNuovoPaniereInCreazione(paniere: Paniere){

        _nuovoPaniereInCreazione.value = paniere

    }


    /*
    /*  FOR DEBUG PURPUSE - NON VANNO USATE  */
    fun nuoviPuntiDiRitiro(onAdded: (id: Int)->Unit){


        /*Per inserire nuovi punti ritiro decommentare i punti sotto e modificare i dati :)*/

        val punti = ArrayList<PuntoRitiro>(5)
        /*
        punti.add( PuntoRitiro(nome = "IKEA Napoli Afragola", indirizzo = "Via Enrico Berlinguer, 2, 80021 Afragola NA", location = Location("").apply{
            this.latitude = 40.908699
            this.longitude = 14.316303
        }))

        punti.add( PuntoRitiro(nome = "Centro di Raccolta Pacchi Vergara", indirizzo = "Via della Libertà, 21, Frattamaggiore NA", location = Location("").apply{
            this.latitude = 40.939617
            this.longitude = 14.267097
        }))
        punti.add( PuntoRitiro(nome = "Chiesa Battista di Napoli", indirizzo = "Via Foria, 93, 80137 Napoli NA", location = Location("").apply{
            this.latitude = 40.857463
            this.longitude = 14.258972
        }))
        punti.add( PuntoRitiro(nome = "Mercatopoli Napoli Pianura", indirizzo = "Via Provinciale Montagna Spaccata, 421, 80126 Napoli NA", location = Location("").apply{
            this.latitude = 40.854558
            this.longitude = 14.162208
        }))
        punti.add( PuntoRitiro(nome = "Ufficio Postale Poste Italiane", indirizzo = "Via Dante Alighieri, 12, 80040 Volla NA", location = Location("").apply{
            this.latitude = 40.878438
            this.longitude = 14.343431
        }))
        */

        var i = 0
        punti.forEach {

            puntiRitiroRep.createLocationPunto(it) {
                onAdded(i)
                i++
            }

        }

    }
    */
}
