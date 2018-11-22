package com.icyflame.foreteller
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.icyflame.foreteller.models.City
import com.icyflame.foreteller.models.Forecast
import com.icyflame.foreteller.models.Result
import kotlinx.android.synthetic.main.fragment_city.*
import java.util.*

class CityFragment : Fragment() {
    var cityName : String = ""
    private var latitude : Double = 0.0
    private var longitude : Double = 0.0
    private var automatic = false
    private lateinit var activityInstance : CityActivity
    private lateinit var myView : View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myView = inflater.inflate(R.layout.fragment_city, container, false)
        return myView
    }

    companion object {
        fun newInstance(name: String, latitude: Double, longitude: Double ): CityFragment {
            val fragment = CityFragment()
            fragment.cityName = name
            fragment.latitude = latitude
            fragment.longitude = longitude

            return fragment
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityInstance = activity as CityActivity
        plusButton.setOnClickListener {
            activityInstance.showSearch()
        }
        if (automatic) minusButton.visibility = View.GONE
        minusButton.setOnClickListener {
            Log.d("currentCityCont", "$latitude,$longitude,$cityName")
            activityInstance.removeCity(City(cityName, latitude, longitude), this)
        }
        makeRequest(latitude, longitude)
        cityTextView.text = cityName
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

    fun setAuto(auto : Boolean){
        automatic = auto
    }

    fun reSetCity(lat : Double, lon: Double, city : String){
        cityName = city
        latitude = lat
        longitude = lon
    }

}
