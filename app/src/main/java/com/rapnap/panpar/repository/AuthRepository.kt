package com.rapnap.panpar.repository

import android.content.ContentValues.TAG
import android.location.Location
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rapnap.panpar.model.Utente
import java.util.*


class AuthRepository {

    //Accedo alle istanze dei Singleton
    private var auth: FirebaseAuth = Firebase.auth
    private var db = Firebase.firestore

    fun firebaseSignInWithGoogle(credential: AuthCredential, onComplete: (result: Utente) -> Unit) {

         auth.signInWithCredential(credential)
             .addOnCompleteListener { task ->
                 if (task.isSuccessful) {
                     // Sign in success
                     val isNewUser = task.result?.additionalUserInfo?.isNewUser?: true

                     obtainUser(isNewUser){

                         Log.d(TAG, "[AuthRep] User obtained: ${it.toString()}")

                         onComplete(it)
                     }

                 } else {
                     // If sign in fails, display a message to the user.
                     Log.d(TAG, "[AuthRep] Error during query")
                 }
             }
     }

    fun firebaseCompleteSignUp(tipologia: Utente.Tipologia, position: Location? = null, onComplete: (result: Utente) -> Unit) {

        val uiid = auth.currentUser?.uid ?: "!!!!!!"
        //Insert nel db
        val userData = hashMapOf(
            "id" to uiid,
            "data" to Timestamp(Date()),
            "location" to GeoPoint(77.0, 77.0),
            "type" to tipologia.toString(),
            "rating" to 0.5,
            "punteggio" to 1000
        )

        //New document
        db.collection("users")
            .document(uiid)
            .set(userData)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${uiid}")

                val user = Utente(userData.get("id") as String)

                val a = userData.get("location") as GeoPoint
                user.location = a.toString()
                user.tipo = tipologia
                user.isNew = false

                onComplete(user)

            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }

    }

    fun login(onComplete: (result: Utente) -> Unit) {

        obtainUser(false){

            Log.d(TAG, "[LOGIN] User obtained: ${it}")
            onComplete(it)
        }
    }

    fun isLoggedIn(): Boolean {

        return (auth.currentUser != null)

    }

    private fun obtainUser(isNew: Boolean, onComplete: (result: Utente) -> Unit) {

        val currentUser = auth.currentUser
        val uiid: String = currentUser?.uid ?: "!!!!!"
        val user = Utente(id = uiid)

        if(!isNew){

            val userRef = db.collection("users").document(uiid)

            // Controlla se sul db c'è la riga
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document.data != null) {

                        //Se c'è chiedo a firebase i dettagli aggiuntivi e li metto in user
                        Log.d(TAG, "User data found: ${document.data}")

                        val type = document.data?.get("type") as String

                        user.tipo = Utente.Tipologia.valueOf(type)
                        val location = document.data?.get("location") as GeoPoint
                        user.location = location.toString()
                        user.punteggio = document.data?.get("punteggio") as Long
                        user.rating = document.data?.get("rating") as Double
                        user.isNew = false
                        Log.d(TAG, "User data created: ${user.toString()}")

                        onComplete(user)
                    } else {
                        //Se non c'è si tratta di un nuovo utente (registrazione da completare)
                        Log.d(TAG, "User data not found! ${document?.data}")
                        user.isNew = true
                        onComplete(user)

                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        } else{
            onComplete(user)
        }

    }
}