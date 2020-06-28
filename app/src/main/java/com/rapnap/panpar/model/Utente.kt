package com.rapnap.panpar.model

data class Utente (val id: String,
                   val location: String = "",
                   val tipo: Tipologia = Tipologia.RICEVENTE,
                   val punteggio: Int = 0, //Ricevente
                   val rating: Int = 0, //Donatore
                   var isNew: Boolean = true
                    )

enum class Tipologia {
    DONATORE, RICEVENTE
}
