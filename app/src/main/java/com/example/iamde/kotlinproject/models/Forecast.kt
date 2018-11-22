package com.example.iamde.kotlinproject.models


data class Forecast(val time:Long,
                   val summary:String,
                   val icon:String,
                    val temperature:Double,
                    val temperatureMin:Double,
                    val temperatureMax:Double,
                    val humidity: Double,
                    val pressure : Double,
                    val visibility : Double)
