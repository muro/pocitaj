package dev.aidistillery.pocitaj

class TestApp : App() {
    override fun onCreate() {
        super.onCreate()
        globals = TestGlobals(this)
    }
}
