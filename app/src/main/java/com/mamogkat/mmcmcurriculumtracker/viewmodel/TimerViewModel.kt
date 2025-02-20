package com.mamogkat.mmcmcurriculumtracker.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {
    // Mutable state for the resend process and countdown (in seconds)
    private val _isResending = mutableStateOf(false)
    val isResending: State<Boolean> get() = _isResending

    private val _resendTime = mutableStateOf(0)
    val resendTime: State<Int> get() = _resendTime

    /**
     * Starts the timer with a given duration (in seconds, default is 300 seconds for 5 minutes).
     * The timer will count down each second until it reaches 0.
     */
    fun startTimer(duration: Int = 300) {
        _resendTime.value = duration
        _isResending.value = true
        viewModelScope.launch {
            while (_resendTime.value > 0) {
                delay(1000L)
                _resendTime.value = _resendTime.value - 1
            }
            _isResending.value = false
        }
    }

    fun resetTimer() {
        _resendTime.value = 0
        _isResending.value = false
    }
}