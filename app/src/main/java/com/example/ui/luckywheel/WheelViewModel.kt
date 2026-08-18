package com.example.ui.luckywheel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.GameRepository
import com.example.ui.GameAudioManager
import com.example.ui.GameSoundEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WheelViewModel(
    context: Context,
    private val gameRepository: GameRepository,
    private val rewardRepository: RewardRepository,
    private val rewardEngine: RewardEngine
) : ViewModel() {

    private val appContext: Context = context.applicationContext

    private val _uiState = MutableStateFlow(WheelState())
    val uiState: StateFlow<WheelState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    init {
        refreshState()
        startCountdownTimer()
    }

    fun refreshState() {
        viewModelScope.launch {
            val profile = gameRepository.getOrCreateProfile()
            val licensePrefs = appContext.getSharedPreferences("game_license_pref", Context.MODE_PRIVATE)
            val expiryTime = licensePrefs.getLong("license_expiry_time", 0L)
            val remainingMs = (expiryTime - System.currentTimeMillis()).coerceAtLeast(0L)
            val subDays = (remainingMs / (1000L * 60 * 60 * 24)).toInt()

            val allowed = rewardRepository.isSpinAllowed()
            val countdown = rewardRepository.getCountdownToNextSpin()
            val lastReward = rewardRepository.getLastRewardText()
            val spins = rewardRepository.getSpinCount()

            _uiState.update {
                it.copy(
                    coins = profile.coins,
                    remainingSubDays = subDays,
                    isSpinAllowed = allowed,
                    countdownMs = countdown,
                    lastRewardText = lastReward,
                    spinCount = spins
                )
            }
        }
    }

    private fun startCountdownTimer() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val countdown = rewardRepository.getCountdownToNextSpin()
                val allowed = rewardRepository.isSpinAllowed()
                _uiState.update {
                    it.copy(
                        countdownMs = countdown,
                        isSpinAllowed = allowed
                    )
                }
            }
        }
    }

    fun spinWheel(onSpinStart: (Int, Float) -> Unit) {
        if (_uiState.value.isSpinning || !_uiState.value.isSpinAllowed) return

        viewModelScope.launch {
            val remainingSubDays = _uiState.value.remainingSubDays
            val selectedIndex = rewardEngine.selectRandomRewardIndex(remainingSubDays)
            val sectors = rewardEngine.getSectors(remainingSubDays)
            val reward = sectors[selectedIndex]

            // Calculate rotation degrees: 5 full turns (1800 deg) plus the sector offset.
            // In Canvas, sector 0 is at 0 degrees, which is on the right side.
            // But we have a fixed pin at the top (which is 270 degrees).
            // To make sector stop at the top pin:
            // Angle of sector center = index * 45 + 22.5
            // To make that stop at the top (270 degrees):
            // We need to rotate the wheel by: 270 - angle of sector center = 270 - (index * 45 + 22.5)
            // Let's add 5 full turns (1800 degrees) to create a nice long spin.
            val sectorAngle = (selectedIndex * 45f) + 22.5f
            val targetDegrees = 1800f + (270f - sectorAngle)

            _uiState.update {
                it.copy(
                    isSpinning = true,
                    selectedIndex = selectedIndex,
                    targetDegrees = targetDegrees,
                    currentReward = reward
                )
            }

            // Trigger wheel start and spin audio
            GameAudioManager.getInstance(appContext).handleSoundEvent(appContext, GameSoundEvent.PlayWheelStart)
            launch {
                delay(400)
                GameAudioManager.getInstance(appContext).handleSoundEvent(appContext, GameSoundEvent.PlayWheelSpin)
            }

            onSpinStart(selectedIndex, targetDegrees)
        }
    }

    fun onSpinAnimationFinished() {
        val reward = _uiState.value.currentReward ?: return
        viewModelScope.launch {
            rewardRepository.applyReward(reward)
            rewardRepository.recordSpin(reward.getDisplayNameFarsi())
            refreshState()
            _uiState.update {
                it.copy(
                    isSpinning = false,
                    lastRewardText = reward.getDisplayNameFarsi()
                )
            }
            // Trigger wheel win / finish audio
            GameAudioManager.getInstance(appContext).handleSoundEvent(appContext, GameSoundEvent.PlayWheelWin)
        }
    }

    fun clearCurrentReward() {
        _uiState.update {
            it.copy(currentReward = null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}

class WheelViewModelFactory(
    private val context: Context,
    private val gameRepository: GameRepository,
    private val rewardRepository: RewardRepository,
    private val rewardEngine: RewardEngine
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WheelViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WheelViewModel(context, gameRepository, rewardRepository, rewardEngine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
