package com.example.myapp.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.room.Room
import com.example.myapp.model.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class RoutineViewModel(application: Application) : AndroidViewModel(application) {
    // 1) DB 인스턴스와 Repository 초기화
    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "app-db"
    )
        .fallbackToDestructiveMigration(true)
        .build()

    private val routineDao = db.routineDao()
    private val recordDao  = db.routineRecordDao()
    private val repository = RoutineRepository(routineDao, recordDao)

    val routines: LiveData<List<RoutineEntity>> = repository.getAllRoutines()

    // 루틴을 추가하는 함수
    fun addRoutine(
        title: String,
        repeatOn: Set<Weekday>,
        startTime: LocalTime,
        isActive: Boolean
    ) {
        val routine = RoutineEntity(
            title = title,
            repeatOn = repeatOn,
            startTime = startTime,
            isActive = isActive
        )
        viewModelScope.launch {
            repository.insertRoutine(routine)
        }
    }

    fun getRoutineById(id: Int): LiveData<RoutineEntity?> {
        return repository.getRoutineById(id)
    }

    fun getRecordsForRoutine(routineId: Int): LiveData<List<RoutineRecordEntity>> {
        return repository.getRecordsForRoutine(routineId)
    }

    fun getRecordsByDate(date: LocalDate): LiveData<List<RoutineRecordEntity>> {
        return repository.getRecordsByDate(date)
    }

    // 루틴 기록을 추가하는 함수
    fun addRecord(routineId: Int, date: LocalDate, detail: String, photoUri: String? = null) {
        val record = RoutineRecordEntity(
            routineId = routineId,
            date = date,
            detail = detail,
            photoUri = photoUri
        )
        viewModelScope.launch {
            repository.insertRecord(record)
        }
    }

    // 오늘 날짜의 루틴 ID와 일치하는 기록을 가져오는 함수
    suspend fun getTodayRecord( routineId: Int, today: LocalDate): RoutineRecordEntity? {
        return repository.getTodayRecord(routineId, today)
    }

    // 루틴 기록을 업데이트하는 함수
    fun updateRecord(record: RoutineRecordEntity) {
        viewModelScope.launch {
            repository.updateRecord(record)
        }
    }

    fun getRecordsWithTitleForDate(date: LocalDate): LiveData<List<RoutineRecordWithTitle>> {
        val result = MutableLiveData<List<RoutineRecordWithTitle>>()

        val recordsLive = getRecordsByDate(date)
        val routinesLive = routines

        recordsLive.observeForever { records ->
            val routineList = routinesLive.value ?: return@observeForever

            val combined = records.mapNotNull {record ->
                val title = routineList.find { it.id == record.routineId }?.title
                title?.let {
                    RoutineRecordWithTitle(record, it)
                }
            }
            result.value = combined
        }

        return result
    }
}
