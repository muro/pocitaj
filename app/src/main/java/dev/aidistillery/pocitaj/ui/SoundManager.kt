package dev.aidistillery.pocitaj.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import dev.aidistillery.pocitaj.R

open class SoundManager(context: Context) {

    private val soundPool: SoundPool
    private val correctSoundId: Int
    private val wrongSoundId: Int
    private val unrecognizedSoundId: Int
    private val levelCompleteSoundId: Int

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        correctSoundId = soundPool.load(context, R.raw.correct, 1)
        wrongSoundId = soundPool.load(context, R.raw.sad, 1)
        unrecognizedSoundId = soundPool.load(context, R.raw.unrecognized, 1)
        levelCompleteSoundId = soundPool.load(context, R.raw.level_complete, 1)
    }

    open fun playCorrect() {
        soundPool.play(correctSoundId, 1f, 1f, 1, 0, 1f)
    }

    open fun playWrong() {
        soundPool.play(wrongSoundId, 1f, 1f, 1, 0, 1f)
    }

    open fun playUnrecognized() {
        soundPool.play(unrecognizedSoundId, 1f, 1f, 1, 0, 1f)
    }

    open fun playLevelComplete() {
        soundPool.play(levelCompleteSoundId, 1f, 1f, 1, 0, 1f)
    }

    @Suppress("unused")
    open fun release() {
        soundPool.release()
    }
}
