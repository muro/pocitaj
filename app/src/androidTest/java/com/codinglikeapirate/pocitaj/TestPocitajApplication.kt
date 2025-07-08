package com.codinglikeapirate.pocitaj

import com.codinglikeapirate.pocitaj.data.ExerciseSource

class TestPocitajApplication : PocitajApplication() {
    override fun onCreate() {
        super.onCreate()
        inkModelManager = FakeInkModelManager
        exerciseSource = ExerciseBook()
    }
}
