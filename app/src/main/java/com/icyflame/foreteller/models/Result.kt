package com.icyflame.foreteller.models

data class Result(
    val latitude:Long,
    val longitude:Long,
    val timezone:String,
    val currently: Forecast,
    val hourly:List<Forecast>,
    val daily:List<Forecast>)