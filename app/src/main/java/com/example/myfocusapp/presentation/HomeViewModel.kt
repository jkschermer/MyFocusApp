package com.example.myfocusapp.presentation

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myfocusapp.domain.TimerModel

class HomeViewModel : ViewModel() {

    private lateinit var _state: MutableLiveData<TimerState>
    var state: LiveData<TimerState> = _state
    private lateinit var _timerModel: MutableLiveData<TimerModel>
    var timerModel: LiveData<TimerModel> = _timerModel
    private lateinit var timer: CountDownTimer

    private fun startTimer() {
        var secondsRemaining = timerModel.value?.secondsRemaining?.times(1000)

        if (secondsRemaining != null)
            timer =
                object : CountDownTimer(secondsRemaining!!, COUNTDOWN_INTERVAL) {
                    override fun onFinish() {
                        onTimerFinished()
                    }

                    override fun onTick(millisUntilFinished: Long) {
                        secondsRemaining = millisUntilFinished / 1000
                        updateCountDownUI()
                    }
                }.start()
    }

    private fun updateCountDownUI() {
        TODO("Not yet implemented")
    }

    private fun onTimerFinished() {

    }

    private fun setSecondsRemaining() {

    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }

    companion object {
        val COUNTDOWN_INTERVAL = 1000L
    }
}

