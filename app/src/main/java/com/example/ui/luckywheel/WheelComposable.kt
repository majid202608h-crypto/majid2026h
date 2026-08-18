package com.example.ui.luckywheel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.data.GameTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun LuckyWheelScreen(
    viewModel: WheelViewModel,
    themeConfig: GameTheme,
    onBackClick: () -> Unit,
    onSpinComplete: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var rotationAngle by remember { mutableStateOf(0f) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var isConfettiActive by remember { mutableStateOf(false) }

    // Dynamic mechanical needle wobble calculation
    val needleWobbleAngle = remember(rotationAngle) {
        val normalizedAngle = rotationAngle % 45f
        if (normalizedAngle < 8f) {
            -18f * (1f - (normalizedAngle / 8f))
        } else if (normalizedAngle > 37f) {
            -18f * ((normalizedAngle - 37f) / 8f)
        } else {
            0f
        }
    }

    // Connect animation to the controller
    WheelAnimationController(
        isSpinning = uiState.isSpinning,
        targetDegrees = uiState.targetDegrees,
        onAnimationFinished = {
            viewModel.onSpinAnimationFinished()
            showSuccessDialog = true
            isConfettiActive = true
            onSpinComplete()
            coroutineScope.launch {
                delay(4000)
                isConfettiActive = false
            }
        },
        onAngleChanged = { angle ->
            rotationAngle = angle
        }
    )

    // Retrieve active list of sectors dynamically from reward engine
    val sectors = remember(uiState.remainingSubDays) {
        RewardEngine().getSectors(uiState.remainingSubDays)
    }

    // Format millisecond timer to "HH:MM:SS" with Persian digits
    val countdownText = remember(uiState.countdownMs) {
        val totalSecs = uiState.countdownMs / 1000L
        val hours = totalSecs / 3600
        val mins = (totalSecs % 3600) / 60
        val secs = totalSecs % 60
        val timeStr = String.format("%02d:%02d:%02d", hours, mins, secs)
        timeStr.toFa()
    }

    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2E1065), // Deep dark purple
                            Color(0xFF4C1D95), // Medium vibrant purple
                            Color(0xFF3B0764)  // Royal deep dark grape
                        )
                    )
                )
        ) {
            // Soft glowing ambient circles in background
            Box(
                modifier = Modifier
                    .offset(x = (-100).dp, y = (-50).dp)
                    .size(300.dp)
                    .blur(60.dp)
                    .background(Color(0xFFC084FC).copy(alpha = 0.15f), CircleShape)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 120.dp, y = 100.dp)
                    .size(350.dp)
                    .blur(70.dp)
                    .background(Color(0xFF60A5FA).copy(alpha = 0.12f), CircleShape)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. TOP HEADER ROW (Coins, Subscription and Back Button)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button (Left)
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Stats Area (Coins & Sub Days)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Coins Pill
                        Row(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(999.dp))
                                .border(1.2.dp, Color(0xFFFBBF24).copy(alpha = 0.3f), RoundedCornerShape(999.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "🪙 ${uiState.coins.toFa()}",
                                color = Color(0xFFFBBF24),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        // Subscription Days Pill
                        Row(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(999.dp))
                                .border(1.2.dp, Color(0xFF60A5FA).copy(alpha = 0.3f), RoundedCornerShape(999.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "اشتراک",
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${uiState.remainingSubDays.toFa()} روز اشتراک",
                                color = Color(0xFF93C5FD),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title Area
                    Text(
                        text = "🎡 گردونه شانس طلایی 🎡",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "هر روز شانس خودت رو امتحان کن و جوایز ویژه ببر!",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
                    )
                }

                // 2. MIDDLE AREA: THE WHEEL CONTAINER
                Box(
                    modifier = Modifier
                        .size(270.dp)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Back shadow/glow layer
                    Box(
                        modifier = Modifier
                            .size(250.dp)
                            .shadow(
                                elevation = 16.dp,
                                shape = CircleShape,
                                clip = false,
                                ambientColor = Color(0xFFA78BFA),
                                spotColor = Color(0xFFA78BFA)
                            )
                    )

                    // Rotating Canvas
                    WheelCanvas(
                        rotationAngle = rotationAngle,
                        sectors = sectors,
                        modifier = Modifier.size(256.dp)
                    )

                    // 3D Pointer needle pin (Top fixed)
                    Canvas(
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = (-6).dp)
                            .graphicsLayer(
                                rotationZ = needleWobbleAngle,
                                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.15f)
                            )
                    ) {
                        val path = Path().apply {
                            moveTo(size.width / 2f, size.height) // pointy tip pointing down
                            lineTo(size.width * 0.15f, size.height * 0.15f)
                            lineTo(size.width * 0.5f, 0f)
                            lineTo(size.width * 0.85f, size.height * 0.15f)
                            close()
                        }
                        
                        // Draw shiny red indicator needle
                        drawPath(
                            path = path,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFEF4444), Color(0xFF991B1B))
                            )
                        )
                        
                        // Golden border on needle
                        drawPath(
                            path = path,
                            color = Color(0xFFFBBF24),
                            style = Stroke(width = 1.dp.toPx())
                        )

                        // Central pivot circle
                        drawCircle(
                            color = Color.White,
                            radius = 2.5.dp.toPx(),
                            center = Offset(size.width / 2f, size.height * 0.25f)
                        )
                    }

                    // Large interactive 3D Spin Button (Central Core Hub)
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    colors = if (uiState.isSpinAllowed && !uiState.isSpinning) {
                                        listOf(Color(0xFFFBBF24), Color(0xFFF59E0B), Color(0xFFD97706))
                                    } else {
                                        listOf(Color(0xFF9CA3AF), Color(0xFF6B7280))
                                    }
                                )
                            )
                            .clickable(enabled = uiState.isSpinAllowed && !uiState.isSpinning) {
                                viewModel.spinWheel { index, target ->
                                    // Animation is started automatically by WheelAnimationController
                                }
                            }
                            .border(1.2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "بچرخون!",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    blurRadius = 3f
                                )
                            )
                        )
                    }
                }

                // Primary status or countdown button below the wheel
                val mainButtonLabel = when {
                    uiState.isSpinning -> "در حال چرخیدن... 🌀"
                    !uiState.isSpinAllowed -> "فردا دوباره سر بزن! 🕒"
                    else -> "🎡 شانس طلایی امروزت رو بچرخون!"
                }

                Button(
                    onClick = {
                        viewModel.spinWheel { _, _ -> }
                    },
                    enabled = uiState.isSpinAllowed && !uiState.isSpinning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF59E0B),
                        disabledContainerColor = Color.White.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(36.dp)
                        .shadow(
                            elevation = if (uiState.isSpinAllowed && !uiState.isSpinning) 4.dp else 0.dp,
                            shape = RoundedCornerShape(10.dp)
                        ),
                    border = if (!uiState.isSpinAllowed) BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)) else null
                ) {
                    Text(
                        text = mainButtonLabel,
                        color = if (uiState.isSpinAllowed && !uiState.isSpinning) Color.White else Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // 3. BOTTOM PANEL (Countdown and Last Reward)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(bottom = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Countdown row
                        if (!uiState.isSpinAllowed) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "زمان باقی‌مانده تا شانس بعدی:",
                                    color = Color(0xFF93C5FD),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = countdownText,
                                    color = Color(0xFF60A5FA),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontFeatureSettings = "tnum"
                                    )
                                )
                            }
                            
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)
                        }

                        // Last reward status row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "آخرین جایزه شما:",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = uiState.lastRewardText ?: "هنوز نچرخوندی! 😊",
                                color = if (uiState.lastRewardText != null) Color(0xFFFBBF24) else Color.White.copy(alpha = 0.4f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Total spin counts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "مجموع دفعات بازی:",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${uiState.spinCount.toFa()} مرتبه",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Confetti animation overlays
            if (isConfettiActive) {
                ConfettiOverlay()
            }

            // 4. PRETTY DIALOG FOR SHOWING WINNING REWARD
            AnimatedVisibility(
                visible = showSuccessDialog && uiState.currentReward != null,
                enter = fadeIn() + scaleIn(animationSpec = spring()),
                exit = fadeOut() + scaleOut()
            ) {
                val reward = uiState.currentReward
                if (reward != null) {
                    Dialog(onDismissRequest = {
                        showSuccessDialog = false
                        viewModel.clearCurrentReward()
                    }) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(2.dp, Color(0xFFFBBF24)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Dynamic banner illustration
                                Text(
                                    text = "🎉 تبریک قهرمان! 🎉",
                                    color = Color(0xFFFBBF24),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center
                                )

                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                        .border(2.dp, Color(0xFFFBBF24).copy(alpha = 0.3f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val icon = when (reward) {
                                        is RewardType.Coins -> "🪙"
                                        is RewardType.Subscription -> "⭐"
                                    }
                                    Text(
                                        text = icon,
                                        fontSize = 50.sp
                                    )
                                }

                                Text(
                                    text = reward.getSuccessMessageFarsi(),
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Button(
                                    onClick = {
                                        showSuccessDialog = false
                                        viewModel.clearCurrentReward()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Text(
                                        text = "ممنون! 😍",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Full screen floating confetti overlay for celebration feeling!
@Composable
fun ConfettiOverlay() {
    val particles = remember {
        List(40) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -0.5f,
                speed = Random.nextFloat() * 200f + 150f,
                color = listOf(
                    Color(0xFFFBBF24),
                    Color(0xFF3B82F6),
                    Color(0xFFEF4444),
                    Color(0xFF10B981),
                    Color(0xFFEC4899),
                    Color(0xFF8B5CF6)
                ).random(),
                size = Random.nextFloat() * 12f + 8f,
                rotationSpeed = Random.nextFloat() * 180f - 90f
            )
        }
    }

    var elapsedSeconds by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        while (true) {
            delay(16)
            elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000f
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val curY = (p.y * size.height + p.speed * elapsedSeconds) % size.height
            val curX = (p.x * size.width + Math.sin((elapsedSeconds * 2 + p.speed).toDouble()).toFloat() * 30f) % size.width
            val curRot = p.rotationSpeed * elapsedSeconds

            rotate(curRot, pivot = Offset(curX, curY)) {
                drawRect(
                    color = p.color,
                    topLeft = Offset(curX - p.size / 2f, curY - p.size / 2f),
                    size = androidx.compose.ui.geometry.Size(p.size, p.size * 1.5f)
                )
            }
        }
    }
}

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val speed: Float,
    val color: Color,
    val size: Float,
    val rotationSpeed: Float
)

private fun String.toFa(): String {
    val faDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return this.map { char ->
        if (char in '0'..'9') faDigits[char - '0'] else char
    }.joinToString("")
}

private fun Int.toFa(): String {
    return this.toString().toFa()
}
