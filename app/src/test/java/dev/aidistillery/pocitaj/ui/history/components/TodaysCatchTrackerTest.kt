package dev.aidistillery.pocitaj.ui.history.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TodaysCatchTrackerTest {

    @Test
    fun getStableSushiIcons_returnsSameIconsForSameSeed() {
        val seed = 12345L
        val count = 3

        val icons1 = getStableSushiIcons(seed, count)
        val icons2 = getStableSushiIcons(seed, count)

        assertEquals("Icons should be stable for the same seed", icons1, icons2)
        assertEquals("Should return the requested number of icons", count, icons1.size)
    }

    @Test
    fun getStableSushiIcons_returnsDifferentIconsForDifferentSeeds() {
        // There is a small chance they could be the same randomly, but with 7 icons and 3 picks, it's unlikely
        // To be safe, we can check a few seeds
        val icons1 = getStableSushiIcons(1L, 3)
        val icons2 = getStableSushiIcons(2L, 3)
        val icons3 = getStableSushiIcons(3L, 3)

        val allSame = (icons1 == icons2) && (icons2 == icons3)
        assertNotEquals("Icons should ideally differ across different seeds", true, allSame)
    }
}
