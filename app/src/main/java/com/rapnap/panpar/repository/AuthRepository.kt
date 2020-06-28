package com.rapnap.panpar.repository

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rapnap.panpar.model.Utente

class AuthRepository {

    private var auth: FirebaseAuth = Firebase.auth

     fun firebaseSignInWithGoogle(credential: AuthCredential): MutableLiveData<Utente> {
         val authenticatedUserMutableLiveData = MutableLiveData<Utente>()

         auth.signInWithCredential(credential)
             .addOnCompleteListener { task ->
                 if (task.isSuccessful) {
                     // Sign in success
                     val isNewUser: Boolean = task.result?.additionalUserInfo?.isNewUser?: true

                     val currentUser = auth.currentUser

                     val uid: String = currentUser?.uid ?: ""
                     val user = Utente(id = uid)

                     user.isNew = isNewUser

                     if(!isNewUser){

                        //Chiedo a firebase i dettagli aggiuntivi e li metto in user

                     }

                     authenticatedUserMutableLiveData.setValue(user)

                 } else {
                     // If sign in fails, display a message to the user.
                 }
             }

         return authenticatedUserMutableLiveData

     }

    fun firebaseCompleteSignUp() {

        TODO()

    }

}