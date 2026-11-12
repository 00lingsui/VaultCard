package com.example.cardmanager

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cardmanager.core.security.AuthViewModel
import com.example.cardmanager.feature.card.ui.CardListScreen
import com.example.cardmanager.feature.settings.SettingsScreen
import com.example.cardmanager.feature.statistics.ui.StatisticsScreen
import com.example.cardmanager.ui.theme.AppDynamicTheme
import com.example.cardmanager.ui.theme.AppThemeStyle
import com.example.cardmanager.ui.theme.LocalThemeState
import com.example.cardmanager.ui.theme.ThemeState
import com.example.cardmanager.ui.theme.rememberThemeState
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val widthSizeClass = windowSizeClass.widthSizeClass
            val themeState = rememberThemeState()
            val authState by authViewModel.authState.collectAsStateWithLifecycle()
            var currentScreen by remember { mutableStateOf("cards") }
            var lastBackPressTimestamp by remember { mutableStateOf(0L) }
            val context = androidx.compose.ui.platform.LocalContext.current

            androidx.activity.compose.BackHandler(enabled = true) {
                if (currentScreen != "cards") {
                    currentScreen = "cards"
                } else {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastBackPressTimestamp < 2000L) {
                        (context as? android.app.Activity)?.finish()
                    } else {
                        lastBackPressTimestamp = currentTime
                        android.widget.Toast.makeText(context, "再按一次退出应用", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }

            val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
            ) {}

            LaunchedEffect(Unit) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
                if (!authState.isAuthenticated) {
                    authViewModel.authenticate(this@MainActivity)
                }
            }

            CompositionLocalProvider(LocalThemeState provides themeState) {
                AppDynamicTheme(themeState = themeState) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        if (authState.isAuthenticated) {
                            when (currentScreen) {
                                "settings" -> SettingsScreen(onNavigateBack = { currentScreen = "cards" })
                                "statistics" -> StatisticsScreen(onNavigateBack = { currentScreen = "cards" })
                                else -> CardListScreen(
                                    widthSizeClass = widthSizeClass,
                                    onNavigateToSettings = { currentScreen = "settings" },
                                    onNavigateToStatistics = { currentScreen = "statistics" }
                                )
                            }
                        } else {
                            BiometricLockScreen(
                                errorMessage = authState.errorMessage,
                                onRetry = { authViewModel.authenticate(this@MainActivity) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BiometricLockScreen(
    errorMessage: String?,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(androidx.compose.ui.res.stringResource(R.string.locked_title), style = MaterialTheme.typography.headlineSmall)
        Text(
            androidx.compose.ui.res.stringResource(R.string.locked_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        errorMessage?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text(androidx.compose.ui.res.stringResource(R.string.unlock_button))
        }
    }
}
