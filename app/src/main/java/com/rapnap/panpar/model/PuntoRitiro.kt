package com.rapnap.panpar.model

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import java.util.*

data class PuntoRitiro (val id: String = "",
                        val nome: String = "",
                        val indirizzo : String = "",
                        val location: Location = Location("").also{
                            it.latitude = 40.643396
                            it.longitude = 14.865041}
)

/* Estensioni utili */

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


fun LatLng.distanceTo(latLng: LatLng) = toLocation().distanceTo(latLng.toLocation())
fun LatLng.distanceTo(loc: Location) = toLocation().distanceTo(loc)
