import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

pattern = r'''        Text\(
            text = label,
            modifier = Modifier\.weight\(1\.2f\),
            textAlign = TextAlign\.Center,
            fontSize = 20\.sp,
            fontWeight = FontWeight\.SemiBold,
            color = Color\.White
        \)'''

replacement = """        Text(
            text = label,
            modifier = Modifier.weight(1.2f),
            textAlign = TextAlign.Center,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Visible
        )"""

new_content = re.sub(pattern, replacement, content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(new_content)
print("Typography updated")
