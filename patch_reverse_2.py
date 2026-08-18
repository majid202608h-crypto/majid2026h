import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

pattern = r'fun ReverseChallengeScreen\(viewModel: GameViewModel, themeConfig: GameTheme\) \{.*?\n\}\n\n// ─── صفحه ۱: ورود اسم'

replacement = """fun ReverseChallengeScreen(viewModel: GameViewModel, themeConfig: GameTheme) {
    val state by viewModel.reverseState.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isLight = !isDarkMode

    LaunchedEffect(Unit) { viewModel.startReverseChallenge() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // HUD: Timer, Score, Exit
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Exit Button
            Button(
                onClick = { viewModel.navigateTo(GameScreen.MainMenu) },
                colors = ButtonDefaults.buttonColors(containerColor = if (isLight) Color(0xFFF1F5F9) else Color(0xFF334155)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                modifier = Modifier.height(32.dp),
                shape = RoundedCornerShape(50.dp)
            ) {
                Text("خروج 🚪", color = if (isLight) Color(0xFF64748B) else Color(0xFFCBD5E1), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            // Score
            Row(
                modifier = Modifier
                    .background(themeConfig.cardBg, RoundedCornerShape(50.dp))
                    .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(50.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔄 امتیاز:", modifier = Modifier.padding(end = 4.dp), fontSize = 14.sp)
                Text(state.score.toFa(), fontWeight = FontWeight.Black, color = Color(0xFFFFD700), fontSize = 16.sp)
            }

            // Timer
            Row(
                modifier = Modifier
                    .background(
                        if (state.secondsLeft <= 15) Color.Red.copy(alpha = 0.15f) else if (isLight) Color(0xFFF1F5F9) else Color(0xFF334155),
                        RoundedCornerShape(50.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⏱", fontSize = 16.sp, modifier = Modifier.padding(end = 4.dp))
                Text(
                    "${state.secondsLeft.toFa()} ثانیه",
                    color = if (state.secondsLeft <= 15) Color.Red
                            else if (isLight) Color(0xFF334155) else Color(0xFFE2E8F0),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // Progress Bar
        val animatedProgress by animateFloatAsState(
            targetValue = state.timerProgress,
            animationSpec = tween(100, easing = androidx.compose.animation.core.LinearEasing),
            label = "ReverseTimerBar"
        )
        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = if (animatedProgress < 0.2f) Color.Red else Color(0xFF00D2D3),
            trackColor = Color.LightGray.copy(alpha = 0.3f)
        )

        Spacer(Modifier.height(12.dp))

        // Banner & Owl
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("🦉", fontSize = 36.sp, modifier = Modifier.padding(end = 8.dp))
            Box(
                modifier = Modifier
                    .background(Color(0xFF00D2D3), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                val msg = if (state.isLocked && state.lastSelectedOptionIndex != null && state.options[state.lastSelectedOptionIndex!!] == state.correctOption) "آفرین! 🎉" else "🔍 حاصل ضرب را پیدا کن"
                Text(msg, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Question Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Takes available space
                .shadow(6.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x2600D2D3), spotColor = Color(0x2600D2D3))
                .border(2.dp, Color(0xFF00D2D3), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = if (isLight) Color.White else Color(0xFF1E293B)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Formula ? x ? = Answer
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("?", color = Color(0xFFFF6D81), fontSize = 68.sp, fontWeight = FontWeight.Black)
                        Text(" × ", color = if (isLight) Color(0xFF475569) else Color(0xFF94A3B8), fontSize = 50.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 4.dp))
                        Text("?", color = Color(0xFFFF6D81), fontSize = 68.sp, fontWeight = FontWeight.Black)
                        Text(" = ", color = if (isLight) Color(0xFF475569) else Color(0xFF94A3B8), fontSize = 50.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 4.dp))
                        Text(
                            text = state.answerToShow.toFa(),
                            color = Color(0xFF00D2D3),
                            fontSize = 80.sp, fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    "یکی از گزینه‌های زیر حاصل‌ضرب ${state.answerToShow.toFa()} هستند",
                    color = Color.Gray,
                    fontSize = 14.sp, textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Options Grid (4 options)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OptionCard(
                    modifier = Modifier.weight(1f).height(120.dp),
                    pair = state.options.getOrNull(0),
                    index = 0,
                    state = state,
                    isLight = isLight,
                    themeConfig = themeConfig,
                    viewModel = viewModel
                )
                OptionCard(
                    modifier = Modifier.weight(1f).height(120.dp),
                    pair = state.options.getOrNull(1),
                    index = 1,
                    state = state,
                    isLight = isLight,
                    themeConfig = themeConfig,
                    viewModel = viewModel
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OptionCard(
                    modifier = Modifier.weight(1f).height(120.dp),
                    pair = state.options.getOrNull(2),
                    index = 2,
                    state = state,
                    isLight = isLight,
                    themeConfig = themeConfig,
                    viewModel = viewModel
                )
                OptionCard(
                    modifier = Modifier.weight(1f).height(120.dp),
                    pair = state.options.getOrNull(3),
                    index = 3,
                    state = state,
                    isLight = isLight,
                    themeConfig = themeConfig,
                    viewModel = viewModel
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Bottom Stats
        Row(
            modifier = Modifier.fillMaxWidth().height(60.dp).padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                shape = RoundedCornerShape(50.dp),
                colors = CardDefaults.cardColors(containerColor = if (isLight) Color.White else Color(0xFF1E293B)),
                border = BorderStroke(1.dp, if (isLight) Color(0xFFE2E8F0) else Color(0xFF334155)),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                Row(
                    modifier = Modifier.fillMaxHeight().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✅ ${state.correctCount.toFa()}", color = Color(0xFF10B981), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("❌ ${state.wrongCount.toFa()}", color = Color(0xFFEF4444), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun OptionCard(
    modifier: Modifier = Modifier,
    pair: Pair<Int, Int>?,
    index: Int,
    state: com.example.ui.ReverseChallengeState,
    isLight: Boolean,
    themeConfig: GameTheme,
    viewModel: GameViewModel
) {
    if (pair == null) return

    val isSelected = state.lastSelectedOptionIndex == index
    val isCorrectPair = pair == state.correctOption
    val locked = state.isLocked

    val borderColor = when {
        locked && isCorrectPair -> Color(0xFF22C55E)
        locked && isSelected && !isCorrectPair -> Color(0xFFEF4444)
        else -> if (isLight) Color(0xFFE2E8F0) else Color(0xFF334155)
    }
    
    val bgColor = when {
        locked && isCorrectPair -> Color(0xFF22C55E)
        locked && isSelected && !isCorrectPair -> Color(0xFFEF4444)
        else -> if (isLight) Color(0xFFF8FAFC) else Color(0xFF1E293B)
    }
    
    val textColor = when {
        locked && (isCorrectPair || (isSelected && !isCorrectPair)) -> Color.White
        else -> if (isLight) Color(0xFF0F172A) else Color.White
    }

    Card(
        onClick = { if (!state.isLocked) viewModel.submitReverseAnswer(pair, index) },
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .border(2.dp, borderColor, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(pair.first.toFa(), color = textColor, fontSize = 48.sp, fontWeight = FontWeight.Black)
                    Text(" × ", color = textColor.copy(alpha = 0.6f), fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                    Text(pair.second.toFa(), color = textColor, fontSize = 48.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

// ─── صفحه ۱: ورود اسم"""

match = re.search(pattern, content, flags=re.DOTALL)
if match:
    new_content = content.replace(match.group(0), replacement)
    with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
        f.write(new_content)
    print("ReverseChallengeScreen replaced!")
else:
    print("Match not found!")
