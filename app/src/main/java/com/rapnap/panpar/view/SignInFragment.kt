package com.rapnap.panpar.view

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rapnap.panpar.R
import com.rapnap.panpar.model.Tipologia
import com.rapnap.panpar.model.Utente
import com.rapnap.panpar.viewmodel.SignInViewModel
import kotlinx.android.synthetic.main.fragment_sign_in.*
import kotlin.math.sign

class SignInFragment : Fragment() {

    private lateinit var signInVM: SignInViewModel
    //private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)

        //Obtain viewmodel
        val sm : SignInViewModel by viewModels()
        signInVM = sm

        // Initialize Firebase Auth
        //this.auth = Firebase.auth

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this.requireActivity(), gso)

        return view
    }

    override fun onStart() {
        super.onStart()

        logInBtn.setOnClickListener{
            startSignInFlow()
        }

    }

    private fun startSignInFlow(){

        val signInIntent = googleSignInClient.getSignInIntent()
        this.startActivityForResult(signInIntent, 1492)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 1492) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                label.setText("firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                label.setText("Google sign in failed")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        //Comunica con ViewModel

        signInVM.signInWithGoogle(credential)

        signInVM.getUser().observe(this, Observer<Utente>{
                //Aggiorna UI in base all'utente

            when(it.isNew){

                true -> {

                    //Carica fragment relativo alle info aggiuntive per la creazione (ruolo, posizione)

                }

                false -> {

                    var id = 0

                    when(it.tipo){

                        Tipologia.DONATORE -> id = R.id.signInToDonatore
                        Tipologia.RICEVENTE -> id = R.id.signInToRicevente

                    }
                    Navigation.findNavController(this.requireView()).navigate(id)
                }

            }

        })


//        auth.signInWithCredential(credential)
//            .addOnCompleteListener(this.requireActivity()) { task ->
//                if (task.isSuccessful) {
//                    // Sign in success
//                    Navigation.findNavController(this.requireView()).navigate(R.id.signInToDonatore)
//                } else {
//                    // If sign in fails, display a message to the user.
//                    label.setText("signInWithCredential:failure")
//                    Snackbar.make(this.requireView(), "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
//                }
//            }
    }


}