package com.rapnap.panpar.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*

abstract class LocationDependantFragment: Fragment() {

    val PERMISSION_ID: Int = 24+23+25
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    //Controlla se l'utente dispone dei permessi necessari ad ottenere la posizione
    open fun checkPermissions(): Boolean {
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

    //Richiesta di permessi
    open fun requestPermissions() {
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
            } else{

                onPermissionDenied()

            }
        }
    }

    //Controllo se la posizione è attivata sul dispositivo
    open fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    //Ottengo l'ultima posizione disponibile utilizzando le funzioni precedenti
    @SuppressLint("MissingPermission")
    open fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        onLocationObtained(location)
                    }
                }
            } else {

                onLocationDisabled()
            }
        } else {
            requestPermissions()
        }
    }


    //Funzione che richiede la posizione dell'utente: se questa viene aggiornata avvia mLocationCallback
    @SuppressLint("MissingPermission")
    open fun requestNewLocationData() {
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
    open val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            onLocationUpdated(locationResult.lastLocation)
        }
    }

    /*  Elementi di Menu nella ActionBar    */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    abstract fun onLocationObtained(location: Location)
    abstract fun onLocationUpdated(location: Location)
    abstract fun onLocationDisabled()
    abstract fun onPermissionDenied()


}