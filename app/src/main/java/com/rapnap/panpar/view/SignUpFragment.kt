package com.rapnap.panpar.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.rapnap.panpar.R
import com.rapnap.panpar.model.Utente
import com.rapnap.panpar.viewmodel.SignUpViewModel
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
            registerAs(Utente.Tipologia.DONATORE)
        }

        registerAsRiceventeBtn.setOnClickListener{
            registerAs(Utente.Tipologia.RICEVENTE)
        }

    }


    fun registerAs(tipologia: Utente.Tipologia){

        signUpVM.registerAs(tipologia)


        signUpVM.user.observe(this, Observer<Utente>{

            when(it.tipo){

                Utente.Tipologia.RICEVENTE -> {

                    //Carica home Ricevente
                    Navigation.findNavController(this.requireView()).navigate(R.id.signUpToRicevente)
                    this.activity?.finish()

                }

                Utente.Tipologia.DONATORE -> {

                    //Carica home Donatore
                    Navigation.findNavController(this.requireView()).navigate(R.id.signUpToDonatore)
                    this.activity?.finish()
                }
            }
        })
    }

}