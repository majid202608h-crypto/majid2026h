import re

with open('app/src/main/java/com/example/ui/GameAudioManager.kt', 'r') as f:
    content = f.read()

# Add to isBlockedEvent
blocked_events = """            GameSoundEvent.PlayWheelFinish -> true
            GameSoundEvent.PlayDuelCorrect -> true
            GameSoundEvent.PlayDuelWrong -> true
            GameSoundEvent.PlayDuelCombo -> true
            GameSoundEvent.PlayDuelTick -> true
            GameSoundEvent.PlayDuelStart -> true
            GameSoundEvent.PlayDuelFinish -> true
            GameSoundEvent.PlayDuelVictory -> true
            GameSoundEvent.PlayDuelDraw -> true
            GameSoundEvent.PlayDuelClick -> true"""
content = content.replace("            GameSoundEvent.PlayWheelFinish -> true", blocked_events)

# Add to handleSoundEvent (World map / Duel mode fallback part)
handlers = """            GameSoundEvent.PlayWheelFinish -> {
                playRandomResource(context, "wheel_finish", 1) {
                    try {
                        toneGen?.startTone(ToneGenerator.TONE_CDMA_PIP, 100)
                    } catch (e: Exception) {
                    }
                }
            }
            GameSoundEvent.PlayDuelCorrect -> {
                try { toneGen?.startTone(ToneGenerator.TONE_CDMA_PIP, 100) } catch (e: Exception) {}
            }
            GameSoundEvent.PlayDuelWrong -> {
                try { toneGen?.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 150) } catch (e: Exception) {}
            }
            GameSoundEvent.PlayDuelCombo -> {
                try { toneGen?.startTone(ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE, 200) } catch (e: Exception) {}
            }
            GameSoundEvent.PlayDuelTick -> {
                try { toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 100) } catch (e: Exception) {}
            }
            GameSoundEvent.PlayDuelStart -> {
                try { toneGen?.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 300) } catch (e: Exception) {}
            }
            GameSoundEvent.PlayDuelFinish -> {
                try { toneGen?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400) } catch (e: Exception) {}
            }
            GameSoundEvent.PlayDuelVictory -> {
                playRandomResource(context, "boss_win", 10) {
                    try { toneGen?.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 400) } catch (e: Exception) {}
                }
            }
            GameSoundEvent.PlayDuelDraw -> {
                try { toneGen?.startTone(ToneGenerator.TONE_PROP_NACK, 300) } catch (e: Exception) {}
            }
            GameSoundEvent.PlayDuelClick -> {
                try { toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 50) } catch (e: Exception) {}
            }"""
            
pattern = r'''            GameSoundEvent\.PlayWheelFinish -> \{
                playRandomResource\(context, "wheel_finish", 1\) \{
                    try \{
                        toneGen\?\.startTone\(ToneGenerator\.TONE_CDMA_PIP, 100\)
                    \} catch \(e: Exception\) \{
                    \}
                \}
            \}'''
content = re.sub(pattern, handlers, content)

with open('app/src/main/java/com/example/ui/GameAudioManager.kt', 'w') as f:
    f.write(content)
print("Patched GameAudioManager")
