import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# 1. Row 1
content = content.replace(
    '// Row 1\n                Row(\n                    modifier = Modifier.fillMaxWidth(),',
    '// Row 1\n                Row(\n                    modifier = Modifier.fillMaxWidth().offset(y = 5.dp),'
)

# 2. Row 2
content = content.replace(
    '// Row 2\n                Row(\n                    modifier = Modifier.fillMaxWidth(),',
    '// Row 2\n                Row(\n                    modifier = Modifier.fillMaxWidth().offset(y = (-5).dp),'
)

# 3. Row 3
content = content.replace(
    'modifier = Modifier\n                        .fillMaxWidth()\n                        .height(96.dp)\n                        .shadow(',
    'modifier = Modifier\n                        .fillMaxWidth()\n                        .height(96.dp)\n                        .offset(y = (-5).dp)\n                        .shadow('
)

# 4. Subscription height
content = content.replace(
    'modifier = Modifier\n                    .fillMaxWidth(0.85f)\n                    .height(54.dp)\n                    .shadow(',
    'modifier = Modifier\n                    .fillMaxWidth(0.85f)\n                    .height(59.dp)\n                    .shadow('
)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

