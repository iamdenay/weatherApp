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
    var fragmentList: MutableList<CityFragment> = mutableListOf()
    private lateinit var mPager: ViewPager
    private lateinit var pagerAdapter: CityFragmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city)
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        cities.addAll(sharedPref.getStringSet("cities", setOf()))
        val fr = CityFragment()
        fragmentList.add(fr)
        Log.d("CITIES", cities.toString())
        if(cities.count() > 0){
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
        pagerAdapter = CityFragmentAdapter(supportFragmentManager, fragmentList)
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

    fun addCity(city : String){
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        val loc = getLocation(city)
        if(loc != null){
            Toast.makeText(this, "NOT NULL", Toast.LENGTH_SHORT).show()
            with (sharedPref.edit()) {
                if(!cities.contains(city)){
                    cities.add(city)
                    putStringSet("cities", cities)
                    apply()
                    val fr = CityFragment()
                    fr.setCity(loc, city)
                    fragmentList.add(fr)
                    pagerAdapter.notifyDataSetChanged()
                }
            }
        }else{
            Toast.makeText(this, "NULL", Toast.LENGTH_SHORT).show()
            Log.d("CITY", "No such location.")
        }
    }

    fun removeCity(city : String, fr:CityFragment){
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        val loc = getLocation(city)
        if(loc != null){
            Toast.makeText(this, "NOT NULL", Toast.LENGTH_SHORT).show()
            with (sharedPref.edit()) {
                if(cities.contains(city)){
                    cities.remove(city)
                    putStringSet("cities", cities)
                    apply()
                    fragmentList.remove(fr)
                    pagerAdapter.notifyDataSetChanged()
                }
            }
        }else{
            Toast.makeText(this, "NULL", Toast.LENGTH_SHORT).show()
            Log.d("CITY", "No such location.")
        }
    }

    private inner class CityFragmentAdapter(fm: FragmentManager, fragmentList: MutableList<CityFragment>) : FragmentStatePagerAdapter(fm) {
        override fun getCount(): Int = fragmentList.count()

        override fun getItem(position: Int): Fragment = fragmentList.get(position)
    }
}
