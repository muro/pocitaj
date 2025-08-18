package dev.aidistillery.pocitaj

class TestApp : App() {
    override val globals: Globals by lazy {
        TestGlobals(this)
    }
}
