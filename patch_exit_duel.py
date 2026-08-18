import re

with open('app/src/main/java/com/example/ui/GameViewModel.kt', 'r') as f:
    content = f.read()

old_exit = r'''    fun goToDuelSetup\(\) \{
        duelTimerJob\?\.cancel\(\)
        _duelState\.value = DuelGameState\(\)
        _currentScreen\.value = GameScreen\.DuelSetup
    \}'''

new_exit = """    fun exitDuelMode() {
        duelTimerJob?.cancel()
        _duelState.value = DuelGameState()
        _currentScreen.value = GameScreen.MainMenu
    }
    
    fun goToDuelSetup() {
        duelTimerJob?.cancel()
        _duelState.value = DuelGameState()
        _currentScreen.value = GameScreen.DuelSetup
    }"""

content = re.sub(old_exit, new_exit, content)

with open('app/src/main/java/com/example/ui/GameViewModel.kt', 'w') as f:
    f.write(content)
print("Patched GameViewModel exitDuelMode")
