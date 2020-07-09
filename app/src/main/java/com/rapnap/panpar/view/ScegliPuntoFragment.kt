package com.rapnap.panpar.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.navGraphViewModels
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.rapnap.panpar.R
import com.rapnap.panpar.extensions.distanceText
import com.rapnap.panpar.extensions.toLatLng
import com.rapnap.panpar.extensions.toLocation
import com.rapnap.panpar.model.PuntoRitiro
import com.rapnap.panpar.viewmodel.NuovoPaniereViewModel
import kotlinx.android.synthetic.main.fragment_scegli_punto.*
import kotlinx.android.synthetic.main.fragment_scegli_punto.view.*

class ScegliPuntoFragment : Fragment(), GoogleMap.OnMarkerClickListener,
    GoogleMap.OnCameraMoveListener, GoogleMap.CancelableCallback {

    private lateinit var gMap: GoogleMap
    private var mapReady = false
    private var isWaitingToShow = false
    private lateinit var punti: List<PuntoRitiro>
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var puntoRitiroVisualizzato: PuntoRitiro
    val PERMISSION_ID: Int = 40
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    private val nuovoPaniereVM: NuovoPaniereViewModel
            by navGraphViewModels(R.id.new_paniere_graph)

    private var currentLocation = LatLng(40.643396, 14.865041)

    //* Callback mappa */
    private val callback = OnMapReadyCallback { googleMap ->
        googleMap?.let {
            Log.d(TAG, "[PANPAR - ScegliPuntoFragment] La mappa è pronta! ")

            gMap = googleMap

            val styleApplied: Boolean

            try {
                when (isUsingNightModeResources()) {
                    true -> {
                        styleApplied = gMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                requireContext(),
                                R.raw.dark_style_json
                            )
                        )
                    }
                    false -> {

                        styleApplied = gMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                requireContext(),
                                R.raw.style_json
                            )
                        )
                    }
                }

                if (!styleApplied) {

                    Log.e(TAG, "[ScegliPuntoFragment] Impossibile applicare stile mappa!")
                }

            } catch (e: Resources.NotFoundException) {
                Log.e(TAG, "[ScegliPuntoFragment] Can't find style. Error: ", e)
            }

            gMap.setMinZoomPreference(8F)
            gMap.setMaxZoomPreference(18F)

            gMap.setOnMarkerClickListener(this)
            gMap.setOnCameraMoveListener(this)
            mapReady = true

            updateMap()

            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 11F))
            Log.e(
                TAG,
                "DI SEGUITO LA TUA POSIZIONE: ${currentLocation.latitude} e ${currentLocation.longitude}"
            )

            if (::puntoRitiroVisualizzato.isInitialized && isWaitingToShow) {
                show(puntoRitiroVisualizzato)
                isWaitingToShow = false
            }
        }
    }

    //Controlla se l'utente dispone dei permessi necessari ad ottenere la posizione
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    //richiede i permessi
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    //Se ottiene i permessi procede
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                //Permessi ottenuti, re-invoca la funzione
                getLastLocation()
            }
        }
    }

    //Controllo se la posizione è attivata sul dispositivo
    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager =
            activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    //Ottengo l'ultima posizione disponibile utilizzando le funzioni precedenti
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        setMapPosition(LatLng(location.latitude, location.longitude))
                    }
                }
            } else {

                Snackbar.make(
                    requireView(),
                    "Attiva la localizzazione per scoprire i punti di ritiro più vicini.",
                    Snackbar.LENGTH_LONG //
                ).setAction(
                    "Vai",
                    {
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivity(intent)
                    }).show()
            }
        } else {
            requestPermissions()
        }
    }

    //Funzione che richiede la posizione dell'utente: se questa viene aggiornata avvia mLocationCallback
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    //Callback relativa al caso in cui la posizione è stata aggiornata
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            currentLocation = LatLng(mLastLocation.latitude, mLastLocation.longitude)
            if (currentLocation != null) {
                setMapPosition()
            }
        }
    }

    //Aggiorna la mappa dalla lista punti ritiro (se possibile)
    private fun updateMap() {
        if (::punti.isInitialized && mapReady) {
            punti.forEach { punto ->
                Log.d(TAG, "[PANPAR - ScegliPuntoFragment] Leggo: ${punto.nome}")
                val pos = LatLng(punto.location.latitude, punto.location.longitude)
                gMap.addMarker(MarkerOptions().position(pos).title(punto.nome)).apply {
                    this.tag = punto
                }

            }
        }
    }

    private fun setMapPosition(location: LatLng = currentLocation) {

        currentLocation = location

        if (mapReady) {
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 11F))
            Log.e(
                TAG,
                "DI SEGUITO LA TUA POSIZIONE: ${currentLocation.latitude} e ${currentLocation.longitude}"
            )
        }

    }


    override fun onMarkerClick(marker: Marker?): Boolean {

        nuovoPaniereVM.setPuntoRitiroVisualizzato(marker?.tag as PuntoRitiro)
        return true
    }

    private fun show(puntoRitiro: PuntoRitiro, speed: Int = 100) {

        name_text_view.text = puntoRitiro.nome
        address_text_view.text = puntoRitiro.indirizzo

        val loc = currentLocation.toLocation()

        distance_text_view.text =
            distanceText(puntoRitiro.location.distanceTo(loc)) + " da qui"

        if (mapReady) {

            gMap.animateCamera(
                CameraUpdateFactory.newLatLng(puntoRitiro.location.toLatLng()),
                speed,
                this
            )

            //Apri  lo sheet
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

            isWaitingToShow = false

        } else {

            Log.d(TAG, "[ScegliPuntoRitiro] Errore: Mappa non pronta alla visualizzazione...")
            isWaitingToShow = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scegli_punto, container, false)

        //getLastLocation()

        //Location utente (dovrebbe venire dal ViewModel)
        val loc = currentLocation.toLocation()

        nuovoPaniereVM.getPuntiDiRitiro(loc, 2000000.0) {}

        nuovoPaniereVM.puntiRitiro.observe(viewLifecycleOwner, Observer<List<PuntoRitiro>> {

            this.punti = it
            updateMap()

        })

        nuovoPaniereVM.puntoRitiroVisualizzato.observe(viewLifecycleOwner, Observer<PuntoRitiro> {

            isWaitingToShow = true
            puntoRitiroVisualizzato = it
            show(it)

        })

        //Configuro il pannello inferiore
        bottomSheetBehavior = BottomSheetBehavior.from(view.punto_bottom_sheet)
        view.set_paniere_details.setOnClickListener { v ->

            /* Cosa faccio quando l'utente preme su "Consegna Qui"*/
            nuovoPaniereVM.setPuntoRitiroScelto(puntoRitiroVisualizzato)
            Navigation.findNavController(requireView()).popBackStack()

        }

        //Inizialmente nascosto
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        //Listener per il button relativo al posizionamento della mappa sulla currentLocation
        view.locationBtn.setOnClickListener {
            getLastLocation()
        }

        return view
    }


    /*  Elementi di Menu nella ActionBar    */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        map_view.onCreate(savedInstanceState)
        map_view.onResume()
        map_view.getMapAsync(callback)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.punti_ritiro_map_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

        R.id.toggle_List -> {
            // User chose to view as List
            Navigation.findNavController(this.requireView()).navigate(R.id.punti_map_To_List)
            mapReady = false
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    /*  Cosa fare quando la mappa viene spostata?    */
    override fun onCameraMove() {

        //Chiudi  lo sheet
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

    }

    /* Called on finished animating camera */
    override fun onFinish() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

    }

    /* Non mi serve ma è necessario per l'ìntefaccia*/
    override fun onCancel() {
        //
    }

    //Controlla se l'app è in Dark Mode
    fun isUsingNightModeResources(): Boolean {
        return when (resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
        }
    }

    override fun onResume() {
        super.onResume()
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        getLastLocation()
    }

}