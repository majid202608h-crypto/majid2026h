package com.example.ui.luckywheel

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun WheelCanvas(
    rotationAngle: Float,
    sectors: List<RewardType>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Warm and engaging colors for the 8 sectors
    val sectorColors = remember {
        listOf(
            Color(0xFF8B5CF6), // Violet
            Color(0xFF3B82F6), // Blue
            Color(0xFF10B981), // Emerald
            Color(0xFFF59E0B), // Amber
            Color(0xFFEC4899), // Pink
            Color(0xFF6366F1), // Indigo
            Color(0xFF14B8A6), // Teal
            Color(0xFFEF4444)  // Red
        )
    }

    // Set up text paint with bold Farsi typeface
    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 34f // Adjust size as appropriate
            isAntiAlias = true
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val diameter = size.minDimension
        val radius = diameter / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = radius - 15.dp.toPx() // padding for outer golden border/rim

        // 1. Draw central rotating wheel
        rotate(rotationAngle, pivot = center) {
            val sweepAngle = 360f / 8f

            for (i in 0 until 8) {
                val startAngle = i * sweepAngle
                val color = sectorColors[i % sectorColors.size]

                // Draw Slice Arc
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    size = Size(outerRadius * 2, outerRadius * 2),
                    topLeft = Offset(center.x - outerRadius, center.y - outerRadius)
                )

                // Draw slice divider line
                val lineAngleRad = Math.toRadians(startAngle.toDouble())
                val endX = center.x + outerRadius * Math.cos(lineAngleRad).toFloat()
                val endY = center.y + outerRadius * Math.sin(lineAngleRad).toFloat()
                drawLine(
                    color = Color.White.copy(alpha = 0.35f),
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 3.dp.toPx()
                )

                // Draw Text label
                if (i < sectors.size) {
                    val reward = sectors[i]
                    val labelText = when (reward) {
                        is RewardType.Coins -> "${reward.amount.toFa()} سکه"
                        is RewardType.Subscription -> "${reward.days.toFa()} روز"
                    }
                    val labelIcon = when (reward) {
                        is RewardType.Coins -> "🪙"
                        is RewardType.Subscription -> "⭐"
                    }

                    // Rotate canvas to draw text perfectly oriented along the bisector of slice
                    val bisectorAngle = startAngle + (sweepAngle / 2f)
                    rotate(bisectorAngle, pivot = center) {
                        drawIntoCanvas { canvas ->
                            // Draw emoji icon
                            textPaint.textSize = 20.dp.toPx()
                            canvas.nativeCanvas.drawText(
                                labelIcon,
                                center.x + (outerRadius * 0.75f),
                                center.y + 6.dp.toPx(),
                                textPaint
                            )
                            
                            // Draw text
                            textPaint.textSize = 12.dp.toPx()
                            canvas.nativeCanvas.drawText(
                                labelText,
                                center.x + (outerRadius * 0.45f),
                                center.y + 5.dp.toPx(),
                                textPaint
                            )
                        }
                    }
                }
            }
        }

        // 2. Outer Rim (Gold & Metallic style with light reflection effect)
        drawCircle(
            brush = Brush.sweepGradient(
                listOf(
                    Color(0xFFFBBF24),
                    Color(0xFFD97706),
                    Color(0xFFFBBF24),
                    Color(0xFFFEF08A),
                    Color(0xFFD97706),
                    Color(0xFFFBBF24)
                ),
                center = center
            ),
            radius = outerRadius,
            center = center,
            style = Stroke(width = 10.dp.toPx())
        )

        // Darker outer rim border for high-contrast separation
        drawCircle(
            color = Color(0xFF78350F),
            radius = outerRadius + 5.dp.toPx(),
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )

        // 3. Carnival Glowing light bulbs on the golden rim
        val lightCount = 16
        val timeBase = System.currentTimeMillis() / 250
        val isFirstSetGlowing = timeBase % 2 == 0L

        for (k in 0 until lightCount) {
            val angleDeg = k * (360f / lightCount)
            val rad = Math.toRadians(angleDeg.toDouble())
            
            // Alternating glow pattern for circus look
            val glowColor = if ((k % 2 == 0) == isFirstSetGlowing) {
                Color(0xFFFEF08A) // bright yellow-white
            } else {
                Color(0xFFD97706) // deep gold
            }

            val bulbX = center.x + (outerRadius - 1.dp.toPx()) * Math.cos(rad).toFloat()
            val bulbY = center.y + (outerRadius - 1.dp.toPx()) * Math.sin(rad).toFloat()

            // Draw glowing bulb
            drawCircle(
                color = glowColor,
                radius = 4.dp.toPx(),
                center = Offset(bulbX, bulbY)
            )

            // Draw tiny inner white core for 3D bulb look
            if ((k % 2 == 0) == isFirstSetGlowing) {
                drawCircle(
                    color = Color.White,
                    radius = 1.5.dp.toPx(),
                    center = Offset(bulbX, bulbY)
                )
            }
        }

        // 4. Center Golden Hub with Shadow (non-rotating static part / core center)
        // Draw elegant circular radial gradient center hub
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFEF08A), Color(0xFFF59E0B), Color(0xFFB45309)),
                center = center,
                radius = 24.dp.toPx()
            ),
            radius = 24.dp.toPx(),
            center = center
        )
        
        drawCircle(
            color = Color.White.copy(alpha = 0.5f),
            radius = 24.dp.toPx(),
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}

private fun Int.toFa(): String {
    val faDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return this.toString().map { char ->
        if (char in '0'..'9') faDigits[char - '0'] else char
    }.joinToString("")
}

