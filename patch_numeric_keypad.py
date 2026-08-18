import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

old_keypad_sig = r'''fun NumericKeypad\(
    currentValue: String,
    onNumberPressed: \(Int\) -> Unit,
    onDelete: \(\) -> Unit,
    onSubmit: \(\) -> Unit,
    enabled: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
\) \{'''

new_keypad_sig = """fun NumericKeypad(
    currentValue: String,
    onNumberPressed: (Int) -> Unit,
    onDelete: () -> Unit,
    onSubmit: () -> Unit,
    enabled: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onPlayClickSound: () -> Unit = {}
) {"""

content = re.sub(old_keypad_sig, new_keypad_sig, content)

old_clickable = r'''                            \.clickable\(
                                interactionSource = interactionSource,
                                indication = androidx\.compose\.foundation\.LocalIndication\.current,
                                enabled = enabled
                            \) \{
                                haptic\.performHapticFeedback\(HapticFeedbackType\.TextHandleMove\)
                                if \(isDelete\) onDelete\(\)
                                else if \(isSubmit\) onSubmit\(\)
                                else onNumberPressed\(key\)
                            \},'''

new_clickable = """                            .clickable(
                                interactionSource = interactionSource,
                                indication = androidx.compose.foundation.LocalIndication.current,
                                enabled = enabled
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onPlayClickSound()
                                if (isDelete) onDelete()
                                else if (isSubmit) onSubmit()
                                else onNumberPressed(key)
                            },"""

content = re.sub(old_clickable, new_clickable, content)

old_keypad_usage = r'''                        if \(showGameplayElements\) \{
                            NumericKeypad\(
                                currentValue = input,
                                onNumberPressed = \{ num -> if \(input\.length < 3\) input \+= num\.toString\(\) \},
                                onDelete = \{ if \(input\.isNotEmpty\(\)\) input = input\.dropLast\(1\) \},
                                onSubmit = \{ 
                                    if \(!player\.answered\) \{
                                        input\.toIntOrNull\(\)\?\.let \{ 
                                            viewModel\.submitDuelAnswer\(playerNum, it\)
                                        \} 
                                    \}
                                \},
                                enabled = !player\.answered,
                                accentColor = accentColor,
                                modifier = Modifier\.fillMaxSize\(\)
                            \)
                        \}'''

new_keypad_usage = """                        if (showGameplayElements) {
                            NumericKeypad(
                                currentValue = input,
                                onNumberPressed = { num -> if (input.length < 3) input += num.toString() },
                                onDelete = { if (input.isNotEmpty()) input = input.dropLast(1) },
                                onSubmit = { 
                                    if (!player.answered) {
                                        input.toIntOrNull()?.let { 
                                            viewModel.submitDuelAnswer(playerNum, it)
                                        } 
                                    }
                                },
                                enabled = !player.answered,
                                accentColor = accentColor,
                                modifier = Modifier.fillMaxSize(),
                                onPlayClickSound = { viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick) }
                            )
                        }"""

content = re.sub(old_keypad_usage, new_keypad_usage, content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
print("Patched NumericKeypad")
