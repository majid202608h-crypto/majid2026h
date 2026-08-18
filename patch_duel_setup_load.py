import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

old_vars = """    var name1 by remember { mutableStateOf("") }
    var name2 by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableStateOf(60) }"""

new_vars = """    val duelPrefs by viewModel.duelPrefs.collectAsStateWithLifecycle()
    
    var name1 by remember(duelPrefs) { mutableStateOf(duelPrefs?.player1Name ?: "") }
    var name2 by remember(duelPrefs) { mutableStateOf(duelPrefs?.player2Name ?: "") }
    var selectedDuration by remember(duelPrefs) { mutableStateOf(duelPrefs?.matchDuration ?: 60) }"""

content = content.replace(old_vars, new_vars)

old_text_field1 = """                                onValueChange = { if (it.length <= 18) name1 = it },"""
new_text_field1 = """                                onValueChange = { if (it.length <= 18) { name1 = it; viewModel.updateDuelPrefs(player1Name = it) } },"""
content = content.replace(old_text_field1, new_text_field1)

old_text_field2 = """                                onValueChange = { if (it.length <= 18) name2 = it },"""
new_text_field2 = """                                onValueChange = { if (it.length <= 18) { name2 = it; viewModel.updateDuelPrefs(player2Name = it) } },"""
content = content.replace(old_text_field2, new_text_field2)

old_duration = """                                            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick)
                                            selectedDuration = dur
                                        }"""
new_duration = """                                            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick)
                                            selectedDuration = dur
                                            viewModel.updateDuelPrefs(matchDuration = dur)
                                        }"""
content = content.replace(old_duration, new_duration)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
print("Patched DuelSetupScreen to save/load prefs")
