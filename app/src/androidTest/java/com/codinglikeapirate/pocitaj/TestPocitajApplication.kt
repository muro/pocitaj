package com.codinglikeapirate.pocitaj

class TestPocitajApplication : PocitajApplication() {
    override fun onCreate() {
        super.onCreate()
        inkModelManager = FakeInkModelManager
        exerciseSource = ExerciseBook()
    }
}
