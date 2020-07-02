package com.rapnap.panpar.view

import android.content.ContentValues.TAG
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.inflate
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.rapnap.panpar.R
import com.rapnap.panpar.model.PuntoRitiro
import com.rapnap.panpar.viewmodel.NuovoPaniereViewModel
import kotlinx.android.synthetic.main.fragment_scegli_punto.*
import kotlinx.android.synthetic.main.fragment_scegli_punto.view.*

class ScegliPuntoFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: PuntiRitiroListAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var gMap: GoogleMap

    private val nuovoPaniereVM: NuovoPaniereViewModel
            by navGraphViewModels(R.id.new_paniere_graph)

    private var bocc = LatLng(40.643396, 14.865041)

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        //val bocc = LatLng(40.643396, 14.865041)
        gMap = googleMap
        gMap.setMinZoomPreference(8F)
        gMap.setMaxZoomPreference(18F)
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bocc, 10F));




    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scegli_punto, container, false)

        //Location utente (dovrebbe venire dal VuewModel)
        var loc = Location("")
        loc.latitude = bocc.latitude
        loc.longitude = bocc.longitude

        viewManager = LinearLayoutManager(this.activity)
        recyclerView = view.punti_ritiro_recycler_view.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

        }

        nuovoPaniereVM.getPuntiDiRitiro(loc, 50.0){}

        nuovoPaniereVM.puntiRitiro.observe(viewLifecycleOwner, Observer<List<PuntoRitiro>> {

            viewAdapter = PuntiRitiroListAdapter(it, loc)
            recyclerView.adapter = viewAdapter

            it.forEach {punto ->

                Log.d(TAG, "Aggiunto Punto a UI: ${punto.nome}")
                val loc = LatLng(punto.location.latitude, punto.location.longitude)

                gMap.addMarker(MarkerOptions().position(bocc).title(punto.nome))
                gMap.moveCamera(CameraUpdateFactory.newLatLng(loc))
                //gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 14F))

            }


        })

        return view
    }

    /*  Elementi di Menu nella ActionBar    */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.punti_ritiro_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }


    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            // User chose the "Settings" item, show the app settings UI...
            val snack = Snackbar.make(requireView(),"Quui potrei avviare il fragment per le impostazioni",Snackbar.LENGTH_LONG)
            snack.show()

            true
        }

        R.id.add_punto -> {

            nuovoPaniereVM.nuoviPuntoDiRitiro{
                val snack = Snackbar.make(requireView(),"Aggiunto punto di ritiro",Snackbar.LENGTH_LONG)
                snack.show()

            }

            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }


}