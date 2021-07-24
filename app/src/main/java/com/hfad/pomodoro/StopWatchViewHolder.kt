package com.hfad.pomodoro

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import android.util.Log
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.hfad.pomodoro.databinding.RecyclerWatchItemBinding

class StopWatchViewHolder(
    private val binding: RecyclerWatchItemBinding,
    private val listener:StopwatchListener,
    private val resources: Resources
) :
    RecyclerView.ViewHolder(binding.root) {

    private var timer: CountDownTimer? = null

    fun bind(stopWatch: StopWatch) {
        binding.timerText.text = stopWatch.currentMs.displayTime()

        binding.customViewTimer.setPeriod(stopWatch.period)
        binding.customViewTimer.setCurrent(stopWatch.currentMs)
        binding.buttonStart.text = "START"

        if(stopWatch.isStarted){
            startTimer(stopWatch)
        }else{
            stopTimer(stopWatch)
        }
        initButtonsListeners(stopWatch)
    }

    private fun initButtonsListeners(stopWatch: StopWatch) {
        binding.buttonStart.setOnClickListener {
            stopWatch.timer?.cancel()
            if (stopWatch.isStarted) {
                Log.i("StopWatchViewHolder","init button listener stop called")

                listener.stop(stopWatch.id, stopWatch.currentMs)
            } else {

                Log.i("StopWatchViewHolder","init button listener start called")
                listener.start(stopWatch.id)
            }
        }

        binding.imageViewBin.setOnClickListener { listener.delete(stopWatch.id) }
    }

    private fun startTimer(stopWatch: StopWatch) {
        binding.buttonStart.text = "STOP"

        Log.i("stopwatchviewholder", "startTimer is called period = ${stopWatch.period}")

        stopWatch.timer?.cancel()
        stopWatch.timer = getCountDownTimer(stopWatch)
        stopWatch.timer?.start()

        binding.customViewTimer.setPeriod(stopWatch.period)
        binding.blinkingView.isInvisible = false
        (binding.blinkingView.background as? AnimationDrawable)?.start()

    }

    private fun stopTimer(stopWatch: StopWatch) {
        Log.i("StopWatchViewHolder", "stopTimer is called")
        binding.buttonStart.text = "START"
        //
        Log.i("StopWatchViewHolder", "$stopWatch")

        stopWatch.timer?.cancel()


        binding.blinkingView.isInvisible = true
        (binding.blinkingView.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownTimer(stopWatch: StopWatch): CountDownTimer? {
        Log.i("Stopwatchviewholder", "getCountDownTimer is called")
        return object : CountDownTimer(stopWatch.currentMs, UNIT_TEN_MS) {
            override fun onTick(millisUntilFinished: Long) {
                Log.i("StopWatchViewHolder", "getcountdowntimer is: ${millisUntilFinished / 1000}")
                binding.timerText.text = millisUntilFinished.displayTime()
                stopWatch.currentMs = millisUntilFinished
                binding.customViewTimer.setCurrent(millisUntilFinished)
            }

            override fun onFinish() {
                Log.i(
                    "Stopwatchviewholder",
                    "on Finish is called, currentMs = ${stopWatch.currentMs}"
                )
                listener.finish(stopWatch.id)
                binding.customViewTimer.setCurrent(0L)
                binding.timerText.text = stopWatch.currentMs.displayTime()
                stopTimer(stopWatch)
                binding.buttonStart.text = "START"
            }

        }
    }

}

