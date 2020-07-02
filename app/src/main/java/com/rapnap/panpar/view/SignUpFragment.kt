package com.rapnap.panpar.view

import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.rapnap.panpar.R
import com.rapnap.panpar.model.Tipologia
import com.rapnap.panpar.model.Utente
import com.rapnap.panpar.viewmodel.SignInViewModel
import com.rapnap.panpar.viewmodel.SignUpViewModel
import kotlinx.android.synthetic.main.fragment_sign_in.*
import kotlinx.android.synthetic.main.fragment_sign_up.*


class SignUpFragment : Fragment() {

    private val signUpVM: SignUpViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)
        return view

    }

    override fun onStart() {
        super.onStart()

        registerAsDonatoreBtn.setOnClickListener{
            registerAs(Tipologia.DONATORE)
        }

        registerAsRiceventeBtn.setOnClickListener{
            registerAs(Tipologia.RICEVENTE)
        }

    }


    fun registerAs(tipologia: Tipologia){

        signUpVM.registerAs(tipologia)


        signUpVM.getUser().observe(this, Observer<Utente>{

            when(it.tipo){

                Tipologia.RICEVENTE -> {

                    //Carica home Ricevente
                    Navigation.findNavController(this.requireView()).navigate(R.id.signUpToRicevente)
                    this.activity?.finish()

                }

                Tipologia.DONATORE -> {

                    //Carica home Donatore
                    Navigation.findNavController(this.requireView()).navigate(R.id.signUpToDonatore)
                    this.activity?.finish()
                }
            }
        })
    }

}