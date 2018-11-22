package com.example.iamde.kotlinproject
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Typeface
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
import android.os.Handler
import android.text.style.StyleSpan
import com.example.iamde.kotlinproject.models.Forecast
import com.example.iamde.kotlinproject.models.Result
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.AutocompletePredictionBuffer
import com.google.android.gms.location.places.Places
import java.io.IOException
import com.miguelcatalan.materialsearchview.MaterialSearchView

class CityFragment : Fragment() {
    private lateinit var locationManager : LocationManager
    private var automatic = true
    private var latitude = 16.20
    private var longitude = 33.34
    private var cityName = ""
    private lateinit var activityInstance : CityActivity
    private lateinit var googleApiClient : GoogleApiClient
    private lateinit var builder: AutocompleteFilter.Builder
    private val STYLE_BOLD = StyleSpan(Typeface.BOLD)
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
        activityInstance = activity as CityActivity
        googleApiClient = GoogleApiClient.Builder(activityInstance)
            .addApi(Places.GEO_DATA_API)
            .build()
        googleApiClient.connect()
        builder =  AutocompleteFilter.Builder()
            .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
        plusButton.setOnClickListener {
            searchView.showSearch()
        }
        minusButton.setOnClickListener {
            activityInstance.removeCity(cityName, this)
        }

        searchView.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                val cities : MutableList<String> = mutableListOf()
                val result = Places
                    .GeoDataApi
                    .getAutocompletePredictions(googleApiClient, newText, null, builder.build())
                result.setResultCallback {
                    val status = it.status
                    val handler = Handler()
                    val size = it.count
                    if (status.isSuccess){
                        for (i in 0 until size){
                            cities.add(it[i].getFullText(STYLE_BOLD).toString())
                            Log.d("PLACE", it[i].getFullText(STYLE_BOLD).toString())
                            searchView.setSuggestions(cities.toTypedArray())
                            searchView.showSuggestions()
                        }
                    }
                }

                return false
            }
        })

        searchView.setOnSearchViewListener(object : MaterialSearchView.SearchViewListener {
            override fun onSearchViewShown() {
                searchView.showSuggestions()
            }

            override fun onSearchViewClosed() {
                searchView.hideKeyboard(searchView)
            }
        })
        if(automatic){
            minusButton.visibility = View.GONE
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

    fun setCity(lat : Double, lon: Double, city : String){
        automatic = false
        cityName = city
        latitude = lat
        longitude = lon
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
