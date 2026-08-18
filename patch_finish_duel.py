import re

with open('app/src/main/java/com/example/ui/GameViewModel.kt', 'r') as f:
    content = f.read()

old_finish = r'''        viewModelScope\.launch \{
            if \(\(result\.score1 \+ result\.score2\) / 10 > 0\)
                repository\.recordCoinsEarned\(\(result\.score1 \+ result\.score2\) / 20\)
            _soundEvents\.emit\(GameSoundEvent\.StarSound\)
            _currentScreen\.value = GameScreen\.DuelResult
        \}'''

new_finish = """        viewModelScope.launch {
            if ((result.score1 + result.score2) / 10 > 0)
                repository.recordCoinsEarned((result.score1 + result.score2) / 20)
            _soundEvents.emit(GameSoundEvent.PlayDuelFinish)
            _currentScreen.value = GameScreen.DuelResult
        }"""

if not re.search(old_finish, content):
    print("Could not find finishDuel part")
else:
    content = re.sub(old_finish, new_finish, content)
    with open('app/src/main/java/com/example/ui/GameViewModel.kt', 'w') as f:
        f.write(content)
    print("Patched finishDuel")
