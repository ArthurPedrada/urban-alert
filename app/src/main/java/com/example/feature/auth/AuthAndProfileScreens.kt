package com.example.feature.auth

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.core.config.PilotCityConfig
import com.example.core.model.AdminAccountInfo
import com.example.core.model.UserRole
import com.example.data.auth.AuthRepository
import com.example.ui.theme.BaiaTeal
import com.example.ui.theme.UrbanEmeraldDark
import com.example.ui.theme.categoryAccentColor

@Composable
fun AuthScreen(
    authRepository: AuthRepository,
    onContinueAsGuest: () -> Unit,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    pendingOfflineCount: Int = 0,
    onOpenOfflineQueue: () -> Unit = {},
) {

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(132.dp),
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_salvador_hero),
                                contentDescription = stringResource(R.string.cd_pilot_location),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                UrbanEmeraldDark.copy(alpha = 0.45f),
                                                Color(0xFF0A241E).copy(alpha = 0.85f),
                                            ),
                                        ),
                                    )
                                    .padding(18.dp),
                                contentAlignment = Alignment.BottomStart,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.22f),
                                        modifier = Modifier.size(44.dp),
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Rounded.AccountCircle,
                                                contentDescription = stringResource(R.string.cd_security_icon),
                                                tint = Color.White,
                                                modifier = Modifier.size(28.dp),
                                            )
                                        }
                                    }
                                    Text(
                                        text = stringResource(R.string.auth_title),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White,
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.auth_subtitle),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            Button(
                                onClick = onContinueAsGuest,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("continue_guest_button"),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Map,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(text = stringResource(R.string.auth_guest_button))
                            }
                        }
                    }
                }
            }

            item {
                OfflineQueueShortcutCard(
                    pendingOfflineCount = pendingOfflineCount,
                    onClick = onOpenOfflineQueue,
                )
            }

            item {
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PrivacyTip,
                                contentDescription = stringResource(R.string.cd_security_icon),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = stringResource(R.string.auth_lgpd_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Text(
                            text = stringResource(R.string.auth_lgpd_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = stringResource(R.string.auth_apple_note),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    fallbackEmail: String?,
    fallbackPhotoUrl: String?,
    adminInfo: AdminAccountInfo? = null,
    onOpenAdminPanel: () -> Unit = {},
    modifier: Modifier = Modifier,
    pendingOfflineCount: Int = 0,
    onOpenOfflineQueue: () -> Unit = {},
    onOpenIncidentDetail: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val actionStatus by viewModel.actionStatus.collectAsStateWithLifecycle()
    val myIncidents by viewModel.myIncidents.collectAsStateWithLifecycle()

    var displayNameInput by remember { mutableStateOf("") }
    var initializedFromProfile by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val loadedProfile = (profileState as? ProfileUiState.Ready)?.profile

    LaunchedEffect(loadedProfile?.userId, loadedProfile?.displayName) {
        if (loadedProfile != null && !initializedFromProfile) {
            displayNameInput = loadedProfile.displayName
            initializedFromProfile = true
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.profile_delete_dialog_title)) },
            text = { Text(stringResource(R.string.profile_delete_dialog_body)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteAccountAndData(credentialManager)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                    modifier = Modifier.testTag("confirm_delete_account_button"),
                ) {
                    Text(stringResource(R.string.profile_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.profile_delete_cancel))
                }
            },
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            val avatarUrl = loadedProfile?.photoUrl ?: fallbackPhotoUrl
                            if (!avatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = stringResource(R.string.cd_user_avatar),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape),
                                )
                            } else {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(56.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Person,
                                            contentDescription = stringResource(R.string.cd_user_avatar),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(28.dp),
                                        )
                                    }
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = stringResource(R.string.profile_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                val emailText = loadedProfile?.email ?: fallbackEmail
                                if (!emailText.isNullOrBlank()) {
                                    Text(
                                        text = emailText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }

                        when (val state = profileState) {
                            ProfileUiState.Loading -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                    Text(
                                        text = "Sincronizando perfil seguro...",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                            is ProfileUiState.Error -> {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                ) {
                                    Text(
                                        text = state.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(12.dp),
                                    )
                                }
                            }
                            is ProfileUiState.Ready -> {
                                val profile = state.profile
                                val roleLabel = when (profile?.resolvedRole() ?: UserRole.CITIZEN) {
                                    UserRole.CITIZEN -> stringResource(R.string.profile_role_citizen)
                                    UserRole.OPERATOR -> stringResource(R.string.profile_role_operator)
                                    UserRole.MUNICIPAL_MANAGER -> stringResource(R.string.profile_role_manager)
                                    UserRole.SUPER_ADMIN -> stringResource(R.string.profile_role_admin)
                                }

                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.VerifiedUser,
                                            contentDescription = stringResource(R.string.cd_security_icon),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Text(
                                            text = "${stringResource(R.string.profile_role_label)}: $roleLabel",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = displayNameInput,
                                    onValueChange = {
                                        displayNameInput = it
                                        viewModel.clearMessage()
                                    },
                                    label = { Text(stringResource(R.string.profile_display_name_label)) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("profile_name_input"),
                                )

                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.LocationCity,
                                            contentDescription = stringResource(R.string.cd_pilot_location),
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                        Column {
                                            Text(
                                                text = stringResource(R.string.profile_city_label),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                            Text(
                                                text = "${PilotCityConfig.salvador.displayName}, ${PilotCityConfig.salvador.stateCode} (${profile?.homeCityCode ?: PilotCityConfig.salvador.cityCode})",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                        }
                                    }
                                }

                                val currentAction = actionStatus
                                if (currentAction is ProfileActionStatus.Message) {
                                    val bannerColor = if (currentAction.isError) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = bannerColor.copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, bannerColor),
                                        modifier = Modifier.testTag("profile_status_banner"),
                                    ) {
                                        Text(
                                            text = currentAction.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = bannerColor,
                                            modifier = Modifier.padding(12.dp),
                                        )
                                    }
                                }

                                val isBusy = currentAction is ProfileActionStatus.Saving ||
                                    currentAction is ProfileActionStatus.Deleting

                                Button(
                                    onClick = {
                                        viewModel.updateProfile(
                                            displayName = displayNameInput,
                                            homeCityCode = PilotCityConfig.salvador.cityCode,
                                        )
                                    },
                                    enabled = !isBusy,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .minimumInteractiveComponentSize()
                                        .testTag("save_profile_button"),
                                ) {
                                    if (currentAction is ProfileActionStatus.Saving) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Rounded.Save,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Spacer(modifier = Modifier.size(8.dp))
                                        Text(stringResource(R.string.profile_save_button))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (adminInfo != null) {
                item {
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AdminPanelSettings,
                                    contentDescription = stringResource(R.string.cd_admin_icon),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                                Text(
                                    text = stringResource(R.string.profile_open_admin_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                            Text(
                                text = stringResource(R.string.profile_open_admin_body),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Button(
                                onClick = onOpenAdminPanel,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("open_admin_panel_button"),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AdminPanelSettings,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(stringResource(R.string.profile_open_admin_button))
                            }
                        }
                    }
                }
            }

            item {
                OfflineQueueShortcutCard(
                    pendingOfflineCount = pendingOfflineCount,
                    onClick = onOpenOfflineQueue,
                )
            }

            item {
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "Minhas ocorrências publicadas (${myIncidents.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (myIncidents.isEmpty()) {
                            Text(
                                text = "Você ainda não publicou ocorrências com esta conta. Use o botão Reportar para colaborar com Salvador.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            myIncidents.take(10).forEach { item ->
                                val catColor = categoryAccentColor(item.categorySlug)
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .minimumInteractiveComponentSize()
                                        .clickable { onOpenIncidentDetail(item.id) },
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(2.dp),
                                        ) {
                                            Text(
                                                text = item.categoryTitle(),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = catColor,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                            Text(
                                                text = item.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Text(
                                                text = "${stringResource(item.resolvedStatus().labelRes)} · ${item.elapsedLabel()}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Rounded.ChevronRight,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.auth_lgpd_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(R.string.auth_lgpd_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        OutlinedButton(
                            onClick = { viewModel.signOut(credentialManager) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("sign_out_button"),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(stringResource(R.string.profile_sign_out_button))
                        }

                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("delete_account_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteForever,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(stringResource(R.string.profile_delete_button))
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun OfflineQueueShortcutCard(
    pendingOfflineCount: Int,
    onClick: () -> Unit,
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BaiaTeal.copy(alpha = 0.32f)),
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = BaiaTeal.copy(alpha = 0.14f),
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.CloudQueue,
                        contentDescription = stringResource(R.string.cd_offline_queue_icon),
                        tint = BaiaTeal,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.offline_queue_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (pendingOfflineCount > 0) {
                        "$pendingOfflineCount item(ns) aguardando envio para o mapa"
                    } else {
                        "Veja rascunhos salvos e envios pendentes no aparelho"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (pendingOfflineCount > 0) {
                Badge(containerColor = BaiaTeal, contentColor = Color.White) {
                    Text("$pendingOfflineCount")
                }
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
