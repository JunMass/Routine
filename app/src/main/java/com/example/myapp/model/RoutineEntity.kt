package com.example.myapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.LocalTime

// 루틴 Entity
@Entity(tableName = "routines")
@TypeConverters(LocalTimeConverter::class, WeekdayConverter::class)
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,           // 루틴 ID
    val title: String,                // 예: "5분 책 읽기"
    val repeatOn: Set<Weekday>,       // MON, WED, FRI 등
    val startTime: LocalTime,         // 알람 시간 (예: 07:30)
    val isActive: Boolean = true      // 켜짐/꺼짐
)

