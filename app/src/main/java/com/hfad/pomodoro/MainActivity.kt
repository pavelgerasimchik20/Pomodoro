package com.hfad.pomodoro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.hfad.pomodoro.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(),LifecycleObserver, StopwatchListener  {
    private lateinit var binding: ActivityMainBinding
    private val stopWatches = mutableListOf<StopWatch>()
    private val stopwatchAdapter = StopwatchAdapter(this)
    private var nextId = 0
    private var startTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("MainActivity", "stopWatches: $stopWatches")
        //add observer
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.buttonAdd.isEnabled = false
        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter

        }
        binding.timeToSetEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.i("MainActivity", "onTextChanged is called")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.i("MainActivity", "onTextChanged is called")
                binding.buttonAdd.isEnabled = true
            }

            override fun afterTextChanged(s: Editable?) {
                Log.i("MainActivity", "onTextChanged is called")
            }


        })
        binding.buttonAdd.setOnClickListener {
            if (binding.timeToSetEditText.text.isNotEmpty()) {
                val timeToSet =
                    Integer.parseInt(binding.timeToSetEditText.text.toString()) * 1000 * 60
                stopWatches.add(StopWatch(nextId++, timeToSet.toLong(), false, timeToSet.toLong(),null))
                stopwatchAdapter.submitList(stopWatches.toList())
                hideKeyboard(this)
                binding.timeToSetEditText.text.clear()
                binding.buttonAdd.isEnabled = false
            } else {
                Toast.makeText(this, "Set timer in minutes", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun start(id: Int) {
        changeStopwatch(id, null, true)
    }

    override fun stop(id: Int, currentMs: Long) {
        changeStopwatch(id, currentMs, false)
    }


    override fun delete(id: Int) {
        stopWatches.remove(stopWatches.find { it.id == id })
        stopwatchAdapter.submitList(stopWatches.toList())
    }

    override fun finish(id: Int) {
        Toast.makeText( this, "Timer is finished!!!",Toast.LENGTH_SHORT).show()
    }

    private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean) {
        val newTimers = mutableListOf<StopWatch>()
        stopWatches.forEach {
            if(it.id == id){
                newTimers.add(StopWatch(it.id,currentMs?:it.currentMs,isStarted,it.period,it.timer))
            }
            else if (it.isStarted) {
                it.timer?.cancel()
                newTimers.add(StopWatch(it.id, currentMs ?: it.currentMs, false, it.period,it.timer))

            } else {
                newTimers.add(it)
            }
        }
        stopwatchAdapter.submitList(newTimers)
        stopWatches.clear()
        stopWatches.addAll(newTimers)
        Log.i("MainActivity","stopwatcheslist: $stopWatches")
    }

    private fun hideKeyboard(context: Context) {
        val inputMethod: InputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethod.hideSoftInputFromWindow(
            this.currentFocus?.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        Log.i("MainActivity","onAppBackgrounded is called")
        startTime = stopWatches.find { it.isStarted }?.currentMs ?: 0L
        //startTime = System.currentTimeMillis()
        Log.i("MainActivity","startTime = ${startTime.displayTime()}")
        if(startTime>0L) {
            val startIntent = Intent(this, ForegroundService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            startIntent.putExtra(STARTED_TIMER_TIME_MS, startTime)
            startService(startIntent)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        Log.i("MainActivity","onAppForegrounded is called")
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onAppDestroyed() {
        Log.i("MainActivity","onAppDestroyed is called")
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }

}