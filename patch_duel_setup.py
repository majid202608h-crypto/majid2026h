import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# I will extract the whole DuelSetupScreen and replace it
# First, let's locate the exact start and end of DuelSetupScreen
