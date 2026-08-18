import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

pattern = r'''            // Stars / Sparkles\n            val starColor = Color\.White\n            \n            // Star 1\n            drawCircle\(\n                color = starColor\.copy\(alpha = starGlow1\),\n                radius = 4\.dp\.toPx\(\),\n                center = Offset\(width \* 0\.15f, height \* 0\.35f\)\n            \)\n            drawCircle\(\n                color = starColor\.copy\(alpha = starGlow1 \* 0\.4f\),\n                radius = 12\.dp\.toPx\(\),\n                center = Offset\(width \* 0\.15f, height \* 0\.35f\)\n            \)\n            \n            // Star 2\n            drawCircle\(\n                color = starColor\.copy\(alpha = starGlow2\),\n                radius = 3\.dp\.toPx\(\),\n                center = Offset\(width \* 0\.8f, height \* 0\.2f\)\n            \)\n            drawCircle\(\n                color = starColor\.copy\(alpha = starGlow2 \* 0\.4f\),\n                radius = 8\.dp\.toPx\(\),\n                center = Offset\(width \* 0\.8f, height \* 0\.2f\)\n            \)\n            \n            // Star 3\n            drawCircle\(\n                color = starColor\.copy\(alpha = starGlow1\),\n                radius = 5\.dp\.toPx\(\),\n                center = Offset\(width \* 0\.25f, height \* 0\.7f\)\n            \)\n            drawCircle\(\n                color = starColor\.copy\(alpha = starGlow1 \* 0\.4f\),\n                radius = 14\.dp\.toPx\(\),\n                center = Offset\(width \* 0\.25f, height \* 0\.7f\)\n            \)\n            \n            // Star 4\n            drawCircle\(\n                color = starColor\.copy\(alpha = starGlow2\),\n                radius = 3\.dp\.toPx\(\),\n                center = Offset\(width \* 0\.75f, height \* 0\.65f\)\n            \)'''

match = re.search(pattern, content, flags=re.DOTALL)
if match:
    new_content = content.replace(match.group(0), "")
    with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
        f.write(new_content)
    print("Stars replaced!")
else:
    print("Match not found.")
