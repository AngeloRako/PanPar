package com.rapnap.panpar.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.rapnap.panpar.R
import com.rapnap.panpar.model.Utente
import com.rapnap.panpar.viewmodel.SignInViewModel
import kotlinx.android.synthetic.main.fragment_sign_in.view.*

class SignInFragment : Fragment() {

    private val signInVM: SignInViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                activity?.finish()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this.requireActivity(), gso)

        view.logInBtn.setOnClickListener{
            startSignInFlow()
        }

        return view
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
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                //
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        //Comunica con ViewModel
        signInVM.signInWithGoogle(credential){

            signInVM.user.observe(this, Observer<Utente>{

                //Aggiorna UI in base all'utente
                when(it.isNew){

                    true -> {
                        //Carica fragment relativo alle info aggiuntive per la creazione (ruolo, posizione)
                        Navigation.findNavController(this.requireView()).navigate(R.id.signInToSignUp)
                    }
                    false -> {

                        var id = 0

                        when(it.tipo){

                            Utente.Tipologia.DONATORE -> id = R.id.signInToDonatore
                            Utente.Tipologia.RICEVENTE -> id = R.id.signInToRicevente
                        }
                        Navigation.findNavController(this.requireView()).navigate(id)
                        this.activity?.finish()
                    }
                }
            })
        }
    }
}