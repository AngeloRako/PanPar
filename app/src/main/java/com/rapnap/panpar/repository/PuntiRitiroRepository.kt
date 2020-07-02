package com.rapnap.panpar.repository

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.location.Location
import android.util.Log
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rapnap.panpar.model.PuntoRitiro
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.extension.getAtLocation
import org.imperiumlabs.geofirestore.extension.setLocation
import kotlin.collections.ArrayList

class PuntiRitiroRepository {

    private var db = Firebase.firestore

    fun getPuntiDiRitiro(
        location: Location,
        maxDistance: Double,
        onComplete: (List<PuntoRitiro>) -> Unit
    ) {

        val punti = ArrayList<PuntoRitiro>()

        val puntiRef = db.collection("punti_ritiro")
        val geoFirestore = GeoFirestore(puntiRef)

        /*
        //Inserimento dei dati aggiuntivi per geolocalizzazione nel db del singolo punto di ritiro
        geoFirestore.setLocation("AuSfhM7M7U1UIjBcMK6q", GeoPoint(40.643396, 14.865041)) { exception ->
            if (exception == null){
                Log.d(TAG, "Location saved on server successfully!")
        }  else {
            Log.d(TAG, "An error has occurred: $exception")
        }
        */

        geoFirestore.getAtLocation(
            GeoPoint(location.latitude, location.longitude),
            maxDistance
        ) { docs, ex ->

            if (ex != null) {
                Log.e(TAG, "onError: ", ex)
                return@getAtLocation
            } else {

                docs?.forEach {

                    val location = Location("")

                    val a = it?.getGeoPoint("l")
                    location.latitude = a!!.latitude
                    location.longitude = a!!.longitude

                    val res = PuntoRitiro(
                        id = it?.getString("id")!!,
                        indirizzo = it?.getString("address")!!,
                        nome = it?.getString("name")!!,
                        location = location
                    )

                    Log.d(TAG, "[PANPAR] Leggo: $res.toString()")

                    punti.add(res)

                }

                onComplete(punti)

            }
        }


    }

    //Funzione per aggiungere un nuovo punto di ritiro
    fun createtLocationPunto(punto: PuntoRitiro, onComplete: ()->Unit) {

        val puntiRef = db.collection("punti_ritiro")
        val geoFirestore = GeoFirestore(puntiRef)

        //Insert nel db di un nuovo paniere
        val puntoData = hashMapOf(
            "id" to "bocc",
            "name" to punto.nome,
            "address" to punto.indirizzo
        )

        //New document with a generated ID
        puntiRef
            .add(puntoData)
            .addOnSuccessListener { documentReference ->
                Log.d(ContentValues.TAG, "Paniere Document added with ID: ${documentReference.id}")

                //Inserimento dei dati aggiuntivi per geolocalizzazione nel db del singolo punto di ritiro
                geoFirestore.setLocation(
                    documentReference.id ,
                    GeoPoint(40.643396, 14.865041)
                ) { exception ->
                    if (exception == null) {
                        Log.d(TAG, "Location saved on server successfully!")
                        onComplete()
                    } else {
                        Log.d(TAG, "An error has occurred: $exception")
                    }

                }

            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)

                //onFailure()

            }


    }
}
