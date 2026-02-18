package dev.aidistillery.pocitaj.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ConfettiAnimation(
    modifier: Modifier = Modifier,
    durationMillis: Int = 3000,
    particleCount: Int = 100
) {
    val isPreview = LocalInspectionMode.current
    val density = LocalDensity.current.density
    val particles = remember(particleCount, density) {
        List(particleCount) { ConfettiParticle().apply { randomize(density) } }
    }

    // Animation progress from 0f to 1f
    val progress = remember { Animatable(if (isPreview) 0.5f else 0f) }
    if (!isPreview) {
        LaunchedEffect(Unit) {
            progress.animateTo(1f, tween(durationMillis, easing = LinearEasing))
        }
    }
    if (progress.value < 1f) {
        Canvas(
            modifier = modifier
                .fillMaxSize()
                .testTag("confetti_animation")
        ) {
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
    var size: Float = 20f
) {
    private val colors = listOf(
        Color(0xFFFFC107), // Amber
        Color(0xFF2196F3), // Blue
        Color(0xFFE91E63), // Pink
        Color(0xFF4CAF50), // Green
        Color(0xFF9C27B0), // Purple
        Color(0xFFFF5722)  // Deep Orange
    )

    fun randomize(density: Float) {
        x = Random.nextFloat() // Normalized 0..1 initially, mapped to width later
        y = Random.nextFloat() * -0.5f // Start above screen
        // Use explicit index to avoid potential issues with Random.random() on collections in tests
        color = colors[Random.nextInt(colors.size)]
        rotation = Random.nextFloat() * 360f
        scaleX = 1f

        // Random falling speed
        speedY = (Random.nextFloat() * 0.01f + 0.005f) * density

        // Random horizontal drift
        speedX = (Random.nextFloat() * 0.002f - 0.001f) * density

        // Random rotation speed
        rotationSpeed = (Random.nextFloat() * 10f - 5f)

        size = (Random.nextFloat() * 10f + 10f) * density
    }

    fun update() {
        // Simple physics simulation based on progress is harder because progress is 0->1 linear.
        // Instead, we simulate "ticks" implicitly by how far we move per frame relative to screen height.
        // However, for a simple fire-and-forget animation, we can just project position based on time.
        // But to keep it responding to the Animatable, let's just use the fact that it receives updates.

        // Actually, since we want a physics-y feel (gravity), using a linear 0-1 progress
        // to drive the *entire* simulation state is tricky unless we pre-calculate paths.
        // A better approach for a strictly visual effect bound to a timer:
        // Let's just move them "down" relative to height.

        // Refactored logic: The `update` method normally would be called delta-time based.
        // Since we are driven by `Animatable`, we can infer "time elapsed" or just move consistently.
        // But `draw` is called every frame the animation updates.

        // Let's just modify the state directly. The canvas redraws on every `progress` change.
        // We accumulate movement.

        y += speedY
        x += speedX + (sin(y * 0.1f) * 0.005f)
        rotation += rotationSpeed

        // Flip effect
        scaleX = cos(rotation * (PI / 180f)).toFloat()
    }
}

private fun DrawScope.drawConfetti(particle: ConfettiParticle) {
    val width = size.width
    val height = size.height

    // Map normalized coordinates to screen
    val drawX = particle.x * width
    // Add screen height * particle.y (since y starts negative and goes positive)
    // We want them to fall *through* the screen.
    val drawY =
        particle.y * height + (height * 0.2f) // Offset to ensure they start entering visible area

    if (drawY > height + particle.size) return // Optimization: don't draw if off-screen

    withTransform({
        translate(left = drawX, top = drawY)
        rotate(degrees = particle.rotation)
        scale(scaleX = particle.scaleX, scaleY = 1f)
    }) {
        drawRect(
            color = particle.color,
            topLeft = Offset(-particle.size / 2, -particle.size / 2),
            size = Size(particle.size, particle.size)
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
