package dev.aidistillery.pocitaj.data

import androidx.compose.ui.graphics.Color
import dev.aidistillery.pocitaj.R

object UserAppearance {
    val icons: Map<String, Int> = mapOf(
        "alligator" to R.drawable.alligator,
        "bull" to R.drawable.bull,
        "butterfly" to R.drawable.butterfly,
        "jellyfish" to R.drawable.jellyfish,
        "lion" to R.drawable.lion,
        "owl" to R.drawable.owl,
        "starfish" to R.drawable.starfish,
        "robot" to R.drawable.robot
    )

    val colors: List<Color> = listOf(
        Color(0xFFF44336), // Red
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0), // Purple
        Color(0xFF673AB7), // Deep Purple
        Color(0xFF3F51B5), // Indigo
        Color(0xFF2196F3), // Blue
        Color(0xFF03A9F4), // Light Blue
        Color(0xFF00BCD4),  // Cyan
        Color(0xFFCCCCCC) // Grey
    )
}
