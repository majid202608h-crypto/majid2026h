import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Replace Spacer heights inside PlayerHalf only. 
# We'll just replace Spacer(Modifier.height(16.dp)) with Spacer(Modifier.height(8.dp)) in the whole file
# Actually it's safer to just replace it generally if it doesn't break anything, but let's be careful.
# Let's replace only occurrences between 6400 and 6650.
lines = content.split('\n')
for i in range(6350, 6650):
    if i < len(lines):
        if 'Spacer(Modifier.height(16.dp))' in lines[i]:
            lines[i] = lines[i].replace('Spacer(Modifier.height(16.dp))', 'Spacer(Modifier.height(8.dp))')

content = '\n'.join(lines)
with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

print("Spacers patched")
