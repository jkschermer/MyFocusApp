package com.example.myfocusapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.myfocusapp.app.AppConstants
import com.example.myfocusapp.app.TimerActivity.Companion.nowSeconds
import com.example.myfocusapp.app.TimerActivity.Companion.removeAlarm
import com.example.myfocusapp.app.TimerActivity.Companion.setAlarm
import com.example.myfocusapp.app.TimerActivity
import com.example.myfocusapp.presentation.TimerState
import com.example.myfocusapp.util.NotificationUtil
import com.example.myfocusapp.util.PrefUtil

class TimerNotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            AppConstants.ACTION_STOP -> {
                removeAlarm(context)
                PrefUtil.setTimerState(TimerState.STOPPED, context)
                NotificationUtil.hideTimerNotification(context)
            }

            AppConstants.ACTION_PAUSE -> {
                var secondsRemaining = PrefUtil.getSecondsRemaining(context)
                val alarmSetTime = PrefUtil.getAlarmSetTime(context)
                val nowSeconds = nowSeconds

                secondsRemaining -= nowSeconds - alarmSetTime
                PrefUtil.setSecondsRemaining(secondsRemaining,context)

                removeAlarm(context)
                PrefUtil.setTimerState(TimerState.PAUSED, context)
                NotificationUtil.showTimerPaused(context)
            }

            AppConstants.ACTION_RESUME -> {
                val secondsRemaining = PrefUtil.getSecondsRemaining(context)
                val wakeUpTime = setAlarm(context, nowSeconds, secondsRemaining)
                PrefUtil.setTimerState(TimerState.STARTED, context)
                NotificationUtil.showTimerRunning(context, wakeUpTime)
            }

            AppConstants.ACTION_START -> {
                val minutesRemaining = PrefUtil.getTimerLength(context)
                val secondsRemaining = minutesRemaining * 60L
                val wakeUpTime = setAlarm(context, nowSeconds, secondsRemaining)
                PrefUtil.setTimerState(TimerState.STARTED, context)
                PrefUtil.setSecondsRemaining(secondsRemaining, context)
                NotificationUtil.showTimerRunning(context, wakeUpTime)
            }
        }
    }
}