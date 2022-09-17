package com.example.myfocusapp.domain

data class TimerModel(
    var secondsRemaining: Long,
    val currentSeconds: Long,
    var timerLengthSeconds: Long
)
