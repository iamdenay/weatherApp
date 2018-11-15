package com.example.iamde.kotlinproject

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_city.*
import java.util.*


class CityActivity : AppCompatActivity() {
    private lateinit var locationManager : LocationManager
    private var latitude = 16.20
    private var longitude = 33.34
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            this@CityActivity.latitude = location.latitude
            this@CityActivity.longitude = location.longitude
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        }

        override fun onProviderEnabled(provider: String) {
        }

        override fun onProviderDisabled(provider: String) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city)
        updateLocation()
        val time = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if(time in 6..17){
            backgroundImageView.setBackgroundResource(R.drawable.sun)
        }else {
            backgroundImageView.setBackgroundResource(R.drawable.moon)
        }
    }

    private fun makeRequest(latitude:Double, longitude:Double) {
        val repo = Repository()
        repo.getCurrentWeather(latitude, longitude, object : OnRepositoryReadyCallback {
            override fun onDataReady(data: Result) {
                val cityName = data.timezone.split("/")[1].replace("_"," ")
                runOnUiThread {
                    cityTextView.text = cityName
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
                    val viewAdapter = ForecastRecyclerViewAdapter(data.daily)
                    val viewManager = LinearLayoutManager(this@CityActivity)
                    recyclerView.apply {
                        setHasFixedSize(true)
                        layoutManager = viewManager
                        adapter = viewAdapter
                    }
                }
            }
        })
    }

    private fun updateLocation(){
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f,locationListener)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f,locationListener)
            var loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (loc != null) {
                makeRequest(loc.latitude, loc.longitude)
            } else {
                loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (loc != null) {
                    makeRequest(loc.latitude, loc.longitude)
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
