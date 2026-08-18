import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Replace modifier in NumericKeypad Card
content = content.replace('modifier = modifier.fillMaxWidth(),', 'modifier = modifier.fillMaxSize(),')
# Replace modifier in NumericKeypad Column
content = content.replace('.fillMaxWidth()\n                .padding(8.dp),', '.fillMaxSize()\n                .padding(8.dp),')

# Also in PlayerHalf we should pass modifier = Modifier.fillMaxSize() to NumericKeypad
pattern = r'''                            NumericKeypad\(\n                                currentValue = input,'''
replacement = '''                            NumericKeypad(
                                modifier = Modifier.fillMaxSize(),
                                currentValue = input,'''
content = re.sub(pattern, replacement, content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

print("Keypad sizing patched")
