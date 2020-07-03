package com.rapnap.panpar.repository

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rapnap.panpar.model.Contenuto
import com.rapnap.panpar.model.Paniere
import com.rapnap.panpar.model.PuntoRitiro
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PaniereRepository {

    //Accedo alle istanze dei Singleton
    private var auth: FirebaseAuth = Firebase.auth
    private var db = Firebase.firestore

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

        val listaPanieri = obtainPanieri(points) {
            panieriMutableLiveData.setValue(it)

            Log.d(TAG, "Ho assegnato il valore all'oggetto osservato." +
                    " La dimensione della lista dei panieri è: " + it.size.toString())

            onComplete(panieriMutableLiveData)
        }

    }

    fun obtainPanieri(points: Long, onComplete: (result: ArrayList<Paniere>) -> Unit) {
        var panieri : ArrayList<Paniere> = arrayListOf(Paniere())

        //Riferimento alla collection "panieri"
        val panieriRef = db.collection("panieri")

        //Query che prende tutti i panieri perché non abbiamo il cazzo di valore
        //del paniere nel documento
        panieriRef.get()
            .addOnSuccessListener { documents ->

                //Per ogni documento ottenuto
                for (document in documents) {

                    //Se non è presente il campo "abbinamento" allora non è stato abbinato
                    //e se l'utente donatore non è lo stesso che sta ora cercando un paniere
                    if (document.data?.get("abbinamento") == null && (document.data?.get("donatore").toString() != auth.currentUser?.uid.toString())) {

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

                        Log.d(TAG, "Paniere creato")

                        //Se il paniere creato ha un valore inferiore ai punti rimanenti all'utente
                        //lo aggiungo alla lista dei panieri
                        if (paniereTemp.calcolaValore() < points) {
                            panieri.add(paniereTemp)
                            Log.d(TAG, "Paniere aggiunto ad i panieri visualizzabili.")
                        }
                    }
                }
                onComplete(panieri)
            }
            .addOnFailureListener() { exception ->
                Log.e(TAG, "Non sono riuscito a fare la query sui Panieri perché sono un perdente")
            }
        //onComplete(panieri)
    }

    fun getPaniere(idPaniere: String){
        TODO()
    }

    fun getStoricoPanier(idDonatore: String){
        TODO()
    }

}