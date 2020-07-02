package com.rapnap.panpar.view

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rapnap.panpar.R
import com.rapnap.panpar.model.Tipologia
import com.rapnap.panpar.model.Utente
import com.rapnap.panpar.viewmodel.SignUpViewModel
import com.rapnap.panpar.viewmodel.WelcomeViewModel
import kotlinx.android.synthetic.main.activity_home_donatore.*


class WelcomeFragment : Fragment() {

    private val welcomeVM: WelcomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_welcome, container, false)

        return view
    }


    override fun onStart() {
        super.onStart()


        //Controllo se utente da viewmodel è nuovo, allora vado a signUp per completare la registrazione

        if(!welcomeVM.isLoggedIn()){
            //Carica fragment Sign In
            Navigation.findNavController(this.requireView()).navigate(R.id.welcomeToSignIn)
        }
        else {

            welcomeVM.login(){

                welcomeVM.getUser().observe(this, Observer<Utente>{

                    Log.d(ContentValues.TAG, "[WF] IT è: ${it.toString()}")

                    if(it.isNew){
                        Log.d(ContentValues.TAG, "[WF] L'utente è nuovo")
                        Navigation.findNavController(this.requireView()).navigate(R.id.welcomeToSignUp)
                    }
                    else{
                        when(it.tipo){

                            Tipologia.RICEVENTE -> {

                                //Carica home Ricevente
                                Navigation.findNavController(this.requireView()).navigate(R.id.welcomeToRicevente)
                                //this.activity?.finish()
                            }

                            Tipologia.DONATORE -> {
                                //Carica home Donatore
                                Navigation.findNavController(this.requireView()).navigate(R.id.welcomeToDonatore)
                                //this.activity?.finish()

                            }
                        }
                    }
                })
            }
        }

    }

}