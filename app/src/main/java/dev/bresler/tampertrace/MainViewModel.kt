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

  init {
    onIntent(MainIntent.CheckRootStatus)
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
}
