import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# We want to replace from `fun NumericKeypad` all the way to the end of `fun DuelGameScreen`
pattern = r'''(@Composable\s+fun NumericKeypad\(.*?\n\s*\}.*?)(?=@Composable\s+fun DuelResultScreen)'''

match = re.search(pattern, content, flags=re.DOTALL)
if match:
    print("Found match!")
    # print(match.group(1)[:100])
else:
    print("No match")
