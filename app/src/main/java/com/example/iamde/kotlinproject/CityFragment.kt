package com.example.iamde.kotlinproject
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_city.*
import java.util.*
import android.location.Geocoder
import java.io.IOException


class CityFragment : Fragment() {
    private lateinit var locationManager : LocationManager
    private var automatic = true
    private var latitude = 16.20
    private var longitude = 33.34
    private var cityName = ""
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if(automatic){
                latitude = location.latitude
                longitude = location.longitude
            }
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_city, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(automatic){
            updateLocation()
        }else {
            makeRequest(latitude, longitude)
        }
        cityTextView.text = cityName
        val time = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if(time in 6..17){
            backgroundImageView.setBackgroundResource(R.drawable.sun)
        }else {
            Log.d("CITYY", backgroundImageView.id.toString())
            backgroundImageView.setBackgroundResource(R.drawable.moon)
        }
    }

    private fun makeRequest(latitude:Double, longitude:Double) {
        val repo = Repository()
        repo.getCurrentWeather(latitude, longitude, object : OnRepositoryReadyCallback {
            override fun onDataReady(data: Result) {
                activity?.runOnUiThread {
                    val summary = data.daily[0].summary
                    val min = data.daily[0].temperatureMin.toInt()
                    val max = data.daily[0].temperatureMax.toInt()
                    val cur = data.currently.temperature.toInt()
                    val vis = when (data.daily[0].visibility){
                        in 0..3 -> "Low visibility."
                        in 4..6 -> "Average visibility."
                        else -> "Good visibility."
                    }
                    summaryTextView.text = "$summary $vis Air temperature is $cur°, maximum $max° and minimum $min°."
                    statusTextView.text = data.currently.summary
                    tempTextView.text = "${cur}°"
                    val unique = mutableListOf<Forecast>()
                    unique.addAll(data.daily)
                    val viewAdapter = ForecastRecyclerViewAdapter(unique)
                    val viewManager = LinearLayoutManager(activity)
                    recyclerView.apply {
                        setHasFixedSize(true)
                        layoutManager = viewManager
                        adapter = viewAdapter
                    }
                }
            }
        })
    }

    fun setCity(loc : Location, city : String){
        automatic = false
        cityName = city
        latitude = loc.latitude
        longitude = loc.longitude
    }

    private fun getCity(lat : Double, lon: Double){
        if (Geocoder.isPresent()) {
            try {
                val gc = Geocoder(activity)
                val addresses = gc.getFromLocation(lat, lon , 1)
                if(!addresses.isEmpty()){
                    cityName = addresses[0].adminArea.split(" ")[0]
                }
            } catch (e: IOException) {
                // handle the exception
            }

        }
    }

    private fun updateLocation(){
        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ContextCompat.checkSelfPermission(activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                ActivityCompat.requestPermissions(activity!!,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f,locationListener)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f,locationListener)
            var loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (loc != null) {
                makeRequest(loc.latitude, loc.longitude)
                if(automatic){
                    getCity(loc.latitude, loc.longitude)
                }
            } else {
                loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (loc != null) {
                    makeRequest(loc.latitude, loc.longitude)
                    if(automatic){
                        getCity(loc.latitude, loc.longitude)
                    }
                } else {
                    // Pick city activity
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    updateLocation()
                } else { }
                return
            }
            else -> { }
        }
    }
}
