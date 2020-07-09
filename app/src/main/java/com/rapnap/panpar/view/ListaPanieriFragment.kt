package com.rapnap.panpar.view

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.rapnap.panpar.R
import com.rapnap.panpar.adapter.OnItemEventListener
import com.rapnap.panpar.adapter.PanieriRecyclerAdapter
import com.rapnap.panpar.extensions.toLocation
import com.rapnap.panpar.model.Paniere
import com.rapnap.panpar.viewmodel.ListaPanieriViewModel
import kotlinx.android.synthetic.main.fragment_lista_panieri.view.*

class ListaPanieriFragment: LocationDependantFragment(), OnItemEventListener<Paniere> {

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: PanieriRecyclerAdapter
    private val listaPanieriVM : ListaPanieriViewModel by viewModels()

    //TODO: Determinare posizione dinamicamente (VM?)
    private var currentLocation = LatLng(40.643396, 14.865041)

    override fun onLocationObtained(location: Location) {
        //L'utente ha dato i permessi e posso fare la query geocalizzata

        //Parte chiamando la funzione che comunica con la repository per prendere i panieri
        //in base al punteggio dell'utente
        listaPanieriVM.getPanieriPrenotabiliFromLocation(location)

    }


    override fun onLocationUpdated(location: Location) {

        listaPanieriVM.getPanieriPrenotabiliFromLocation(location)

    }

    override fun onLocationDisabled() {

        val defaultLocation = Location("")
        defaultLocation.latitude = 40.878437
        defaultLocation.longitude = 14.343430

        listaPanieriVM.getPanieriPrenotabiliFromLocation(defaultLocation)
    }

    override fun onPermissionDenied(){

        val defaultLocation = Location("")
        defaultLocation.latitude = 40.878437
        defaultLocation.longitude = 14.343430

        listaPanieriVM.getPanieriPrenotabiliFromLocation(defaultLocation)

    }

    override fun onStart() {
        super.onStart()

        val defaultLocation = Location("")
        defaultLocation.latitude = 40.878437
        defaultLocation.longitude = 14.343430

        listaPanieriVM.getPanieriPrenotabiliFromLocation(defaultLocation)
        listaPanieriVM.listaPanieri.observe(this, Observer<ArrayList<Paniere>> {
                //Log.d("ACTIVITY", "Ho assegnato ad i panieri i valori che stavano nel DB." +
                //        " La dimensione della lista dei panieri è: " + panieriList.size.toString())
                adapter.setData(it)

        })

        getLastLocation()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lista_panieri, container, false)

        linearLayoutManager = LinearLayoutManager(this.activity)
        view.rec_view.layoutManager = linearLayoutManager


        //Popolare tutta in una botta con il costruttore mettendo tutto nella onComplete qui dentro
        adapter = PanieriRecyclerAdapter(ArrayList<Paniere>(), currentLocation.toLocation(), this)
        view.rec_view.adapter = adapter

        return view
    }


    override fun onEventHappened(item: Paniere, view: View?) {
        listaPanieriVM.updatePaniereFollowers(item.id)

        Snackbar.make(
            requireView(),
            "Sei ora in coda per l'assegnazione di questo paniere!",
            Snackbar.LENGTH_LONG //
        ).setAction(
            "Ok",
            {
                it.visibility = View.GONE
            }).show()

    }

}