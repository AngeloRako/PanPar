package com.rapnap.panpar.repository

import android.content.ContentValues
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rapnap.panpar.model.Paniere
import java.text.SimpleDateFormat
import java.util.*

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

}