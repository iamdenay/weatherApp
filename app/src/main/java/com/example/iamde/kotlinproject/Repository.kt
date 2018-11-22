package com.example.iamde.kotlinproject
import com.example.iamde.kotlinproject.models.Result
import okhttp3.*
import org.json.JSONException
import java.io.IOException
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.HttpUrl


interface OnRepositoryReadyCallback {
    fun onDataReady(data: Result)
}

class Repository {
    private val url = "https://api.darksky.net/forecast/"
    private val key = "7425ef4eefc95db29d111899b4251c53"
    private val client = OkHttpClient()
    private val gson = Gson()

    fun getCurrentWeather(lat:Double, long:Double, onRepositoryReadyCallback: OnRepositoryReadyCallback) {
        val urlBuilder = HttpUrl.parse("$url$key/$lat,$long")!!.newBuilder()
        urlBuilder.addQueryParameter("units", "si")
        val query = urlBuilder.build().toString()
        val request = Request.Builder().url(query) .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                try {
                    val gsonBuilder =  GsonBuilder()
                    val deserializer = ForecastDeserializer()
                    gsonBuilder.registerTypeAdapter(Result::class.java, deserializer)
                    val customGson = gsonBuilder.create()
                    val customObject = customGson.fromJson(response.body()?.string(), Result::class.java)
                    onRepositoryReadyCallback.onDataReady(customObject)
                } catch (e: JSONException) { }

            }
        })
    }
}

