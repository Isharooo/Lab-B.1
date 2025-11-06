package mobappdev.example.nback_cimpl.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.GameApplication
import mobappdev.example.nback_cimpl.NBackHelper
import mobappdev.example.nback_cimpl.data.UserPreferencesRepository

interface GameViewModel {
    val gameState: StateFlow<GameState>
    val score: StateFlow<Int>
    val highscore: StateFlow<Int>
    val nBack: Int
    val nrOfEvents: Int
    val eventIntervalMs: Long

    fun setGameType(gameType: GameType)
    fun startGame()
    fun checkMatch()
}

class GameVM(
    private val userPreferencesRepository: UserPreferencesRepository
): GameViewModel, ViewModel() {
    private val _gameState = MutableStateFlow(GameState())
    override val gameState: StateFlow<GameState>
        get() = _gameState.asStateFlow()

    private val _score = MutableStateFlow(0)
    override val score: StateFlow<Int>
        get() = _score

    private val _highscore = MutableStateFlow(0)
    override val highscore: StateFlow<Int>
        get() = _highscore

    override val nBack: Int = 2
    override val nrOfEvents: Int = 10
    override val eventIntervalMs: Long = 2000L

    private var job: Job? = null
    private val nBackHelper = NBackHelper()
    private var events = emptyArray<Int>()
    private var correctResponses = 0

    override fun setGameType(gameType: GameType) {
        _gameState.value = _gameState.value.copy(gameType = gameType)
    }

    override fun startGame() {
        job?.cancel()
        _score.value = 0
        correctResponses = 0

        events = nBackHelper.generateNBackString(nrOfEvents, 9, 30, nBack).toList().toTypedArray()
        Log.d("GameVM", "The following sequence was generated: ${events.contentToString()}")

        job = viewModelScope.launch {
            when (gameState.value.gameType) {
                GameType.Audio -> runAudioGame()
                GameType.AudioVisual -> runAudioVisualGame()
                GameType.Visual -> runVisualGame(events)
            }
        }
    }

    override fun checkMatch() {
        val currentIndex = _gameState.value.eventIndex

        if (currentIndex >= nBack) {
            val currentValue = events[currentIndex]
            val nBackValue = events[currentIndex - nBack]

            if (currentValue == nBackValue) {
                // Korrekt!
                _score.value = _score.value + 1
                correctResponses++
                _gameState.value = _gameState.value.copy(
                    feedback = FeedbackType.CORRECT,
                    correctCount = correctResponses
                )
            } else {
                // Fel!
                _score.value = maxOf(0, _score.value - 1)
                _gameState.value = _gameState.value.copy(
                    feedback = FeedbackType.INCORRECT
                )
            }

            // Återställ feedback efter kort tid
            viewModelScope.launch {
                delay(300)
                _gameState.value = _gameState.value.copy(feedback = FeedbackType.NONE)
            }
        }
    }

    private fun runAudioGame() {
        // TODO: Audio implementation
    }

    private suspend fun runVisualGame(events: Array<Int>){
        _gameState.value = _gameState.value.copy(
            eventValue = -1,
            eventIndex = 0,
            isRunning = true
        )
        delay(1000)

        for ((index, value) in events.withIndex()) {
            _gameState.value = _gameState.value.copy(
                eventValue = value,
                eventIndex = index
            )
            delay(eventIntervalMs)
        }

        _gameState.value = _gameState.value.copy(
            eventValue = -1,
            isRunning = false
        )

        if (_score.value > _highscore.value) {
            viewModelScope.launch {
                userPreferencesRepository.saveHighScore(_score.value)
            }
        }
    }

    private fun runAudioVisualGame(){
        // TODO: Dual n-back implementation
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GameApplication)
                GameVM(application.userPreferencesRespository)
            }
        }
    }

    init {
        viewModelScope.launch {
            userPreferencesRepository.highscore.collect {
                _highscore.value = it
            }
        }
    }
}

enum class GameType{
    Audio,
    Visual,
    AudioVisual
}

enum class FeedbackType {
    NONE,
    CORRECT,
    INCORRECT
}

data class GameState(
    val gameType: GameType = GameType.Visual,
    val eventValue: Int = -1,
    val eventIndex: Int = 0,
    val isRunning: Boolean = false,
    val feedback: FeedbackType = FeedbackType.NONE,
    val correctCount: Int = 0
)

class FakeVM: GameViewModel{
    override val gameState: StateFlow<GameState>
        get() = MutableStateFlow(GameState()).asStateFlow()
    override val score: StateFlow<Int>
        get() = MutableStateFlow(2).asStateFlow()
    override val highscore: StateFlow<Int>
        get() = MutableStateFlow(42).asStateFlow()
    override val nBack: Int
        get() = 2
    override val nrOfEvents: Int
        get() = 10
    override val eventIntervalMs: Long
        get() = 2000L

    override fun setGameType(gameType: GameType) {}
    override fun startGame() {}
    override fun checkMatch() {}
}