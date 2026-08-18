import re

with open('app/src/main/java/com/example/ui/GameViewModel.kt', 'r') as f:
    content = f.read()

new_prefs_state = """    val duelPrefs: StateFlow<com.example.data.DuelPrefsEntity?> = repository.duelPrefs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateDuelPrefs(player1Name: String? = null, player2Name: String? = null, player1Avatar: String? = null, player2Avatar: String? = null, matchDuration: Int? = null) {
        viewModelScope.launch {
            repository.updateDuelPrefsAtomic { prefs ->
                prefs.copy(
                    player1Name = player1Name ?: prefs.player1Name,
                    player2Name = player2Name ?: prefs.player2Name,
                    player1Avatar = player1Avatar ?: prefs.player1Avatar,
                    player2Avatar = player2Avatar ?: prefs.player2Avatar,
                    matchDuration = matchDuration ?: prefs.matchDuration
                )
            }
        }
    }
"""

content = content.replace('    val tableStats: StateFlow<List<TableStatEntity>> = repository.tableStats\n        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())', '    val tableStats: StateFlow<List<TableStatEntity>> = repository.tableStats\n        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())\n\n' + new_prefs_state)

with open('app/src/main/java/com/example/ui/GameViewModel.kt', 'w') as f:
    f.write(content)
print("Patched GameViewModel.kt")
