import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

old_res = r'''    LaunchedEffect\(Unit\) \{
        visible = true
        viewModel\.triggerSound\(com\.example\.ui\.GameSoundEvent\.PlayFinishRand\)
    \}'''

new_res = """    LaunchedEffect(Unit) {
        visible = true
        if (result.winner == null) {
            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelDraw)
        } else {
            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelVictory)
        }
    }"""

if not re.search(old_res, content):
    print("Could not find DuelResultScreen LaunchedEffect")
else:
    content = re.sub(old_res, new_res, content)
    with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
        f.write(content)
    print("Patched DuelResultScreen sound")
