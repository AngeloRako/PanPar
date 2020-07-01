package com.rapnap.panpar.model

import java.util.Date

data class Paniere (

    var id: String = "",
    var puntoRitiro: PuntoRitiro = PuntoRitiro(),
    var dataInserimento: Date = Date(),
    var contenuto: List<Contenuto> = ArrayList<Contenuto>(),
    var richieste: ArrayList<Richiesta>? = null,
    var donatore: String = "",
    var nRichieste: Int = 0,
    var abbinamento: Abbinamento? = null,
    var dataRicezione: Date? = null

)

enum class Contenuto {
    PASTA, CONFETTURA, BIBITE, SORPRESA, ALTRO
}
