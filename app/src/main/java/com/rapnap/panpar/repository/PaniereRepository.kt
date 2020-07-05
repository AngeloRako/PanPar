package com.rapnap.panpar.repository

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QueryDocumentSnapshot
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

    fun getListaPanieri(location: String){
        TODO()
    }



    fun getPaniere(idPaniere: String){
        TODO()
    }

    fun getStoricoPanier(idDonatore: String){
        TODO()
    }

    //Metodo per ottenere la lista dei panieri relativa ai Donatori, se appunto tipologia = "donatore",
    //mentre relativa invece ai riceventi, se tipologia = "ricevente".
    //Per il momento mostrerò tutti i panieri che ho richiesto e mostrerò quindi anche i panieri richiesti
    //ma che sono stati assegnati a qualche altro utente. Poi in un secondo momento, se tra questi
    //panieri sono presenti anche quelli che ho vinto, basta controllare il campo apposito
    //e vedere se l'id è presente.
    fun getListaPanieriPerTipologia(tipologia: String, onComplete: (result: ArrayList<Paniere>) -> Unit){
        val panieri: ArrayList<Paniere> = arrayListOf(Paniere())
        val panieriRef: CollectionReference = db.collection("panieri")
        val uiid = auth.currentUser?.uid ?: "!!!!!!"

        when(tipologia){

            "donatore" -> {
                panieriRef
                    .whereEqualTo("donatore", uiid)
                    .get()
                    .addOnSuccessListener {
                        for (document: QueryDocumentSnapshot in it) {

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

                            panieri.add(paniereTemp)
                            Log.d(TAG, "Paniere aggiunto alla lista panieri donatore")

                            panieri.removeAt(0)
                            onComplete(panieri)
                        }
                    }
                    .addOnFailureListener() { exception ->
                        Log.e(TAG, "Nessun paniere relativo relativo ai donatori !")
                    }
            }

            "ricevente" -> {

                panieriRef
                    .whereArrayContains("riceventi", uiid)
                    .get()
                    .addOnSuccessListener {
                        for (document: QueryDocumentSnapshot in it) {

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

                            panieri.add(paniereTemp)
                            Log.d(TAG, "Paniere aggiunto alla lista panieri ricevente")
                            panieri.removeAt(0)

                            onComplete(panieri)
                        }
                    }
                    .addOnFailureListener() { exception ->
                        Log.e(TAG, "Nessun paniere relativo relativo ai riceventi !")
                    }
            }
        }
    }

}