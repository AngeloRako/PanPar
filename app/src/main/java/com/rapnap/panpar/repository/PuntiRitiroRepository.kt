package com.rapnap.panpar.repository

import android.content.ContentValues.TAG
import android.location.Location
import android.util.Log
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rapnap.panpar.model.PuntoRitiro
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.extension.getAtLocation

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

        geoFirestore.getAtLocation(
            GeoPoint(location.latitude, location.longitude),
            maxDistance
        ) { docs, ex ->

            if (ex != null) {
                Log.e(TAG, "onError: ", ex)
                return@getAtLocation
            } else {

                docs?.forEach {

                    val a = it.getGeoPoint("l")
                    val location = Location("").apply{
                        this.latitude = a!!.latitude
                        this.longitude = a!!.longitude
                    }

                    val res = PuntoRitiro(
                        id = it.getString("id")!!,
                        indirizzo = it.getString("address")!!,
                        nome = it.getString("name")!!,
                        location = location
                    )

                    Log.d(TAG, "[PANPAR] Leggo: $res.toString()")

                    punti.add(res)

                }
                onComplete(punti)
            }
        }


    }

    /*
    //DEBUG
    //Funzione per aggiungere un nuovo punto di ritiro
    fun createLocationPunto(punto: PuntoRitiro, onComplete: ()->Unit) {

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
                Log.d(TAG, "Paniere Document added with ID: ${documentReference.id}")

                //Inserimento dei dati aggiuntivi per geolocalizzazione nel db del singolo punto di ritiro
                geoFirestore.setLocation(
                    documentReference.id ,
                    GeoPoint(punto.location.latitude, punto.location.longitude)
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
                Log.w(TAG, "Error adding document", e)

                //onFailure()

            }
    }
     */
}
