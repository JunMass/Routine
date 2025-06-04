package com.example.myapp.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.LocalDate


@Entity(
    tableName = "routine_records",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("routineId")]
)
@TypeConverters(LocalDateConverter::class)
data class RoutineRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val routineId: Int,    // RoutineEntity.id 와 매핑
    val date: LocalDate,   // 수행한 실제 날짜
    val detail: String?,   // 상세 내용
    val photoUri: String?  // 사진 URI
)
