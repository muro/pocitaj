package dev.aidistillery.pocitaj

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import dev.aidistillery.pocitaj.data.AdaptiveExerciseSource
import dev.aidistillery.pocitaj.data.ExerciseSource
import dev.aidistillery.pocitaj.data.User
import kotlinx.coroutines.runBlocking
import org.junit.Before

class AdaptiveTestGlobals(context: Context) : TestGlobals(context) {
    override val exerciseSource: ExerciseSource by lazy {
        AdaptiveExerciseSource(factMasteryDao, exerciseAttemptDao, activeUserManager)
    }
}

abstract class AdaptiveExerciseUiTest : BaseExerciseUiTest() {
    @Before
    override fun setup() {
        val application =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApp
        application.globals = AdaptiveTestGlobals(application)
        globals = application.globals as TestGlobals
        runBlocking {
            if (globals.userDao.getUser(1) == null) {
                globals.userDao.insert(User(id = 1, name = "Default User"))
            }
            globals.activeUserManager.init()
        }
        waitForAppToBeReady()
    }
}