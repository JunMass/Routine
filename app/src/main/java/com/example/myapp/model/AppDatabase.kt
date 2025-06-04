package com.example.myapp.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        RoutineEntity::class,
        RoutineRecordEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(
    LocalDateConverter::class,
    LocalTimeConverter::class,
    WeekdayConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routineDao(): RoutineDao
    abstract fun routineRecordDao(): RoutineRecordDao
}