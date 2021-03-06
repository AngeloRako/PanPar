package com.rapnap.panpar.repository

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.location.Location
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rapnap.panpar.model.Paniere
import com.rapnap.panpar.model.PuntoRitiro
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class PaniereRepository {

    //Accedo alle istanze dei Singleton
    private var auth: FirebaseAuth = Firebase.auth
    private var db = Firebase.firestore

    private val acceptableDistanceInMeters: Int = 25000

    fun createNewPaniere(paniere: Paniere, onComplete: () -> Unit) {

        val uiid = auth.currentUser?.uid ?: "!!!!!!"
        val sdf = SimpleDateFormat(".uuuuMMddHHmmss", Locale.getDefault())
        val timestamp: String = sdf.format(Date())

        //Insert nel db di un nuovo paniere
        val paniereData = hashMapOf(
            "id" to "$uiid$timestamp",
            "data_inserimento" to Timestamp(Date()),
            "location" to GeoPoint(
                paniere.puntoRitiro.location.latitude,
                paniere.puntoRitiro.location.longitude
            ),
            "data_consegna_prevista" to Timestamp(paniere.dataConsegnaPrevista!!),
            "donatore" to uiid,
            "nome_punto_ritiro" to paniere.puntoRitiro.nome,
            "contenuto" to paniere.contenuto.toList(),
            "indirizzo" to paniere.puntoRitiro.indirizzo,
            "stato" to paniere.stato.toString(),
            "n_richieste" to 0,
            "valore" to paniere.calcolaValore()
        )

        //New document with a generated ID
        db.collection("panieri")
            .add(paniereData)
            .addOnSuccessListener { documentReference ->
                Log.d(ContentValues.TAG, "Paniere Document added with ID: ${paniereData["id"]}")

                onComplete()

            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)

                //onFailure()

            }
    }

    fun deletePaniere() {
        TODO()
    }

    fun updatePaniere() {
        TODO()
    }

    suspend fun obtainPanieri(
        points: Long,
        userLocationAsGeopoint: GeoPoint
    ): ArrayList<Paniere>? {
        try {

            var panieri: ArrayList<Paniere> = arrayListOf(Paniere())

            //Riferimento alla collection "panieri"
            val panieriRef = db.collection("panieri")
            val userLocationAsLocation: Location = Location("")
            if (userLocationAsGeopoint != null) {
                userLocationAsLocation.latitude = userLocationAsGeopoint.latitude
                userLocationAsLocation.longitude = userLocationAsGeopoint.longitude
            }

            //Query che prende tutti i panieri perché non abbiamo il cazzo di valore
            //del paniere nel documento
            val documents = panieriRef
                .whereEqualTo("stato", "IN_ATTESA_DI_MATCH")
                .orderBy("data_consegna_prevista", Query.Direction.DESCENDING)
                .get()
                .await()

            //Per ogni documento ottenuto
            for (document in documents) {

                //Se non è presente il campo "abbinamento" allora non è stato abbinato
                //e se l'utente donatore non è lo stesso che sta ora cercando un paniere
                if (document.data?.get("donatore")
                        .toString() != auth.currentUser?.uid.toString()
                ) {

                    //Vari "cast" per rendere ammissibili i tipi delle cose che prendiamo dal db
                    val tempGeoPoint = document.getGeoPoint("location")
                    val tempLocation = Location("")
                    if (tempGeoPoint != null) {
                        tempLocation.latitude = tempGeoPoint.latitude
                        tempLocation.longitude = tempGeoPoint.longitude
                    }

                    val tempTimestamp = document.getTimestamp("data_inserimento")
                    var tempDate: Date = Date()
                    if (tempTimestamp != null) {
                        tempDate = tempTimestamp.toDate()
                    }

                    val tempString = document.data?.get("contenuto") as ArrayList<String>
                    val tempContMutable: MutableSet<Paniere.Contenuto> = hashSetOf()

                    //Usare l'iterator()
                    tempString.forEach {
                        tempContMutable.add(Paniere.Contenuto.valueOf(it))
                    }

                    val tempContFixed: MutableSet<Paniere.Contenuto> = tempContMutable

                    var tempConsegna = Date()
                    document.getTimestamp("data_consegna_prevista")?.let {
                        tempConsegna = it.toDate()
                    }

                    val paniereID = document.data?.get("id") as String
                    //Creo un paniere con i dati presi dal DB

                    val paniereTemp = Paniere(
                        id = paniereID,
                        puntoRitiro = PuntoRitiro(
                            nome = document.data?.get("nome_punto_ritiro") as String,
                            indirizzo = document.data?.get("indirizzo") as String,
                            location = tempLocation
                        ),
                        dataInserimento = tempDate,
                        dataConsegnaPrevista = tempConsegna,
                        contenuto = tempContFixed,
                        donatore = document.data?.get("donatore") as String,
                        stato = Paniere.Stato.valueOf(document.data?.get("stato") as String)
                    )

                    //Imposto il ricevente se è avvenuto un match
                    if(paniereTemp.stato != Paniere.Stato.IN_ATTESA_DI_MATCH){
                        paniereTemp.ricevente = document.data?.get("ricevente") as String
                    }

                    var totalValue = totalValueOfFollow(auth.currentUser?.uid.toString())
                    totalValue += paniereTemp.calcolaValore()

                    var isAlreadyFollowing = false
                    if (document.data?.get("coda_riceventi") != null
                    ) {
                        isAlreadyFollowing =
                            (document.data?.get("coda_riceventi") as ArrayList<String>).contains(
                                auth.currentUser?.uid
                            )
                    }

                    //Se il paniere creato ha un valore inferiore ai punti rimanenti all'utente
                    //e se la distanza punto di ritiro - posizione dell'utente è inferiore
                    //ai due km (costante acceptableDistanceInMeters = 2000 metri, è un esempio)
                    if (totalValue < points
                        && tempLocation.distanceTo(userLocationAsLocation) < acceptableDistanceInMeters
                        && !isAlreadyFollowing
                    ) {
                        //OTTIMIZZAZIONE: posso fare la get qui di tutti i campi che non siano
                        //il valore e la posizione, così magari velocizzo perché li getto solo
                        //se la condizione sul valore e sulla posizione sono specificate
                        //Qui lo faccio solo per l'immagine per il momento

                        paniereTemp.immagine = document.data?.get("immagine") as String?

                        panieri.add(paniereTemp)

                        Log.d(
                            ContentValues.TAG,
                            "Paniere aggiunto ad i panieri visualizzabili:   ${paniereTemp.toString()} ho ben: ${panieri.size} in totale!! ;D"
                        )
                    } else {
                        Log.e(
                            TAG,
                            "NON POSSO AGGIUNGERLO: ${totalValue}, Donatore: ${paniereTemp.donatore} |= ${auth.currentUser?.uid} Location: ${tempLocation.distanceTo(
                                userLocationAsLocation
                            )}"
                        )
                    }

                }
            }
            //Rimuovo il primo elemento della lista che è un paniere vuoto
            //serviva solo ad inizializzare l'ArrayList sennò urlava
            panieri.removeAt(0)
            Log.d(TAG, "STO TORNANDO CON: ${panieri.size} PANIERI")

            return panieri

        } catch (e: Exception) {
            Log.e(
                ContentValues.TAG,
                "Non sono riuscito a fare la query sui Panieri perché sono un perdente"
            )
            return null
        }


    }

    suspend fun updatePaniereFollowers(id: String, punti: Int): Boolean {
        Log.d("REPOSITORY", "id del paniere: " + id)

        var isAlreadyFollowing: Boolean = false
        var paniereID: String = ""
        val panieriRef = db.collection("panieri")
        try {

            var totalValue = totalValueOfFollow(id)
            val documents = panieriRef.whereEqualTo("id", id)
                .get()
                .await()
            for (document in documents) {

                var riceventiOnCurrentPaniere: ArrayList<String> = arrayListOf("")

                val codaRic = document.data?.get("coda_riceventi")

                if (codaRic != null) {

                    riceventiOnCurrentPaniere = codaRic as ArrayList<String>

                }

                isAlreadyFollowing =
                    riceventiOnCurrentPaniere.contains(auth.currentUser?.uid)

                val tempString = document.data?.get("contenuto") as ArrayList<String>
                val tempContMutable: MutableSet<Paniere.Contenuto> = hashSetOf()
                tempString.forEach {
                    tempContMutable.add(Paniere.Contenuto.valueOf(it))
                }
                val tempContFixed: MutableSet<Paniere.Contenuto> = tempContMutable

                val tempPaniere = Paniere(contenuto = tempContFixed)
                totalValue += tempPaniere.calcolaValore()

                Log.d(
                    "CHECK",
                    "Valore: " + totalValue.toString() + ". Punti: " + punti.toString()
                )

                paniereID = document.id
            }
            if (!isAlreadyFollowing && totalValue < punti) {
                //NOTA: usare transaction
                panieriRef.document(paniereID)
                    .update("n_richieste", FieldValue.increment(1))
                panieriRef.document(paniereID)
                    .update("coda_riceventi", FieldValue.arrayUnion(auth.currentUser?.uid))
                return true

            } else {
                Log.d(
                    "REPOSITORY", "Già stai seguendo questo paniere o non hai " +
                            "abbastanza punti, marpione."
                )
                return false
            }
        } catch (e: Exception) {
            Log.e("REPOSITORY", "Errore: ${e.localizedMessage}")
            return false
        }

    }


    suspend fun totalValueOfFollow(id: String): Long {

        try {
            var totalValue: Long = 0
            val panieriRef = db.collection("panieri")

            val documents = panieriRef.whereArrayContains("coda_riceventi", id)
                .get()
                .await()

            for (document in documents) {
                val tempString = document.data?.get("contenuto") as ArrayList<String>
                val tempContMutable: MutableSet<Paniere.Contenuto> = hashSetOf()
                tempString.forEach {
                    tempContMutable.add(Paniere.Contenuto.valueOf(it))
                }
                val tempContFixed: MutableSet<Paniere.Contenuto> = tempContMutable

                val tempPaniere = Paniere(contenuto = tempContFixed)
                totalValue += tempPaniere.calcolaValore()
            }
            return totalValue
        } catch (e: Exception) {
            Log.d("REPOSITORY", "Errore: ${e}")
            return -1
        }
    }

    fun getPaniere(idPaniere: String) {
        TODO()
    }

    fun getStoricoPanier(idDonatore: String) {
        TODO()
    }

    //Metodo per ottenere la lista dei panieri relativa ai Donatori, se appunto tipologia = "donatore",
    //mentre relativa invece ai riceventi, se tipologia = "ricevente".
    //Per il momento mostrerò tutti i panieri che ho richiesto e mostrerò quindi anche i panieri richiesti
    //ma che sono stati assegnati a qualche altro utente. Poi in un secondo momento, se tra questi
    //panieri sono presenti anche quelli che ho vinto, basta controllare il campo apposito
    //e vedere se l'id è presente.
    fun getListaPanieriPerTipologia(
        tipologia: String,
        onComplete: (result: ArrayList<Paniere>) -> Unit
    ): ListenerRegistration? {
        val panieri = ArrayList<Paniere>()
        val panieriRef: CollectionReference = db.collection("panieri")
        val uiid = auth.currentUser?.uid ?: "!!!!!!"

        return when (tipologia) {

            "donatore" -> {
                panieriRef
                    .whereEqualTo("donatore", uiid)
                    .orderBy("data_inserimento", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, e ->
                        //.get()
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e)
                            return@addSnapshotListener
                        }
                        if (!(snapshot != null && snapshot.metadata.hasPendingWrites())) {
                            panieri.clear()
                            val it = snapshot?.documents
                            for (document in it!!) {

                                val tempGeoPoint = document.getGeoPoint("location")
                                val tempLocation = Location("")
                                if (tempGeoPoint != null) {
                                    tempLocation.latitude = tempGeoPoint.latitude
                                    tempLocation.longitude = tempGeoPoint.longitude
                                }

                                val tempTimestamp = document.getTimestamp("data_inserimento")
                                var tempDate: Date = Date()
                                if (tempTimestamp != null) {
                                    tempDate = tempTimestamp.toDate()
                                }

                                var tempConsegna = Date()
                                document.getTimestamp("data_consegna_prevista")?.let {
                                    tempConsegna = it.toDate()
                                }

                                val tempString =
                                    document.data?.get("contenuto") as ArrayList<String>
                                val tempContenuto: MutableSet<Paniere.Contenuto> = hashSetOf()

                                tempString.forEach {
                                    tempContenuto.add(Paniere.Contenuto.valueOf(it))
                                }


                                //Creo un paniere con i dati presi dal DB
                                val paniereTemp = Paniere(
                                    document.data?.get("id") as String,
                                    PuntoRitiro(
                                        nome = document.data?.get("nome_punto_ritiro") as String,
                                        indirizzo = document.data?.get("indirizzo") as String,
                                        location = tempLocation
                                    ),
                                    tempDate,
                                    tempConsegna,
                                    tempContenuto,
                                    donatore = document.data?.get("donatore") as String,
                                    stato = Paniere.Stato.valueOf(document.data?.get("stato") as String)
                                )

                                //Imposto il ricevente se è avvenuto un match
                                if(paniereTemp.stato != Paniere.Stato.IN_ATTESA_DI_MATCH){
                                    paniereTemp.ricevente = document.data?.get("ricevente") as String
                                }

                                Log.d(ContentValues.TAG, "Paniere creato")

                                panieri.add(paniereTemp)
                                Log.d(
                                    ContentValues.TAG,
                                    "Paniere aggiunto alla lista panieri donatore"
                                )

                            }
                            onComplete(panieri)
                        }
                    }
            }

            "ricevente" -> {
                panieriRef
                    .whereArrayContains("coda_riceventi", uiid)
                    .orderBy("data_consegna_prevista", Query.Direction.DESCENDING)
                    //.get()
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e)
                            return@addSnapshotListener
                        }
                        if (!(snapshot != null && snapshot.metadata.hasPendingWrites())) {
                            panieri.clear()
                            val it = snapshot?.documents
                            for (document in it!!) {

                                val tempGeoPoint = document.getGeoPoint("location")
                                val tempLocation = Location("")
                                if (tempGeoPoint != null) {
                                    tempLocation.latitude = tempGeoPoint.latitude
                                    tempLocation.longitude = tempGeoPoint.longitude
                                }

                                val tempTimestamp = document.getTimestamp("data_inserimento")
                                var tempDate: Date = Date()
                                if (tempTimestamp != null) {
                                    tempDate = tempTimestamp.toDate()
                                }

                                var tempConsegna = Date()
                                document.getTimestamp("data_consegna_prevista")?.let {
                                    tempConsegna = it.toDate()
                                }

                                val tempString =
                                    document.data?.get("contenuto") as ArrayList<String>
                                val tempContenuto: MutableSet<Paniere.Contenuto> = hashSetOf()

                                tempString.forEach {
                                    tempContenuto.add(Paniere.Contenuto.valueOf(it))
                                }

                                //Creo un paniere con i dati presi dal DB
                                val paniereTemp = Paniere(
                                    id = document.data?.get("id") as String,
                                    puntoRitiro = PuntoRitiro(
                                        nome = document.data?.get("nome_punto_ritiro") as String,
                                        indirizzo = document.data?.get("indirizzo") as String,
                                        location = tempLocation
                                    ),
                                    dataInserimento = tempDate,
                                    dataConsegnaPrevista = tempConsegna,
                                    contenuto = tempContenuto,
                                    donatore = document.data?.get("donatore") as String,
                                    stato = Paniere.Stato.valueOf(document.data?.get("stato") as String)
                                )

                                //Imposto il ricevente se è avvenuto un match
                                if(paniereTemp.stato != Paniere.Stato.IN_ATTESA_DI_MATCH){
                                    paniereTemp.ricevente = document.data?.get("ricevente") as String
                                }

                                Log.d(ContentValues.TAG, "Paniere creato")

                                panieri.add(paniereTemp)
                                Log.d(
                                    ContentValues.TAG,
                                    "Paniere aggiunto alla lista panieri ricevente"
                                )
                            }
                            onComplete(panieri)
                        }
                    }
            }
            else -> null
        }
    }

}
