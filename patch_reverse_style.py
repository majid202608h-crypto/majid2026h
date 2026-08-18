import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

pattern = r'fun ReverseChallengeScreen\(viewModel: GameViewModel, themeConfig: GameTheme\) \{.*?\n\}\n\n// ─── صفحه ۱: ورود اسم'

replacement = """fun ReverseChallengeScreen(viewModel: GameViewModel, themeConfig: GameTheme) {
    val state by viewModel.reverseState.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isLightMode = !isDarkMode

    LaunchedEffect(Unit) { viewModel.startReverseChallenge() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = state.secondsLeft.toFa(),
                    fontSize = 32.sp,
                    color = if (state.secondsLeft < 10) Color.Red else Color(0xFFFFD32A),
                    fontWeight = FontWeight.Black
                )
                Text("ثانیه باقی مانده", color = if (isLightMode) Color(0xFF475569) else Color.LightGray, fontSize = 9.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = state.score.toFa(),
                    fontSize = 32.sp,
                    color = if (isLightMode) Color(0xFF0F172A) else Color.White,
                    fontWeight = FontWeight.Black
                )
                Text("امتیاز نهایی", color = if (isLightMode) Color(0xFF475569) else Color.LightGray, fontSize = 9.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🔥 ${state.streak.toFa()}",
                    fontSize = 24.sp,
                    color = Color(0xFFFF6348),
                    fontWeight = FontWeight.Black
                )
                Text("ضرب متوالی", color = if (isLightMode) Color(0xFF475569) else Color.LightGray, fontSize = 9.sp)
            }

            IconButton(
                onClick = { viewModel.navigateTo(GameScreen.MainMenu) },
                modifier = Modifier.background(themeConfig.cardBg, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit",
                    tint = if (isLightMode) themeConfig.primaryColor else Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Fully animated and glass-smooth timer bar
        val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
            targetValue = state.timerProgress,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 100, easing = androidx.compose.animation.core.LinearEasing),
            label = "SmoothReverseTimer"
        )

        // Timer progress indicator
        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (state.secondsLeft < 10) Color.Red else Color(0xFF2ED573),
            trackColor = Color.White.copy(alpha = 0.08f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Dialogue mascot helper
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Start
        ) {
            val currentAvatarIcon = GameConfig.AVATARS.find { it.id == (viewModel.userProfile.value?.activeAvatar ?: "owl") }?.icon ?: "🦉"

            Text(
                text = currentAvatarIcon,
                fontSize = 46.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .background(themeConfig.cardBg, RoundedCornerShape(14.dp, 14.dp, 14.dp, 0.dp))
                    .border(1.dp, themeConfig.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp, 14.dp, 14.dp, 0.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                val msg = if (state.isLocked && state.lastSelectedOptionIndex != null && state.options[state.lastSelectedOptionIndex!!] == state.correctOption) "آفرین! 🎉" else state.charMessage
                Text(
                    text = msg,
                    color = if (isLightMode) Color(0xFF0F172A) else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Formula representation Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = themeConfig.cardBg),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "?",
                            color = Color(0xFF00D2D3),
                            fontSize = 62.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = " × ",
                            color = Color(0xFFFF6D81),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Text(
                            text = "?",
                            color = Color(0xFF00D2D3),
                            fontSize = 62.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = " = ",
                            color = Color(0xFFFFD32A),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Text(
                            text = state.answerToShow.toFa(),
                            color = Color(0xFFFFD32A),
                            fontSize = 62.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Answers
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.options.size) { index ->
                val option = state.options[index]
                val isCorrectVal = option == state.correctOption
                val itemSelected = state.lastSelectedOptionIndex == index
                val isLight = isLightMode

                val backgroundCol = when {
                    state.isLocked && isCorrectVal -> Color(0xFF2ED573) // Solid Vivid Green
                    state.isLocked && itemSelected && !isCorrectVal -> Color(0xFFEA2027) // Solid Vivid Red
                    else -> themeConfig.cardBg
                }

                val borderCol = when {
                    state.isLocked && isCorrectVal -> Color(0xFF2ED573)
                    state.isLocked && itemSelected && !isCorrectVal -> Color(0xFFEA2027)
                    else -> if (isLight) themeConfig.primaryColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.15f)
                }

                val txtColor = when {
                    state.isLocked && (isCorrectVal || (itemSelected && !isCorrectVal)) -> Color.White
                    else -> if (isLight) Color(0xFF0F172A) else Color.White
                }

                Card(
                    onClick = {
                        if (!state.isLocked) {
                            viewModel.submitReverseAnswer(option, index)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(86.dp)
                        .border(2.dp, borderCol, RoundedCornerShape(16.dp))
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = backgroundCol,
                        contentColor = txtColor,
                        disabledContainerColor = backgroundCol,
                        disabledContentColor = txtColor
                    ),
                    enabled = true
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = option.first.toFa(),
                                    color = txtColor,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = " × ",
                                    color = txtColor.copy(alpha = 0.6f),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                Text(
                                    text = option.second.toFa(),
                                    color = txtColor,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

// ─── صفحه ۱: ورود اسم"""

match = re.search(pattern, content, flags=re.DOTALL)
if match:
    new_content = content.replace(match.group(0), replacement)
    with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
        f.write(new_content)
    print("Replaced ReverseChallengeScreen successfully!")
else:
    print("Match not found!")
