package com.nidoham.kaveya.view.animation.voice

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import kotlin.math.min
import kotlin.math.sin

class VoiceAnimationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val corePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ripplePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var coreRadius = 80f
    private val maxCoreRadius = 90f
    private val minCoreRadius = 70f
    private var glowRadius = 100f
    private var pulseAnimator: ValueAnimator? = null
    private var rippleAnimator: ValueAnimator? = null
    private val ripples = mutableListOf<Ripple>()
    private var amplitude = 0f
    private var isAnimating = false
    private var isUserMode = false
    private var isAddingRipples = true
    private val baseSize = 200f
    private var scale = 1f

    init {
        setColors(false) // Default to AI colors
        glowPaint.alpha = 50
    }

    /** Sets default colors based on mode (user or AI). */
    private fun setColors(isUser: Boolean) {
        val coreColors = if (isUser) {
            intArrayOf(Color.WHITE, Color.YELLOW)
        } else {
            intArrayOf(
                ContextCompat.getColor(context, android.R.color.white),
                ContextCompat.getColor(context, android.R.color.holo_blue_light)
            )
        }
        val coreGradient = RadialGradient(
            0f, 0f, maxCoreRadius,
            coreColors,
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        corePaint.shader = coreGradient

        val rippleColors = if (isUser) {
            intArrayOf(Color.YELLOW, Color.TRANSPARENT)
        } else {
            intArrayOf(
                ContextCompat.getColor(context, android.R.color.holo_blue_light),
                Color.TRANSPARENT
            )
        }
        val rippleGradient = RadialGradient(
            0f, 0f, 150f,
            rippleColors,
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        ripplePaint.shader = rippleGradient
        ripplePaint.alpha = 100

        val glowColors = if (isUser) {
            intArrayOf(Color.YELLOW, Color.TRANSPARENT)
        } else {
            intArrayOf(
                ContextCompat.getColor(context, android.R.color.holo_blue_light),
                Color.TRANSPARENT
            )
        }
        val glowGradient = RadialGradient(
            0f, 0f, 120f,
            glowColors,
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        glowPaint.shader = glowGradient
    }

    /** Allows custom colors for core, glow, and ripples. */
    fun setCustomColors(coreStart: Int, coreEnd: Int, glowColor: Int, rippleColor: Int) {
        val coreGradient = RadialGradient(
            0f, 0f, maxCoreRadius,
            intArrayOf(coreStart, coreEnd),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        corePaint.shader = coreGradient

        val rippleGradient = RadialGradient(
            0f, 0f, 150f,
            intArrayOf(rippleColor, Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        ripplePaint.shader = rippleGradient

        val glowGradient = RadialGradient(
            0f, 0f, 120f,
            intArrayOf(glowColor, Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        glowPaint.shader = glowGradient
        invalidate()
    }

    /** Sets the amplitude externally (e.g., from microphone input). */
    fun setAmplitude(value: Float) {
        amplitude = value.coerceIn(0f, 100f)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val minSize = min(w, h).toFloat()
        scale = if (minSize > 0) minSize / baseSize else 1f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.translate(width / 2f, height / 2f)
        canvas.scale(scale, scale)
        canvas.drawCircle(0f, 0f, glowRadius, glowPaint)
        for (ripple in ripples) {
            ripplePaint.alpha = ripple.alpha.toInt() // Convert Float to Int for Paint.alpha
            canvas.drawCircle(0f, 0f, ripple.radius, ripplePaint)
        }
        canvas.drawCircle(0f, 0f, coreRadius, corePaint)
    }

    fun startUserSpeakingAnimation(duration: Long = 1000) {
        startAnimation(duration, true)
    }

    fun startAISpeakingAnimation(duration: Long = 1000) {
        startAnimation(duration, false)
    }

    private fun startAnimation(duration: Long, isUser: Boolean) {
        stopAnimation()
        isUserMode = isUser
        isAddingRipples = true
        setColors(isUser)
        startAnimationInternal(duration)
    }

    private fun startAnimationInternal(duration: Long) {
        isAnimating = true
        pulseAnimator = ValueAnimator.ofFloat(minCoreRadius, maxCoreRadius).apply {
            this.duration = duration
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                coreRadius = animation.animatedValue as Float
                invalidate()
            }
            start()
        }

        rippleAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = 100 // Slower ripple addition (was 50ms)
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                if (!isUserMode || isAddingRipples) { // Only simulate amplitude in AI mode or if adding ripples
                    amplitude = if (isUserMode) {
                        amplitude // Use externally set value in user mode
                    } else {
                        val time = System.currentTimeMillis() % 1000 / 1000f
                        (sin(time * 2 * Math.PI) * 50f + 50f).toFloat()
                    }
                }

                if (isAddingRipples) {
                    val rippleRadius = 50f + amplitude * 0.5f
                    ripples.add(Ripple(rippleRadius, 100f)) // Use Float literal
                }

                val iterator = ripples.iterator()
                while (iterator.hasNext()) {
                    val ripple = iterator.next()
                    ripple.radius += 2f
                    ripple.alpha -= 1.5f // Slower fade for longer-lasting ripples
                    if (ripple.alpha <= 0) {
                        iterator.remove()
                    }
                }

                glowRadius = 100f + amplitude * 0.3f
                glowPaint.alpha = (50 + amplitude * 0.5f).toInt().coerceAtMost(100)
                invalidate()
            }
            start()
        }
    }

    fun stopAnimation() {
        pulseAnimator?.cancel()
        rippleAnimator?.cancel()
        pulseAnimator = null
        rippleAnimator = null
        isAddingRipples = false // Stop adding new ripples, let existing ones fade
        if (ripples.isEmpty()) { // Reset only if no ripples remain
            coreRadius = minCoreRadius
            glowRadius = 100f
            glowPaint.alpha = 50
            isAnimating = false
        }
        invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }

    fun isAnimating(): Boolean = isAnimating

    private data class Ripple(var radius: Float, var alpha: Float)
}