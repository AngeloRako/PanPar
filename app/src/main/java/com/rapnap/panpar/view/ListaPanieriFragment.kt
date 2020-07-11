package com.rapnap.panpar.view

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.rapnap.panpar.R
import com.rapnap.panpar.adapter.OnItemEventListener
import com.rapnap.panpar.adapter.PanieriRecyclerAdapter
import com.rapnap.panpar.model.Paniere
import com.rapnap.panpar.viewmodel.ListaPanieriViewModel
import kotlinx.android.synthetic.main.fragment_lista_panieri.*

class ListaPanieriFragment : LocationDependantFragment(), OnItemEventListener<Paniere>, SwipeRefreshLayout.OnRefreshListener {

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: PanieriRecyclerAdapter
    private val listaPanieriVM: ListaPanieriViewModel by viewModels()

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

        listaPanieriVM.getPanieriPrenotabiliFromLocation(currentLocation)

    }

    override fun onPermissionDenied() {

        listaPanieriVM.getPanieriPrenotabiliFromLocation(currentLocation)

    }

    override fun onStart() {
        super.onStart()

        linearLayoutManager = LinearLayoutManager(this.activity)
        rec_view.layoutManager = linearLayoutManager


        //Popolare tutta in una botta con il costruttore mettendo tutto nella onComplete qui dentro
        adapter = PanieriRecyclerAdapter(ArrayList<Paniere>(), currentLocation, this)
        rec_view.adapter = adapter

        listaPanieriVM.getPanieriPrenotabiliFromLocation(currentLocation)
        listaPanieriVM.listaPanieri.observe(this, Observer<ArrayList<Paniere>> {
            Log.d(
                "ACTIVITY", "Ho assegnato ad i panieri i valori che stavano nel DB." +
                        " La dimensione della lista dei panieri è: ${it.size}"
            )
            progress_loader.visibility = View.GONE
            no_result_layout.visibility = View.GONE
            adapter.setData(it)
            if (it.isEmpty()) {
                no_result_layout.visibility = View.VISIBLE
            }
            swipe_layout.isRefreshing = false
        })

        swipe_layout.setOnRefreshListener(this)
        getLastLocation()


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lista_panieri, container, false)

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

    override fun onRefresh() {
        listaPanieriVM.getPanieriPrenotabiliFromLocation(currentLocation)
    }

}