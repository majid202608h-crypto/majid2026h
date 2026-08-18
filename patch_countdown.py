import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

old_cd = r'''    LaunchedEffect\(Unit\) \{
        viewModel\.triggerSound\(com\.example\.ui\.GameSoundEvent\.PlayTone\(600, 100\)\)
        haptic\.performHapticFeedback\(androidx\.compose\.ui\.hapticfeedback\.HapticFeedbackType\.TextHandleMove\)
        delay\(1000\)
        
        countdownStep = "۲"
        viewModel\.triggerSound\(com\.example\.ui\.GameSoundEvent\.PlayTone\(600, 100\)\)
        haptic\.performHapticFeedback\(androidx\.compose\.ui\.hapticfeedback\.HapticFeedbackType\.TextHandleMove\)
        delay\(1000\)
        
        countdownStep = "۱"
        viewModel\.triggerSound\(com\.example\.ui\.GameSoundEvent\.PlayTone\(600, 100\)\)
        haptic\.performHapticFeedback\(androidx\.compose\.ui\.hapticfeedback\.HapticFeedbackType\.TextHandleMove\)
        delay\(1000\)
        
        countdownStep = "🚀 شروع!"
        viewModel\.triggerSound\(com\.example\.ui\.GameSoundEvent\.PlayTone\(1200, 300\)\)
        haptic\.performHapticFeedback\(androidx\.compose\.ui\.hapticfeedback\.HapticFeedbackType\.LongPress\)
        delay\(1000\)
        
        countdownStep = null
        countdownFinished = true
    \}'''

new_cd = """    LaunchedEffect(Unit) {
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
    }"""

if not re.search(old_cd, content):
    print("Could not find countdown code")
else:
    content = re.sub(old_cd, new_cd, content)
    with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
        f.write(content)
    print("Patched countdown sounds")
