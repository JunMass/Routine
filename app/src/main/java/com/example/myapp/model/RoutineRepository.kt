package com.example.myapp.model

import androidx.lifecycle.LiveData
import java.time.LocalDate

class RoutineRepository(
    private val routineDao: RoutineDao,
    private val recordDao: RoutineRecordDao
) {
    // ────────────────── RoutineEntity CRUD ──────────────────

    /** 전체 루틴 목록 조회 */
    fun getAllRoutines(): LiveData<List<RoutineEntity>> =
        routineDao.getAllRoutines()

    /** 특정 ID 의 루틴 조회 */
    fun getRoutineById(id: Int): LiveData<RoutineEntity?> =
        routineDao.getRoutineById(id)

    /** 루틴 삽입 (반환값은 새로 생성된 PK) */
    suspend fun insertRoutine(routine: RoutineEntity): Long =
        routineDao.insertRoutine(routine)

    /** 루틴 수정 */
    suspend fun updateRoutine(routine: RoutineEntity) =
        routineDao.updateRoutine(routine)

    /** 루틴 삭제 */
    suspend fun deleteRoutine(routine: RoutineEntity) =
        routineDao.deleteRoutine(routine)


    /** 특정 날짜에 수행된 루틴 조회
    fun getPerformedRoutinesByDate(date: LocalDate): LiveData<List<RoutineEntity>> =
        routineDao.getPerformedRoutinesByDate(date)*/


    // ────────────────── RoutineRecordEntity CRUD ──────────────────

    /** 특정 루틴의 수행 기록 전체 조회 */
    fun getRecordsForRoutine(routineId: Int): LiveData<List<RoutineRecordEntity>> =
        recordDao.getRecordsForRoutine(routineId)

    /** 특정 날짜의 모든 수행 기록 조회 */
    fun getRecordsByDate(date: LocalDate): LiveData<List<RoutineRecordEntity>> =
        recordDao.getRecordsByDate(date)

    /** 수행 기록 삽입 (반환값은 새로 생성된 PK) */
    suspend fun insertRecord(record: RoutineRecordEntity): Long =
        recordDao.insertRecord(record)

    /** 수행 기록 수정 */
    suspend fun updateRecord(record: RoutineRecordEntity) =
        recordDao.updateRecord(record)

    /** 수행 기록 삭제 */
    suspend fun deleteRecord(record: RoutineRecordEntity) =
        recordDao.deleteRecord(record)

    /** 특정 날짜에 수행된 루틴 조회 */
    suspend fun getTodayRecord(routineId: Int, todayDate: LocalDate): RoutineRecordEntity? =
        recordDao.getTodayRecord(routineId, todayDate)
}
