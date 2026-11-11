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
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardmanager.R
import com.example.cardmanager.core.backup.BackupManager
import com.example.cardmanager.core.webdav.WebDavClient
import com.example.cardmanager.core.webdav.WebDavConfig
import com.example.cardmanager.core.webdav.WebDavPreferenceManager
import com.example.cardmanager.core.worker.WebDavSyncWorker
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

    var webDavConfig by remember { mutableStateOf(WebDavPreferenceManager.getConfig(context)) }
    var serverUrl by remember { mutableStateOf(webDavConfig.serverUrl) }
    var username by remember { mutableStateOf(webDavConfig.username) }
    var password by remember { mutableStateOf(webDavConfig.password) }
    var autoSyncEnabled by remember { mutableStateOf(webDavConfig.autoSyncEnabled) }
    var passwordVisible by remember { mutableStateOf(false) }

    var isTestingConnection by remember { mutableStateOf(false) }
    var isBackingUpWebDav by remember { mutableStateOf(false) }
    var isRestoringWebDav by remember { mutableStateOf(false) }
    var showWebDavRestoreConfirm by remember { mutableStateOf(false) }

    fun updateAndPersistWebDav(
        newUrl: String = serverUrl,
        newUsername: String = username,
        newPassword: String = password,
        newAutoSync: Boolean = autoSyncEnabled
    ) {
        val updated = webDavConfig.copy(
            serverUrl = newUrl.trim(),
            username = newUsername.trim(),
            password = newPassword,
            autoSyncEnabled = newAutoSync
        )
        webDavConfig = updated
        WebDavPreferenceManager.saveConfig(context, updated)
        WebDavSyncWorker.scheduleDailySync(context, newAutoSync)
    }

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

            // Section: WebDAV Cloud Sync
            item {
                SectionHeader(
                    title = "云端备份与同步 (WebDAV)",
                    icon = Icons.Outlined.CloudSync
                )
                Spacer(Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(cardRadius),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = serverUrl,
                            onValueChange = {
                                serverUrl = it
                                updateAndPersistWebDav(newUrl = it)
                            },
                            label = { Text("WebDAV 服务器地址") },
                            placeholder = { Text("如: https://dav.jianguoyun.com/dav/VaultCard/") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it
                                updateAndPersistWebDav(newUsername = it)
                            },
                            label = { Text("用户名 / 账号邮箱") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                updateAndPersistWebDav(newPassword = it)
                            },
                            label = { Text("密码 / 应用授权码") },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    updateAndPersistWebDav()
                                    coroutineScope.launch {
                                        isTestingConnection = true
                                        val testResult = WebDavClient.testConnection(webDavConfig)
                                        isTestingConnection = false
                                        testResult.fold(
                                            onSuccess = {
                                                snackbarHostState.showSnackbar("WebDAV 连接成功，认证通过！")
                                            },
                                            onFailure = { err ->
                                                snackbarHostState.showSnackbar("连接失败: ${err.localizedMessage ?: "未知错误"}")
                                            }
                                        )
                                    }
                                },
                                enabled = !isTestingConnection && !isBackingUpWebDav && !isRestoringWebDav,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isTestingConnection) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(Modifier.width(6.dp))
                                }
                                Text("测试连接")
                            }

                            Button(
                                onClick = {
                                    updateAndPersistWebDav()
                                    coroutineScope.launch {
                                        isBackingUpWebDav = true
                                        snackbarHostState.showSnackbar("正在上传云端备份...")
                                        val jsonString = backupManager.createBackupJsonString(context)
                                        val uploadResult = WebDavClient.uploadBackup(webDavConfig, jsonString)
                                        isBackingUpWebDav = false
                                        uploadResult.fold(
                                            onSuccess = {
                                                WebDavPreferenceManager.updateLastSyncTimestamp(context)
                                                webDavConfig = WebDavPreferenceManager.getConfig(context)
                                                snackbarHostState.showSnackbar("云端备份成功！")
                                            },
                                            onFailure = { err ->
                                                snackbarHostState.showSnackbar("备份失败: ${err.localizedMessage ?: "未知错误"}")
                                            }
                                        )
                                    }
                                },
                                enabled = !isTestingConnection && !isBackingUpWebDav && !isRestoringWebDav,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isBackingUpWebDav) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(Modifier.width(6.dp))
                                }
                                Text("立即备份")
                            }

                            OutlinedButton(
                                onClick = {
                                    updateAndPersistWebDav()
                                    showWebDavRestoreConfirm = true
                                },
                                enabled = !isTestingConnection && !isBackingUpWebDav && !isRestoringWebDav,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isRestoringWebDav) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(Modifier.width(6.dp))
                                }
                                Text("恢复备份")
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "每日自动云端备份",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                val syncInfo = if (webDavConfig.lastSyncTimestamp > 0L) {
                                    val formattedDate = java.text.SimpleDateFormat(
                                        "MM/dd HH:mm",
                                        java.util.Locale.getDefault()
                                    ).format(java.util.Date(webDavConfig.lastSyncTimestamp))
                                    "上次自动同步：$formattedDate"
                                } else {
                                    "后台每24小时自动上传，需连接网络"
                                }
                                Text(
                                    text = syncInfo,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = autoSyncEnabled,
                                onCheckedChange = { isChecked ->
                                    autoSyncEnabled = isChecked
                                    updateAndPersistWebDav(newAutoSync = isChecked)
                                    val msg = if (isChecked) "已开启每日自动备份" else "已关闭自动备份"
                                    coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            )
                        }
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

    if (showWebDavRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { showWebDavRestoreConfirm = false },
            title = { Text("确认恢复云端备份？") },
            text = { Text("将从 WebDAV 云端下载 vaultcard_backup.json 并合并导入本地卡片与记录。") },
            confirmButton = {
                Button(
                    onClick = {
                        showWebDavRestoreConfirm = false
                        coroutineScope.launch {
                            isRestoringWebDav = true
                            snackbarHostState.showSnackbar("正在从 WebDAV 下载备份...")
                            val downloadResult = WebDavClient.downloadBackup(webDavConfig)
                            downloadResult.fold(
                                onSuccess = { jsonContent ->
                                    val restoreResult = backupManager.restoreFromJsonString(context, jsonContent)
                                    isRestoringWebDav = false
                                    restoreResult.fold(
                                        onSuccess = { (cards, txs) ->
                                            snackbarHostState.showSnackbar("云端恢复完成：已恢复 $cards 张卡片，$txs 条交易记录")
                                        },
                                        onFailure = { err ->
                                            snackbarHostState.showSnackbar("数据恢复失败: ${err.localizedMessage ?: "数据格式错误"}")
                                        }
                                    )
                                },
                                onFailure = { err ->
                                    isRestoringWebDav = false
                                    snackbarHostState.showSnackbar("下载失败: ${err.localizedMessage ?: "未知错误"}")
                                }
                            )
                        }
                    }
                ) {
                    Text("开始恢复")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWebDavRestoreConfirm = false }) {
                    Text("取消")
                }
            }
        )
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
