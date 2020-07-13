package com.rapnap.panpar.view

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.rapnap.panpar.R
import com.rapnap.panpar.model.Utente
import com.rapnap.panpar.viewmodel.WelcomeViewModel


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

                welcomeVM.user.observe(this, Observer<Utente>{

                    Log.d(ContentValues.TAG, "[WF] IT è: ${it.toString()}")

                    if(it.isNew){
                        Log.d(ContentValues.TAG, "[WF] L'utente è nuovo")
                        Navigation.findNavController(this.requireView()).navigate(R.id.welcomeToSignUp)
                    }
                    else{
                        when(it.tipo){

                            Utente.Tipologia.RICEVENTE -> {

                                //Carica home Ricevente
                                Navigation.findNavController(this.requireView()).navigate(R.id.welcomeToRicevente)
                                this.activity?.finish()
                            }

                            Utente.Tipologia.DONATORE -> {
                                //Carica home Donatore
                                Navigation.findNavController(this.requireView()).navigate(R.id.welcomeToDonatore)
                                this.activity?.finish()

                            }
                        }
                    }
                })
            }
        }

    }

}