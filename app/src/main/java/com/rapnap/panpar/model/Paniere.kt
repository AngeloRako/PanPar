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
    var dataRicezione: Date? = null,
    var immagine : String? = null,
    var stato: Stato = Stato.IN_ATTESA_DI_MATCH

) {

    fun calcolaValore() : Long {
        var valore : Long = 0
        contenuto.forEach {
            when(it) {
                Contenuto.PASTA -> valore += 50
                Contenuto.CONFETTURA -> valore += 60
                Contenuto.BIBITE -> valore += 40
                Contenuto.SORPRESA -> valore += (kotlin.random.Random.nextInt(70) + 30)
                Contenuto.ALTRO -> valore += (kotlin.random.Random.nextInt(70) + 30)
            }
        }

        return valore
    }

}
enum class Contenuto {
    PASTA, CONFETTURA, BIBITE, SORPRESA, VESTIARIO, CONSERVATI, ALTRO
}

enum class Stato {
    IN_ATTESA_DI_MATCH, ASSEGNATO, IN_GIACENZA, RITIRATO
}
