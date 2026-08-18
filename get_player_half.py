import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

pattern = r'''(        @Composable\n\s*fun PlayerHalf\(.*?)(?=\n\s*if \(!countdownFinished\))'''
match = re.search(pattern, content, flags=re.DOTALL)
if match:
    with open('player_half_original.txt', 'w') as f2:
        f2.write(match.group(1))
    print("Extracted to player_half_original.txt")
else:
    print("Not found")
