package com.rapnap.panpar.model

import android.location.Location

data class PuntoRitiro (val id: String = "bocc",
                        val nome: String = "Chiesa del Bambin Gesù",
                        val indirizzo : String = "via Picentino n 25 – 84098 – Pontecagnano Faiano (SA)",
                        val location: Location = Location("").also{
                            it.latitude = 40.643396
                            it.longitude = 14.865041}
)