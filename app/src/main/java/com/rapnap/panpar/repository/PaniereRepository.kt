package com.rapnap.panpar.repository

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.rapnap.panpar.model.Contenuto
import com.rapnap.panpar.model.Paniere
import com.rapnap.panpar.model.PuntoRitiro
import kotlinx.android.synthetic.main.recyclerview_item_row.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PaniereRepository {

    //Accedo alle istanze dei Singleton
    private var auth: FirebaseAuth = Firebase.auth
    private var db = Firebase.firestore
    //private var storage = Firebase.storage

    private val acceptableDistanceInMeters : Int = 2000

    fun createNewPaniere(paniere: Paniere, onComplete: () -> Unit ) {

        val uiid = auth.currentUser?.uid ?: "!!!!!!"
        val sdf = SimpleDateFormat(".uuuuMMddHHmmss", Locale.getDefault())
        val timestamp: String = sdf.format(Date())

        //Insert nel db di un nuovo paniere
        val paniereData = hashMapOf(
            "id" to "$uiid$timestamp",
            "data_inserimento" to Timestamp(Date()),
            "location" to GeoPoint(paniere.puntoRitiro.location.latitude, paniere.puntoRitiro.location.longitude),
            "donatore" to uiid,
            "nome_punto_ritiro" to paniere.puntoRitiro.nome,
            "contenuto" to paniere.contenuto,
            "indirizzo" to paniere.puntoRitiro.indirizzo

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

    fun deletePaniere(){
        TODO()
    }

    fun updatePaniere(){
        TODO()
    }

    fun getListaPanieri(location: GeoPoint, points: Long, onComplete: (result: MutableLiveData<ArrayList<Paniere>>) -> Unit){
        //Aggiungere il controllo per vicinanza con il GeoPoint passato

        val panieriMutableLiveData = MutableLiveData<ArrayList<Paniere>>()

        val listaPanieri = obtainPanieri(points, location) {
            panieriMutableLiveData.setValue(it)

            Log.d("REPOSITORY", panieriMutableLiveData.value.toString())

            Log.d(TAG, "Ho assegnato il valore all'oggetto osservato." +
                    " La dimensione della lista dei panieri è: " + it.size.toString())

            onComplete(panieriMutableLiveData)
        }

    }

    fun obtainPanieri(points: Long, userLocationAsGeopoint: GeoPoint, onComplete: (result: ArrayList<Paniere>) -> Unit) {
        var panieri : ArrayList<Paniere> = arrayListOf(Paniere())

        //Riferimento alla collection "panieri"
        val panieriRef = db.collection("panieri")
        val userLocationAsLocation : Location = Location("")
        if (userLocationAsGeopoint != null) {
            userLocationAsLocation.latitude = userLocationAsGeopoint.latitude
            userLocationAsLocation.longitude = userLocationAsGeopoint.longitude
        }

        //Query che prende tutti i panieri perché non abbiamo il cazzo di valore
        //del paniere nel documento
        panieriRef.get()
            .addOnSuccessListener { documents ->

                //Per ogni documento ottenuto
                for (document in documents) {

                    //Se non è presente il campo "abbinamento" allora non è stato abbinato
                    //e se l'utente donatore non è lo stesso che sta ora cercando un paniere
                    if (document.data?.get("abbinamento") == null && (document.data?.get("donatore").toString() != auth.currentUser?.uid.toString())) {

                        //Vari "cast" per rendere ammissibili i tipi delle cose che prendiamo dal db
                        val tempGeoPoint = document.getGeoPoint("location")
                        val tempLocation = Location("")
                        if (tempGeoPoint != null) {
                            tempLocation.latitude = tempGeoPoint.latitude
                            tempLocation.longitude = tempGeoPoint.longitude
                        }

                        val tempTimestamp = document.getTimestamp("data_inserimento")
                        var tempDate : Date = Date()
                        if (tempTimestamp != null) {
                            tempDate = tempTimestamp.toDate()
                        }

                        val tempString = document.data?.get("contenuto") as ArrayList<String>
                        val tempContMutable : MutableList<Contenuto> = mutableListOf()

                        tempString.forEach {
                            tempContMutable.add(Contenuto.valueOf(it))
                        }

                        val tempContFixed : List <Contenuto> = tempContMutable

                        Log.d("REPOSITORY", tempContFixed.get(0).toString())

                        //Creo un paniere con i dati presi dal DB
                        val paniereTemp = Paniere(
                            document.data?.get("id") as String,
                            PuntoRitiro(nome = document.data?.get("nome_punto_ritiro") as String,
                                indirizzo = document.data?.get("indirizzo") as String,
                                location = tempLocation),
                            tempDate,
                            tempContFixed,
                            null,
                            document.data?.get("donatore") as String,
                            0,
                            null,
                            null
                        )

                        Log.d("REPOSITORY", "Paniere creato: " + paniereTemp.contenuto.toString())

                        //Se il paniere creato ha un valore inferiore ai punti rimanenti all'utente
                        //e se la distanza punto di ritiro - posizione dell'utente è inferiore
                        //ai due km (costante acceptableDistanceInMeters = 2000 metri, è un esempio)
                        if (paniereTemp.calcolaValore() < points && tempLocation.distanceTo(userLocationAsLocation) < acceptableDistanceInMeters) {

                            //OTTIMIZZAZIONE: posso fare la get qui di tutti i campi che non siano
                            //il valore e la posizione, così magari velocizzo perché li getto solo
                            //se la condizione sul valore e sulla posizione sono specificate
                            //Qui lo faccio solo per l'immagine per il momento
                            paniereTemp.immagine = document.data?.get("immagine") as String

                            panieri.add(paniereTemp)
                            Log.d(TAG, "Paniere aggiunto ad i panieri visualizzabili. Immagine: " + paniereTemp.immagine.toString())
                        }
                    }
                }
                //Rimuovo il primo elemento della lista che è un paniere vuoto
                //serviva solo ad inizializzare l'ArrayList sennò urlava
                panieri.removeAt(0)
                onComplete(panieri)
            }
            .addOnFailureListener() { exception ->
                Log.e(TAG, "Non sono riuscito a fare la query sui Panieri perché sono un perdente")
            }
        //onComplete(panieri)
    }

//    fun obtainBmp(imgRef: String, onComplete: (result: Bitmap?) -> Unit) {
//        val gsReference = storage.getReferenceFromUrl(imgRef)
//        val ONE_MEGABYTE: Long = 1024 * 1024
//        var bmp : Bitmap? = null
//
//        gsReference.getBytes(ONE_MEGABYTE).addOnSuccessListener {
//            bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
//
//            Log.d("IMG", "Sono un campione: " + bmp.toString())
//        }.addOnFailureListener {
//            bmp = BitmapFactory.decodeFile("drawable/empty_png.png")
//            Log.d("IMG", "Sono un fallito")
//        }
//
//        onComplete(bmp)
//    }

    fun updatePaniereFollowers(id: String, punti: Int) {
        Log.d("REPOSITORY", "id del paniere: " + id)

        var isAlreadyFollowing : Boolean = false
        var paniereID : String = ""
        val panieriRef = db.collection("panieri")

        checkIfFollowingTooMany(id, punti) {
            if (!it) {
                panieriRef.whereEqualTo("id", id)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            val riceventiOnCurrentPaniere = document.data?.get("riceventi") as ArrayList<String>
                            isAlreadyFollowing = riceventiOnCurrentPaniere.contains(id)
                            paniereID = document.id
                        }
                        if(!isAlreadyFollowing) {
                            //NOTA: usare transaction
                            panieriRef.document(paniereID)
                                .update("n_richieste", FieldValue.increment(1))
                            panieriRef.document(paniereID)
                                .update("riceventi", FieldValue.arrayUnion(auth.currentUser?.uid))

                        } else {
                            Log.d("REPOSITORY", "Già stai seguendo questo paniere")
                        }
                    }
                    .addOnFailureListener() {
                        Log.d("REPOSITORY", "Sono un fallito")
                    }
            } else {
                Log.d("REPOSITORY", "Stai già seguendo troppi panieri, marpione")
            }
        }

    }

    fun checkIfFollowingTooMany(id: String, punti: Int, onComplete: (tooMany: Boolean) -> Unit) {
        var totalValue : Long = 0
        val panieriRef = db.collection("panieri")

        panieriRef.whereArrayContains("riceventi", id)
            .get()
            .addOnSuccessListener {documents ->
                for(document in documents) {
                    val tempString = document.data?.get("contenuto") as ArrayList<String>
                    val tempContMutable: MutableList<Contenuto> = mutableListOf()
                    tempString.forEach {
                        tempContMutable.add(Contenuto.valueOf(it))
                    }
                    val tempContFixed: List<Contenuto> = tempContMutable

                    val tempPaniere = Paniere(contenuto = tempContFixed)
                    totalValue += tempPaniere.calcolaValore()
                }
                onComplete(totalValue > punti)
            }
            .addOnFailureListener() {
                Log.d("REPOSITORY", "Sono un fallito ed ho richiesto troppi panieri")
            }
    }

    fun getPaniere(idPaniere: String){
        TODO()
    }

    fun getStoricoPanier(idDonatore: String){
        TODO()
    }

}