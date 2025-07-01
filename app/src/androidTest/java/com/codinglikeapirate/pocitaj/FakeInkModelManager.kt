package com.codinglikeapirate.pocitaj

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.digitalink.Ink

object FakeInkModelManager : InkModelManager {
    var recognitionResult = "123"

    override fun setModel(languageTag: String): String {
        return "fake model set"
    }

    override fun deleteActiveModel(): Task<String?> {
        return Tasks.forResult("fake model deleted")
    }

    override fun download(): Task<String?> {
        return Tasks.forResult("fake model downloaded")
    }

    override suspend fun recognizeInk(ink: Ink, hint: String): String {
        return recognitionResult
    }
}
