package com.example.myapp.Alarm

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.myapp.R
import com.example.myapp.model.AppDatabase
import com.example.myapp.model.Weekday
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val routineId = intent.getIntExtra("ROUTINE_ID", 0)
        if (routineId == 0) return

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(context)
            val today = LocalDate.now()

            // 1. 오늘 날짜의 루틴 기록이 있는지 확인
            val recordCount = db.routineRecordDao().getRecordCountForToday(routineId, today)

            // 2. 기록이 없을 때만 (0개일 때만) 알림을 표시
            if (recordCount == 0) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val routineTitle = intent.getStringExtra("ROUTINE_TITLE") ?: "루틴 시간입니다!"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        "routine_alarm_channel",
                        "루틴 알림",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "설정한 루틴의 시작을 알립니다."
                    }
                    notificationManager.createNotificationChannel(channel)
                }

                val builder = NotificationCompat.Builder(context, "routine_alarm_channel")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(routineTitle)
                    .setContentText("오늘의 루틴을 실천할 시간이에요! 💪")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)

                notificationManager.notify(routineId, builder.build())
            }

            // 3. 알림 표시 여부와 상관없이, 다음 알람은 항상 다시 설정
            rescheduleNextAlarm(context, routineId)
        }
    }

    private fun rescheduleNextAlarm(context: Context, routineId: Int) {
        // 백그라운드에서 DB 작업 수행
        CoroutineScope(Dispatchers.IO).launch {
            // AppDatabase 싱글턴 인스턴스를 통해 DAO에 접근
            val db = AppDatabase.getInstance(context)
            val routine = db.routineDao().getRoutineByIdSuspend(routineId) // DAO에 추가한 suspend 함수 사용

            if (routine != null && routine.isActive) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val nextAlarmTime = getNextAlarmTime(routine.startTime, routine.repeatOn)

                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("ROUTINE_ID", routine.id)
                    putExtra("ROUTINE_TITLE", routine.title)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    routine.id,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextAlarmTime.timeInMillis,
                    pendingIntent
                )
            }
        }
    }

    // 다음 알람 시간을 계산하는 헬퍼 함수
    private fun getNextAlarmTime(startTime: LocalTime, repeatOn: Set<Weekday>): Calendar {
        val now = Calendar.getInstance()

        // 오늘부터 7일간 확인
        for (i in 0..7) {
            val nextPossibleDay = Calendar.getInstance()
            nextPossibleDay.add(Calendar.DAY_OF_YEAR, i)
            val dayOfWeek = nextPossibleDay.get(Calendar.DAY_OF_WEEK)

            if (repeatOn.contains(intToWeekday(dayOfWeek))) {
                val nextAlarmTime = Calendar.getInstance().apply {
                    time = nextPossibleDay.time // 기준 날짜를 i일 후로 설정
                    set(Calendar.HOUR_OF_DAY, startTime.hour)
                    set(Calendar.MINUTE, startTime.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // 계산된 알람 시간이 현재 시간보다 미래인 경우에만 유효
                if (nextAlarmTime.after(now)) {
                    return nextAlarmTime
                }
            }
        }

        // 만약 7일 내에 유효한 다음 알람이 없다면, 가장 빠른 요일에 다음주로 설정
        val fallbackDay = Calendar.getInstance()
        fallbackDay.add(Calendar.DAY_OF_YEAR, 7)
        return fallbackDay
    }

    // Calendar의 요일(Int)을 Weekday Enum으로 변환
    private fun intToWeekday(dayOfWeek: Int): Weekday {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> Weekday.SUNDAY
            Calendar.MONDAY -> Weekday.MONDAY
            Calendar.TUESDAY -> Weekday.TUESDAY
            Calendar.WEDNESDAY -> Weekday.WEDNESDAY
            Calendar.THURSDAY -> Weekday.THURSDAY
            Calendar.FRIDAY -> Weekday.FRIDAY
            Calendar.SATURDAY -> Weekday.SATURDAY
            else -> throw IllegalArgumentException("Invalid day of week")
        }
    }
}