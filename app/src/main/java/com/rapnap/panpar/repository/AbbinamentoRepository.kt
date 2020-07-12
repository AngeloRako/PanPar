package com.rapnap.panpar.repository

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rapnap.panpar.model.Abbinamento

class AbbinamentoRepository {
    private var auth: FirebaseAuth = Firebase.auth
    private var db = Firebase.firestore

    fun getAbbinamenti(onComplete: (ArrayList<Abbinamento>) -> Unit) {
        var thisAbbinamenti = ArrayList<Abbinamento>()

        db.collection("abbinamenti").whereEqualTo("id_ricevente", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { documents ->

                if (documents != null) {
                    for (document in documents) {
                        thisAbbinamenti.add(Abbinamento(
                            ricevente = document.data?.get("id_ricevente") as String,
                            donatore = document.data?.get("id_donatore") as String,
                            paniere = document.data?.get("id_paniere") as String,
                            codiceSegreto = document.data?.get("codice_segreto") as String
                        ))
                    }
                    onComplete(thisAbbinamenti)
                }
            }
            .addOnFailureListener() {
                Log.d(TAG, "Paniere non abbinato")
            }
    }
}