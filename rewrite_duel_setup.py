import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

pattern = r'''@Composable\s+fun DuelSetupScreen\(viewModel: com\.example\.ui\.GameViewModel, themeConfig: GameTheme\) \{.*?(?=@Composable|\Z)'''

new_setup_screen = """@Composable
fun DuelSetupScreen(viewModel: com.example.ui.GameViewModel, themeConfig: GameTheme) {
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isLight = !isDark

    var name1 by remember { mutableStateOf("") }
    var name2 by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableStateOf(60) }

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
                drawCircle(
                    color = Color(0xFF8B5CF6).copy(alpha = 0.15f),
                    radius = 250.dp.toPx(),
                    center = Offset(size.width * 0.5f, size.height * 0.5f)
                )
                // Sparkles
                drawCircle(color = Color.White.copy(alpha = 0.6f), radius = 4.dp.toPx(), center = Offset(size.width * 0.1f, size.height * 0.3f))
                drawCircle(color = Color.White.copy(alpha = 0.4f), radius = 6.dp.toPx(), center = Offset(size.width * 0.85f, size.height * 0.15f))
                drawCircle(color = Color.White.copy(alpha = 0.5f), radius = 3.dp.toPx(), center = Offset(size.width * 0.7f, size.height * 0.6f))
                drawCircle(color = Color.White.copy(alpha = 0.8f), radius = 5.dp.toPx(), center = Offset(size.width * 0.3f, size.height * 0.8f))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                Text(
                    text = "⚔️ چالش دو نفره",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color(0xFF6366F1).copy(alpha = 0.8f),
                            blurRadius = 16f
                        )
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "نام هر دو بازیکن را وارد کنید.",
                    fontSize = 16.sp,
                    color = Color(0xFFC7D2FE)
                )

                Spacer(Modifier.height(32.dp))

                // Player 1 Card (Blue)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A).copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("بازیکن اول", fontSize = 14.sp, color = Color(0xFF93C5FD), fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFF3B82F6), CircleShape)
                                    .border(2.dp, Color(0xFFBFDBFE), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👦", fontSize = 24.sp)
                            }
                            Spacer(Modifier.width(16.dp))
                            OutlinedTextField(
                                value = name1,
                                onValueChange = { if (it.length <= 18) name1 = it },
                                placeholder = { Text("نام بازیکن اول...", color = Color(0xFF93C5FD).copy(alpha=0.6f)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
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
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B).copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("بازیکن دوم", fontSize = 14.sp, color = Color(0xFF6EE7B7), fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFF10B981), CircleShape)
                                    .border(2.dp, Color(0xFFA7F3D0), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👧", fontSize = 24.sp)
                            }
                            Spacer(Modifier.width(16.dp))
                            OutlinedTextField(
                                value = name2,
                                onValueChange = { if (it.length <= 18) name2 = it },
                                placeholder = { Text("نام بازیکن دوم...", color = Color(0xFF6EE7B7).copy(alpha=0.6f)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
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

                Spacer(Modifier.height(24.dp))

                // Duration Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⏱ مدت مسابقه", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        
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
                                        .background(bg, RoundedCornerShape(16.dp))
                                        .border(1.dp, bdColor, RoundedCornerShape(16.dp))
                                        .clickable(
                                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                            indication = androidx.compose.foundation.LocalIndication.current
                                        ) {
                                            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick)
                                            selectedDuration = dur
                                        }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${with(viewModel) { dur.toFa() }} ثانیه",
                                        color = txtColor,
                                        fontSize = 14.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Start Button
                val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val btnScale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, animationSpec = spring(), label = "")
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .graphicsLayer { scaleX = btnScale; scaleY = btnScale }
                        .shadow(if (isPressed) 4.dp else 16.dp, RoundedCornerShape(32.dp), spotColor = Color(0xFFEC4899))
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
                    Text("🚀 شروع مسابقه", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                }

                Spacer(Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick)
                        viewModel.navigateTo(com.example.ui.GameScreen.MainMenu)
                    }
                ) {
                    Text("بازگشت", color = Color(0xFFA5B4FC), fontSize = 16.sp)
                }
                
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
"""

match = re.search(pattern, content, flags=re.DOTALL)
if match:
    # Check if there is another @Composable immediately after
    start_idx = match.start()
    end_idx = match.end()
    
    # We replace from start_idx to the next @Composable, but let's be more precise
    new_content = content[:start_idx] + new_setup_screen + "\n" + content[end_idx:]
    with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
        f.write(new_content)
    print("Patched DuelSetupScreen successfully")
else:
    print("Could not find DuelSetupScreen")
