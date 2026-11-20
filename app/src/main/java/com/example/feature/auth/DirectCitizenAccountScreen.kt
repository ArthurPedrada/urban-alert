package com.example.feature.auth

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.config.PilotCityConfig
import com.example.data.auth.CitizenSessionStore
import com.example.data.incidents.IncidentDocument
import com.example.data.incidents.IncidentRepository
import com.example.ui.theme.UrbanEmeraldDark
import com.example.ui.theme.categoryAccentColor
import kotlinx.coroutines.flow.catch

@Composable
fun DirectCitizenAccountScreen(
    citizenSessionStore: CitizenSessionStore,
    incidentRepository: IncidentRepository,
    pendingOfflineCount: Int,
    onOpenOfflineQueue: () -> Unit,
    onOpenIncidentDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val session by citizenSessionStore.session.collectAsStateWithLifecycle()
    val myIncidentsFlow = remember(session.guestId) {
        incidentRepository.observeIncidentsByAuthor(session.guestId)
            .catch { emit(emptyList()) }
    }
    val myIncidents: List<IncidentDocument> by myIncidentsFlow.collectAsStateWithLifecycle(
        initialValue = emptyList(),
    )

    var displayNameInput by remember { mutableStateOf(session.displayName) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(session.guestId, session.displayName) {
        displayNameInput = session.displayName
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reiniciar identificador local?") },
            text = {
                Text(
                    "Um novo código anônimo será criado neste aparelho. As ocorrências já enviadas permanecem no mapa público, mas deixarão de aparecer nesta lista local.",
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        citizenSessionStore.resetLocalIdentity()
                        statusMessage = "Identificador local renovado com sucesso."
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                    modifier = Modifier.testTag("confirm_reset_guest_id_button"),
                ) {
                    Text("Reiniciar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
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
                    Column(modifier = Modifier.fillMaxWidth()) {
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
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.22f),
                                        modifier = Modifier.size(46.dp),
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Rounded.AccountCircle,
                                                contentDescription = stringResource(R.string.cd_user_avatar),
                                                tint = Color.White,
                                                modifier = Modifier.size(28.dp),
                                            )
                                        }
                                    }
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = stringResource(R.string.auth_title),
                                            style = MaterialTheme.typography.titleLarge,
                                            color = Color.White,
                                        )
                                        Text(
                                            text = stringResource(R.string.auth_apple_note),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White.copy(alpha = 0.88f),
                                        )
                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.VerifiedUser,
                                        contentDescription = stringResource(R.string.cd_security_icon),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = "Identificador local anônimo ativo",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        )
                                        Text(
                                            text = session.guestId,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = displayNameInput,
                                onValueChange = {
                                    displayNameInput = it
                                    statusMessage = null
                                },
                                label = { Text("Nome local (opcional)") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("direct_citizen_name_input"),
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
                                            text = "${PilotCityConfig.salvador.displayName}, ${PilotCityConfig.salvador.stateCode} (${session.homeCityCode})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                    }
                                }
                            }

                            if (!statusMessage.isNullOrBlank()) {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.testTag("direct_citizen_status_banner"),
                                ) {
                                    Text(
                                        text = statusMessage.orEmpty(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    citizenSessionStore.updateDisplayName(displayNameInput)
                                    statusMessage = "Nome local salvo neste aparelho."
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("save_local_name_button"),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Salvar nome local")
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
                                text = "Você ainda não publicou ocorrências com este identificador local. Toque em Reportar para enviar agora sem login.",
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
                        OutlinedButton(
                            onClick = { showResetDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("reset_local_identity_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Reiniciar identificador local")
                        }
                    }
                }
            }
        }
    }
}
