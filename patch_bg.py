import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

pattern = r'''    // Modern AAA mobile game background: Dark purple gradient, soft lighting\.\n    val bgBrush = Brush\.verticalGradient\(\n        listOf\(\n            Color\(0xFF1E1B4B\), // Very dark indigo/purple\n            Color\(0xFF312E81\),\n            Color\(0xFF1E1B4B\)\n        \)\n    \)\n\n    Box\(modifier = Modifier\.fillMaxSize\(\)\.background\(bgBrush\)\) \{\n        // Particles or subtle glowing orbs\n        Canvas\(modifier = Modifier\.fillMaxSize\(\)\) \{\n            drawCircle\(\n                color = Color\(0xFF6366F1\)\.copy\(alpha = 0\.15f\),\n                radius = 300\.dp\.toPx\(\),\n                center = Offset\(size\.width \* 0\.2f, size\.height \* 0\.2f\)\n            \)\n            drawCircle\(\n                color = Color\(0xFFEC4899\)\.copy\(alpha = 0\.15f\),\n                radius = 400\.dp\.toPx\(\),\n                center = Offset\(size\.width \* 0\.8f, size\.height \* 0\.8f\)\n            \)\n        \}'''

replacement = """    // Modern AAA mobile game background: Premium Fantasy Purple
    val bgBrush = Brush.verticalGradient(
        listOf(
            Color(0xFF0F0C29), // Deepest purple/black
            Color(0xFF302B63), // Mid purple
            Color(0xFF24243E)  // Dark slate purple
        )
    )

    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "bgAnim")
    
    val ambientGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(4000, easing = androidx.compose.animation.core.LinearOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "ambientGlow"
    )
    
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(8000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "floatAnim"
    )
    
    val starGlow1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(2500, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "starGlow1"
    )

    val starGlow2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(3500, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "starGlow2"
    )

    Box(modifier = Modifier.fillMaxSize().background(bgBrush)) {
        // Magical Particles and Ambient Glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            // Top glowing orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.3f * ambientGlow), Color.Transparent),
                    center = Offset(width * 0.2f, height * 0.15f + (floatAnim * 60f)),
                    radius = width * 0.7f
                ),
                center = Offset(width * 0.2f, height * 0.15f + (floatAnim * 60f)),
                radius = width * 0.7f
            )
            
            // Bottom glowing orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFEC4899).copy(alpha = 0.25f * ambientGlow), Color.Transparent),
                    center = Offset(width * 0.85f, height * 0.85f - (floatAnim * 50f)),
                    radius = width * 0.8f
                ),
                center = Offset(width * 0.85f, height * 0.85f - (floatAnim * 50f)),
                radius = width * 0.8f
            )
            
            // Center subtle highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF6366F1).copy(alpha = 0.2f), Color.Transparent),
                    center = Offset(width * 0.5f, height * 0.5f),
                    radius = width * 0.6f
                ),
                center = Offset(width * 0.5f, height * 0.5f),
                radius = width * 0.6f
            )
            
            // Stars / Sparkles
            val starColor = Color.White
            
            // Star 1
            drawCircle(
                color = starColor.copy(alpha = starGlow1),
                radius = 4.dp.toPx(),
                center = Offset(width * 0.15f, height * 0.35f)
            )
            drawCircle(
                color = starColor.copy(alpha = starGlow1 * 0.4f),
                radius = 12.dp.toPx(),
                center = Offset(width * 0.15f, height * 0.35f)
            )
            
            // Star 2
            drawCircle(
                color = starColor.copy(alpha = starGlow2),
                radius = 3.dp.toPx(),
                center = Offset(width * 0.8f, height * 0.2f)
            )
            drawCircle(
                color = starColor.copy(alpha = starGlow2 * 0.4f),
                radius = 8.dp.toPx(),
                center = Offset(width * 0.8f, height * 0.2f)
            )
            
            // Star 3
            drawCircle(
                color = starColor.copy(alpha = starGlow1),
                radius = 5.dp.toPx(),
                center = Offset(width * 0.25f, height * 0.7f)
            )
            drawCircle(
                color = starColor.copy(alpha = starGlow1 * 0.4f),
                radius = 14.dp.toPx(),
                center = Offset(width * 0.25f, height * 0.7f)
            )
            
            // Star 4
            drawCircle(
                color = starColor.copy(alpha = starGlow2),
                radius = 3.dp.toPx(),
                center = Offset(width * 0.75f, height * 0.65f)
            )
            
            // Vignette effect overlay
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color(0xFF03001C).copy(alpha = 0.5f)),
                    center = Offset(width / 2, height / 2),
                    radius = maxOf(width, height) * 0.8f
                ),
                size = size
            )
        }"""

match = re.search(pattern, content, flags=re.DOTALL)
if match:
    new_content = content.replace(match.group(0), replacement)
    with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
        f.write(new_content)
    print("Background replaced!")
else:
    print("Match not found.")
