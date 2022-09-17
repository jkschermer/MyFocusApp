package com.example.myfocusapp.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.example.myfocusapp.R
import com.example.myfocusapp.TimerExpiredReceiver
import com.example.myfocusapp.databinding.ActivityTimerBinding
import com.example.myfocusapp.presentation.TimerState
import com.example.myfocusapp.util.NotificationUtil
import com.example.myfocusapp.util.PrefUtil
import java.util.*

class TimerActivity : AppCompatActivity(R.layout.activity_timer) {

    private lateinit var ui: ActivityTimerBinding
    private lateinit var timer: CountDownTimer
    private var timerLengthSeconds: Long = 0
    private var timerState = TimerState.STOPPED
    private var secondsRemaining: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityTimerBinding.inflate(layoutInflater)
        val view = ui.root
        setContentView(view)
        supportActionBar?.setIcon(R.drawable.ic_timer)
        supportActionBar?.title = " Focus App"

        initClickListeners()
        supportActionBar?.hide()
    }

    private fun initClickListeners() {
        ui.btnStart.setOnClickListener {
            startTimer()
            timerState = TimerState.STARTED
            updateButtons()
        }

        ui.btnStop.setOnClickListener {
            timer.cancel()
            onTimerFinished()
        }

        ui.btnPause.setOnClickListener {
            timerState = TimerState.PAUSED
            timer.cancel()
            updateButtons()
        }
    }

    override fun onResume() {
        super.onResume()

        initTimer()
        removeAlarm(this)
        NotificationUtil.hideTimerNotification(this)
    }

    override fun onPause() {
        super.onPause()

        if (timerState == TimerState.STARTED) {
            timer.cancel()
            val wakeUpTime = setAlarm(this, nowSeconds, secondsRemaining)
            NotificationUtil.showTimerRunning(this, wakeUpTime)
        } else if (timerState == TimerState.PAUSED) {
            NotificationUtil.showTimerPaused(this)
        }

        PrefUtil.setPreviousTimerLengthSeconds(timerLengthSeconds, this)
        PrefUtil.setSecondsRemaining(secondsRemaining, this)
        PrefUtil.setTimerState(timerState, this)
    }

    private fun initTimer() {
        timerState = PrefUtil.getTimerState(this)

        if (timerState == TimerState.STOPPED) {
            setNewTimerLength()
        } else {
            setPreviousTimerLength()
        }

        secondsRemaining =
            if (timerState == TimerState.STARTED || timerState == TimerState.PAUSED)
                PrefUtil.getSecondsRemaining(this)
            else {
                timerLengthSeconds
            }

        val alarmSetTime = PrefUtil.getAlarmSetTime(this)

        if (alarmSetTime > 0) {
            secondsRemaining -= nowSeconds - alarmSetTime
        }
        if (secondsRemaining <= 0) {
            onTimerFinished()
        } else {
            if (timerState == TimerState.STARTED) {
                startTimer()
            }
        }

        updateButtons()
        updateCountDownUI()
    }

    private fun startTimer() {
        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
            override fun onFinish() = onTimerFinished()

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                updateCountDownUI()
            }
        }.start()
    }

    private fun onTimerFinished() {
        timerState = TimerState.STOPPED

        setNewTimerLength()
        ui.progressBar.progress = 0
        PrefUtil.setSecondsRemaining(timerLengthSeconds, this)
        secondsRemaining = timerLengthSeconds
        updateButtons()
        updateCountDownUI()
    }

    private fun setNewTimerLength() {
        val lengthInMinutes = PrefUtil.getTimerLength(this)
        timerLengthSeconds = (lengthInMinutes * 60L)
        ui.progressBar.max = timerLengthSeconds.toInt()
    }

    private fun setPreviousTimerLength() {
        timerLengthSeconds = PrefUtil.getPreviousTimerLengthSeconds(this)
        ui.progressBar.max = timerLengthSeconds.toInt()
    }


    private fun updateButtons() {
        when (timerState) {
            TimerState.STARTED -> {
                ui.btnStart.isEnabled = false
                ui.btnStop.isEnabled = true
                ui.btnPause.isEnabled = true
            }
            TimerState.PAUSED -> {
                ui.btnStart.isEnabled = true
                ui.btnStop.isEnabled = true
                ui.btnPause.isEnabled = false
            }
            TimerState.STOPPED -> {
                ui.btnStart.isEnabled = true
                ui.btnStop.isEnabled = false
                ui.btnPause.isEnabled = false
            }
        }
    }

    private fun updateCountDownUI() {
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining % 60
        val secondsStr = secondsInMinuteUntilFinished.toString()
        ui.progressTv.text = "$minutesUntilFinished:${
            if (secondsStr.length == 2) secondsStr else "0" + secondsStr
        }"
        ui.progressBar.progress =
            (timerLengthSeconds - secondsRemaining).toInt()
    }

    companion object {

        fun setAlarm(context: Context, nowSeconds: Long, secondsRemaining: Long): Long {
            val wakeupTime = (nowSeconds + secondsRemaining) * 1000
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeupTime, pendingIntent)
            PrefUtil.setAlarmSetTime(nowSeconds, context)

            return wakeupTime
        }

        fun removeAlarm(context: Context) {
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            PrefUtil.setAlarmSetTime(0, context)
        }

        val nowSeconds: Long
            get() = Calendar.getInstance().timeInMillis / 1000
    }
}