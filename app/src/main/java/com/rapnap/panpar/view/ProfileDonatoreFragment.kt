package com.rapnap.panpar.view

import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.CornerFamily
import com.rapnap.panpar.R
import com.rapnap.panpar.adapter.PanieriSinteticiAdapter
import com.rapnap.panpar.model.Paniere
import com.rapnap.panpar.model.Utente
import com.rapnap.panpar.viewmodel.ProfileDonatoreViewModel
import kotlinx.android.synthetic.main.fragment_profile_donatore.*
import kotlinx.android.synthetic.main.fragment_profile_donatore.view.*


class ProfileDonatoreFragment : Fragment() {

    //Istanzio alcune variabili utili per: prelevare i dati dall'account Google loggato
    private val pdvm: ProfileDonatoreViewModel by viewModels()
    private var backPressedTime = 0L
    private lateinit var acct: GoogleSignInAccount
    private lateinit var adapter: PanieriSinteticiAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private val radius = 50F
    private lateinit var cardView: MaterialCardView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.elevation = 0F

        //Override del metodo onBackPressed affinché esca dall'applicazione quando tappo due volte indietro.
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    activity?.finish()
                } else {
                    Toast.makeText(
                        activity?.applicationContext,
                        "Premi indietro di nuovo per uscire",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                backPressedTime = System.currentTimeMillis()
            }
        })

        //Attivo menu opzioni
        setHasOptionsMenu(true)

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

        //view.lista_panieri_ricevente_view.shapeAppearanceModel = ShapeAppearanceModel(lista_panieri_ricevente_view.shapeAppearanceModel.toBuilder()).set

        cardView = view.lista_panieri_donatore_view
        cardView.setShapeAppearanceModel(
            cardView.getShapeAppearanceModel()
                .toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, radius)
                .setTopRightCorner(CornerFamily.ROUNDED, radius)
                .setBottomRightCorner(CornerFamily.ROUNDED, 0F)
                .setBottomLeftCorner(CornerFamily.ROUNDED, 0F)
                .build()
        )

        bottomSheetBehavior = BottomSheetBehavior.from(view.lista_panieri_donatore_view)
        bottomSheetBehavior.peekHeight = resources.configuration.screenHeightDp
        bottomSheetBehavior.addBottomSheetCallback(BottomSheetListener())

        return view
    }

    override fun onStart() {
        super.onStart()

        pdvm.obtainDonatore()

        //Osservo il donatore fornito dal ViewModel per aggiornare l'UI
        pdvm.donatore.observe(this, Observer<Utente> {
            ratingBar.rating = it.rating.toFloat()
        })

        //Ottengo l'oggetto relativo all'ultimo utente loggato
        acct = GoogleSignIn.getLastSignedInAccount(this.activity)!!

        //Visualizzo una Label personalizzata per l'utente loggato comprensiva di Nome e Cognome
        //Si presuppone che il Donatore non debba per forza rimanere nell'anonimato
        labelHomeDonatore1.text =
            Html.fromHtml("Salve Donatore " + "<b>" + getName(acct) + "</b>" + "," + "<br>" + "di seguito il resoconto delle tue azioni:")

        //Visualizzo con una WebView l'immagine del profilo dell'utente loggato in Google.
        //L'immagine è prelevata in termini di URI, che viene castato a String
        Glide.with(this).load(getPhoto(acct).toString()).into(profilePic)


        //Configura recycler view
        linearLayoutManager = LinearLayoutManager(this.activity)
        lista_panieri_donatore.layoutManager = linearLayoutManager
        adapter = PanieriSinteticiAdapter(ArrayList<Paniere>(), Utente.Tipologia.DONATORE)
        lista_panieri_donatore.adapter = adapter

        pdvm.obtainPanieri()
        pdvm.panieriDonatore.observe(requireActivity(), Observer<ArrayList<Paniere>> {

            (lista_panieri_donatore.adapter as PanieriSinteticiAdapter).setData(it)

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

    //Menu opzioni
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profilo_donatore_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

        R.id.go_to_ricevente -> {

            //Permette il passaggio all'activity Ricevente
            //Inoltre invoca il metodo changeRole affinché l'utente possa veder cambiata la sua tipologia
            Navigation.findNavController(requireView()).navigate(R.id.HDtoHR)
            pdvm.changeRole()
            this.activity?.finish()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    inner class BottomSheetListener: BottomSheetBehavior.BottomSheetCallback() {

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            //Log.d(TAG, "ONSLIDE DICE: ${slideOffset}")

            val newValue = radius*(1-slideOffset)

            cardView.setShapeAppearanceModel(
                cardView.getShapeAppearanceModel()
                    .toBuilder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, newValue)
                    .setTopRightCorner(CornerFamily.ROUNDED, newValue)
                    .setBottomRightCorner(CornerFamily.ROUNDED, 0F)
                    .setBottomLeftCorner(CornerFamily.ROUNDED, 0F)
                    .build()
            )



        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {

            when(newState){

                BottomSheetBehavior.STATE_EXPANDED -> {

                TransitionManager.beginDelayedTransition(lista_panieri_donatore_view, AutoTransition().apply{duration = 150})
                titolo_lista.visibility = View.GONE
                    (requireActivity() as AppCompatActivity).supportActionBar?.title = "Panieri donati"
            }
                BottomSheetBehavior.STATE_DRAGGING -> {
                    TransitionManager.beginDelayedTransition(lista_panieri_donatore_view, AutoTransition())
                    titolo_lista.visibility = View.VISIBLE
                    (requireActivity() as AppCompatActivity).supportActionBar?.title = "Panpar"
                }
            }

        }

    }



}



