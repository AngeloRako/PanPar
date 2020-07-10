package com.rapnap.panpar.view

import android.os.Bundle
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.CornerFamily
import com.rapnap.panpar.R
import com.rapnap.panpar.adapter.PanieriSinteticiAdapter
import com.rapnap.panpar.model.Paniere
import com.rapnap.panpar.model.Tipologia
import com.rapnap.panpar.model.Utente
import com.rapnap.panpar.viewmodel.ProfileRiceventeViewModel
import kotlinx.android.synthetic.main.fragment_profile_ricevente.*
import kotlinx.android.synthetic.main.fragment_profile_ricevente.view.*


class ProfileRiceventeFragment : Fragment(R.layout.fragment_profile_ricevente) {

    private val prvm: ProfileRiceventeViewModel by viewModels()
    private var backPressedTime = 0L
    private lateinit var adapter: PanieriSinteticiAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private val radius = 50F
    private lateinit var cardView: MaterialCardView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        val view=  inflater.inflate(R.layout.fragment_profile_ricevente, container, false)

        cardView = view.lista_panieri_ricevente_view
        cardView.setShapeAppearanceModel(
            cardView.getShapeAppearanceModel()
                .toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, radius)
                .setTopRightCorner(CornerFamily.ROUNDED, radius)
                .setBottomRightCorner(CornerFamily.ROUNDED, 0F)
                .setBottomLeftCorner(CornerFamily.ROUNDED, 0F)
                .build()
        )

        bottomSheetBehavior = BottomSheetBehavior.from(view.lista_panieri_ricevente_view)
        val height = resources.configuration.screenHeightDp
        bottomSheetBehavior.peekHeight = (height * 1.8).toInt()
        bottomSheetBehavior.addBottomSheetCallback(BottomSheetListener())


        return view
    }

    override fun onStart() {
        super.onStart()

        listaPanieriBtn.setOnClickListener {
            Navigation.findNavController(this.requireView())
                .navigate(R.id.homeRiceventeToListaPanieri)
        }

        //Metodo utilizzato per mostrare il punteggio residuo dell'utente ricevente.
        mostraPunteggio()

        (activity as AppCompatActivity).supportActionBar?.elevation = 0F


        //Configuro Adapter
        linearLayoutManager = LinearLayoutManager(this.activity)
        lista_panieri_ricevente.layoutManager = linearLayoutManager
        adapter = PanieriSinteticiAdapter(ArrayList<Paniere>(), Tipologia.RICEVENTE)
        lista_panieri_ricevente.adapter = adapter

        //Osservo i dati del view model
        prvm.obtainPanieri()
        prvm.panieriRicevente.observe(this, Observer<ArrayList<Paniere>> {
            (lista_panieri_ricevente.adapter as PanieriSinteticiAdapter).setData(it)
        })

    }

    //Imposta il punteggio del donatore nella Label se questo è cambiato. Viene utilizzato il pattern Observer affinché
    //possa appunto "osservare" i cambiamenti che vengono effettuati su un certo oggetto.
    fun mostraPunteggio() {
        prvm.obtainPuntiRicevente()
        prvm.ricevente.observe(this, Observer<Utente> {
            var punteggio = it.punteggio
            puntiLabel.setText("${punteggio} Punti")
        })
    }

    //Menu opzioni
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profilo_ricevente_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

        R.id.go_to_donatore -> {

            //Permette il passaggio all'activity Donatore
            //Inoltre invoca il metodo changeRole affinché l'utente possa veder cambiata la sua tipologia
            Navigation.findNavController(this.requireView()).navigate(R.id.HRtoHD)
            prvm.changeRole()
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

                    TransitionManager.beginDelayedTransition(lista_panieri_ricevente_view, AutoTransition().apply{duration = 150})
                    titolo_lista.visibility = View.GONE
                    (requireActivity() as AppCompatActivity).supportActionBar?.title = "Panieri seguiti"
                }
                BottomSheetBehavior.STATE_DRAGGING -> {
                    TransitionManager.beginDelayedTransition(lista_panieri_ricevente_view, AutoTransition())
                    titolo_lista.visibility = View.VISIBLE
                    (requireActivity() as AppCompatActivity).supportActionBar?.title = "Panpar"
                }
            }

        }

    }



}