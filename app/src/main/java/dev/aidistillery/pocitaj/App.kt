package dev.aidistillery.pocitaj

import android.app.Application

open class App : Application() {
    open lateinit var globals: Globals

    override fun onCreate() {
        super.onCreate()
        globals = ProductionGlobals(this)
        app = this
    }

    companion object {
        lateinit var app: App
    }
}
