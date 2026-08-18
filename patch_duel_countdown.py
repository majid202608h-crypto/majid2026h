import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

old_cd = r'''    var countdownFinished by remember \{ mutableStateOf\(false\) \}
    var countdownStep by remember \{ mutableStateOf<String\?>\("۳"\) \}
    val haptic = androidx\.compose\.ui\.platform\.LocalHapticFeedback\.current

    LaunchedEffect\(Unit\) \{
        viewModel\.triggerSound\(com\.example\.ui\.GameSoundEvent\.PlayDuelTick\)
        haptic\.performHapticFeedback\(androidx\.compose\.ui\.hapticfeedback\.HapticFeedbackType\.TextHandleMove\)
        delay\(1000\)
        
        countdownStep = "۲"
        viewModel\.triggerSound\(com\.example\.ui\.GameSoundEvent\.PlayDuelTick\)
        haptic\.performHapticFeedback\(androidx\.compose\.ui\.hapticfeedback\.HapticFeedbackType\.TextHandleMove\)
        delay\(1000\)
        
        countdownStep = "۱"
        viewModel\.triggerSound\(com\.example\.ui\.GameSoundEvent\.PlayDuelTick\)
        haptic\.performHapticFeedback\(androidx\.compose\.ui\.hapticfeedback\.HapticFeedbackType\.TextHandleMove\)
        delay\(1000\)
        
        countdownStep = "🚀 شروع!"
        viewModel\.triggerSound\(com\.example\.ui\.GameSoundEvent\.PlayDuelStart\)
        haptic\.performHapticFeedback\(androidx\.compose\.ui\.hapticfeedback\.HapticFeedbackType\.LongPress\)
        delay\(1000\)
        
        countdownStep = null
        countdownFinished = true
    \}'''

new_cd = """    var countdownFinished by remember(state.phase) { mutableStateOf(state.phase != com.example.ui.DuelPhase.Playing) }
    var countdownStep by remember(state.phase) { mutableStateOf<String?>(if (state.phase == com.example.ui.DuelPhase.Playing) "۳" else null) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    LaunchedEffect(state.phase) {
        if (state.phase == com.example.ui.DuelPhase.Playing && !countdownFinished) {
            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelTick)
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
            delay(1000)
            
            countdownStep = "۲"
            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelTick)
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
            delay(1000)
            
            countdownStep = "۱"
            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelTick)
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
            delay(1000)
            
            countdownStep = "🚀 شروع!"
            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelStart)
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            delay(1000)
            
            countdownStep = null
            countdownFinished = true
        }
    }"""

content = re.sub(old_cd, new_cd, content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
print("Patched duel countdown")
