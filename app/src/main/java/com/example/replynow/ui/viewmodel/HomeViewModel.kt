package com.example.replynow.ui.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.replynow.R
import com.example.replynow.domain.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class InstalledApp(
    val name: String,
    val packageName: String,
    val iconRes: Int,
    val accentColor: Long,
    val pendingCount: Int = 0
)

data class HomeUiState(
    val installedApps: List<InstalledApp> = emptyList(),
    val totalPending: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val repository: MessageRepository
) : AndroidViewModel(application) {

    companion object {
        val ALL_SUPPORTED_APPS = listOf(
            Triple("WhatsApp", "com.whatsapp", R.drawable.whatsapp) to 0xFF25D366L,
            Triple("Messenger", "com.facebook.orca", R.drawable.messenger) to 0xFF0084FFL,
            Triple("Instagram", "com.instagram.android", R.drawable.instagram) to 0xFFE1306CL,
            Triple("Telegram", "org.telegram.messenger", R.drawable.telegram) to 0xFF0088CCL,
            Triple("Snapchat", "com.snapchat.android", R.drawable.snapchat) to 0xFFFFFC00L,
            Triple("Discord", "com.discord", R.drawable.discord) to 0xFF5865F2L,
            Triple("Slack", "com.Slack", R.drawable.slack) to 0xFF4A154BL,
            Triple("Teams", "com.microsoft.teams", R.drawable.teams) to 0xFF6264A7L,
            Triple("Signal", "org.thoughtcrime.securesms", R.drawable.signal) to 0xFF3A76F0L,
            Triple("Gmail", "com.google.android.gm", R.drawable.gmail) to 0xFFEA4335L,
            Triple("Outlook", "com.microsoft.office.outlook", R.drawable.outlook) to 0xFF0078D4L,
            Triple("LinkedIn", "com.linkedin.android", R.drawable.linkedin) to 0xFF0A66C2L,
            Triple("Facebook", "com.facebook.katana", R.drawable.facebook) to 0xFF1877F2L,
            Triple("X", "com.twitter.android", R.drawable.x) to 0xFF000000L,
            Triple("TikTok", "com.zhiliaoapp.musically", R.drawable.tiktok) to 0xFF010101L,
            Triple("Viber", "com.viber.voip", R.drawable.viber) to 0xFF7360F2L,
            Triple("WeChat", "com.tencent.mm", R.drawable.wechat) to 0xFF07C160L,
            Triple("Messages", "com.google.android.apps.messaging", R.drawable.messages) to 0xFF1A73E8L,
            Triple("Google Chat", "com.google.android.apps.dynamite", R.drawable.gchat) to 0xFF00AC47L,
            Triple("Zoom", "us.zoom.videomeetings", R.drawable.zoom) to 0xFF2D8CFFL,
            Triple("WA Business", "com.whatsapp.w4b", R.drawable.whatsappbusiness) to 0xFF25D366L,
        )
    }

    private val installedPackages: Set<String> = run {
        val pm = application.packageManager
        ALL_SUPPORTED_APPS.map { it.first.second }.filter { pkg ->
            try {
                pm.getPackageInfo(pkg, 0)
                true
            } catch (_: PackageManager.NameNotFoundException) {
                false
            }
        }.toSet()
    }

    val uiState: StateFlow<HomeUiState> = repository.getPendingCountsByPackage()
        .map { countsMap ->
            val apps = ALL_SUPPORTED_APPS
                .filter { it.first.second in installedPackages }
                .map { (triple, color) ->
                    InstalledApp(
                        name = triple.first,
                        packageName = triple.second,
                        iconRes = triple.third,
                        accentColor = color,
                        pendingCount = countsMap[triple.second] ?: 0
                    )
                }
                .sortedByDescending { it.pendingCount }
            HomeUiState(
                installedApps = apps,
                totalPending = apps.sumOf { it.pendingCount },
                isLoading = false
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())
}
