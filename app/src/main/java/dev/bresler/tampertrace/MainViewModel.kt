package dev.bresler.tampertrace

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.scottyab.rootbeer.RootBeer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

sealed interface MainUiState {
  data object Loading : MainUiState
  data object Secure : MainUiState
  data object Rooted : MainUiState
}

sealed interface MainIntent {
  data object CheckRootStatus : MainIntent
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

  private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
  val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

  private val _fridaPortDetected = MutableStateFlow<Boolean?>(null)
  val fridaPortDetected: StateFlow<Boolean?> = _fridaPortDetected.asStateFlow()

  init {
    onIntent(MainIntent.CheckRootStatus)
    checkFridaPort()
  }

  fun onIntent(intent: MainIntent) {
    when (intent) {
      MainIntent.CheckRootStatus -> checkRootStatus()
    }
  }

  private fun checkRootStatus() {
    viewModelScope.launch {
      _uiState.value = MainUiState.Loading
      val isRooted = withContext(Dispatchers.IO) {
        RootBeer(getApplication()).isRooted
      }
      _uiState.value = if (isRooted) MainUiState.Rooted else MainUiState.Secure
    }
  }

  private fun checkFridaPort() {
    viewModelScope.launch {
      val detected = withContext(Dispatchers.IO) {
        try {
          Socket().use { socket ->
            socket.connect(InetSocketAddress("127.0.0.1", 27042), 500)
            true
          }
        } catch (e: Exception) {
          false
        }
      }
      _fridaPortDetected.value = detected
    }
  }
}
