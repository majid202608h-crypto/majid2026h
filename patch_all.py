import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# In DuelSetupScreen, we want to allow clicking the avatar to change it and save it.
old_setup_vars = """    val duelPrefs by viewModel.duelPrefs.collectAsStateWithLifecycle()
    
    var name1 by remember(duelPrefs) { mutableStateOf(duelPrefs?.player1Name ?: "") }
    var name2 by remember(duelPrefs) { mutableStateOf(duelPrefs?.player2Name ?: "") }
    var selectedDuration by remember(duelPrefs) { mutableStateOf(duelPrefs?.matchDuration ?: 60) }"""

new_setup_vars = """    val duelPrefs by viewModel.duelPrefs.collectAsStateWithLifecycle()
    
    var name1 by remember(duelPrefs) { mutableStateOf(duelPrefs?.player1Name ?: "") }
    var name2 by remember(duelPrefs) { mutableStateOf(duelPrefs?.player2Name ?: "") }
    var avatar1 by remember(duelPrefs) { mutableStateOf(duelPrefs?.player1Avatar ?: "👦") }
    var avatar2 by remember(duelPrefs) { mutableStateOf(duelPrefs?.player2Avatar ?: "👧") }
    var selectedDuration by remember(duelPrefs) { mutableStateOf(duelPrefs?.matchDuration ?: 60) }
    
    val p1Avatars = listOf("👦", "👧", "🧒", "👱‍♂️", "👩", "👨", "👩‍🦱", "👨‍🦱")
    val p2Avatars = listOf("🐯", "🐻", "🐶", "🐱", "🐰", "🦊", "🐼", "🐨", "🐸", "🦁")
"""
content = content.replace(old_setup_vars, new_setup_vars)

old_avatar1 = """                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFF3B82F6), CircleShape)
                                    .border(2.dp, Color(0xFFBFDBFE), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👦", fontSize = 24.sp)"""
new_avatar1 = """                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFF3B82F6), CircleShape)
                                    .border(2.dp, Color(0xFFBFDBFE), CircleShape)
                                    .clickable {
                                        viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick)
                                        val next = p1Avatars[(p1Avatars.indexOf(avatar1) + 1) % p1Avatars.size]
                                        avatar1 = next
                                        viewModel.updateDuelPrefs(player1Avatar = next)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(avatar1, fontSize = 24.sp)"""
content = content.replace(old_avatar1, new_avatar1)

old_avatar2 = """                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFF10B981), CircleShape)
                                    .border(2.dp, Color(0xFFA7F3D0), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👧", fontSize = 24.sp)"""
new_avatar2 = """                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFF10B981), CircleShape)
                                    .border(2.dp, Color(0xFFA7F3D0), CircleShape)
                                    .clickable {
                                        viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick)
                                        val next = p2Avatars[(p2Avatars.indexOf(avatar2) + 1) % p2Avatars.size]
                                        avatar2 = next
                                        viewModel.updateDuelPrefs(player2Avatar = next)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(avatar2, fontSize = 24.sp)"""
content = content.replace(old_avatar2, new_avatar2)

# In DuelGameScreen, we need to load avatars from viewModel.duelPrefs
old_duel_avatars = """    // Shared Timer & Random Avatars
    val prefs = remember { context.getSharedPreferences("duel_prefs", android.content.Context.MODE_PRIVATE) }
    val avatar1 = remember { 
        prefs.getString("avatar1", null) ?: listOf("👦", "👧", "🧒", "👱‍♂️", "👩", "👨", "👩‍🦱", "👨‍🦱").random().also { prefs.edit().putString("avatar1", it).apply() }
    }
    val avatar2 = remember { 
        prefs.getString("avatar2", null) ?: listOf("🐯", "🐻", "🐶", "🐱", "🐰", "🦊", "🐼", "🐨").random().also { prefs.edit().putString("avatar2", it).apply() }
    }"""
new_duel_avatars = """    val duelPrefs by viewModel.duelPrefs.collectAsStateWithLifecycle()
    val avatar1 = duelPrefs?.player1Avatar ?: "👦"
    val avatar2 = duelPrefs?.player2Avatar ?: "👧"
"""
content = content.replace(old_duel_avatars, new_duel_avatars)


# In DuelResultScreen, we need to load avatars from viewModel.duelPrefs
old_result_avatar = """                        val avatar = if (result.winner == 1) {
                            androidx.compose.ui.platform.LocalContext.current.getSharedPreferences("duel_prefs", android.content.Context.MODE_PRIVATE).getString("avatar1", "👦") ?: "👦"
                        } else {
                            androidx.compose.ui.platform.LocalContext.current.getSharedPreferences("duel_prefs", android.content.Context.MODE_PRIVATE).getString("avatar2", "🐯") ?: "🐯"
                        }"""
new_result_avatar = """                        val duelPrefs by viewModel.duelPrefs.collectAsStateWithLifecycle()
                        val avatar = if (result.winner == 1) {
                            duelPrefs?.player1Avatar ?: "👦"
                        } else {
                            duelPrefs?.player2Avatar ?: "🐯"
                        }"""
content = content.replace(old_result_avatar, new_result_avatar)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
print("Patched all screens")
