import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

pattern = r'''                    // Question\n                    Box\(Modifier\.fillMaxWidth\(\)\.weight\(0\.35f\), contentAlignment = Alignment\.Center\) \{\n                        if \(showGameplayElements\) \{\n                            CompositionLocalProvider\(androidx\.compose\.ui\.platform\.LocalLayoutDirection provides androidx\.compose\.ui\.unit\.LayoutDirection\.Ltr\) \{\n                            Row\(verticalAlignment = Alignment\.CenterVertically\) \{\n                                Text\(with\(viewModel\) \{ player\.a\.toFa\(\) \}, fontSize = 52\.sp, fontWeight = FontWeight\.Black, color = Color\.White\)\n                                Text\(" × ", fontSize = 40\.sp, color = Color\.White\.copy\(alpha = 0\.7f\), fontWeight = FontWeight\.Bold\)\n                                Text\(with\(viewModel\) \{ player\.b\.toFa\(\) \}, fontSize = 52\.sp, fontWeight = FontWeight\.Black, color = Color\.White\)\n                                Text\(" = ", fontSize = 40\.sp, color = Color\.White\.copy\(alpha = 0\.7f\), fontWeight = FontWeight\.Bold\)\n                                AnimatedContent\(\n                                    targetState = if \(input\.isEmpty\(\)\) "\?" else input,\n                                    transitionSpec = \{\n                                        \(scaleIn\(initialScale = 0\.5f\) \+ fadeIn\(tween\(200\)\)\) togetherWith \(scaleOut\(targetScale = 1\.5f\) \+ fadeOut\(tween\(200\)\)\)\n                                    \},\n                                    label = "answerAnim"\n                                \) \{ text ->\n                                    val faText = if \(text == "\?"\) "\?" else with\(viewModel\) \{ text\.toIntOrNull\(\)\?\.toFa\(\) \?: text \}\n                                    Text\(\n                                        text = faText,\n                                        fontSize = 56\.sp,\n                                        fontWeight = FontWeight\.Black,\n                                        color = if \(text == "\?"\) accentColor else Color\(0xFFFFD700\) // Gold color for answer\n                                    \)\n                                \}\n                            \}\n                            \}\n                        \}\n                    \}'''

replacement = """                    // Question Card
                    var questionShakeTrigger by remember { mutableStateOf(0) }
                    var questionBounceTrigger by remember { mutableStateOf(0) }
                    var currentQuestionKey by remember { mutableStateOf("${player.a}_${player.b}") }
                    
                    LaunchedEffect(player.a, player.b) {
                        currentQuestionKey = "${player.a}_${player.b}"
                    }

                    LaunchedEffect(player.feedbackIsCorrect) {
                        if (player.feedbackIsCorrect == false) {
                            questionShakeTrigger++
                        } else if (player.feedbackIsCorrect == true) {
                            questionBounceTrigger++
                        }
                    }

                    val shakeOffset by animateFloatAsState(
                        targetValue = if (questionShakeTrigger % 2 == 0) 0f else 15f,
                        animationSpec = spring(dampingRatio = 0.15f, stiffness = 3000f),
                        finishedListener = { if (questionShakeTrigger % 2 != 0) questionShakeTrigger++ },
                        label = "shake"
                    )

                    val bounceScale by animateFloatAsState(
                        targetValue = if (questionBounceTrigger % 2 == 0) 1f else 1.05f,
                        animationSpec = spring(dampingRatio = 0.4f, stiffness = 600f),
                        finishedListener = { if (questionBounceTrigger % 2 != 0) questionBounceTrigger++ },
                        label = "bounce"
                    )

                    val isCorrect = player.feedbackIsCorrect == true
                    val isWrong = player.feedbackIsCorrect == false
                    
                    val questionBgColor = when {
                        isCorrect -> Color(0xFF10B981).copy(alpha = 0.2f)
                        isWrong -> Color(0xFFEF4444).copy(alpha = 0.2f)
                        else -> Color(0xFF312E81).copy(alpha = 0.4f)
                    }
                    
                    val questionBorderColor = when {
                        isCorrect -> Color(0xFF10B981).copy(alpha = 0.8f)
                        isWrong -> Color(0xFFEF4444).copy(alpha = 0.8f)
                        else -> Color.White.copy(alpha = 0.15f)
                    }
                    
                    val shadowGlowColor = when {
                        isCorrect -> Color(0xFF10B981)
                        isWrong -> Color(0xFFEF4444)
                        else -> accentColor
                    }

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(0.35f)
                            .padding(horizontal = 16.dp)
                            .offset(x = shakeOffset.dp)
                            .graphicsLayer {
                                scaleX = bounceScale
                                scaleY = bounceScale
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (showGameplayElements) {
                            AnimatedContent(
                                targetState = currentQuestionKey,
                                transitionSpec = {
                                    (scaleIn(initialScale = 0.8f) + fadeIn(tween(200))) togetherWith (scaleOut(targetScale = 1.2f) + fadeOut(tween(200)))
                                },
                                label = "questionAnim"
                            ) { _ ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(0.9f)
                                        .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = shadowGlowColor, spotColor = shadowGlowColor),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = questionBgColor),
                                    border = BorderStroke(1.dp, questionBorderColor)
                                ) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(with(viewModel) { player.a.toFa() }, fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color.White)
                                                Text(" × ", fontSize = 36.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                                                Text(with(viewModel) { player.b.toFa() }, fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color.White)
                                                Text(" = ", fontSize = 36.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                                                
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
                                                        fontSize = 52.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = if (text == "?") accentColor else Color(0xFFFFD700)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }"""

match = re.search(pattern, content, flags=re.DOTALL)
if match:
    new_content = content.replace(match.group(0), replacement)
    with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
        f.write(new_content)
    print("Question card replaced!")
else:
    print("Match not found. Checking...")

