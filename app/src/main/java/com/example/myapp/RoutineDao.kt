package com.example.myapp.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import java.time.LocalDate

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun getRoutineByIdSuspend(id: Int): RoutineEntity?
    /** 전체 루틴 리스트를 LiveData로 반환 */
    @Query("SELECT * FROM routines")
    fun getAllRoutines(): LiveData<List<RoutineEntity>>

    /** 특정 ID의 루틴을 LiveData로 반환 */
    @Query("SELECT * FROM routines WHERE id = :id")
    fun getRoutineById(id: Int): LiveData<RoutineEntity?>

    /** 루틴 삽입(이미 ID가 존재하면 교체) */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    /** 루틴 업데이트 */
    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    /** 루틴 삭제 */
    @Delete
    suspend fun deleteRoutine(routine: RoutineEntity)

}
