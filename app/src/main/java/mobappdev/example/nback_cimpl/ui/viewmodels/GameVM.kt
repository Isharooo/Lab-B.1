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
import mobappdev.example.nback_cimpl.AudioPlayer
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
    val gridSize: Int

    fun setGameType(gameType: GameType)
    fun startGame()
    fun checkMatch()
    fun saveSettings(nBack: Int, events: Int, interval: Long, grid: Int)
}

class GameVM(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val audioPlayer: AudioPlayer
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

    private val _nBack = MutableStateFlow(2)
    override val nBack: Int
        get() = _nBack.value

    private val _nrOfEvents = MutableStateFlow(10)
    override val nrOfEvents: Int
        get() = _nrOfEvents.value

    private val _eventIntervalMs = MutableStateFlow(2000L)
    override val eventIntervalMs: Long
        get() = _eventIntervalMs.value

    private val _gridSize = MutableStateFlow(3)
    override val gridSize: Int
        get() = _gridSize.value

    private var job: Job? = null
    private val nBackHelper = NBackHelper()
    private var events = emptyArray<Int>()
    private var audioEvents = emptyArray<Int>()
    private var correctResponses = 0

    override fun setGameType(gameType: GameType) {
        _gameState.value = _gameState.value.copy(gameType = gameType)
    }

    override fun startGame() {
        job?.cancel()
        _score.value = 0
        correctResponses = 0

        when (_gameState.value.gameType) {
            GameType.Visual -> {
                val totalPositions = gridSize * gridSize
                events = nBackHelper.generateNBackString(nrOfEvents, totalPositions, 30, nBack).toList().toTypedArray()
                Log.d("GameVM", "Visual sequence: ${events.contentToString()}")
            }
            GameType.Audio -> {
                audioEvents = nBackHelper.generateNBackString(nrOfEvents, 8, 30, nBack).toList().toTypedArray()
                Log.d("GameVM", "Audio sequence: ${audioEvents.contentToString()}")
            }
            GameType.AudioVisual -> {
                val totalPositions = gridSize * gridSize
                events = nBackHelper.generateNBackString(nrOfEvents, totalPositions, 30, nBack).toList().toTypedArray()
                audioEvents = nBackHelper.generateNBackString(nrOfEvents, 8, 30, nBack).toList().toTypedArray()
                Log.d("GameVM", "Visual: ${events.contentToString()}")
                Log.d("GameVM", "Audio: ${audioEvents.contentToString()}")
            }
        }

        job = viewModelScope.launch {
            when (gameState.value.gameType) {
                GameType.Audio -> runAudioGame(audioEvents)
                GameType.AudioVisual -> runAudioVisualGame(events, audioEvents)
                GameType.Visual -> runVisualGame(events)
            }
        }
    }

    override fun checkMatch() {
        val currentIndex = _gameState.value.eventIndex

        if (currentIndex >= nBack) {
            val isCorrect = when (_gameState.value.gameType) {
                GameType.Visual -> {
                    events[currentIndex] == events[currentIndex - nBack]
                }
                GameType.Audio -> {
                    audioEvents[currentIndex] == audioEvents[currentIndex - nBack]
                }
                GameType.AudioVisual -> {
                    events[currentIndex] == events[currentIndex - nBack] ||
                            audioEvents[currentIndex] == audioEvents[currentIndex - nBack]
                }
            }

            if (isCorrect) {
                _score.value = _score.value + 1
                correctResponses++
                _gameState.value = _gameState.value.copy(
                    feedback = FeedbackType.CORRECT,
                    correctCount = correctResponses
                )
            } else {
                _score.value = maxOf(0, _score.value - 1)
                _gameState.value = _gameState.value.copy(
                    feedback = FeedbackType.INCORRECT
                )
            }

            viewModelScope.launch {
                delay(300)
                _gameState.value = _gameState.value.copy(feedback = FeedbackType.NONE)
            }
        }
    }

    override fun saveSettings(nBack: Int, events: Int, interval: Long, grid: Int) {
        viewModelScope.launch {
            userPreferencesRepository.saveSettings(nBack, events, interval, grid)
        }
    }

    private suspend fun runAudioGame(audioEvents: Array<Int>) {
        _gameState.value = _gameState.value.copy(
            audioValue = -1,
            eventIndex = 0,
            isRunning = true
        )
        delay(1000)

        for ((index, value) in audioEvents.withIndex()) {
            _gameState.value = _gameState.value.copy(
                audioValue = value,
                eventIndex = index
            )
            audioPlayer.playLetter(value)
            delay(eventIntervalMs)
        }

        _gameState.value = _gameState.value.copy(
            audioValue = -1,
            isRunning = false
        )

        if (_score.value > _highscore.value) {
            viewModelScope.launch {
                userPreferencesRepository.saveHighScore(_score.value)
            }
        }
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

    private suspend fun runAudioVisualGame(events: Array<Int>, audioEvents: Array<Int>) {
        _gameState.value = _gameState.value.copy(
            eventValue = -1,
            audioValue = -1,
            eventIndex = 0,
            isRunning = true
        )
        delay(1000)

        for (index in events.indices) {
            _gameState.value = _gameState.value.copy(
                eventValue = events[index],
                audioValue = audioEvents[index],
                eventIndex = index
            )
            audioPlayer.playLetter(audioEvents[index])
            delay(eventIntervalMs)
        }

        _gameState.value = _gameState.value.copy(
            eventValue = -1,
            audioValue = -1,
            isRunning = false
        )

        if (_score.value > _highscore.value) {
            viewModelScope.launch {
                userPreferencesRepository.saveHighScore(_score.value)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GameApplication)
                GameVM(application.userPreferencesRespository, application.audioPlayer)
            }
        }
    }

    init {
        viewModelScope.launch {
            userPreferencesRepository.highscore.collect {
                _highscore.value = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.nBackValue.collect {
                _nBack.value = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.eventCount.collect {
                _nrOfEvents.value = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.eventInterval.collect {
                _eventIntervalMs.value = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.gridSize.collect {
                _gridSize.value = it
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
    val audioValue: Int = -1,
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
    override val nBack: Int = 2
    override val nrOfEvents: Int = 10
    override val eventIntervalMs: Long = 2000L
    override val gridSize: Int = 3

    override fun setGameType(gameType: GameType) {}
    override fun startGame() {}
    override fun checkMatch() {}
    override fun saveSettings(nBack: Int, events: Int, interval: Long, grid: Int) {}
}