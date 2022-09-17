package com.example.myfocusapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.myfocusapp.app.TimerActivity
import com.example.myfocusapp.presentation.TimerState
import com.example.myfocusapp.util.NotificationUtil
import com.example.myfocusapp.util.PrefUtil

class TimerExpiredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        NotificationUtil.showTimerExpired(context)

        PrefUtil.setTimerState(TimerState.STOPPED, context)
        PrefUtil.setAlarmSetTime(0, context)
    }
}