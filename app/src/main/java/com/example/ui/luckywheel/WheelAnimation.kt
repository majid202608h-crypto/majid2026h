package com.example.ui.luckywheel

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class OvershootEasing(private val tension: Float = 1.2f) : Easing {
    private val interpolator = OvershootInterpolator(tension)
    override fun transform(fraction: Float): Float {
        return interpolator.getInterpolation(fraction)
    }
}

// EaseOut easing curve that slows down smoothly
class EaseOutEasing : Easing {
    override fun transform(fraction: Float): Float {
        val t = fraction - 1.0f
        return t * t * t * t * t + 1.0f // Quintic ease out
    }
}

@Composable
fun WheelAnimationController(
    isSpinning: Boolean,
    targetDegrees: Float,
    onAnimationFinished: () -> Unit,
    onAngleChanged: (Float) -> Unit
) {
    val context = LocalContext.current
    val rotationAnim = remember { Animatable(0f) }
    
    // Get vibrator for click effect
    val vibrator = remember {
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    // Tick sound and vibration on sector boundary crossings
    var lastSectorIndex = remember { -1 }

    LaunchedEffect(isSpinning) {
        if (isSpinning && targetDegrees > 0f) {
            lastSectorIndex = -1
            rotationAnim.snapTo(0f)
            
            rotationAnim.animateTo(
                targetValue = targetDegrees,
                animationSpec = tween(
                    durationMillis = 4200,
                    easing = EaseOutEasing()
                )
            ) {
                val currentAngle = value
                onAngleChanged(currentAngle)
                
                // Track crossed sector boundary (offset by 22.5 to trigger exactly at the dividing pin)
                val currentSectorIndex = (((currentAngle + 22.5f) % 360f) / 45f).toInt()
                if (currentSectorIndex != lastSectorIndex) {
                    lastSectorIndex = currentSectorIndex
                    
                    // Trigger short haptic tick
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator?.vibrate(VibrationEffect.createOneShot(12L, 45))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator?.vibrate(12L)
                        }
                    } catch (e: Exception) {
                        // Safe fallback
                    }
                }
            }
            onAnimationFinished()
        }
    }
}
