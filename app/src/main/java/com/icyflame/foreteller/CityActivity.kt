package com.icyflame.foreteller

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import com.icyflame.foreteller.models.City
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.AutocompletePredictionBuffer
import com.google.android.gms.location.places.Places
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.miguelcatalan.materialsearchview.SearchAdapter
import kotlinx.android.synthetic.main.activity_city.*
import java.io.IOException


class CityActivity : AppCompatActivity() {
    private var latitude : Double = 0.0
    private var longitude : Double = 0.0
    private lateinit var locationManager : LocationManager
    private var cities :MutableSet<String> = mutableSetOf()
    private var fragmentList: MutableList<CityFragment> = mutableListOf()
    private lateinit var mPager: ViewPager
    private lateinit var pagerAdapter: CityFragmentAdapter
    private val span = StyleSpan(Typeface.BOLD)
    private lateinit var buffer : AutocompletePredictionBuffer
    private var searchCities : MutableList<String> = mutableListOf()
    private lateinit var googleApiClient : GoogleApiClient
    private lateinit var builder: AutocompleteFilter.Builder
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            latitude = location.latitude
            longitude = location.longitude
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city)
        googleApiClient = GoogleApiClient.Builder(this)
            .addApi(Places.GEO_DATA_API)
            .build()
        googleApiClient.connect()
        builder =  AutocompleteFilter.Builder()
            .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)

        configureViews()
        configureFragments()
    }

    private fun configureViews(){
        searchView.setAdapter(SearchAdapter(this, searchCities.toTypedArray()))
        searchView.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchCities = mutableListOf()
                val result = Places
                    .GeoDataApi
                    .getAutocompletePredictions(googleApiClient, newText, null, builder.build())
                result.setResultCallback {
                    val status = it.status
                    val size = it.count
                    if (status.isSuccess){
                        buffer = it
                        for (i in 0 until size){
                            searchCities.add(it[i].getFullText(span).toString())
                            Log.d("PLACE", it[i].getFullText(span).toString())
                        }
                        searchView.setAdapter(SearchAdapter(this@CityActivity, searchCities.toTypedArray()))
                        searchView.showSuggestions()
                    }
                }

                return false
            }
        })

        searchView.setOnItemClickListener { _, _, position, _ ->
            Places.getGeoDataClient(this)
                .getPlaceById(buffer[position].placeId).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val myPlace = it.result?.get(0)
                        if (myPlace != null) {
                            addCity(City(myPlace.name.toString(), myPlace.latLng.latitude, myPlace.latLng.longitude))
                        }
                        it.result?.release()
                    } else {
                        Log.e(ContentValues.TAG, "Place not found.")
                    }
                }
            searchView.closeSearch()
        }

        searchView.setOnSearchViewListener(object : MaterialSearchView.SearchViewListener {
            override fun onSearchViewShown() {
                searchView.showSuggestions()
            }

            override fun onSearchViewClosed() {
                searchView.hideKeyboard(searchView)
            }
        })
    }

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (mPager.currentItem == 0) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }
            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Please press BACK twice to exit", Toast.LENGTH_SHORT).show()
        } else {
            mPager.currentItem = mPager.currentItem - 1
        }
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 1000)
    }



    private fun configureFragments(){
        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        cities.addAll(sharedPref.getStringSet("cities", setOf()))
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }

            if(cities.count() > 0){
                for(city in cities){
                    val name = city.split(',')[0]
                    val lat = city.split(',')[1].toDouble()
                    val lon = city.split(',')[2].toDouble()
                    val fr = CityFragment.newInstance(name, lat, lon )
                    fragmentList.add(fr)
                }
            }
            mPager = findViewById(R.id.pager)

            pagerAdapter = CityFragmentAdapter(supportFragmentManager, fragmentList)
            mPager.adapter = pagerAdapter
        } else {
            val lastCityName = sharedPref.getString("lastCityName", "")
            val lastCityLat = sharedPref.getFloat("lastCityLat", 0f)
            val lastCityLon = sharedPref.getFloat("lastCityLon", 0f)

            if(lastCityName!!.isNotBlank()){
                val fr = CityFragment.newInstance(lastCityName, lastCityLat.toDouble(), lastCityLon.toDouble())
                fr.setAuto(true)
                fragmentList.add(fr)
            }

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f,locationListener)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f,locationListener)
            var loc : Location? = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (loc != null) {
                val name = getCity(loc.latitude, loc.longitude)
                if (name.isNotBlank()) {
                    if (fragmentList.size == 0) {
                        val fr = CityFragment.newInstance(name, loc.latitude, loc.longitude)
                        fr.setAuto(true)
                        fragmentList.add(fr)
                    }else {
                        fragmentList[0].reSetCity( loc.latitude, loc.longitude,name)
                        pagerAdapter.setFragmentLis(fragmentList)
                    }
                }
            } else {
                loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if(loc != null){
                    val name = getCity(loc.latitude, loc.longitude)
                    if (name.isNotBlank()) {
                        if (fragmentList.size == 0) {
                            val fr = CityFragment.newInstance(name, loc.latitude, loc.longitude)
                            fr.setAuto(true)
                            fragmentList.add(fr)
                        }else {
                            fragmentList[0].reSetCity( loc.latitude, loc.longitude,name)
                            pagerAdapter.setFragmentLis(fragmentList)
                        }
                    }
                }
            }

            if(cities.count() > 0){
                for(city in cities){
                    Log.d("NUMBERR", city)
                    val lat = city.split(',')[0].toDouble()
                    val lon = city.split(',')[1].toDouble()
                    val name = city.split(',')[2]
                    val fr = CityFragment.newInstance(name, lat, lon)
                    fragmentList.add(fr)
                }
            }
            mPager = findViewById(R.id.pager)
            pagerAdapter = CityFragmentAdapter(supportFragmentManager, fragmentList)
            mPager.adapter = pagerAdapter
            if (fragmentList.size == 0){
                val handler = Handler()
                handler.postDelayed({
                    blackPlusButton.setOnClickListener {
                        showSearch()
                    }
                    blackPlusButton.visibility = View.VISIBLE
                    searchView.showSearch()
                }, 100)
            }
        }
    }

    fun showSearch(){
        searchView.showSearch()
    }

    private fun getCity(lat : Double, lon: Double) : String{
        var cityName = ""
        if (Geocoder.isPresent()) {
            try {
                val gc = Geocoder(this)
                val addresses = gc.getFromLocation(lat, lon , 1)
                if(!addresses.isEmpty()){
                    cityName = addresses[0].adminArea
                }
            } catch (e: IOException) {
                // handle the exception
            }

        }
        return cityName
    }

    private fun addCity(city : City){
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        Toast.makeText(this, "NOT NULL", Toast.LENGTH_SHORT).show()
        with (sharedPref.edit()) {
            if(!cities.contains(city.name)){
                cities.add("${city.latitude},${city.longitude},${city.name}")
                putStringSet("cities", cities)
                apply()
                val fr = CityFragment.newInstance(city.name, city.latitude, city.longitude)
                if(blackPlusButton.visibility == View.VISIBLE){
                    blackPlusButton.visibility = View.GONE
                }
                fragmentList.add(fr)
                pagerAdapter.setFragmentLis(fragmentList)
                mPager.currentItem = fragmentList.count()
            }
        }
        Log.d("currentCities", cities.toString())
    }

    fun removeCity(city : City, fr:CityFragment){
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            if(cities.contains("${city.latitude},${city.longitude},${city.name}")){
                cities.remove("${city.latitude},${city.longitude},${city.name}")
                putStringSet("cities", cities)
                apply()
            }
        }
        fragmentList.remove(fr)
        supportFragmentManager.beginTransaction().remove(fr).commit()
        pagerAdapter.setFragmentLis(fragmentList)
        if (fragmentList.size == 0){
            blackPlusButton.setOnClickListener {
                showSearch()
            }
            blackPlusButton.visibility = View.VISIBLE
            searchView.showSearch()
        }
        Log.d("currentCities", cities.toString())
        Log.d("currentCitiesShoud", "${city.latitude},${city.longitude},${city.name}")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    configureFragments()
                } else { }
                return
            }
            else -> { }
        }
    }

    private fun getCityFromLoc(lat : Double, lon: Double) : String?{
        var cityName : String? = null
        if (Geocoder.isPresent()) {
            try {
                val gc = Geocoder(this)
                val addresses = gc.getFromLocation(lat, lon , 1)
                if(!addresses.isEmpty()){
                    cityName = addresses[0].adminArea
                    if(cityName != null){
                        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
                        with (sharedPref.edit()) {
                            putString("lastCityName", cityName)
                            putFloat("lastCityLat", lat.toFloat())
                            putFloat("lastCityLon", lon.toFloat())
                            apply()
                        }
                    }
                }
            } catch (e: IOException) {
                // handle the exception
            }

        }
        return cityName
    }


}
