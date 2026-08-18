import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

pattern = r'''@Composable\s+fun DuelStatsScreen\(viewModel: com\.example\.ui\.GameViewModel, themeConfig: GameTheme\) \{.*?fun ComparisonRow.*?\}\s*\}'''

new_stats_screen = """@Composable
fun DuelStatsScreen(viewModel: com.example.ui.GameViewModel, themeConfig: GameTheme) {
    val result by viewModel.duelResult.collectAsStateWithLifecycle()
    val state by viewModel.duelState.collectAsStateWithLifecycle()
    val duelPrefs by viewModel.duelPrefs.collectAsStateWithLifecycle()
    
    val p1Score = result.score1
    val p2Score = result.score2
    val p1Name = state.player1.name
    val p2Name = state.player2.name
    val avatar1 = duelPrefs?.player1Avatar ?: "👦"
    val avatar2 = duelPrefs?.player2Avatar ?: "👧"
    val isDraw = result.winner == null
    val winnerName = if (isDraw) "مساوی" else result.winnerName
    val scoreDiff = kotlin.math.abs(p1Score - p2Score)
    val duration = state.totalSeconds
    val p1 = state.player1
    val p2 = state.player2
    
    var visible by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing), label = "countUp"
    )
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    fun fa(num: Any): String = num.toString().replace("0", "۰").replace("1", "۱").replace("2", "۲").replace("3", "۳").replace("4", "۴").replace("5", "۵").replace("6", "۶").replace("7", "۷").replace("8", "۸").replace("9", "۹")
    
    val p1Total = p1.correctCount + p1.wrongCount
    val p2Total = p2.correctCount + p2.wrongCount
    val p1Acc = if (p1Total > 0) ((p1.correctCount.toFloat() / p1Total) * 100).toInt() else 0
    val p2Acc = if (p2Total > 0) ((p2.correctCount.toFloat() / p2Total) * 100).toInt() else 0
    val p1Fast = if (p1.fastestTimeMs == Long.MAX_VALUE) 0f else p1.fastestTimeMs / 1000f
    val p2Fast = if (p2.fastestTimeMs == Long.MAX_VALUE) 0f else p2.fastestTimeMs / 1000f
    val p1Avg = if (p1Total > 0) (p1.totalTimeMs.toFloat() / p1Total) / 1000f else 0f
    val p2Avg = if (p2Total > 0) (p2.totalTimeMs.toFloat() / p2Total) / 1000f else 0f

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF1E1B4B), Color(0xFF312E81))))
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + slideInVertically(initialOffsetY = { it / 4 }, animationSpec = tween(600)),
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🏆 آمار مسابقه", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 8.dp, top = 8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(avatar1, fontSize = 48.sp)
                            Text(p1Name, fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Text("🆚", fontSize = 24.sp)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(avatar2, fontSize = 48.sp)
                            Text(p2Name, fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    val cardBorder = if (!isDraw) BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)) else BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    val cardShadow = if (!isDraw) androidx.compose.ui.graphics.Shadow(color = Color(0xFFFFD700).copy(alpha = 0.6f), blurRadius = 16f) else androidx.compose.ui.graphics.Shadow(color = Color.Transparent, blurRadius = 0f)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        border = cardBorder,
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            Modifier.padding(vertical = 12.dp, horizontal = 16.dp), 
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (isDraw) {
                                Text("🤝 مسابقه مساوی شد", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🏆", fontSize = 36.sp, style = androidx.compose.ui.text.TextStyle(shadow = cardShadow))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = winnerName, 
                                        fontSize = 28.sp, 
                                        fontWeight = FontWeight.ExtraBold, 
                                        color = Color(0xFFFFD700), 
                                        style = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = Color(0xFFFFD700).copy(alpha = 0.5f), blurRadius = 12f))
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("اختلاف امتیاز", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                                    Text(fa((scoreDiff * animProgress).toInt()), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("مدت مسابقه", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                                    Text("${fa((duration * animProgress).toInt())} ثانیه", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Column(Modifier.padding(vertical = 8.dp, horizontal = 12.dp)) {
                            ComparisonRow("✅ پاسخ صحیح", fa((p1.correctCount * animProgress).toInt()), fa((p2.correctCount * animProgress).toInt()), p1.correctCount > p2.correctCount, p2.correctCount > p1.correctCount)
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            ComparisonRow("❌ پاسخ غلط", fa((p1.wrongCount * animProgress).toInt()), fa((p2.wrongCount * animProgress).toInt()), p1.wrongCount < p2.wrongCount && p1Total>0, p2.wrongCount < p1.wrongCount && p2Total>0)
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            ComparisonRow("🎯 درصد دقت", "${fa((p1Acc * animProgress).toInt())}٪", "${fa((p2Acc * animProgress).toInt())}٪", p1Acc > p2Acc, p2Acc > p1Acc)
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            ComparisonRow("🔥 بیشترین کمبو", fa((p1.maxCombo * animProgress).toInt()), fa((p2.maxCombo * animProgress).toInt()), p1.maxCombo > p2.maxCombo, p2.maxCombo > p1.maxCombo)
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            
                            val p1FastStr = if (p1Fast > 0) "${fa(String.format(java.util.Locale.US, "%.1f", p1Fast * animProgress))} ثانیه" else "-"
                            val p2FastStr = if (p2Fast > 0) "${fa(String.format(java.util.Locale.US, "%.1f", p2Fast * animProgress))} ثانیه" else "-"
                            val winFast1 = p1Fast > 0 && (p1Fast < p2Fast || p2Fast == 0f)
                            val winFast2 = p2Fast > 0 && (p2Fast < p1Fast || p1Fast == 0f)
                            ComparisonRow("⚡ سریع‌ترین", p1FastStr, p2FastStr, winFast1, winFast2)
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            
                            val p1AvgStr = if (p1Avg > 0) "${fa(String.format(java.util.Locale.US, "%.1f", p1Avg * animProgress))} ثانیه" else "-"
                            val p2AvgStr = if (p2Avg > 0) "${fa(String.format(java.util.Locale.US, "%.1f", p2Avg * animProgress))} ثانیه" else "-"
                            val winAvg1 = p1Avg > 0 && (p1Avg < p2Avg || p2Avg == 0f)
                            val winAvg2 = p2Avg > 0 && (p2Avg < p1Avg || p1Avg == 0f)
                            ComparisonRow("⏱ میانگین زمان", p1AvgStr, p2AvgStr, winAvg1, winAvg2)
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            
                            ComparisonRow("⭐ امتیاز نهایی", fa((p1Score * animProgress).toInt()), fa((p2Score * animProgress).toInt()), p1Score > p2Score, p2Score > p1Score)
                        }
                    }
                    
                    Button(
                        onClick = { 
                            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick)
                            viewModel.rematchDuel() 
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 8.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                    ) {
                        Text("بازی مجدد 🔄", color = Color(0xFF1E1B4B), fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                    
                    Button(
                        onClick = { 
                            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick)
                            viewModel.exitDuelMode() 
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Text("بازگشت به منوی اصلی 🏠", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ComparisonRow(label: String, val1: String, val2: String, win1: Boolean, win2: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = val1,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = if (win1) Color(0xFFFFD700) else Color.White,
            style = if (win1) androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = Color(0xFFFFD700).copy(alpha = 0.5f), blurRadius = 8f)) else androidx.compose.ui.text.TextStyle.Default
        )
        Text(
            text = label,
            modifier = Modifier.weight(1.2f),
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Text(
            text = val2,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = if (win2) Color(0xFFFFD700) else Color.White,
            style = if (win2) androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = Color(0xFFFFD700).copy(alpha = 0.5f), blurRadius = 8f)) else androidx.compose.ui.text.TextStyle.Default
        )
    }
}
"""

match = re.search(pattern, content, flags=re.DOTALL)
if match:
    new_content = content[:match.start()] + new_stats_screen + content[match.end():]
    with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
        f.write(new_content)
    print("Patched DuelStatsScreen successfully")
else:
    print("Could not find DuelStatsScreen with regex")
