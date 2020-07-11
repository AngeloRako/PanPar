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
    var dataRicezione: Date? = null,
    var immagine : String? = null,
    var stato: Stato = Stato.IN_ATTESA_DI_MATCH,
    var valore: Long = 0,
    var ricevente: String = ""
) {

    fun calcolaValore() : Long {

        var valore : Long = 0
        contenuto.forEach {
            when(it) {
                Contenuto.PASTA -> valore += 50
                Contenuto.CONFETTURA -> valore += 60
                Contenuto.BIBITE -> valore += 40
                Contenuto.VESTIARIO -> valore += 100
                Contenuto.CONSERVATI -> valore += 80
            }
        }

        this.valore = valore
        return valore
    }

    enum class Contenuto {
        PASTA, CONFETTURA, BIBITE, VESTIARIO, CONSERVATI
    }

    enum class Stato {
        IN_ATTESA_DI_MATCH, ASSEGNATO, IN_GIACENZA, RITIRATO
    }


}
