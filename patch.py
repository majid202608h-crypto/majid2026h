import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Make sure to keep the result screen
pattern = r'''(@Composable\s+fun NumericKeypad\(.*?)(?=@Composable\s+fun DuelResultScreen)'''

new_ui = """@Composable
fun NumericKeypad(
    currentValue: String,
    onNumberPressed: (Int) -> Unit,
    onDelete: () -> Unit,
    onSubmit: () -> Unit,
    enabled: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onPlayClickSound: () -> Unit = {}
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    val keys = listOf(
        listOf(3, 2, 1),
        listOf(6, 5, 4),
        listOf(9, 8, 7),
        listOf(-1, 0, -2) // -1 for Delete, -2 for Submit
    )

    fun fa(num: Any): String = num.toString().replace("0", "۰").replace("1", "۱").replace("2", "۲").replace("3", "۳").replace("4", "۴").replace("5", "۵").replace("6", "۶").replace("7", "۷").replace("8", "۸").replace("9", "۹")

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    val isAction = key < 0
                    val isSubmit = key == -2
                    val isDelete = key == -1
                    
                    val baseColor = when {
                        isSubmit -> Color(0xFF10B981)
                        isDelete -> Color(0xFFEF4444)
                        else -> accentColor
                    }
                    
                    val buttonBrush = Brush.verticalGradient(
                        listOf(baseColor.copy(alpha=0.55f), baseColor.copy(alpha=0.85f))
                    )
                    
                    val glowColor = baseColor.copy(alpha = 0.5f)

                    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.94f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 400f),
                        label = "scaleAnim"
                    )
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                shadowElevation = if (isPressed) 2.dp.toPx() else 6.dp.toPx()
                                shape = RoundedCornerShape(16.dp)
                                clip = false
                            }
                            .shadow(
                                elevation = if (isPressed) 2.dp else 6.dp,
                                shape = RoundedCornerShape(16.dp),
                                ambientColor = glowColor,
                                spotColor = glowColor
                            )
                            .background(buttonBrush, RoundedCornerShape(16.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .clickable(
                                interactionSource = interactionSource,
                                indication = androidx.compose.foundation.LocalIndication.current,
                                enabled = enabled
                            ) {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                onPlayClickSound()
                                if (isDelete) onDelete()
                                else if (isSubmit) onSubmit()
                                else onNumberPressed(key)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDelete) {
                            Text("⌫", fontSize = 28.sp, color = Color.White)
                        } else if (isSubmit) {
                            Text("✓", fontSize = 30.sp, color = Color.White, fontWeight = FontWeight.Black)
                        } else {
                            Text(fa(key), fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DuelGameScreen(viewModel: com.example.ui.GameViewModel, themeConfig: GameTheme) {
    val state by viewModel.duelState.collectAsStateWithLifecycle()
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isLight = !isDark
    val context = androidx.compose.ui.platform.LocalContext.current

    var countdownFinished by remember(state.phase) { mutableStateOf(state.phase != com.example.ui.DuelPhase.Playing) }
    var countdownStep by remember(state.phase) { mutableStateOf<String?>(if (state.phase == com.example.ui.DuelPhase.Playing) "۳" else null) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    LaunchedEffect(state.phase) {
        if (state.phase == com.example.ui.DuelPhase.Playing && !countdownFinished) {
            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelTick)
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
            delay(1000)
            
            countdownStep = "۲"
            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelTick)
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
            delay(1000)
            
            countdownStep = "۱"
            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelTick)
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
            delay(1000)
            
            countdownStep = "🚀 شروع!"
            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelStart)
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            delay(1000)
            
            countdownStep = null
            countdownFinished = true
        }
    }

    val duelPrefs by viewModel.duelPrefs.collectAsStateWithLifecycle()
    val avatar1 = duelPrefs?.player1Avatar ?: "👦"
    val avatar2 = duelPrefs?.player2Avatar ?: "👧"

    val bgBrush = Brush.verticalGradient(
        listOf(
            Color(0xFF1E1B4B), // Very dark indigo/purple
            Color(0xFF312E81),
            Color(0xFF1E1B4B)
        )
    )

    Box(modifier = Modifier.fillMaxSize().background(bgBrush)) {
        // Particles and glowing orbs
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFF6366F1).copy(alpha = 0.2f),
                radius = 350.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.15f)
            )
            drawCircle(
                color = Color(0xFFEC4899).copy(alpha = 0.2f),
                radius = 450.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.85f)
            )
            drawCircle(
                color = Color(0xFF10B981).copy(alpha = 0.15f),
                radius = 250.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.5f)
            )
        }

        @Composable
        fun PlayerHalf(
            player: com.example.ui.DuelPlayerState,
            playerNum: Int,
            isFlipped: Boolean,
            accentColor: Color,
            modifierWeight: Modifier = Modifier,
            timeLeft: Int,
            totalTime: Int,
            avatarEmoji: String,
            showGameplayElements: Boolean
        ) {
            var input by remember { mutableStateOf("") }
            LaunchedEffect(player.a, player.b) { input = "" }
            
            fun fa(num: Any): String = num.toString().replace("0", "۰").replace("1", "۱").replace("2", "۲").replace("3", "۳").replace("4", "۴").replace("5", "۵").replace("6", "۶").replace("7", "۷").replace("8", "۸").replace("9", "۹")

            val rotation = if (isFlipped) 180f else 0f
            
            val interactionScale by animateFloatAsState(
                targetValue = if (input.isNotEmpty()) 1.02f else 1f,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f), label = ""
            )
            
            var shakeTrigger by remember { mutableStateOf(0) }
            LaunchedEffect(player.feedbackIsCorrect) {
                if (player.feedbackIsCorrect == false) {
                    shakeTrigger++
                    input = ""
                }
            }
            val shakeOffset by animateFloatAsState(
                targetValue = if (shakeTrigger % 2 == 0) 0f else 12f,
                animationSpec = spring(dampingRatio = 0.15f, stiffness = 2500f), label = "shake"
            )

            Box(
                modifier = modifierWeight
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .graphicsLayer { rotationZ = rotation }
            ) {
                Column(Modifier.fillMaxSize()) {
                    // Header Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { scaleX = interactionScale; scaleY = interactionScale },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar & Name
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(52.dp)
                                        .background(Brush.radialGradient(listOf(accentColor.copy(alpha=0.5f), Color.Transparent)), CircleShape)
                                        .border(2.dp, accentColor.copy(alpha=0.7f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(avatarEmoji, fontSize = 30.sp)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(player.name, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🏆", fontSize = 16.sp)
                                        Spacer(Modifier.width(6.dp))
                                        AnimatedContent(targetState = player.score, transitionSpec = { scaleIn() togetherWith scaleOut() }, label = "") { sc ->
                                            Text(fa(sc), fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFD700))
                                        }
                                    }
                                }
                            }
                            
                            // Timer Ring
                            val progress = if (totalTime > 0) timeLeft.toFloat() / totalTime else 0f
                            val timerColor = when {
                                progress < 0.2f -> Color(0xFFEF4444)
                                progress < 0.5f -> Color(0xFFF59E0B)
                                else -> Color(0xFF10B981)
                            }
                            val timerPulse by animateFloatAsState(
                                targetValue = if (progress < 0.2f) 1.15f else 1f,
                                animationSpec = infiniteRepeatable(tween(400), RepeatMode.Reverse), label = ""
                            )
                            
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(56.dp)
                                    .graphicsLayer { if (progress < 0.2f) { scaleX = timerPulse; scaleY = timerPulse } }
                            ) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxSize(),
                                    color = timerColor,
                                    trackColor = Color.White.copy(alpha=0.1f),
                                    strokeWidth = 5.dp,
                                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                                    gapSize = 0.dp
                                )
                                Text(fa(timeLeft), fontSize = 20.sp, fontWeight = FontWeight.Black, color = timerColor)
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(10.dp))
                    
                    // Question Area
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(0.35f)
                            .offset(x = shakeOffset.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (showGameplayElements) {
                            val isCorrect = player.feedbackIsCorrect == true
                            val isWrong = player.feedbackIsCorrect == false
                            val questionBg = when {
                                isCorrect -> Brush.horizontalGradient(listOf(Color(0xFF10B981).copy(alpha=0.4f), Color.Transparent, Color(0xFF10B981).copy(alpha=0.4f)))
                                isWrong -> Brush.horizontalGradient(listOf(Color.Red.copy(alpha=0.4f), Color.Transparent, Color.Red.copy(alpha=0.4f)))
                                else -> Brush.horizontalGradient(listOf(accentColor.copy(alpha=0.2f), Color.Transparent, accentColor.copy(alpha=0.2f)))
                            }
                            val questionBorderColor = when {
                                isCorrect -> Color(0xFF10B981)
                                isWrong -> Color.Red
                                else -> Color.White.copy(alpha=0.15f)
                            }
                            
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.9f)
                                    .background(questionBg, RoundedCornerShape(24.dp))
                                    .border(1.dp, questionBorderColor, RoundedCornerShape(24.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AnimatedContent(targetState = player.a, label = "a", transitionSpec = { scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut() }) { a ->
                                            Text(fa(a), fontSize = 52.sp, fontWeight = FontWeight.Black, color = Color.White)
                                        }
                                        Text(" × ", fontSize = 40.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                                        AnimatedContent(targetState = player.b, label = "b", transitionSpec = { scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut() }) { b ->
                                            Text(fa(b), fontSize = 52.sp, fontWeight = FontWeight.Black, color = Color.White)
                                        }
                                        Text(" = ", fontSize = 40.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                                        
                                        AnimatedContent(
                                            targetState = if (input.isEmpty()) "?" else input,
                                            transitionSpec = {
                                                (scaleIn(initialScale = 0.7f) + fadeIn(tween(150))) togetherWith (scaleOut(targetScale = 1.3f) + fadeOut(tween(150)))
                                            },
                                            label = "answerAnim"
                                        ) { text ->
                                            Text(
                                                text = fa(text),
                                                fontSize = 60.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (text == "?") accentColor.copy(alpha=0.7f) else Color(0xFFFFD700),
                                                style = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = Color(0xFFFFD700).copy(alpha=if(text=="?") 0f else 0.6f), blurRadius = 14f))
                                            )
                                        }
                                    }
                                }
                                
                                // Floating Combo/Feedback Text
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = player.feedbackText.isNotEmpty() && player.feedbackIsCorrect == true,
                                    enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn() + scaleIn(),
                                    exit = fadeOut() + scaleOut(),
                                    modifier = Modifier.align(Alignment.TopEnd).offset(x = (-16).dp, y = 16.dp)
                                ) {
                                    Text(
                                        text = player.feedbackText, 
                                        fontSize = 20.sp, 
                                        fontWeight = FontWeight.Black, 
                                        color = Color(0xFF10B981), 
                                        style = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(blurRadius=8f, color=Color.Black.copy(alpha=0.5f)))
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    // Keypad
                    Box(Modifier.fillMaxWidth().weight(0.65f)) {
                        if (showGameplayElements) {
                            NumericKeypad(
                                currentValue = input,
                                onNumberPressed = { num -> if (input.length < 3) input += num.toString() },
                                onDelete = { if (input.isNotEmpty()) input = input.dropLast(1) },
                                onSubmit = { 
                                    if (!player.answered) {
                                        input.toIntOrNull()?.let { 
                                            viewModel.submitDuelAnswer(playerNum, it)
                                        } 
                                    }
                                },
                                enabled = !player.answered,
                                accentColor = accentColor,
                                modifier = Modifier.fillMaxSize(),
                                onPlayClickSound = { viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick) }
                            )
                        }
                    }
                }
            }
        }

        val insets = androidx.compose.foundation.layout.WindowInsets.safeDrawing.asPaddingValues()
        val bottomInset = insets.calculateBottomPadding()
        val topInset = insets.calculateTopPadding()
        val maxInset = androidx.compose.ui.unit.max(bottomInset, topInset).coerceAtLeast(16.dp)
        
        Column(Modifier.fillMaxSize().padding(top = maxInset, bottom = maxInset)) {
            // Top Half (Player 2, Flipped)
            PlayerHalf(
                player = state.player2, playerNum = 2, isFlipped = true,
                accentColor = Color(0xFFF43F5E), // Rose Pink
                modifierWeight = Modifier.weight(1f),
                timeLeft = state.secondsLeft,
                totalTime = state.totalSeconds,
                avatarEmoji = avatar2,
                showGameplayElements = countdownFinished
            )

            // Splitter
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(4.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(alpha=0.4f), Color.Transparent))))
            Spacer(Modifier.height(8.dp))

            // Bottom Half (Player 1, Normal)
            PlayerHalf(
                player = state.player1, playerNum = 1, isFlipped = false,
                accentColor = Color(0xFF3B82F6), // Blue
                modifierWeight = Modifier.weight(1f),
                timeLeft = state.secondsLeft,
                totalTime = state.totalSeconds,
                avatarEmoji = avatar1,
                showGameplayElements = countdownFinished
            )
        }
        
        if (!countdownFinished) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .pointerInput(Unit) { detectTapGestures { } },
                contentAlignment = Alignment.Center
            ) {
                countdownStep?.let { step ->
                    AnimatedContent(
                        targetState = step,
                        transitionSpec = {
                            (scaleIn(initialScale = 0.5f, animationSpec = spring(dampingRatio = 0.5f, stiffness = 200f)) + fadeIn(tween(200))) togetherWith (scaleOut(targetScale = 1.5f, animationSpec = tween(200)) + fadeOut(tween(200)))
                        },
                        label = "countdown"
                    ) { currentStep ->
                        Text(
                            text = currentStep,
                            fontSize = if (currentStep.length > 1) 80.sp else 140.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color(0xFF6366F1).copy(alpha = 0.9f),
                                    blurRadius = 32f
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}
"""

new_content = re.sub(pattern, new_ui, content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(new_content)

print("Updated DuelGameScreen and NumericKeypad.")
