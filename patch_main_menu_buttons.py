import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Replace goToDuelSetup with exitDuelMode where the text is Main Menu
pattern = r'''                    viewModel\.goToDuelSetup\(\) 
                \},
                modifier = Modifier
                    \.fillMaxWidth\(\)
                    \.height\(56\.dp\)
                    \.padding\(bottom = 12\.dp\),
                shape = RoundedCornerShape\(28\.dp\),
                colors = ButtonDefaults\.buttonColors\(containerColor = Color\.White\.copy\(alpha = 0\.15f\)\),
                border = BorderStroke\(1\.dp, Color\.White\.copy\(alpha = 0\.3f\)\)
            \) \{
                Text\("بازگشت به منوی اصلی 🏠"'''

replacement = """                    viewModel.exitDuelMode() 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
            ) {
                Text("بازگشت به منوی اصلی 🏠\""""

content = re.sub(pattern, replacement, content)

pattern2 = r'''                        onClick = \{ viewModel\.goToDuelSetup\(\) \},
                        modifier = Modifier
                            \.fillMaxWidth\(\)
                            \.height\(56\.dp\),
                        shape = RoundedCornerShape\(28\.dp\),
                        colors = ButtonDefaults\.buttonColors\(
                            containerColor = Color\.White\.copy\(alpha = 0\.15f\)
                        \),
                        border = BorderStroke\(1\.dp, Color\.White\.copy\(alpha = 0\.3f\)\)
                    \) \{
                        Text\(
                            text = "بازگشت به منوی اصلی 🏠"'''

replacement2 = """                        onClick = { viewModel.exitDuelMode() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.15f)
                        ),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "بازگشت به منوی اصلی 🏠\""""
                            
content = re.sub(pattern2, replacement2, content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
print("Patched Main Menu buttons")
