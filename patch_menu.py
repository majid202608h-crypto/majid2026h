import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

pattern = r'''                Button\(\n                    onClick = \{ viewModel\.goToDuelSetup\(\) \},\n                    modifier = Modifier\.fillMaxWidth\(\)\.height\(56\.dp\),\n                    shape = RoundedCornerShape\(50\.dp\),\n                    colors = ButtonDefaults\.buttonColors\(\n                        containerColor = Color\(0xFF7F77DD\)\.copy\(alpha = 0\.15f\)\n                    \),\n                    border = BorderStroke\(1\.5\.dp, Color\(0xFF7F77DD\)\.copy\(alpha = 0\.5f\)\)\n                \) \{\n                    Row\(verticalAlignment = Alignment\.CenterVertically\) \{\n                        Text\("👥", fontSize = 22\.sp, modifier = Modifier\.padding\(end = 8\.dp\)\)\n                        Column \{\n                            Text\("چالش دو نفره", color = Color\(0xFF534AB7\),\n                                fontSize = 15\.sp, fontWeight = FontWeight\.Black\)\n                            Text\("هر کدام نیمه‌ای از صفحه",\n                                color = Color\(0xFF534AB7\)\.copy\(alpha = 0\.7f\), fontSize = 10\.sp\)\n                        \}\n                    \}\n                \}\n\n                Spacer\(modifier = Modifier\.height\(4\.dp\)\)\n\n                // Row 3 \(Shop spanning full width with smaller height\)\n                Card\(\n                    onClick = \{ viewModel\.navigateTo\(GameScreen\.Shop\) \},\n                    shape = RoundedCornerShape\(28\.dp\),\n                    border = BorderStroke\(1\.dp, if \(isDarkMode\) Color\(0x26FFFFFF\) else Color\.White\.copy\(alpha = 0\.5f\)\),\n                    colors = CardDefaults\.cardColors\(containerColor = if \(isDarkMode\) Color\(0xFF1E1B4B\)\.copy\(alpha = 0\.75f\) else Color\.White\.copy\(alpha = 0\.75f\)\),\n                    modifier = Modifier\n                        \.fillMaxWidth\(\)\n                        \.height\(76\.dp\)\n                        \.shadow\(\n                            elevation = 10\.dp,\n                            shape = RoundedCornerShape\(28\.dp\),\n                            ambientColor = if \(isDarkMode\) Color\(0x1A000000\) else Color\(0x1A785AB4\),\n                            spotColor = if \(isDarkMode\) Color\(0x1A000000\) else Color\(0x1A785AB4\)\n                        \)\n                \) \{\n                    Row\(\n                        modifier = Modifier\n                            \.fillMaxSize\(\)\n                            \.padding\(vertical = 8\.dp, horizontal = 16\.dp\),\n                        verticalAlignment = Alignment\.CenterVertically,\n                        horizontalArrangement = Arrangement\.Center\n                    \) \{\n                        Box\(\n                            modifier = Modifier\n                                \.size\(42\.dp\)\n                                \.background\(\n                                    brush = Brush\.radialGradient\(\n                                        colors = listOf\(Color\(0xFFD16CFF\)\.copy\(alpha = 0\.25f\), Color\(0xFFD16CFF\)\.copy\(alpha = 0\.05f\)\)\n                                    \),\n                                    shape = CircleShape\n                                \)\n                                \.border\(1\.dp, Color\(0xFFD16CFF\)\.copy\(alpha = 0\.4f\), CircleShape\),\n                            contentAlignment = Alignment\.Center\n                        \) \{\n                            Text\("🏪", fontSize = 24\.sp\)\n                        \}\n                        Spacer\(modifier = Modifier\.width\(16\.dp\)\)\n                        Column \{\n                            Text\(\n                                text = "فروشگاه جادویی",\n                                color = if \(isDarkMode\) Color\(0xFFE9D5FF\) else Color\(0xFF5B10D3\),\n                                fontWeight = FontWeight\.ExtraBold,\n                                fontSize = 16\.sp\n                            \)\n                            Text\(\n                                text = "خرید آواتار و شخصی‌سازی محیط",\n                                color = if \(isDarkMode\) Color\(0xFFA78BFA\) else Color\(0xFF706FD3\),\n                                fontWeight = FontWeight\.Medium,\n                                fontSize = 12\.sp\n                            \)\n                        \}\n                    \}\n                \}\n\n            \}\n\n            // Bottom section: Subscription button capsule\n            Button\(\n                onClick = \{ viewModel\.navigateTo\(GameScreen\.Subscription\) \},\n                colors = ButtonDefaults\.buttonColors\(containerColor = if \(isDarkMode\) Color\(0xFF1E1B4B\)\.copy\(alpha = 0\.85f\) else Color\.White\),\n                border = BorderStroke\(2\.dp, if \(isDarkMode\) Color\(0xFFA855F7\) else Color\(0xFF9B4DFF\)\),\n                shape = RoundedCornerShape\(50\.dp\),\n                modifier = Modifier\n                    \.fillMaxWidth\(0\.85f\)\n                    \.height\(56\.dp\)\n                    \.shadow\(\n                        elevation = 8\.dp,\n                        shape = RoundedCornerShape\(50\.dp\),\n                        ambientColor = if \(isDarkMode\) Color\(0x1A000000\) else Color\(0x269B4DFF\),\n                        spotColor = if \(isDarkMode\) Color\(0x1A000000\) else Color\(0x269B4DFF\)\n                    \)\n            \) \{\n                Row\(\n                    verticalAlignment = Alignment\.CenterVertically,\n                    horizontalArrangement = Arrangement\.Center\n                \) \{\n                    Text\("💎", fontSize = 16\.sp\)\n                    Spacer\(modifier = Modifier\.width\(8\.dp\)\)\n                    Text\(\n                        text = "خرید اشتراک ویژه",\n                        color = if \(isDarkMode\) Color\(0xFFE9D5FF\) else Color\(0xFF9B4DFF\),\n                        fontWeight = FontWeight\.ExtraBold,\n                        fontSize = 15\.sp\n                    \)\n                \}\n            \}'''

