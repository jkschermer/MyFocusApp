package com.example.myfocusapp.util

import android.os.CountDownTimer
import com.example.myfocusapp.app.TimerActivity
import com.example.myfocusapp.presentation.TimerState


class TimerUtil {

    companion object {
        var timerLengthSeconds: Long = 0L
        var secondsRemaining: Long = 0L
        var timerState = TimerState.STOPPED
        lateinit var timer: CountDownTimer
    }
}