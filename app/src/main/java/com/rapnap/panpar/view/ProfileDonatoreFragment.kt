package com.rapnap.panpar.view

import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.rapnap.panpar.R
import com.rapnap.panpar.model.Utente
import com.rapnap.panpar.viewmodel.ProfileDonatoreViewModel
import kotlinx.android.synthetic.main.fragment_profile_donatore.*
import kotlinx.android.synthetic.main.fragment_profile_donatore.view.*


class ProfileDonatoreFragment : Fragment() {

    //Istanzio alcune variabili utili per: prelevare i dati dall'account Google loggato
    private val pdvm: ProfileDonatoreViewModel by viewModels()
    private var backPressedTime = 0L
    private lateinit var acct: GoogleSignInAccount

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
        val view = inflater.inflate(R.layout.fragment_profile_donatore, container, false)

        //Imposto onClickListener sul bottone di creazione paniere
        view.donateBtn.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.donatoreToNuovoPaniere)
        }

        return view
    }


    override fun onStart() {
        super.onStart()

        //Invoco il metodo mostraRating per settare la ratingBar
        impostaRating()

        //Ottengo l'oggetto relativo all'ultimo utente loggato
        acct = GoogleSignIn.getLastSignedInAccount(this.activity)!!

        //Visualizzo una Label personalizzata per l'utente loggato comprensiva di Nome e Cognome
        //Si presuppone che il Donatore non debba per forza rimanere nell'anonimato
        labelHomeDonatore1.text = Html.fromHtml("Salve Donatore " + "<b>" + getName(acct) + "</b>" + "," + "<br>" + "di seguito il resoconto delle tue azioni:")

        //Visualizzo con una WebView l'immagine del profilo dell'utente loggato in Google.
        //L'immagine è prelevata in termini di URI, che viene castano a String affinché sia
        //compatibile con il parametro di ingresso di loadUrl
        profiloDonatore.loadUrl(getPhoto(acct).toString())
        //Navigation.findNavController(this.requireView()).navigate(R.id.donatoreToNuovoPaniere)

        //Callback relativa al pulsante switchRoleBtn: permette il passaggio all'activity Ricevente
        //Inoltre invoca il metodo changeRole affinché l'utente possa veder cambiata la sua tipologia
        switchRoleBtn.setOnClickListener{
            Navigation.findNavController(requireView()).navigate(R.id.HDtoHR)
            pdvm.changeRole()
            this.activity?.finish()
        }
    }

    //Imposta il rating del donatore nella ratingBar se questo è cambiato. Viene utilizzato il pattern Observer affinché
    //possa appunto "osservare" i cambiamenti che vengono effettuati su un certo oggetto.
    fun impostaRating() {
        pdvm.obtainRatingDonatore().observe(this, Observer<Utente>{
            ratingBar.rating = it.rating.toFloat()
        })
    }

    //Metodo relativo all'ottenimento del nome utente
    fun getName(account: GoogleSignInAccount): String? {
        return pdvm.obtainNameFromGoogle(account)
    }

    //Metodo relativo all'ottenimento dell'immagine in termini del suo URI
    fun getPhoto(account: GoogleSignInAccount): Uri? {
        return pdvm.obtainImageFromGoogle(account)
    }
}