replacement = """                // Row 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        onClick = { viewModel.goToDuelSetup() },
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.dp, if (isDarkMode) Color(0x26FFFFFF) else Color.White.copy(alpha = 0.5f)),
                        colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF1E1B4B).copy(alpha = 0.75f) else Color.White.copy(alpha = 0.75f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(116.dp)
                            .shadow(
                                elevation = 10.dp,
                                shape = RoundedCornerShape(28.dp),
                                ambientColor = if (isDarkMode) Color(0x1A000000) else Color(0x1A785AB4),
                                spotColor = if (isDarkMode) Color(0x1A000000) else Color(0x1A785AB4)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(Color(0xFF7F77DD).copy(alpha = 0.25f), Color(0xFF7F77DD).copy(alpha = 0.05f))
                                        ),
                                        shape = CircleShape
                                    )
                                    .border(1.dp, Color(0xFF7F77DD).copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👥", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "چالش دو نفره",
                                color = if (isDarkMode) Color(0xFFE9D5FF) else Color(0xFF5B10D3),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "نبرد دونفره",
                                color = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF706FD3),
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Card(
                        onClick = { viewModel.navigateTo(GameScreen.Shop) },
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.dp, if (isDarkMode) Color(0x26FFFFFF) else Color.White.copy(alpha = 0.5f)),
                        colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF1E1B4B).copy(alpha = 0.75f) else Color.White.copy(alpha = 0.75f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(116.dp)
                            .shadow(
                                elevation = 10.dp,
                                shape = RoundedCornerShape(28.dp),
                                ambientColor = if (isDarkMode) Color(0x1A000000) else Color(0x1A785AB4),
                                spotColor = if (isDarkMode) Color(0x1A000000) else Color(0x1A785AB4)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(Color(0xFFD16CFF).copy(alpha = 0.25f), Color(0xFFD16CFF).copy(alpha = 0.05f))
                                        ),
                                        shape = CircleShape
                                    )
                                    .border(1.dp, Color(0xFFD16CFF).copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🏪", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "فروشگاه جادویی",
                                color = if (isDarkMode) Color(0xFFE9D5FF) else Color(0xFF5B10D3),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "خرید آواتار",
                                color = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF706FD3),
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

            }

            // Bottom section: Subscription button capsule
            Button(
                onClick = { viewModel.navigateTo(GameScreen.Subscription) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                border = BorderStroke(2.dp, Color(0xFFDAA520)),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(64.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(50.dp),
                        ambientColor = Color(0x4DDAA520),
                        spotColor = Color(0x4DDAA520)
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("💎", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "خرید اشتراک ویژه",
                        color = Color(0xFF4A3C00),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
            }"""

match = re.search(pattern, content, flags=re.DOTALL)
if match:
    new_content = content.replace(match.group(0), replacement)
    with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
        f.write(new_content)
    print("Main Menu Layout Replaced!")
else:
    print("Match not found.")

