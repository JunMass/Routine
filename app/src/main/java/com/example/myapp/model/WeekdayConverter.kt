package com.example.myapp.model

import androidx.room.TypeConverter

object WeekdayConverter {
    @TypeConverter
    fun fromWeekdaySet(days: Set<Weekday>?): String? =
        days?.joinToString(",") { it.name }

    @TypeConverter
    fun toWeekdaySet(value: String?): Set<Weekday>? =
        value?.split(",")?.mapNotNull { name ->
            Weekday.entries.find { it.name == name }
        }?.toSet()
}
