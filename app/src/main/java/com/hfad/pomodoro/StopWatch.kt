package com.hfad.pomodoro

import android.os.CountDownTimer

data class StopWatch(
    val id:Int,
    var currentMs:Long,
    var isStarted:Boolean,
    val period:Long,
    var timer:CountDownTimer?
)
