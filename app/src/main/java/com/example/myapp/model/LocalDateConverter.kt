package com.example.myapp.model

import androidx.room.TypeConverter
import java.time.LocalDate

enum class Weekday {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}

class LocalDateConverter {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? = date?.toEpochDay()
    @TypeConverter
    fun toLocalDate(epoch: Long?): LocalDate? = epoch?.let { LocalDate.ofEpochDay(it) }
}