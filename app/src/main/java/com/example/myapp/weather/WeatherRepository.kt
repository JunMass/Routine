package com.example.myapp.weather

import android.content.Context
import android.util.Log
import com.example.myapp.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class WeatherRepository(private val context: Context) {
    private val apiKey = context.getString(R.string.open_weather_api_key)

    fun getWeather(lat: Double, lon: Double, onSuccess: (String) -> Unit, onFailure: () -> Unit) {
        val weatherService = ApiClient.getWeatherService()
        val call = weatherService.getCurrentWeather(lat, lon, apiKey)

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weather = response.body()
                    Log.d("WeatherRepository", "Weather data: $weather")
                    weather?.let {
                        val temp = it.main.temp - 273.15
                        val description = it.weather[0].description
                        onSuccess("Temperature: %.1f°C\nDescription: %s".format(temp, description))
                    } ?: onFailure()
                } else {
                    onFailure()
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.e("WeatherDebug", "API 요청 실패: ${t.message}")
                onFailure()
            }
        })
    }
}