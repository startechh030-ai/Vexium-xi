package lux.obris.app

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ObrisApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("ObrisApp", "Application onCreate")

        // Global crash handler to log crashes
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("ObrisCrash", "UNCAUGHT EXCEPTION on ${thread.name}: ${throwable.message}", throwable)
        }
    }
}
