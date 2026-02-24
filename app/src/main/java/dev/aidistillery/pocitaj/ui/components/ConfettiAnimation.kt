package dev.aidistillery.pocitaj.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import dev.aidistillery.pocitaj.ui.theme.AnimationDurations
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class ConfettiShape {
    RECTANGLE, STAR
}

enum class ConfettiPalette {
    DEFAULT, GOLD_SILVER
}

@Composable
fun ConfettiAnimation(
    modifier: Modifier = Modifier,
    durationMillis: Int = AnimationDurations.ConfettiMs,
    particleCount: Int = 100,
    shape: ConfettiShape = ConfettiShape.RECTANGLE,
    palette: ConfettiPalette = ConfettiPalette.DEFAULT
) {
    val isPreview = LocalInspectionMode.current
    val density = LocalDensity.current.density
    val particles = remember(particleCount, density, shape, palette) {
        List(particleCount) {
            ConfettiParticle(shape = shape, palette = palette).apply { randomize(density) }
        }
    }

    // Animation progress from 0f to 1f
    val progress = remember { Animatable(if (isPreview) 0.5f else 0f) }
    if (!isPreview) {
        LaunchedEffect(Unit) {
            progress.animateTo(1f, tween(durationMillis, easing = LinearEasing))
        }
    }

    // Use derivedStateOf to avoid recomposing the outer function on every frame
    val isVisible by remember {
        androidx.compose.runtime.derivedStateOf { isPreview || progress.value < 1f }
    }
    if (isVisible) {
        Canvas(
            modifier = modifier
                .fillMaxSize()
                .testTag("confetti_animation")
        ) {
            // Read progress.value inside the drawing block to trigger redrawing
            // without recomposing the whole ConfettiAnimation composable.
            @Suppress("UNUSED_VARIABLE")
            val p = progress.value
            particles.forEach {
                it.update()
                drawConfetti(it)
            }
        }
    }
}

private class ConfettiParticle(
    var x: Float = 0f,
    var y: Float = 0f,
    var color: Color = Color.Red,
    var rotation: Float = 0f,
    var scaleX: Float = 1f,
    var speedY: Float = 0f,
    var speedX: Float = 0f,
    var rotationSpeed: Float = 0f,
    var size: Float = 20f,
    val shape: ConfettiShape = ConfettiShape.RECTANGLE,
    val palette: ConfettiPalette = ConfettiPalette.DEFAULT
) {
    private val defaultColors = listOf(
        Color(0xFFFFC107), // Amber
        Color(0xFF2196F3), // Blue
        Color(0xFFE91E63), // Pink
        Color(0xFF4CAF50), // Green
        Color(0xFF9C27B0), // Purple
        Color(0xFFFF5722)  // Deep Orange
    )

    private val goldSilverColors = listOf(
        Color(0xFFFFD700), // Gold
        Color(0xFFDAA520), // Goldenrod
        Color(0xFFFFEC8B), // Light Goldenrod
        Color(0xFFC0C0C0), // Silver
        Color(0xFFE8E8E8), // Platinum
        Color(0xFF7F7F7F)  // Dark Gray
    )

    fun randomize(density: Float) {
        x = Random.nextFloat()
        y = Random.nextFloat() * -0.5f

        val colorList =
            if (palette == ConfettiPalette.GOLD_SILVER) goldSilverColors else defaultColors
        color = colorList[Random.nextInt(colorList.size)]
        
        rotation = Random.nextFloat() * 360f
        scaleX = 1f

        speedY = (Random.nextFloat() * 0.01f + 0.005f) * density
        speedX = (Random.nextFloat() * 0.002f - 0.001f) * density
        rotationSpeed = (Random.nextFloat() * 10f - 5f)
        size = (Random.nextFloat() * 10f + 10f) * density
        if (shape == ConfettiShape.STAR) {
            size *= 2.5f
        }
    }

    fun update() {
        y += speedY
        x += speedX + (sin(y * 0.1f) * 0.005f)
        rotation += rotationSpeed
        scaleX = cos(rotation * (PI / 180f)).toFloat()
    }
}

private fun DrawScope.drawConfetti(particle: ConfettiParticle) {
    val width = size.width
    val height = size.height

    val drawX = particle.x * width
    val drawY = particle.y * height + (height * 0.2f)

    if (drawY > height + particle.size) return 

    withTransform({
        translate(left = drawX, top = drawY)
        rotate(degrees = particle.rotation)
        scale(scaleX = particle.scaleX, scaleY = 1f)
    }) {
        when (particle.shape) {
            ConfettiShape.RECTANGLE -> {
                drawRect(
                    color = particle.color,
                    topLeft = Offset(-particle.size / 2, -particle.size / 2),
                    size = Size(particle.size, particle.size)
                )
            }

            ConfettiShape.STAR -> {
                drawStar(particle.color, particle.size)
            }
        }
    }
}

private fun DrawScope.drawStar(color: Color, size: Float) {
    val segments = 10
    val outerRadius = size / 2
    val innerRadius = size / 4

    for (i in 0 until segments) {
        val angle1 = (i * 2 * PI / segments) - PI / 2
        val angle2 = ((i + 1) * 2 * PI / segments) - PI / 2

        val r1 = if (i % 2 == 0) outerRadius else innerRadius
        val r2 = if (i % 2 == 0) innerRadius else outerRadius

        drawLine(
            color = color,
            start = Offset(cos(angle1).toFloat() * r1, sin(angle1).toFloat() * r1),
            end = Offset(cos(angle2).toFloat() * r2, sin(angle2).toFloat() * r2),
            strokeWidth = size * 0.1f
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun PreviewConfetti() {
    dev.aidistillery.pocitaj.ui.theme.AppTheme {
        ConfettiAnimation()
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Light Mode - Stars",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO
)
@androidx.compose.ui.tooling.preview.Preview(
    name = "Dark Mode - Stars",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewStarConfettiGold() {
    dev.aidistillery.pocitaj.ui.theme.AppTheme {
        ConfettiAnimation(
            shape = ConfettiShape.STAR,
            palette = ConfettiPalette.GOLD_SILVER
        )
    }
}
