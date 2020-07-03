package com.rapnap.panpar.model

import java.util.Date
import kotlin.random.Random.Default.nextInt

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

) {

    fun calcolaValore() : Long {
        var valore : Long = 0
        contenuto.forEach {
            when(it) {
                Contenuto.PASTA -> valore += 50
                Contenuto.CONFETTURA -> valore += 60
                Contenuto.BIBITE -> valore += 40
                Contenuto.SORPRESA -> valore += (nextInt(70) + 30)
                Contenuto.ALTRO -> valore += (nextInt(70) + 30)
            }
        }

        return valore
    }

}

enum class Contenuto {
    PASTA, CONFETTURA, BIBITE, SORPRESA, ALTRO
}
