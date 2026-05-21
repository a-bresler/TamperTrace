package dev.bresler.tampertrace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.bresler.tampertrace.ui.theme.TamperTraceTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      TamperTraceTheme {
        val viewModel: MainViewModel = viewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val fridaPortDetected by viewModel.fridaPortDetected.collectAsStateWithLifecycle()
        val fridaMemoryDetected by viewModel.fridaMemoryDetected.collectAsStateWithLifecycle()
        SecurityStatusScreen(
          uiState = uiState,
          fridaPortDetected = fridaPortDetected,
          fridaMemoryDetected = fridaMemoryDetected,
          onExit = { finish() },
        )
      }
    }
  }
}

@Composable
fun SecurityStatusScreen(uiState: MainUiState, fridaPortDetected: Boolean?, fridaMemoryDetected: Boolean?, onExit: () -> Unit) {
  val bgColors = when (uiState) {
    MainUiState.Rooted -> listOf(Color(0xFF0D0005), Color(0xFF2A0010), Color(0xFF0D0005))
    MainUiState.Secure -> listOf(Color(0xFF00100A), Color(0xFF002A18), Color(0xFF00100A))
    MainUiState.Loading -> listOf(Color(0xFF080808), Color(0xFF141414), Color(0xFF080808))
  }

  Box(
    modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(bgColors)),
    contentAlignment = Alignment.Center,
  ) {
    when (uiState) {
      MainUiState.Loading -> LoadingScreen()
      MainUiState.Rooted -> RootAlertScreen(fridaPortDetected = fridaPortDetected, fridaMemoryDetected = fridaMemoryDetected, onExit = onExit)
      MainUiState.Secure -> SecureScreen(fridaPortDetected = fridaPortDetected, fridaMemoryDetected = fridaMemoryDetected)
    }
  }
}

@Composable
fun LoadingScreen() {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(20.dp),
  ) {
    CircularProgressIndicator(color = Color(0x88FFFFFF), strokeWidth = 2.dp, modifier = Modifier.size(40.dp))
    Text(text = "Scanning device…", color = Color(0x66FFFFFF), fontSize = 14.sp)
  }
}

@Composable
fun RootAlertScreen(fridaPortDetected: Boolean?, fridaMemoryDetected: Boolean?, onExit: () -> Unit) {
  val transition = rememberInfiniteTransition(label = "pulse")

  val ring1Scale by transition.animateFloat(
    initialValue = 0.55f,
    targetValue = 1.5f,
    animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Restart),
    label = "ring1Scale",
  )
  val ring1Alpha by transition.animateFloat(
    initialValue = 0.6f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
    label = "ring1Alpha",
  )
  val ring2Scale by transition.animateFloat(
    initialValue = 0.55f,
    targetValue = 1.5f,
    animationSpec =
      infiniteRepeatable(tween(1400, delayMillis = 500, easing = FastOutSlowInEasing), RepeatMode.Restart),
    label = "ring2Scale",
  )
  val ring2Alpha by transition.animateFloat(
    initialValue = 0.6f,
    targetValue = 0f,
    animationSpec =
      infiniteRepeatable(tween(1400, delayMillis = 500, easing = LinearEasing), RepeatMode.Restart),
    label = "ring2Alpha",
  )

  Column(
    modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
      Box(
        modifier =
          Modifier.size(140.dp)
            .scale(ring1Scale)
            .alpha(ring1Alpha)
            .background(Color(0x44FF1744), CircleShape)
      )
      Box(
        modifier =
          Modifier.size(140.dp)
            .scale(ring2Scale)
            .alpha(ring2Alpha)
            .background(Color(0x44FF1744), CircleShape)
      )
      Box(
        modifier = Modifier.size(96.dp).background(Color(0x33FF1744), CircleShape),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Filled.Warning,
          contentDescription = null,
          tint = Color(0xFFFF1744),
          modifier = Modifier.size(48.dp),
        )
      }
    }

    Spacer(Modifier.height(28.dp))

    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFF1744)) {
      Text(
        text = "SECURITY THREAT",
        color = Color.White,
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 2.5.sp,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
      )
    }

    Spacer(Modifier.height(14.dp))

    Text(
      text = "Root\nDetected",
      color = Color.White,
      fontSize = 44.sp,
      fontWeight = FontWeight.Black,
      textAlign = TextAlign.Center,
      lineHeight = 50.sp,
    )

    Spacer(Modifier.height(14.dp))

    Text(
      text = "This device has been rooted or modified.\nTamperTrace cannot operate on a\ncompromised environment.",
      color = Color(0x99FFFFFF),
      fontSize = 14.sp,
      textAlign = TextAlign.Center,
      lineHeight = 21.sp,
    )

    Spacer(Modifier.height(36.dp))

    Surface(
      shape = RoundedCornerShape(14.dp),
      color = Color(0x1AFF1744),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        StatusRow("Root Access", "Confirmed", danger = true)
        StatusRow(
          label = "Frida Port",
          value = when (fridaPortDetected) { null -> "Checking…"; true -> "Active"; false -> "Not Detected" },
          danger = fridaPortDetected == true,
        )
        StatusRow(
          label = "Memory Maps",
          value = when (fridaMemoryDetected) { null -> "Checking…"; true -> "Artifacts Found"; false -> "Clean" },
          danger = fridaMemoryDetected == true,
        )
      }
    }

    Spacer(Modifier.height(32.dp))

    Button(
      onClick = onExit,
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
      modifier = Modifier.fillMaxWidth().height(54.dp),
    ) {
      Text(text = "Exit Application", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
  }
}

