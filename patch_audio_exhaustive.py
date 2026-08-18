import re

with open('app/src/main/java/com/example/ui/GameAudioManager.kt', 'r') as f:
    content = f.read()

old_code = r'''            GameSoundEvent\.PlayDuelClick -> \{
                try \{ toneGen\?\.startTone\(ToneGenerator\.TONE_PROP_BEEP, 50\) \} catch \(e: Exception\) \{\}
            \}'''

new_code = """            GameSoundEvent.PlayDuelClick -> {
                try { toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 50) } catch (e: Exception) {}
            }
            else -> {
                // Ignore missing handlers
            }"""

content = re.sub(old_code, new_code, content)

with open('app/src/main/java/com/example/ui/GameAudioManager.kt', 'w') as f:
    f.write(content)
print("Patched exhaustive when")
