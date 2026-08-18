import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

pattern = r'''(        @Composable\n\s*fun PlayerHalf\(.*?)(?=\n\s*if \(!countdownFinished\))'''
match = re.search(pattern, content, flags=re.DOTALL)
if not match:
    print("No match found")
    import sys
    sys.exit(1)

target = match.group(1)

replacement = """        @Composable
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
            
            var lastScore by remember { mutableStateOf(player.score) }
            var scoreIncrement by remember { mutableStateOf(0) }
            var scoreTrigger by remember { mutableStateOf(0) }
            
            LaunchedEffect(player.score) {
                if (player.score > lastScore) {
                    scoreIncrement = player.score - lastScore
                    lastScore = player.score
                    scoreTrigger++
                }
            }
            
            val scoreScale by animateFloatAsState(
                targetValue = if (scoreTrigger % 2 == 0) 1f else 1.3f,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
                finishedListener = { if (scoreTrigger % 2 != 0) scoreTrigger++ },
                label = "scoreScale"
            )
            
            var inputTrigger by remember { mutableStateOf(0) }
            LaunchedEffect(input) {
                if (input.isNotEmpty()) {
                    inputTrigger++
                }
            }
            val interactionScale by animateFloatAsState(
                targetValue = if (inputTrigger % 2 == 0) 1f else 1.02f,
                animationSpec = tween(durationMillis = 150, easing = LinearOutSlowInEasing),
                finishedListener = { if (inputTrigger % 2 != 0) inputTrigger++ },
                label = "interactionScale"
            )
            
            val cardBorderColor = if (inputTrigger % 2 != 0) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.15f)
            val cardShadow = if (inputTrigger % 2 != 0) 12.dp else 4.dp
            
            val progress = if (totalTime > 0) timeLeft.toFloat() / totalTime else 0f
            val timerColor = when {
                progress > 0.5f -> Color(0xFF4ADE80) // Green
                progress > 0.25f -> Color(0xFFFFD700) // Gold
                progress > 0.1f -> Color(0xFFF97316) // Orange
                else -> Color(0xFFEF4444) // Red
            }
            
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(1000, easing = LinearEasing),
                label = "animatedProgress"
            )

            val rotation = if (isFlipped) 180f else 0f

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
                            .graphicsLayer { scaleX = interactionScale; scaleY = interactionScale }
                            .shadow(cardShadow, RoundedCornerShape(24.dp), ambientColor = accentColor, spotColor = accentColor),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF312E81).copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(1.dp, cardBorderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar and Name
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(56.dp)
                                        .background(Color.White.copy(alpha=0.15f), CircleShape)
                                        .border(2.dp, Color.White.copy(alpha=0.8f), CircleShape)
                                        .shadow(4.dp, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(avatarEmoji, fontSize = 32.sp)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(player.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
                                    
                                    // Score Row
                                    Box(contentAlignment = Alignment.BottomStart) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.graphicsLayer { scaleX = scoreScale; scaleY = scoreScale }) {
                                            Text("🏆", fontSize = 18.sp)
                                            Spacer(Modifier.width(4.dp))
                                            Text(with(viewModel) { player.score.toFa() }, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFD700))
                                        }
                                        
                                        // Floating score feedback
                                        androidx.compose.animation.AnimatedVisibility(
                                            visible = scoreTrigger % 2 != 0,
                                            enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn(),
                                            exit = fadeOut(tween(400)) + slideOutVertically(targetOffsetY = { -50 }),
                                            modifier = Modifier.offset(x = 60.dp, y = (-20).dp)
                                        ) {
                                            Text("+" + with(viewModel) { scoreIncrement.toFa() }, color = Color(0xFF4ADE80), fontWeight = FontWeight.Black, fontSize = 24.sp)
                                        }
                                    }
                                }
                            }
                            
                            // Timer Ring
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(64.dp)
                            ) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    progress = { animatedProgress },
                                    modifier = Modifier.fillMaxSize(),
                                    color = if (showGameplayElements) timerColor else Color.Transparent,
                                    trackColor = if (showGameplayElements) Color.White.copy(alpha=0.1f) else Color.Transparent,
                                    strokeWidth = 6.dp,
                                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                                    gapSize = 0.dp
                                )
                                Text(
                                    with(viewModel) { timeLeft.toFa() }, 
                                    fontSize = 22.sp, 
                                    fontWeight = FontWeight.Black, 
                                    color = if (showGameplayElements) timerColor else Color.Transparent
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Question
                    Box(Modifier.fillMaxWidth().weight(0.35f), contentAlignment = Alignment.Center) {
                        if (showGameplayElements) {
                            CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(with(viewModel) { player.a.toFa() }, fontSize = 52.sp, fontWeight = FontWeight.Black, color = Color.White)
                                Text(" × ", fontSize = 40.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                                Text(with(viewModel) { player.b.toFa() }, fontSize = 52.sp, fontWeight = FontWeight.Black, color = Color.White)
                                Text(" = ", fontSize = 40.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                                AnimatedContent(
                                    targetState = if (input.isEmpty()) "?" else input,
                                    transitionSpec = {
                                        (scaleIn(initialScale = 0.5f) + fadeIn(tween(200))) togetherWith (scaleOut(targetScale = 1.5f) + fadeOut(tween(200)))
                                    },
                                    label = "answerAnim"
                                ) { text ->
                                    val faText = if (text == "?") "?" else with(viewModel) { text.toIntOrNull()?.toFa() ?: text }
                                    Text(
                                        text = faText,
                                        fontSize = 56.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (text == "?") accentColor else Color(0xFFFFD700) // Gold color for answer
                                    )
                                }
                            }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

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
                accentColor = Color(0xFF10B981), // Emerald Green
                modifierWeight = Modifier.weight(1f),
                timeLeft = state.secondsLeft,
                totalTime = state.totalSeconds,
                avatarEmoji = avatar2,
                showGameplayElements = countdownFinished
            )

            // Splitter
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(2.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(alpha=0.3f), Color.Transparent))))
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
"""

new_content = content.replace(target, replacement)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(new_content)

print("Replacement string method done. Diff length:", len(new_content) - len(content))
