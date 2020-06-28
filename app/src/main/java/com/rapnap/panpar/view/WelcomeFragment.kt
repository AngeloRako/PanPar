package com.rapnap.panpar.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rapnap.panpar.R


class WelcomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }


    override fun onStart() {
        super.onStart()

        // Initialize Firebase Auth
        this.auth = Firebase.auth

        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = auth.currentUser

        if(currentUser == null){
            //Carica fragment Sign In
            Navigation.findNavController(this.requireView()).navigate(R.id.welcomeToSignIn)
        }
        else {
            //Passa avanti


            //TODO
            //Controlla su viewmodel e passa avanti con criterio

            Navigation.findNavController(this.requireView()).navigate(R.id.welcomeToDonatore)

        }

    }

}