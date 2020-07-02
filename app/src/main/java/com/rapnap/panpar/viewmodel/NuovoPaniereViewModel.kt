package com.rapnap.panpar.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.Auth
import com.rapnap.panpar.model.PuntoRitiro
import com.rapnap.panpar.repository.AuthRepository
import com.rapnap.panpar.repository.PuntiRitiroRepository

class NuovoPaniereViewModel: ViewModel() {

    private val puntiRitiroRep = PuntiRitiroRepository() //Forse ho bisogno di un factory per il VM per evitare memory leaks?
    private val _puntiRitiro = MutableLiveData<List<PuntoRitiro>>()
    val puntiRitiro: LiveData<List<PuntoRitiro>>
        get() = _puntiRitiro

    //Ottieni i punti di ritiro a una certa distanza massima da una location (async)
    fun getPuntiDiRitiro(location: Location, maxDistance: Double, onComplete: () -> Unit ) {

        puntiRitiroRep.getPuntiDiRitiro(location, maxDistance){

            _puntiRitiro.value = it
            onComplete()

        }

    }

    fun nuoviPuntoDiRitiro(onAdded: (id: Int)->Unit){

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

            puntiRitiroRep.createtLocationPunto(it) {
                onAdded(i)
                i++
            }

        }


    }

}
