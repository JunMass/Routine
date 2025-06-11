package com.example.myapp.ui.home

import android.app.Application
import androidx.lifecycle.*
import com.example.myapp.weather.LocationProvider
import com.example.myapp.weather.WeatherRepository
import com.example.myapp.weather.WeatherResponse

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val _weatherInfo = MutableLiveData<WeatherResponse>()
    val weatherInfo: LiveData<WeatherResponse> = _weatherInfo

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var isLoaded = false

    fun loadWeather(locationProvider: LocationProvider, weatherRepository: WeatherRepository) {
        if (isLoaded) return
        isLoaded = true

        locationProvider.getLocation { lat, lon ->
            weatherRepository.getWeather(
                lat, lon,
                onSuccess = { temp, iconUrl, cityName ->
                    // WeatherResponse는 (main, weather, name) 구조이므로 임시 객체 생성
                    val response = WeatherResponse(
                        main = com.example.myapp.weather.Main(temp, 0),
                        weather = listOf(com.example.myapp.weather.Weather("", iconUrl)),
                        name = cityName
                    )
                    _weatherInfo.postValue(response)
                },
                onFailure = {
                    _error.postValue("날씨 정보를 불러오지 못했습니다")
                }
            )
        }
    }
}