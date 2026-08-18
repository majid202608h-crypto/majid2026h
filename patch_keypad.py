import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

pattern = r'''                            if \(isDelete\) \{\n                                Text\(\n                                    text = "⌫", \n                                    fontSize = 24\.sp, \n                                    color = contentColor,\n                                    textAlign = androidx\.compose\.ui\.text\.style\.TextAlign\.Center,\n                                    style = androidx\.compose\.ui\.text\.TextStyle\(\n                                        platformStyle = androidx\.compose\.ui\.text\.PlatformTextStyle\(\n                                            includeFontPadding = false\n                                        \)\n                                    \),\n                                    modifier = Modifier\.wrapContentSize\(Alignment\.Center\)\.offset\(y = \(-2\)\.dp\)\n                                \)\n                            \} else if \(isSubmit\) \{\n                                Text\(\n                                    text = "✓", \n                                    fontSize = 26\.sp, \n                                    color = contentColor, \n                                    fontWeight = FontWeight\.Black,\n                                    textAlign = androidx\.compose\.ui\.text\.style\.TextAlign\.Center,\n                                    style = androidx\.compose\.ui\.text\.TextStyle\(\n                                        platformStyle = androidx\.compose\.ui\.text\.PlatformTextStyle\(\n                                            includeFontPadding = false\n                                        \)\n                                    \),\n                                    modifier = Modifier\.wrapContentSize\(Alignment\.Center\)\.offset\(y = \(-2\)\.dp\)\n                                \)\n                            \} else \{\n                                Text\(\n                                    text = key\.toFa\(\),\n                                    fontSize = 32\.sp,\n                                    color = contentColor,\n                                    fontWeight = FontWeight\.Black,\n                                    textAlign = androidx\.compose\.ui\.text\.style\.TextAlign\.Center,\n                                    style = androidx\.compose\.ui\.text\.TextStyle\(\n                                        platformStyle = androidx\.compose\.ui\.text\.PlatformTextStyle\(\n                                            includeFontPadding = false\n                                        \)\n                                    \),\n                                    modifier = Modifier\.wrapContentSize\(Alignment\.Center\)\.offset\(y = \(-2\)\.dp\)\n                                \)\n                            \}'''

replacement = """                            if (isDelete) {
                                Text(
                                    text = "⌫", 
                                    fontSize = 26.sp, 
                                    color = contentColor,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    style = androidx.compose.ui.text.TextStyle(
                                        platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                                            includeFontPadding = false
                                        ),
                                        lineHeightStyle = androidx.compose.ui.text.style.LineHeightStyle(
                                            alignment = androidx.compose.ui.text.style.LineHeightStyle.Alignment.Center,
                                            trim = androidx.compose.ui.text.style.LineHeightStyle.Trim.Both
                                        )
                                    ),
                                    modifier = Modifier.wrapContentSize(Alignment.Center).offset(y = (-3).dp)
                                )
                            } else if (isSubmit) {
                                Text(
                                    text = "✓", 
                                    fontSize = 28.sp, 
                                    color = contentColor, 
                                    fontWeight = FontWeight.Black,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    style = androidx.compose.ui.text.TextStyle(
                                        platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                                            includeFontPadding = false
                                        ),
                                        lineHeightStyle = androidx.compose.ui.text.style.LineHeightStyle(
                                            alignment = androidx.compose.ui.text.style.LineHeightStyle.Alignment.Center,
                                            trim = androidx.compose.ui.text.style.LineHeightStyle.Trim.Both
                                        )
                                    ),
                                    modifier = Modifier.wrapContentSize(Alignment.Center).offset(y = (-3).dp)
                                )
                            } else {
                                Text(
                                    text = key.toFa(),
                                    fontSize = 34.sp,
                                    color = contentColor,
                                    fontWeight = FontWeight.Black,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    style = androidx.compose.ui.text.TextStyle(
                                        platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                                            includeFontPadding = false
                                        ),
                                        lineHeightStyle = androidx.compose.ui.text.style.LineHeightStyle(
                                            alignment = androidx.compose.ui.text.style.LineHeightStyle.Alignment.Center,
                                            trim = androidx.compose.ui.text.style.LineHeightStyle.Trim.Both
                                        )
                                    ),
                                    modifier = Modifier.wrapContentSize(Alignment.Center).offset(y = (-5).dp)
                                )
                            }"""

match = re.search(pattern, content, flags=re.DOTALL)
if match:
    new_content = content.replace(match.group(0), replacement)
    with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
        f.write(new_content)
    print("Keypad text replaced!")
else:
    print("Match not found.")
