import re

with open('app/src/main/java/com/example/ui/GameViewModel.kt', 'r') as f:
    content = f.read()

old_code = r'''        viewModelScope\.launch \{
            _soundEvents\.emit\(if \(isCorrect\) GameSoundEvent\.CorrectSound else GameSoundEvent\.WrongSound\)
            delay\(650\)
            loadDuelQuestion\(player\)
        \}'''

new_code = """        viewModelScope.launch {
            if (isCorrect) {
                if (mult > 1) {
                    _soundEvents.emit(GameSoundEvent.PlayDuelCombo)
                } else {
                    _soundEvents.emit(GameSoundEvent.PlayDuelCorrect)
                }
            } else {
                _soundEvents.emit(GameSoundEvent.PlayDuelWrong)
            }
            delay(650)
            loadDuelQuestion(player)
        }"""

if not re.search(old_code, content):
    print("Could not find old code in submitDuelAnswer")
else:
    content = re.sub(old_code, new_code, content)
    with open('app/src/main/java/com/example/ui/GameViewModel.kt', 'w') as f:
        f.write(content)
    print("Patched submitDuelAnswer")
