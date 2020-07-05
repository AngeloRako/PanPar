package com.rapnap.panpar.model

import java.util.*
import kotlin.collections.HashSet

data class Paniere (

    var id: String = "",
    var puntoRitiro: PuntoRitiro = PuntoRitiro(),
    var dataInserimento: Date = Date(),
    var dataConsegnaPrevista: Date? = null,
    var contenuto: MutableSet<Contenuto> = HashSet<Contenuto>(),
    var richieste: ArrayList<Richiesta>? = null,
    var donatore: String = "",
    var nRichieste: Int = 0,
    var abbinamento: Abbinamento? = null,
    var dataRicezione: Date? = null

)

enum class Contenuto {
    PASTA, CONFETTURA, BIBITE, SORPRESA, ALTRO
}
