package com.example.myapp.weather

import android.app.Activity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import android.location.Location


class LocationProvider (
    private val activity: Activity,
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
) {
    fun getLocation(onResult: (Double, Double) -> Unit) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(activity, "위치 권한이 없습니다", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.getCurrentLocation(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null
        ).addOnSuccessListener { location: Location? ->
            if (location != null) {
                onResult(location.latitude, location.longitude)
            } else {
                Toast.makeText(activity, "위치를 가져올 수 없습니다", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(activity, "위치 요청 실패", Toast.LENGTH_SHORT).show()
        }
    }
}