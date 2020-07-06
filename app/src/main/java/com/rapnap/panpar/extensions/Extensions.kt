package com.rapnap.panpar.extensions

import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.google.android.gms.maps.model.LatLng
import java.util.*

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}


//Posizioni

fun LatLng.distanceTo(latLng: LatLng) = toLocation().distanceTo(latLng.toLocation())
fun LatLng.distanceTo(loc: Location) = toLocation().distanceTo(loc)
fun LatLng.toLocation() = Location("").also{
    it.latitude = this.latitude
    it.longitude = this.longitude
}

fun Location.toLatLng() = LatLng(latitude, longitude)

fun distanceText(distance: Float): String {
    val distanceString: String

    if (distance < 1000)
        if (distance < 1)
            distanceString = String.format(Locale.US, "%dm", 1)
        else
            distanceString = String.format(Locale.US, "%dm", Math.round(distance))
    else if (distance > 10000)
    //if (distance < 1000000)
        distanceString = String.format(Locale.US, "%dkm", Math.round(distance / 1000))
    //else
    //    distanceString = "FAR"
    else
        distanceString = String.format(Locale.US, "%.2fkm", distance / 1000)

    return distanceString
}


