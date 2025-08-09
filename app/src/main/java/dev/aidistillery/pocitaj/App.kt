package dev.aidistillery.pocitaj

import android.app.Application

open class App : Application() {
    open val globals: Globals by lazy {
        ProductionGlobals(this)
    }

    override fun onCreate() {
        super.onCreate()
        app = this
    }

    companion object {
        lateinit var app: App
    }
}
