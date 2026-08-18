import re

with open('app/src/main/java/com/example/ui/GameViewModel.kt', 'r') as f:
    content = f.read()

new_events = """    object PlayWheelFinish : GameSoundEvent()
    
    // Duel Sounds
    object PlayDuelCorrect : GameSoundEvent()
    object PlayDuelWrong : GameSoundEvent()
    object PlayDuelCombo : GameSoundEvent()
    object PlayDuelTick : GameSoundEvent()
    object PlayDuelStart : GameSoundEvent()
    object PlayDuelFinish : GameSoundEvent()
    object PlayDuelVictory : GameSoundEvent()
    object PlayDuelDraw : GameSoundEvent()
    object PlayDuelClick : GameSoundEvent()
"""
if "PlayDuelCorrect" not in content:
    content = content.replace("    object PlayWheelFinish : GameSoundEvent()", new_events)
    with open('app/src/main/java/com/example/ui/GameViewModel.kt', 'w') as f:
        f.write(content)
    print("Patched GameSoundEvent")
else:
    print("Already patched GameSoundEvent")
