package com.codinglikeapirate.pocitaj

class TestApp : App() {
    override fun onCreate() {
        super.onCreate()
        inkModelManager = FakeInkModelManager
        exerciseSource = ExerciseBook()
    }
}
