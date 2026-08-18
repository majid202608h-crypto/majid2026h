import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Replace from NumericKeypad to the end of DuelGameScreen
match_regex = r'@Composable\nfun NumericKeypad\([\s\S]*?            modifierWeight = Modifier\.weight\(1f\)\n        \)\n    \}\n\}'

replacement = """@Composable
fun NumericKeypad(
    currentValue: String,
    onNumberPressed: (Int) -> Unit,
    onDelete: () -> Unit,
    onSubmit: () -> Unit,
    enabled: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    isFlipped: Boolean = false
) {
    val haptic = LocalHapticFeedback.current

    val keys = listOf(
        listOf(1, 2, 3),
        listOf(4, 5, 6),
        listOf(7, 8, 9),
        listOf(-2, 0, -1) // -2 for Submit, -1 for Delete
    )
    
    // Reverse rows for player 2 so they see 123 at the "top" of their visual keypad (which is rendered upside down)
    // Actually, if it's rotated 180 degrees via modifier, they'll see 123 at bottom unless we reverse both rows and columns.
    // The prompt didn't ask for inverted keypad. Wait, previously it was rotated 180. If rotated 180, 123 is at the bottom.
    // If we want 123 at the top for them, we should reverse rows AND columns. 
    // Let's just use the exact layout 123/456/789/v0x. When rotated 180, it will appear upside down.
    // To make it right for player 2 (flipped), we need to reverse rows and items if we don't rotate it, OR just let the parent rotate it. The parent rotates the whole half.

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
                    
                    val buttonBrush = when {
                        isSubmit -> Brush.verticalGradient(listOf(Color(0xFF4ADE80), Color(0xFF16A34A)))
                        isDelete -> Brush.verticalGradient(listOf(Color(0xFFF87171), Color(0xFFDC2626)))
                        else -> Brush.verticalGradient(listOf(accentColor.copy(alpha=0.7f), accentColor))
                    }
                    
                    val glowColor = when {
                        isSubmit -> Color(0xFF22C55E).copy(alpha = 0.5f)
                        isDelete -> Color(0xFFEF4444).copy(alpha = 0.5f)
                        else -> accentColor.copy(alpha = 0.5f)
                    }

                    var isPressed by remember { mutableStateOf(false) }
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.95f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                        label = "scaleAnim"
                    )
                    
                    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                shadowElevation = if (isPressed) 2.dp.toPx() else 8.dp.toPx()
                                shape = RoundedCornerShape(20.dp)
                                clip = false
                            }
                            .shadow(
                                elevation = if (isPressed) 2.dp else 12.dp,
                                shape = RoundedCornerShape(20.dp),
                                ambientColor = glowColor,
                                spotColor = glowColor
                            )
                            .background(buttonBrush, RoundedCornerShape(20.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                            .clickable(
                                interactionSource = interactionSource,
                                indication = androidx.compose.foundation.LocalIndication.current,
                                enabled = enabled
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (isDelete) onDelete()
                                else if (isSubmit) onSubmit()
                                else onNumberPressed(key)
                            }
                            .pointerInput(enabled) {
                                detectTapGestures(
                                    onPress = {
                                        isPressed = true
                                        try {
                                            awaitRelease()
                                        } finally {
                                            isPressed = false
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDelete) {
                            Text("⌫", fontSize = 28.sp, color = Color.White)
                        } else if (isSubmit) {
                            Text("✓", fontSize = 30.sp, color = Color.White, fontWeight = FontWeight.Black)
                        } else {
                            Text(key.toString(), fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Black)
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

    // Modern AAA mobile game background: Dark purple gradient, soft lighting.
    val bgBrush = Brush.verticalGradient(
        listOf(
            Color(0xFF1E1B4B), // Very dark indigo/purple
            Color(0xFF312E81),
            Color(0xFF1E1B4B)
        )
    )

    Box(modifier = Modifier.fillMaxSize().background(bgBrush)) {
        // Particles or subtle glowing orbs
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFF6366F1).copy(alpha = 0.15f),
                radius = 300.dp.toPx(),
                center = Offset(size.width * 0.2f, size.height * 0.2f)
            )
            drawCircle(
                color = Color(0xFFEC4899).copy(alpha = 0.15f),
                radius = 400.dp.toPx(),
                center = Offset(size.width * 0.8f, size.height * 0.8f)
            )
        }

        @Composable
        fun PlayerHalf(
            player: com.example.ui.DuelPlayerState,
            playerNum: Int,
            isFlipped: Boolean,
            accentColor: Color,
            modifierWeight: Modifier = Modifier
        ) {
            var input by remember { mutableStateOf("") }
            LaunchedEffect(player.a, player.b) { input = "" }

            val rotation = if (isFlipped) 180f else 0f

            Box(
                modifier = modifierWeight
                    .fillMaxWidth()
                    .padding(12.dp)
                    .graphicsLayer { rotationZ = rotation }
            ) {
                Column(Modifier.fillMaxSize()) {
                    // Header: Avatar, Name, Score, Combo
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(48.dp).background(accentColor.copy(alpha = 0.3f), CircleShape).border(2.dp, accentColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("P$playerNum", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(player.name, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                                if (player.streak >= 3) {
                                    val infiniteTransition = rememberInfiniteTransition(label = "combo")
                                    val scale by infiniteTransition.animateFloat(
                                        initialValue = 0.95f,
                                        targetValue = 1.05f,
                                        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
                                        label = "comboScale"
                                    )
                                    Text(
                                        with(viewModel) { player.streak.toFa() } + "× Combo 🔥",
                                        fontSize = 14.sp, color = Color(0xFFFDE047), fontWeight = FontWeight.Bold,
                                        modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
                                    )
                                }
                            }
                        }
                        
                        // Score
                        Box(
                            Modifier.background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("🏆 ${with(viewModel) { player.score.toFa() }}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFFFBBF24))
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Question
                    Box(Modifier.fillMaxWidth().weight(0.25f), contentAlignment = Alignment.Center) {
                        CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(with(viewModel) { player.a.toFa() }, fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color.White)
                                Text(" × ", fontSize = 36.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                                Text(with(viewModel) { player.b.toFa() }, fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color.White)
                                Text(" = ", fontSize = 36.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                                Text("؟", fontSize = 48.sp, fontWeight = FontWeight.Black, color = accentColor)
                            }
                        }
                    }

                    // Answer Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .shadow(16.dp, RoundedCornerShape(24.dp), ambientColor = accentColor, spotColor = accentColor)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                            .border(2.dp, Brush.linearGradient(listOf(accentColor, Color.White.copy(alpha=0.3f))), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val displayText = if (input.isEmpty()) "..." else input
                        Text(
                            text = displayText,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            color = if (input.isEmpty()) Color.White.copy(alpha = 0.3f) else Color.White,
                            letterSpacing = 4.sp
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Keypad
                    NumericKeypad(
                        currentValue = input,
                        onNumberPressed = { num -> if (input.length < 3) input += num.toString() },
                        onDelete = { if (input.isNotEmpty()) input = input.dropLast(1) },
                        onSubmit = { input.toIntOrNull()?.let { viewModel.submitDuelAnswer(playerNum, it) } },
                        enabled = !player.answered,
                        accentColor = accentColor,
                        modifier = Modifier.fillMaxWidth().weight(0.75f)
                    )
                    
                    // Progress bar (Visual feedback for correct/incorrect could be added here)
                    if (player.feedbackText.isNotEmpty()) {
                        Text(
                            player.feedbackText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (player.feedbackIsCorrect == true) Color(0xFF4ADE80) else Color(0xFFF87171),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Column(Modifier.fillMaxSize()) {
            // Top Half (Player 2, Flipped)
            PlayerHalf(
                player = state.player2, playerNum = 2, isFlipped = true,
                accentColor = Color(0xFF10B981), // Emerald Green
                modifierWeight = Modifier.weight(1f)
            )

            // Center Timer
            Box(Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.Center) {
                // Background glow behind timer
                Box(Modifier.fillMaxWidth(0.6f).fillMaxHeight().background(
                    Brush.horizontalGradient(listOf(Color(0xFF10B981).copy(alpha=0.2f), Color.Transparent, Color(0xFF3B82F6).copy(alpha=0.2f)))
                ))
                
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(32.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "timerPulse")
                    val pulse by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = if (state.secondsLeft <= 10) 1.2f else 1f,
                        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
                        label = "timerPulseAnim"
                    )
                    Text(
                        "⏱ ${with(viewModel) { state.secondsLeft.toFa() }}",
                        fontSize = 28.sp, fontWeight = FontWeight.Black,
                        color = if (state.secondsLeft <= 10) Color(0xFFEF4444) else Color.White,
                        modifier = Modifier.graphicsLayer { scaleX = pulse; scaleY = pulse }
                    )
                }
            }

            // Bottom Half (Player 1, Normal)
            PlayerHalf(
                player = state.player1, playerNum = 1, isFlipped = false,
                accentColor = Color(0xFF3B82F6), // Blue
                modifierWeight = Modifier.weight(1f)
            )
        }
    }
}"""

# Actually, the file contains @Composable fun NumericKeypad ... then fun DuelGameScreen... till the end.
# Wait, let me replace it using regex precisely.

import sys
if not re.search(match_regex, content):
    print("Could not find the target block to replace.")
    sys.exit(1)

content = re.sub(match_regex, replacement, content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

print("Patch applied successfully.")
