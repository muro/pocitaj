package dev.aidistillery.pocitaj

class TestApp : App() {
    override lateinit var globals: Globals

    override fun onCreate() {
        super.onCreate()
        globals = TestGlobals(this)
    }
}
