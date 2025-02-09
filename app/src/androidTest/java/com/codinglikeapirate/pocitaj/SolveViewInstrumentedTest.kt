package com.codinglikeapirate.pocitaj

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class SolveViewInstrumentedTest {

    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(SolveActivity::class.java)

    @Test
    fun showsSolveView() {
        onView(withId(R.id.solve_view)).check(matches(isDisplayed()))
    }
}