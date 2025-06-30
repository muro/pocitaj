package com.codinglikeapirate.pocitaj

import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.digitalink.Ink

interface InkModelManager {
    /** Sets the model for a given language tag. */
    fun setModel(languageTag: String): String

    /** Deletes the active model. */
    fun deleteActiveModel(): Task<String?>

    /** Downloads the active model. */
    fun download(): Task<String?>

    /**
     * Recognizes the ink from the user's drawing.
     * @param hint A hint to the recognizer to improve accuracy.
     */
    fun recognizeInk(
        ink: Ink,
        hint: String,
        onResult: (String) -> Unit
    )
}
