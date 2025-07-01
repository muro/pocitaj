package com.codinglikeapirate.pocitaj

class TestPocitajApplication : PocitajApplication() {
    override val inkModelManager: InkModelManager
        get() = FakeInkModelManager
}
