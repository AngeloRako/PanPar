package com.rapnap.panpar.view
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import java.lang.ref.WeakReference


object CurrentLocationManager : LocationListener {

    const val REQUEST_CODE_ACCESS_LOCATION = 123

    fun checkLocationPermission(activity: Activity) {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_ACCESS_LOCATION
            )
        } else {
            Thread(Runnable {
                // Moves the current Thread into the background
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
                //
                requestLocationUpdates(activity)
            }).start()
        }
    }

    /**
     * be used in HomeActivity.
     */
    const val REQUEST_CHECK_SETTINGS = 55
    /**
     * The number of millis in the future from the call to start().
     * until the countdown is done and onFinish() is called.
     *
     *
     * It is also the interval along the way to receive onTick(long) callbacks.
     */
    private const val TWENTY_SECS: Long = 20000
    /**
     * Timer to get location from history when requestLocationUpdates don't return result.
     */
    private var mCountDownTimer: CountDownTimer? = null
    /**
     * WeakReference of current activity.
     */
    private var mWeakReferenceActivity: WeakReference<Activity>? = null
    /**
     * user's location.
     */
    var currentLocation: Location? = null

    @Synchronized
    fun requestLocationUpdates(activity: Activity) {
        if (mWeakReferenceActivity == null) {
            mWeakReferenceActivity = WeakReference(activity)
        } else {
            mWeakReferenceActivity?.clear()
            mWeakReferenceActivity = WeakReference(activity)
        }
        //create location request: https://developer.android.com/training/location/change-location-settings.html#prompt
        val mLocationRequest = LocationRequest()
        // Which your app prefers to receive location updates. Note that the location updates may be
        // faster than this rate, or slower than this rate, or there may be no updates at all
        // (if the device has no connectivity)
        mLocationRequest.interval = 20000
        //This method sets the fastest rate in milliseconds at which your app can handle location updates.
        // You need to set this rate because other apps also affect the rate at which updates are sent
        mLocationRequest.fastestInterval = 10000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        //Get Current Location Settings
        val builder = LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest)
        //Next check whether the current location settings are satisfied
        val client = LocationServices.getSettingsClient(activity)
        val task = client.checkLocationSettings(builder.build())
        //Prompt the User to Change Location Settings
        task.addOnSuccessListener(activity) {
            Log.d("CurrentLocationManager", "OnSuccessListener")
            // All location settings are satisfied. The client can initialize location requests here.
            // If it's failed, the result after user updated setting is sent to onActivityResult of HomeActivity.
            val activity1 = mWeakReferenceActivity?.get()
            if (activity1 != null) {
                startRequestLocationUpdate(activity1.applicationContext)
            }
        }

        task.addOnFailureListener(activity) { e ->
            Log.d("CurrentLocationManager", "addOnFailureListener")
            val statusCode = (e as ApiException).statusCode
            when (statusCode) {
                CommonStatusCodes.RESOLUTION_REQUIRED ->
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        val activity1 = mWeakReferenceActivity?.get()
                        if (activity1 != null) {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            val resolvable = e as ResolvableApiException
                            resolvable.startResolutionForResult(
                                activity1, REQUEST_CHECK_SETTINGS
                            )
                        }
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                        sendEx.printStackTrace()
                    }

                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    // Location settings are not satisfied. However, we have no way
                    // to fix the settings so we won't show the dialog.
                }
            }
        }
    }

    fun startRequestLocationUpdate(appContext: Context) {
        val mLocationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                appContext.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //Utilities.showProgressDialog(mWeakReferenceActivity.get());
            if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 10000, 0f, this
                )
            } else {
                mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 10000, 0f, this
                )
            }
        }

        /*Timer to call getLastKnownLocation() when requestLocationUpdates don 't return result*/
        countDownUpdateLocation()
    }

    override fun onLocationChanged(location: Location?) {
        if (location != null) {
            stopRequestLocationUpdates()
            currentLocation = location
        }
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

    }

    override fun onProviderEnabled(provider: String) {

    }

    override fun onProviderDisabled(provider: String) {

    }

    /**
     * Init CountDownTimer to to get location from history when requestLocationUpdates don't return result.
     */
    @Synchronized
    private fun countDownUpdateLocation() {
        mCountDownTimer?.cancel()
        mCountDownTimer = object : CountDownTimer(TWENTY_SECS, TWENTY_SECS) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                if (mWeakReferenceActivity != null) {
                    val activity = mWeakReferenceActivity?.get()
                    if (activity != null && ActivityCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val location = (activity.applicationContext
                            .getSystemService(Context.LOCATION_SERVICE) as LocationManager)
                            .getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                        stopRequestLocationUpdates()
                        onLocationChanged(location)
                    } else {
                        stopRequestLocationUpdates()
                    }
                } else {
                    mCountDownTimer?.cancel()
                    mCountDownTimer = null
                }
            }
        }.start()
    }

    /**
     * The method must be called in onDestroy() of activity to
     * removeUpdateLocation and cancel CountDownTimer.
     */
    fun stopRequestLocationUpdates() {
        val activity = mWeakReferenceActivity?.get()
        if (activity != null) {
            /*if (ActivityCompat.checkSelfPermission(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {*/
            (activity.applicationContext
                .getSystemService(Context.LOCATION_SERVICE) as LocationManager).removeUpdates(this)
            /*}*/
        }
        mCountDownTimer?.cancel()
        mCountDownTimer = null
    }
}
