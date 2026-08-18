import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

pattern = r'''@Composable\s+fun DuelSetupScreen\(viewModel: com\.example\.ui\.GameViewModel, themeConfig: GameTheme\) \{.*?(?=@Composable|\Z)'''

new_setup_screen = """@Composable
fun DuelSetupScreen(viewModel: com.example.ui.GameViewModel, themeConfig: GameTheme) {
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isLight = !isDark

    val duelPrefs by viewModel.duelPrefs.collectAsStateWithLifecycle()
    
    var name1 by remember(duelPrefs) { mutableStateOf(duelPrefs?.player1Name ?: "") }
    var name2 by remember(duelPrefs) { mutableStateOf(duelPrefs?.player2Name ?: "") }
    var avatar1 by remember(duelPrefs) { mutableStateOf(duelPrefs?.player1Avatar ?: "👦") }
    var avatar2 by remember(duelPrefs) { mutableStateOf(duelPrefs?.player2Avatar ?: "👧") }
    var selectedDuration by remember(duelPrefs) { mutableStateOf(duelPrefs?.matchDuration ?: 60) }
    
    val p1Avatars = listOf("👦", "👧", "🧒", "👱‍♂️", "👩", "👨", "👩‍🦱", "👨‍🦱")
    val p2Avatars = listOf("🐯", "🐻", "🐶", "🐱", "🐰", "🦊", "🐼", "🐨", "🐸", "🦁")

    val bgBrush = Brush.verticalGradient(
        listOf(
            Color(0xFF1E1B4B),
            Color(0xFF312E81),
            Color(0xFF1E1B4B)
        )
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(modifier = Modifier.fillMaxSize().background(bgBrush)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = Color(0xFF6366F1).copy(alpha = 0.15f), radius = 250.dp.toPx(), center = Offset(size.width * 0.2f, size.height * 0.2f))
                drawCircle(color = Color(0xFFEC4899).copy(alpha = 0.1f), radius = 350.dp.toPx(), center = Offset(size.width * 0.8f, size.height * 0.8f))
                // Minimal sparkles
                drawCircle(color = Color.White.copy(alpha = 0.4f), radius = 4.dp.toPx(), center = Offset(size.width * 0.15f, size.height * 0.25f))
                drawCircle(color = Color.White.copy(alpha = 0.3f), radius = 3.dp.toPx(), center = Offset(size.width * 0.85f, size.height * 0.15f))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(32.dp))

                Text(
                    text = "⚔️ چالش دو نفره",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color(0xFF6366F1).copy(alpha = 0.8f),
                            blurRadius = 12f
                        )
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "آماده نبرد هستید؟ نام بازیکنان را وارد کنید",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFC7D2FE),
                    lineHeight = 28.sp
                )

                Spacer(Modifier.height(24.dp))

                // Player 1 Card (Blue)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A).copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFF3B82F6), CircleShape)
                                .border(2.dp, Color(0xFFBFDBFE), CircleShape)
                                .clickable {
                                    viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick)
                                    val next = p1Avatars[(p1Avatars.indexOf(avatar1) + 1) % p1Avatars.size]
                                    avatar1 = next
                                    viewModel.updateDuelPrefs(player1Avatar = next)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(avatar1, fontSize = 28.sp)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("بازیکن اول", fontSize = 18.sp, color = Color(0xFF93C5FD), fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = name1,
                                onValueChange = { if (it.length <= 18) { name1 = it; viewModel.updateDuelPrefs(player1Name = it) } },
                                placeholder = { Text("نام...", color = Color(0xFF93C5FD).copy(alpha=0.5f), fontSize = 20.sp, fontWeight = FontWeight.Medium) },
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                                modifier = Modifier.fillMaxWidth().height(64.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF60A5FA),
                                    unfocusedBorderColor = Color(0xFF3B82F6).copy(alpha=0.3f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Player 2 Card (Green)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B).copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFF10B981), CircleShape)
                                .border(2.dp, Color(0xFFA7F3D0), CircleShape)
                                .clickable {
                                    viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick)
                                    val next = p2Avatars[(p2Avatars.indexOf(avatar2) + 1) % p2Avatars.size]
                                    avatar2 = next
                                    viewModel.updateDuelPrefs(player2Avatar = next)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(avatar2, fontSize = 28.sp)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("بازیکن دوم", fontSize = 18.sp, color = Color(0xFF6EE7B7), fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = name2,
                                onValueChange = { if (it.length <= 18) { name2 = it; viewModel.updateDuelPrefs(player2Name = it) } },
                                placeholder = { Text("نام...", color = Color(0xFF6EE7B7).copy(alpha=0.5f), fontSize = 20.sp, fontWeight = FontWeight.Medium) },
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                                modifier = Modifier.fillMaxWidth().height(64.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF34D399),
                                    unfocusedBorderColor = Color(0xFF10B981).copy(alpha=0.3f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Duration Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⏱ مدت مسابقه", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(30, 60, 90, 120).forEach { dur ->
                                val selected = selectedDuration == dur
                                val scale by animateFloatAsState(targetValue = if (selected) 1.05f else 1f, label = "")
                                val bg = if (selected) Color(0xFF8B5CF6) else Color.White.copy(alpha=0.1f)
                                val txtColor = if (selected) Color.White else Color(0xFFA5B4FC)
                                val bdColor = if (selected) Color(0xFFC4B5FD) else Color.Transparent

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .graphicsLayer { scaleX = scale; scaleY = scale }
                                        .background(bg, RoundedCornerShape(12.dp))
                                        .border(1.dp, bdColor, RoundedCornerShape(12.dp))
                                        .clickable(
                                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                            indication = androidx.compose.foundation.LocalIndication.current
                                        ) {
                                            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick)
                                            selectedDuration = dur
                                            viewModel.updateDuelPrefs(matchDuration = dur)
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${with(viewModel) { dur.toFa() }} ثانیه",
                                        color = txtColor,
                                        fontSize = 18.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Start Button
                val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val btnScale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, animationSpec = spring(), label = "")
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .graphicsLayer { scaleX = btnScale; scaleY = btnScale }
                        .shadow(if (isPressed) 4.dp else 16.dp, RoundedCornerShape(32.dp), spotColor = Color(0xFF8B5CF6))
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))),
                            RoundedCornerShape(32.dp)
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = androidx.compose.foundation.LocalIndication.current
                        ) {
                            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick)
                            viewModel.startDuel(name1, name2, selectedDuration)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🚀 شروع مسابقه", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                }

                Spacer(Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick)
                        viewModel.navigateTo(com.example.ui.GameScreen.MainMenu)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, Color(0xFFA5B4FC).copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack, 
                        contentDescription = "بازگشت", 
                        tint = Color(0xFFA5B4FC), 
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("بازگشت به منو", color = Color(0xFFA5B4FC), fontSize = 20.sp, fontWeight = FontWeight.Medium)
                }
                
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
"""

match = re.search(pattern, content, flags=re.DOTALL)
if match:
    start_idx = match.start()
    end_idx = match.end()
    new_content = content[:start_idx] + new_setup_screen + "\n" + content[end_idx:]
    with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
        f.write(new_content)
    print("Patched DuelSetupScreen typography successfully")
else:
    print("Could not find DuelSetupScreen")
