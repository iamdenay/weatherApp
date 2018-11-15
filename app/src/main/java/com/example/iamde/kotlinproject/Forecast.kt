package com.example.iamde.kotlinproject

import com.google.gson.annotations.SerializedName

data class Result(
    val latitude:Long,
    val longitude:Long,
    val timezone:String,
    val currently:Forecast,
    val hourly:List<Forecast>,
    val daily:List<Forecast>)

data class Forecast(val time:Long,
                   val summary:String,
                   val icon:String,
                    val temperature:Double,
                    val temperatureMin:Double,
                    val temperatureMax:Double,
                    val humidity: Double,
                    val pressure : Double,
                    val visibility : Double)
