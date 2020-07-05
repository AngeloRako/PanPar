package com.rapnap.panpar.repository

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rapnap.panpar.model.Tipologia
import com.rapnap.panpar.model.Utente

class UtenteRepository {

    private var auth: FirebaseAuth = Firebase.auth
    private var db = Firebase.firestore

    //Metodo per il cambiamento di Tipologia Utente: viene effettuata una Update utilizzando come
    //id documento, l'id utente prelevato da quello loggato.
    fun updateUserType(tipologia: String){
        val currentUser = auth.currentUser
        val uiid: String = currentUser?.uid?: "samba"

        db.collection("users")
            .document(uiid)
            .update("type", tipologia)
    }

    //Metodo per ottenere l'utente ed i suoi attributi. La query viene sempre effettuata utilizzando
    //come id quello prelevato Firebase Authentication. Inoltre, per assicurarmi di effettuare
    //qualsiasi operazione sull'utente prelevato prima che finisca la query, passo come parametro di
    //ingresso, una onComplete.
    fun getUser(onComplete: (Utente) -> Unit){

        val currentUser = auth.currentUser
        val uiid: String = currentUser?.uid?: "samba"
        val user = Utente(id = uiid)

        val userRef = db.collection("users").document(uiid)
        userRef.get()
            .addOnSuccessListener { document ->
                if(document.data != null){
                    val type = document.data?.get("type") as String
                    user.tipo = Tipologia.valueOf(type)
                    val location = document.data?.get("location") as GeoPoint
                    user.location = location.toString()
                    user.punteggio = document.data?.get("punteggio") as Long
                    user.rating = document.data?.get("rating") as Double
                    onComplete(user)
                } else {
                    Log.d(ContentValues.TAG, "User data not found! ${document?.data}")
                }
            }
    }

}