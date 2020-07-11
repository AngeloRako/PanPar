package com.rapnap.panpar.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LocationViewModel: ViewModel() {

    private val _currentLocation = MutableLiveData<Location>()
    val currentLocation: LiveData<Location>
        get() = _currentLocation

    fun defaultLocation(){
        val currentLocation = Location("")
        currentLocation.latitude = 40.878437
        currentLocation.longitude = 14.343430
        _currentLocation.value = currentLocation
    }

    fun setLocation(location: Location){
        _currentLocation.value = location
    }

    fun isPositionSet(): Boolean{

        return _currentLocation.value != null

    }


}