package com.rapnap.panpar.model

data class Utente (var id: String,
                   var location: String = "",
                   var tipo: Tipologia = Tipologia.DONATORE,
                   var punteggio: Long = 0, //Ricevente
                   var rating: Double = 0.0, //Donatore
                   var isNew: Boolean = true
                    ){

    enum class Tipologia {
        DONATORE, RICEVENTE
    }

}

