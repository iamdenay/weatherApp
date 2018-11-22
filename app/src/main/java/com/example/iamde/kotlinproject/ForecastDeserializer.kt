package com.example.iamde.kotlinproject

import com.example.iamde.kotlinproject.models.Forecast
import com.example.iamde.kotlinproject.models.Result
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class ForecastDeserializer: JsonDeserializer<Result>{
    private val gson = Gson()
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Result {
        val jsonObject = json.asJsonObject

        val current = gson.fromJson(jsonObject.get("currently").asJsonObject,
            Forecast::class.java)
        val hourly : List<Forecast> = gson.fromJson(jsonObject.get("hourly")
            .asJsonObject.getAsJsonArray("data"), object: TypeToken<List<Forecast>>(){}.type)
        val daily : List<Forecast> = gson.fromJson(jsonObject.get("daily")
            .asJsonObject.getAsJsonArray("data"), object: TypeToken<List<Forecast>>(){}.type)

        return Result(
            latitude = jsonObject.get("latitude").asLong,
            longitude = jsonObject.get("longitude").asLong,
            timezone = jsonObject.get("timezone").asString,
            currently = current,
            hourly = hourly,
            daily = daily
        )
    }
}