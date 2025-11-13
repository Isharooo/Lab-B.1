package mobappdev.example.nback_cimpl

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import mobappdev.example.nback_cimpl.data.UserPreferencesRepository

private const val APP_PREFERENCES_NAME = "game_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = APP_PREFERENCES_NAME
)

class GameApplication: Application() {
    lateinit var userPreferencesRespository: UserPreferencesRepository
    lateinit var audioPlayer: AudioPlayer

    override fun onCreate() {
        super.onCreate()
        userPreferencesRespository = UserPreferencesRepository(dataStore)
        audioPlayer = AudioPlayer(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        audioPlayer.shutdown()
    }
}