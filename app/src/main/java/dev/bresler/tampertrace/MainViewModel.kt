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
import java.io.File
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

  private val _fridaMemoryDetected = MutableStateFlow<Boolean?>(null)
  val fridaMemoryDetected: StateFlow<Boolean?> = _fridaMemoryDetected.asStateFlow()

  init {
    onIntent(MainIntent.CheckRootStatus)
    checkFridaPort()
    checkMemoryMaps()
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

  private fun checkMemoryMaps() {
    viewModelScope.launch {
      val detected = withContext(Dispatchers.IO) {
        try {
          File("/proc/self/maps").bufferedReader().useLines { lines ->
            lines.any { line ->
              val lower = line.lowercase()

              // Named Frida artifacts in the path column
              if (lower.contains("frida") || lower.contains("linjector")) return@any true

              // Anonymous executable region: r-xp, device 00:00, inode 0, no path
              // Indicates runtime code injection (Frida writes executable stubs this way)
              val parts = line.trim().split("\\s+".toRegex())
              val perms = parts.getOrNull(1) ?: return@any false
              val dev   = parts.getOrNull(3) ?: return@any false
              val inode = parts.getOrNull(4) ?: return@any false
              perms.contains('x') && dev == "00:00" && inode == "0" && parts.size == 5
            }
          }
        } catch (e: Exception) {
          false
        }
      }
      _fridaMemoryDetected.value = detected
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
