package com.example.myapp.model

import androidx.room.TypeConverter
import java.time.LocalTime

object LocalTimeConverter {
    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? = time?.toString()
    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it) }
}
