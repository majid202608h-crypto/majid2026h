import re
with open('app/src/main/java/com/example/ui/GameAudioManager.kt', 'r') as f:
    content = f.read()

old_click = r'''            GameSoundEvent\.PlayDuelClick -> \{
                try \{ toneGen\?\.startTone\(ToneGenerator\.TONE_PROP_BEEP, 50\) \} catch \(e: Exception\) \{\}
            \}'''
new_click = """            GameSoundEvent.PlayDuelClick -> {
                try { toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 50) } catch (e: Exception) {}
            }
            else -> {}"""
content = re.sub(old_click, new_click, content)
with open('app/src/main/java/com/example/ui/GameAudioManager.kt', 'w') as f:
    f.write(content)
print("Patched exhaustive 2")
