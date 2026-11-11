package com.example.cardmanager.feature.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardmanager.R
import com.example.cardmanager.core.backup.BackupManager
import com.example.cardmanager.ui.theme.AppThemeMode
import com.example.cardmanager.ui.theme.AppThemeStyle
import com.example.cardmanager.ui.theme.LocalThemeState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeState = LocalThemeState.current
    val isMiui = themeState.currentStyle == AppThemeStyle.MIUIX
    val cardRadius = if (isMiui) 24.dp else 16.dp

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val backupManager = remember { BackupManager() }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { destUri ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar("正在导出数据备份...")
                val result = backupManager.exportBackup(context, destUri)
                result.fold(
                    onSuccess = { count ->
                        snackbarHostState.showSnackbar("备份导出成功：已备份 $count 条记录")
                    },
                    onFailure = { err ->
                        snackbarHostState.showSnackbar("导出失败: ${err.localizedMessage ?: "未知错误"}")
                    }
                )
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { sourceUri ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar("正在恢复数据备份...")
                val result = backupManager.importBackup(context, sourceUri)
                result.fold(
                    onSuccess = { (cards, txs) ->
                        snackbarHostState.showSnackbar("恢复成功：已导入 $cards 张卡片，$txs 条交易记录")
                    },
                    onFailure = { err ->
                        snackbarHostState.showSnackbar("导入失败: ${err.localizedMessage ?: "备份文件无效"}")
                    }
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = if (isMiui) (-0.5).sp else 0.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Section 1: UI Design Style
            item {
                SectionHeader(
                    title = stringResource(R.string.ui_style),
                    icon = Icons.Outlined.Style
                )
                Spacer(Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(cardRadius),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        StyleOptionRow(
                            title = stringResource(R.string.style_miuix),
                            subtitle = "优雅圆角、充裕间距与 HyperOS 极简质感",
                            selected = themeState.currentStyle == AppThemeStyle.MIUIX,
                            onClick = { themeState.setStyle(AppThemeStyle.MIUIX) }
                        )
                        StyleOptionRow(
                            title = stringResource(R.string.style_material3),
                            subtitle = "生动的 Material You 动态排版与自适应色彩",
                            selected = themeState.currentStyle == AppThemeStyle.MATERIAL3,
                            onClick = { themeState.setStyle(AppThemeStyle.MATERIAL3) }
                        )
                    }
                }
            }

            // Section 2: Theme Appearance Mode
            item {
                SectionHeader(
                    title = stringResource(R.string.theme_mode),
                    icon = Icons.Outlined.Palette
                )
                Spacer(Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(cardRadius),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        ThemeModeOptionRow(
                            title = stringResource(R.string.mode_system),
                            icon = Icons.Outlined.BrightnessAuto,
                            selected = themeState.currentMode == AppThemeMode.SYSTEM,
                            onClick = { themeState.setMode(AppThemeMode.SYSTEM) }
                        )
                        ThemeModeOptionRow(
                            title = stringResource(R.string.mode_light),
                            icon = Icons.Outlined.LightMode,
                            selected = themeState.currentMode == AppThemeMode.LIGHT,
                            onClick = { themeState.setMode(AppThemeMode.LIGHT) }
                        )
                        ThemeModeOptionRow(
                            title = stringResource(R.string.mode_dark),
                            icon = Icons.Outlined.DarkMode,
                            selected = themeState.currentMode == AppThemeMode.DARK,
                            onClick = { themeState.setMode(AppThemeMode.DARK) }
                        )
                    }
                }
            }

            // Section 3: Data Backup & Recovery
            item {
                SectionHeader(
                    title = "数据备份与恢复",
                    icon = Icons.Outlined.Storage
                )
                Spacer(Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(cardRadius),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        SettingsActionRow(
                            title = "导出备份 (JSON)",
                            subtitle = "将卡片信息与消费记录保存为本地 JSON 文件",
                            icon = Icons.Outlined.CloudUpload,
                            onClick = {
                                val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                                exportLauncher.launch("vaultcard_backup_$timestamp.json")
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        SettingsActionRow(
                            title = "恢复备份 (JSON)",
                            subtitle = "从文件导入并合并卡片与交易记录",
                            icon = Icons.Outlined.CloudDownload,
                            onClick = {
                                importLauncher.launch(arrayOf("application/json", "*/*"))
                            }
                        )
                    }
                }
            }

            // Section 4: App Version
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "VaultCard",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "智能信用卡与还款管理管家",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "v1.0.3",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun StyleOptionRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        RadioButton(
            selected = selected,
            onClick = onClick
        )
    }
}

@Composable
private fun ThemeModeOptionRow(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
