package com.example.myapp.weather

import android.content.Context
import android.util.Log
import com.example.myapp.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class WeatherRepository(context: Context) {
    private val apiKey = context.getString(R.string.open_weather_api_key)

    val cityNameMap = mapOf(
        "Seoul" to "서울특별시",
        "Busan" to "부산광역시",
        "Daegu" to "대구광역시",
        "Incheon" to "인천광역시",
        "Daejeon" to "대전광역시",
        "Gwangju" to "광주광역시",
        "Ulsan" to "울산광역시",
        "Pohang" to "포항시",
        "Gyeongju" to "경주시",
        "Gimcheon" to "김천시",
        "Andong" to "안동시",
        "Gumi" to "구미시",
        "Eisen" to "영주시", // 또는 영천시
        "Sangju" to "상주시",
        "Mungyeong" to "문경시",
        "Gyeongsan-si" to "경산시",
        "Changwon" to "창원시",
        "Chinju" to "진주시",
        "T’aep’ong-dong" to "통영시",
        "Seisan-ri" to "사천시",
        "Kimhae" to "김해시",
        "Miryang" to "밀양시",
        "Sinhyeon" to "거제시",
        "Yangsan" to "양산시"
    )

    fun getWeather(
        lat: Double,
        lon: Double,
        onSuccess: (Double, String, String) -> Unit, // (온도, 아이콘URL, 도시명)
        onFailure: () -> Unit
    ) {
        val weatherService = ApiClient.getWeatherService()
        val call = weatherService.getCurrentWeather(lat, lon, apiKey, lang = "kr", units = "metric")

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weather = response.body()
                    weather?.let {
                        val temp = it.main.temp
                        val icon = it.weather[0].icon
                        Log.d("WeatherRepository", "온도: $temp, 아이콘: $icon")
                        val iconUrl = "https://openweathermap.org/img/wn/${icon}@2x.png"
                        val cityName = cityNameMap[it.name] ?: it.name
                        onSuccess(temp, iconUrl, cityName)
                    } ?: onFailure()
                } else {
                    onFailure()
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                onFailure()
            }
        })
    }
}