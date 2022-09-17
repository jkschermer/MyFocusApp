package com.example.myfocusapp.app

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.myfocusapp.R
import com.example.myfocusapp.app.TimerActivity.Companion.nowSeconds
import com.example.myfocusapp.app.TimerActivity.Companion.removeAlarm
import com.example.myfocusapp.app.TimerActivity.Companion.setAlarm
import com.example.myfocusapp.databinding.FragmentHomeBinding
import com.example.myfocusapp.presentation.HomeViewModel
import com.example.myfocusapp.presentation.TimerState
import com.example.myfocusapp.util.NotificationUtil
import com.example.myfocusapp.util.PrefUtil
import com.example.myfocusapp.util.TimerUtil.Companion.secondsRemaining
import com.example.myfocusapp.util.TimerUtil.Companion.timer
import com.example.myfocusapp.util.TimerUtil.Companion.timerLengthSeconds
import com.example.myfocusapp.util.TimerUtil.Companion.timerState

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by viewModels()
    private var _ui: FragmentHomeBinding? = null
    private val ui get() = _ui

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _ui = FragmentHomeBinding.inflate(inflater, container, false)
        return ui?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ui?.btnStart?.setOnClickListener { v ->
            startTimer()
            timerState = TimerState.STARTED
            updateButtons()
        }

        ui?.btnStop?.setOnClickListener { v ->
            timer.cancel()
            timerState = TimerState.STOPPED
            updateButtons()
        }

        ui?.btnPause?.setOnClickListener { v ->
            timerState = TimerState.PAUSED
            timer.cancel()
            updateButtons()
        }
    }

    override fun onResume() {
        super.onResume()

        initTimer()
        removeAlarm(requireContext())
        NotificationUtil.hideTimerNotification(requireContext())
    }

    override fun onPause() {
        super.onPause()

        if (timerState == TimerState.STARTED) {
            timer.cancel()
            val wakeUpTime = setAlarm(requireContext(), nowSeconds, secondsRemaining)
            NotificationUtil.showTimerRunning(requireContext(), wakeUpTime)
        } else if (timerState == TimerState.PAUSED) {
            NotificationUtil.showTimerPaused(requireContext())
        }

        PrefUtil.setPreviousTimerLengthSeconds(timerLengthSeconds, requireContext())
        PrefUtil.setSecondsRemaining(secondsRemaining, requireContext())
        PrefUtil.setTimerState(timerState, requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _ui = null
    }

    private fun onTimerFinished() {
        setNewTimerLength()

        ui?.progressBar?.progress = 0
        PrefUtil.setSecondsRemaining(timerLengthSeconds, requireContext())
        secondsRemaining = timerLengthSeconds

        updateButtons()
        updateCountDownUI()
    }

    private fun setNewTimerLength() {
        val lengthInMinutes = PrefUtil.getTimerLength(requireContext())
        timerLengthSeconds = (lengthInMinutes * 60L)
        ui?.progressBar?.max = timerLengthSeconds.toInt()
    }

    private fun setPreviousTimerLength() {
        timerLengthSeconds = PrefUtil.getPreviousTimerLengthSeconds(requireContext())
        ui?.progressBar?.max = timerLengthSeconds.toInt()
    }

    private fun initTimer() {
        timerState = PrefUtil.getTimerState(requireContext())

        if (timerState == TimerState.STOPPED) {
            setNewTimerLength()
        } else {
            setPreviousTimerLength()
        }

        secondsRemaining =
            if (timerState == TimerState.STARTED || timerState == TimerState.PAUSED)
                PrefUtil.getSecondsRemaining(requireContext())
            else {
                timerLengthSeconds
            }


        val alarmSetTime = PrefUtil.getAlarmSetTime(requireContext())

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


    private fun updateButtons() {
        when (timerState) {
            TimerState.STARTED -> {
                ui?.btnStart?.isEnabled = false
                ui?.btnStop?.isEnabled = true
                ui?.btnPause?.isEnabled = true
            }
            TimerState.PAUSED -> {
                ui?.btnStart?.isEnabled = true
                ui?.btnStop?.isEnabled = true
                ui?.btnPause?.isEnabled = false
            }
            TimerState.STOPPED -> {
                ui?.btnStart?.isEnabled = true
                ui?.btnStop?.isEnabled = false
                ui?.btnPause?.isEnabled = false
            }
        }
    }

    private fun updateCountDownUI() {
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining % 60
        val secondsStr = secondsInMinuteUntilFinished.toString()
        ui?.progressTv?.text = "$minutesUntilFinished:${
            if (secondsStr.length == 2) secondsStr else "0" + secondsStr
        }"
        ui?.progressBar?.progress = (timerLengthSeconds - secondsRemaining).toInt()
    }
}