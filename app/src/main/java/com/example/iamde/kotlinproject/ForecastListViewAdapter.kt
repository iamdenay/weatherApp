package com.example.iamde.kotlinproject

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.iamde.kotlinproject.models.Forecast
import kotlinx.android.synthetic.main.forecast_list_item.view.*
import java.text.SimpleDateFormat
import java.util.*

class ForecastRecyclerViewAdapter(private var items: List<Forecast>) : RecyclerView.Adapter<ForecastRecyclerViewAdapter.ForecastViewHolder>() {

    class ForecastViewHolder(row: View) : RecyclerView.ViewHolder(row){
        var dayTextView: TextView? = null
        var minTempView: TextView? = null
        var maxTempView: TextView? = null
        var dayIconView: ImageView? = null

        init {
            this.dayTextView = row.dayTextView
            this.minTempView = row.minTempView
            this.maxTempView = row.maxTempView
            this.dayIconView = row.dayIconView
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ForecastViewHolder {
        val view = LayoutInflater.from(p0.context)
            .inflate(R.layout.forecast_list_item, p0, false)
        return ForecastViewHolder(view)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        when (position) {
            0 -> holder.dayTextView?.text = "Today"
            1 -> holder.dayTextView?.text = "Tomorrow"
            else -> holder.dayTextView?.text = getDayOfWeek(items[position].time)
        }
        val min = items[position].temperatureMin.toInt()
        val max = items[position].temperatureMax.toInt()
        holder.minTempView?.text = "$min°"
        holder.maxTempView?.text = "$max°"
        val id = when(items[position].icon){
            "clear-day" -> R.drawable.icon_sun
            "clear-night" -> R.drawable.icon_sun
            "partly-cloudy-day" -> R.drawable.icon_clodysun
            "partly-cloudy-night" -> R.drawable.icon_clodysun
            "cloudy" -> R.drawable.icon_clouds
            "fog" -> R.drawable.icon_clouds
            "hail" -> R.drawable.icon_shower
            "tornado" -> R.drawable.icon_shower
            "rain" -> R.drawable.icon_rain
            "thunderstorm" -> R.drawable.icon_storm
            "snow" -> R.drawable.icon_snow
            "sleet" -> R.drawable.icon_snow
            else -> R.drawable.icon_sun
        }
        holder.dayIconView?.setImageResource(id)
    }

    override fun getItemCount() = items.size

    private fun getDayOfWeek(timestamp: Long) : String {
        val sdf = SimpleDateFormat("EEEE", Locale.US)
        val d = Date(timestamp*1000)
        return  sdf.format(d)
    }
}