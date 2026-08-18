import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

old_btn1 = r'''            Button\(
                onClick = \{ viewModel\.rematchDuel\(\) \},
                modifier = Modifier'''
new_btn1 = """            Button(
                onClick = { 
                    viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick)
                    viewModel.rematchDuel() 
                },
                modifier = Modifier"""
content = re.sub(old_btn1, new_btn1, content)

old_btn2 = r'''            Button\(
                onClick = \{ viewModel\.goToDuelSetup\(\) \},
                modifier = Modifier'''
new_btn2 = """            Button(
                onClick = { 
                    viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick)
                    viewModel.goToDuelSetup() 
                },
                modifier = Modifier"""
content = re.sub(old_btn2, new_btn2, content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
print("Patched Result buttons")
