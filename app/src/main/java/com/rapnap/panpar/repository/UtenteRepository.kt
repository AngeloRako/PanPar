package com.rapnap.panpar.repository

import android.content.ContentValues
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rapnap.panpar.model.Tipologia
import com.rapnap.panpar.model.Utente
import kotlinx.coroutines.tasks.await

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
    suspend fun getUser(): Utente?{

        val currentUser = auth.currentUser
        val uiid: String = currentUser?.uid?: "samba"
        val user = Utente(id = uiid)

        try{
        val userRef = db.collection("users").document(uiid)
        val document = userRef.get().await()
            if(document.data != null){
                    val type = document.data?.get("type") as String
                    user.tipo = Tipologia.valueOf(type)
                    val location = document.data?.get("location") as GeoPoint
                    user.location = location.toString()
                    user.punteggio = document.data?.get("punteggio") as Long
                    user.rating = document.data?.get("rating") as Double
                    return user
            }else{
                Log.d(ContentValues.TAG, "Error fetching user: User not found")
                return null
            }
        }catch (e: Exception){
            Log.d(ContentValues.TAG, "Error fetching user: ${e.localizedMessage}")
            return null
        }
    }

}