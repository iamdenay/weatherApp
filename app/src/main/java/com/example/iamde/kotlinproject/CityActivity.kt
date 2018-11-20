package com.example.iamde.kotlinproject

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_city.*
import java.util.*
import kotlin.collections.ArrayList
import android.location.Geocoder
import android.util.Log
import java.io.IOException


class CityActivity : AppCompatActivity() {
    private var cityCount = 3
    private var cities :MutableSet<String> = mutableSetOf()
    var currentCity = 1
    var fragmentList: MutableList<CityFragment> = mutableListOf()
    private lateinit var mPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city)
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        cityCount = sharedPref.getInt("cityCount", 1)
        cityCount = 3
        cities.addAll(sharedPref.getStringSet("cities", setOf()))
        cities = mutableSetOf("Astana", "London", "Oslo")
        val fr = CityFragment()
        fragmentList.add(fr)
        if(cityCount > 1){
            for(city in cities){
                val loc = getLocation(city)
                if(loc != null){
                    val fr = CityFragment()
                    fr.setCity(loc, city)
                    fragmentList.add(fr)
                }

            }
        }

        mPager = findViewById(R.id.pager)
        val pagerAdapter = CityFragmentAdapter(supportFragmentManager, fragmentList)
        mPager.adapter = pagerAdapter
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

        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 1000)
    }

    private fun getLocation(city : String) : Location?{
        var location : Location? = null
        if (Geocoder.isPresent()) {
            try {
                val gc = Geocoder(this)
                val addresses = gc.getFromLocationName(city, 1)
                if(!addresses.isEmpty()){
                    location = Location("")
                    location.latitude = addresses[0].latitude
                    location.longitude = addresses[0].longitude
                }
            } catch (e: IOException) {
                // handle the exception
            }

        }
        return location
    }

    private fun addCity(city : String){
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        if(getLocation(city) != null){
            with (sharedPref.edit()) {
                cities.add(city)
                commit()
            }
        }else{
            Log.d("CITY", "No such location.")
        }

    }

    private inner class CityFragmentAdapter(fm: FragmentManager, fragmentList: MutableList<CityFragment>) : FragmentStatePagerAdapter(fm) {
        override fun getCount(): Int = fragmentList.count()

        override fun getItem(position: Int): Fragment = fragmentList.get(position)
    }
}