@Composable
fun SecureScreen(fridaPortDetected: Boolean?, fridaMemoryDetected: Boolean?) {
  val scale = remember { Animatable(0f) }
  LaunchedEffect(Unit) {
    scale.animateTo(
      targetValue = 1f,
      animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
    )
  }

  Column(
    modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Box(
      modifier = Modifier.size(120.dp).scale(scale.value).background(Color(0x2200E676), CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = Icons.Filled.Lock,
        contentDescription = null,
        tint = Color(0xFF00E676),
        modifier = Modifier.size(52.dp),
      )
    }

    Spacer(Modifier.height(28.dp))

    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF00E676)) {
      Text(
        text = "ALL CLEAR",
        color = Color(0xFF001A0D),
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 2.5.sp,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
      )
    }

    Spacer(Modifier.height(14.dp))

    Text(
      text = "Device\nSecure",
      color = Color.White,
      fontSize = 44.sp,
      fontWeight = FontWeight.Black,
      textAlign = TextAlign.Center,
      lineHeight = 50.sp,
    )

    Spacer(Modifier.height(14.dp))

    Text(
      text = "No root access detected. Your device\npassed all integrity checks and is running\nin a trusted environment.",
      color = Color(0x99FFFFFF),
      fontSize = 14.sp,
      textAlign = TextAlign.Center,
      lineHeight = 21.sp,
    )

    Spacer(Modifier.height(36.dp))

    Surface(
      shape = RoundedCornerShape(14.dp),
      color = Color(0x1A00E676),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        StatusRow("Root Access", "Not Detected", danger = false)
        StatusRow(
          label = "Frida Port",
          value = when (fridaPortDetected) { null -> "Checking…"; true -> "Active"; false -> "Not Detected" },
          danger = fridaPortDetected == true,
        )
        StatusRow(
          label = "Memory Maps",
          value = when (fridaMemoryDetected) { null -> "Checking…"; true -> "Artifacts Found"; false -> "Clean" },
          danger = fridaMemoryDetected == true,
        )
      }
    }
  }
}

@Composable
fun StatusRow(label: String, value: String, danger: Boolean) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(text = label, color = Color(0x77FFFFFF), fontSize = 13.sp)
    Text(
      text = value,
      color = if (danger) Color(0xFFFF5252) else Color(0xFF69F0AE),
      fontSize = 13.sp,
      fontWeight = FontWeight.SemiBold,
    )
  }
}
