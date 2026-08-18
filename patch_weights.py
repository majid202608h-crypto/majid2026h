import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Replace weight(0.35f) -> weight(0.3f) for Question
content = content.replace('.weight(0.35f)', '.weight(0.3f)')
# Replace weight(0.65f) -> weight(0.7f) for Keypad
content = content.replace('.weight(0.65f)', '.weight(0.7f)')

# Optional: reduce Spacer sizes in PlayerHalf if they are still 16
# Actually wait, let's just do weights first and see.

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

print("Weights patched")
