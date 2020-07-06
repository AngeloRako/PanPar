package com.rapnap.panpar.view

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.rapnap.panpar.R
import com.rapnap.panpar.model.Paniere
import com.rapnap.panpar.model.Utente
import com.rapnap.panpar.viewmodel.ProfileRiceventeViewModel
import kotlinx.android.synthetic.main.fragment_profile_donatore.switchRoleBtn
import kotlinx.android.synthetic.main.fragment_profile_ricevente.*


class ProfileRiceventeFragment : Fragment(R.layout.fragment_profile_ricevente) {

    private val prvm: ProfileRiceventeViewModel by viewModels()
    private var backPressedTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Override del metodo onBackPressed affinché esca dall'applicazione quando tappo due volte indietro.
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    activity?.finish()
                } else {
                    Toast.makeText(activity?.applicationContext, "Premi indietro di nuovo per uscire", Toast.LENGTH_SHORT).show()
                }
                backPressedTime = System.currentTimeMillis()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_ricevente, container, false)
    }

    override fun onStart() {
        super.onStart()

        //Callback relativa al pulsante switchRoleBtn: permette il passaggio all'activity Donatore
        //Inoltre invoca il metodo changeRole affinché l'utente possa veder cambiata la sua tipologia
        switchRoleBtn.setOnClickListener{
            Navigation.findNavController(this.requireView()).navigate(R.id.HRtoHD)
            prvm.changeRole()
            this.activity?.finish()
        }

        listaPanieriBtn.setOnClickListener{
            Navigation.findNavController(this.requireView()).navigate(R.id.homeRiceventeToListaPanieri)
        }

        //Metodo utilizzato per mostrare il punteggio residuo dell'utente ricevente.
        mostraPunteggio()

        funzioneTest()
    }

    fun funzioneTest(){
        prvm.funzionetestVM().observe(this, Observer<ArrayList<Paniere>>{
            it.forEach {
                Log.d(TAG, "L'ID DEL PANIERE E': ${it.id}")
            }
        })
    }

    //Imposta il punteggio del donatore nella Label se questo è cambiato. Viene utilizzato il pattern Observer affinché
    //possa appunto "osservare" i cambiamenti che vengono effettuati su un certo oggetto.
    fun mostraPunteggio() {
        prvm.obtainPuntiRicevente().observe(this, Observer<Utente>{
            var punteggio = it.punteggio
            puntiLabel.setText("Hai a disposizione: " + punteggio + " Punti")
        })
    }
}