package edu.bu.terriertrade

import android.app.Application
import edu.bu.terriertrade.di.AppContainer
import edu.bu.terriertrade.di.DefaultAppContainer

/** Registered in AndroidManifest.xml. Builds the AppContainer once when the app starts. */
class TerrierTradeApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
