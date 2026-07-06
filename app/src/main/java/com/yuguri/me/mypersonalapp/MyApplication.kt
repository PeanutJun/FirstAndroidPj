package com.yuguri.me.mypersonalapp

import android.app.Application
import android.util.Log

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("MyApp", "Uncaught exception in thread: ${thread.name}", throwable)
        }
        
        Thread.setDefaultUncaughtExceptionHandler(Thread.UncaughtExceptionHandler { _, e ->
            Log.e("Crash", "App crashed", e)
        })
    }
}