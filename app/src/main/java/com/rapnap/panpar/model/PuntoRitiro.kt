package com.rapnap.panpar.model

import android.location.Location

data class PuntoRitiro (val id: String = "",
                        val nome: String = "",
                        val indirizzo : String = "",
                        val location: Location = Location("").also{
                            it.latitude = 40.643396
                            it.longitude = 14.865041}
)
