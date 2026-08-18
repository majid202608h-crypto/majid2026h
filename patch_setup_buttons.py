import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

old_dur = r'''\.clickable \{ selectedDuration = dur \}'''
new_dur = """.clickable { 
                            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick)
                            selectedDuration = dur 
                        }"""
content = re.sub(old_dur, new_dur, content)

old_start = r'''        Button\(
            onClick = \{ viewModel\.startDuel\(name1, name2, selectedDuration\) \},
            modifier = Modifier'''
new_start = """        Button(
            onClick = { 
                viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick)
                viewModel.startDuel(name1, name2, selectedDuration) 
            },
            modifier = Modifier"""
content = re.sub(old_start, new_start, content)

old_back = r'''TextButton\(onClick = \{ viewModel\.navigateTo\(com\.example\.ui\.GameScreen\.MainMenu\) \}\) \{'''
new_back = """TextButton(onClick = { 
            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick)
            viewModel.navigateTo(com.example.ui.GameScreen.MainMenu) 
        }) {"""
content = re.sub(old_back, new_back, content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
print("Patched setup buttons")
