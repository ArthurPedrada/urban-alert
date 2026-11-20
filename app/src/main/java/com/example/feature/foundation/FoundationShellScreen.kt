package com.example.feature.foundation

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AccessibleForward
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.HighlightOff
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Signpost
import androidx.compose.material.icons.rounded.Traffic
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.config.PilotCityConfig
import com.example.core.config.SalvadorNeighborhoodPreset
import com.example.core.model.IncidentStatus
import com.example.core.model.PublicAgency
import com.example.core.model.Severity
import com.example.core.model.UrbanCategory
import com.example.ui.theme.BaiaTeal
import com.example.ui.theme.SeverityCriticalColor
import com.example.ui.theme.SeverityHighColor
import com.example.ui.theme.SeverityLowColor
import com.example.ui.theme.SeverityMediumColor
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.StatusInProgressColor
import com.example.ui.theme.StatusOpenColor
import com.example.ui.theme.StatusRejectedColor
import com.example.ui.theme.StatusResolvedColor
import com.example.ui.theme.StatusUnderReviewColor
import com.example.ui.theme.UrbanEmerald
import com.example.ui.theme.UrbanEmeraldDark
import com.example.ui.theme.categoryAccentColor

/**
 * Central Explorar Salvador: apresenta o panorama ilustrado da cidade-piloto,
 * atalhos para todas as funções (Reportar com IA, Fila Offline, Privacidade EXIF),
 * bairros rápidos, órgãos públicos oficiais (156 / CODESAL 199 / EMBASA) e catálogo urbano.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FoundationShellScreen(
    modifier: Modifier = Modifier,
    pendingOfflineCount: Int = 0,
    onOpenMap: () -> Unit = {},
    onOpenReport: () -> Unit = {},
    onOpenOfflineQueue: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
    onJumpToNeighborhood: (SalvadorNeighborhoodPreset) -> Unit = {},
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val categories = remember { PilotCityConfig.initialCategories }
    val neighborhoods = remember { PilotCityConfig.salvadorNeighborhoods }
    val agencies = remember { PilotCityConfig.salvadorAgencies }
    val pilotCity = remember { PilotCityConfig.salvador }
    var selectedCategory by remember { mutableStateOf(categories.first()) }

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
                .widthIn(max = 640.dp)
                .testTag("foundation_scroll_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                FoundationTopBar(
                    cityLabel = "${pilotCity.displayName}, ${pilotCity.stateCode}",
                )
            }

            item {
                CartographicHeroCard(
                    centerLat = pilotCity.center.latitude,
                    centerLng = pilotCity.center.longitude,
                    onOpenMap = onOpenMap,
                    onOpenReport = onOpenReport,
                )
            }

            item {
                QuickActionsHubSection(
                    pendingOfflineCount = pendingOfflineCount,
                    onOpenReport = onOpenReport,
                    onOpenOfflineQueue = onOpenOfflineQueue,
                    onOpenPrivacy = onOpenPrivacy,
                    onOpenMap = onOpenMap,
                )
            }

            item {
                NeighborhoodShortcutsSection(
                    neighborhoods = neighborhoods,
                    onSelectNeighborhood = onJumpToNeighborhood,
                )
            }

            item {
                SalvadorAgenciesDirectorySection(
                    agencies = agencies,
                    onOpenPortal = { url -> uriHandler.openUri(url) },
                    onCallPhone = { phone ->
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                        context.startActivity(intent)
                    },
                )
            }

            item {
                CategoryTaxonomySection(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onSelectCategory = { selectedCategory = it },
                )
            }

            item {
                HowItWorksSection()
            }

            item {
                SemanticReferenceSection()
            }
        }
    }
}

@Composable
private fun FoundationTopBar(cityLabel: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        brush = Brush.linearGradient(listOf(UrbanEmerald, BaiaTeal)),
                        shape = CircleShape,
                    ),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsActive,
                        contentDescription = stringResource(R.string.cd_brand_emblem),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Column {
                Text(
                    text = stringResource(R.string.foundation_brand_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = stringResource(R.string.foundation_brand_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.testTag("pilot_city_badge"),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = stringResource(R.string.cd_pilot_location),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = cityLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun CartographicHeroCard(
    centerLat: Double,
    centerLng: Double,
    onOpenMap: () -> Unit,
    onOpenReport: () -> Unit,
) {
    Card(
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("foundation_hero_card"),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.img_salvador_hero),
                contentDescription = stringResource(R.string.cd_pilot_location),
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                UrbanEmeraldDark.copy(alpha = 0.72f),
                                Color(0xFF0A2A24).copy(alpha = 0.90f),
                            ),
                        ),
                    ),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.20f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                ) {
                    Text(
                        text = stringResource(R.string.foundation_milestone_badge),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }

                Text(
                    text = stringResource(R.string.foundation_hero_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                )

                Text(
                    text = stringResource(R.string.foundation_hero_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.92f),
                )

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = Color.Black.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.20f)),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.foundation_stack_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                        )
                        Text(
                            text = "Coordenada base: $centerLat, $centerLng",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f),
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = onOpenReport,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UrbanEmeraldLightFallback,
                            contentColor = Color(0xFF062617),
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize(),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AddLocationAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reportar")
                    }
                    OutlinedButton(
                        onClick = onOpenMap,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.65f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize(),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Map,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ver mapa")
                    }
                }
            }
        }
    }
}

private val UrbanEmeraldLightFallback = Color(0xFF4BE09A)

@Composable
private fun QuickActionsHubSection(
    pendingOfflineCount: Int,
    onOpenReport: () -> Unit,
    onOpenOfflineQueue: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenMap: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Todas as funções do app",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            QuickHubCard(
                title = "Assistente IA",
                subtitle = "Foto + Gemini",
                icon = Icons.Rounded.AutoAwesome,
                accent = UrbanEmerald,
                onClick = onOpenReport,
                modifier = Modifier.weight(1f),
            )
            QuickHubCard(
                title = "Fila Offline",
                subtitle = if (pendingOfflineCount > 0) {
                    "$pendingOfflineCount pendente(s)"
                } else {
                    "Rascunhos locais"
                },
                icon = Icons.Rounded.CloudQueue,
                accent = BaiaTeal,
                badgeCount = pendingOfflineCount,
                onClick = onOpenOfflineQueue,
                modifier = Modifier
                    .weight(1f)
                    .testTag("explore_open_offline_queue"),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            QuickHubCard(
                title = "Mapa ao vivo",
                subtitle = "Ocorrências e filtros",
                icon = Icons.Rounded.Map,
                accent = Color(0xFF0E86D4),
                onClick = onOpenMap,
                modifier = Modifier.weight(1f),
            )
            QuickHubCard(
                title = "Privacidade EXIF",
                subtitle = "Sanitizador LGPD",
                icon = Icons.Rounded.Shield,
                accent = SolarAmber,
                onClick = onOpenPrivacy,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun QuickHubCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0,
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.28f)),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accent.copy(alpha = 0.14f),
                    modifier = Modifier.size(38.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                if (badgeCount > 0) {
                    Badge(
                        containerColor = accent,
                        contentColor = Color.White,
                    ) {
                        Text("$badgeCount")
                    }
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun NeighborhoodShortcutsSection(
    neighborhoods: List<SalvadorNeighborhoodPreset>,
    onSelectNeighborhood: (SalvadorNeighborhoodPreset) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Explorar bairros de Salvador",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(neighborhoods, key = { it.id }) { region ->
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clickable { onSelectNeighborhood(region) },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = BaiaTeal,
                            modifier = Modifier.size(20.dp),
                        )
                        Column {
                            Text(
                                text = region.name,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = region.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SalvadorAgenciesDirectorySection(
    agencies: List<PublicAgency>,
    onOpenPortal: (String) -> Unit,
    onCallPhone: (String) -> Unit,
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Canais públicos de Salvador",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Diretório oficial para acionamento direto. Registrar aqui no app não abre protocolo automático na prefeitura.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            agencies.forEach { agency ->
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "${agency.acronym} · ${agency.name}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = agency.competenceSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            val phone = agency.officialPhone
                            if (!phone.isNullOrBlank()) {
                                OutlinedButton(
                                    onClick = { onCallPhone(phone) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .minimumInteractiveComponentSize(),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Call,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ligar $phone")
                                }
                            }
                            val portalUrl = agency.officialPortalUrl
                            if (!portalUrl.isNullOrBlank()) {
                                Button(
                                    onClick = { onOpenPortal(portalUrl) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BaiaTeal,
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .minimumInteractiveComponentSize(),
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Portal")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryTaxonomySection(
    categories: List<UrbanCategory>,
    selectedCategory: UrbanCategory,
    onSelectCategory: (UrbanCategory) -> Unit,
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.foundation_categories_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.foundation_categories_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                categories.forEach { category ->
                    val isSelected = category.id == selectedCategory.id
                    val accent = categoryAccentColor(category.slug)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectCategory(category) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("category_chip_${category.slug}"),
                        leadingIcon = {
                            Icon(
                                imageVector = categoryIcon(category.slug),
                                contentDescription = stringResource(R.string.cd_category_icon),
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        label = {
                            Text(
                                text = category.title,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            iconColor = accent,
                            selectedContainerColor = accent.copy(alpha = 0.16f),
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                            selectedLeadingIconColor = accent,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            selectedBorderColor = accent.copy(alpha = 0.6f),
                            selectedBorderWidth = 1.5.dp,
                        ),
                    )
                }
            }

            val selectedAccent = categoryAccentColor(selectedCategory.slug)
            AnimatedVisibility(visible = true) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = selectedAccent.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, selectedAccent.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("selected_category_detail"),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = categoryIcon(selectedCategory.slug),
                                contentDescription = stringResource(R.string.cd_category_icon),
                                tint = selectedAccent,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                text = selectedCategory.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Text(
                            text = selectedCategory.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "Gravidade típica: ${
                                stringResource(selectedCategory.defaultSeverityHint.labelRes)
                            }",
                            style = MaterialTheme.typography.labelMedium,
                            color = selectedAccent,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HowItWorksSection() {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("roadmap_section_card"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Como colaborar em 3 passos",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            HowItWorksRow(
                step = "1",
                title = "Registre com privacidade",
                body = "Tire ou escolha uma foto: os metadados EXIF e GPS pessoais são removidos no seu aparelho.",
                accent = UrbanEmerald,
            )
            HowItWorksRow(
                step = "2",
                title = "IA Gemini sugere, você decide",
                body = "A IA sugere categoria, gravidade e resumo, com edição manual obrigatória antes de publicar.",
                accent = BaiaTeal,
            )
            HowItWorksRow(
                step = "3",
                title = "Confirmação comunitária e modo offline",
                body = "Sem internet? Salve na fila offline. No mapa, cidadãos confirmam se o problema ainda existe.",
                accent = SolarAmber,
            )
        }
    }
}

@Composable
private fun HowItWorksRow(
    step: String,
    title: String,
    body: String,
    accent: Color,
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = accent.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.25f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = accent,
                modifier = Modifier.size(26.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = step,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SemanticReferenceSection() {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.foundation_semantics_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.foundation_semantics_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = stringResource(R.string.foundation_severity_group),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Severity.entries.forEach { severity ->
                    SemanticPill(
                        label = stringResource(severity.labelRes),
                        icon = severityIcon(severity),
                        accent = severityColor(severity),
                        contentDescription = stringResource(R.string.cd_severity_icon),
                    )
                }
            }

            Text(
                text = stringResource(R.string.foundation_status_group),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IncidentStatus.entries.forEach { status ->
                    SemanticPill(
                        label = stringResource(status.labelRes),
                        icon = statusIcon(status),
                        accent = statusColor(status),
                        contentDescription = stringResource(R.string.cd_status_icon),
                    )
                }
            }
        }
    }
}

@Composable
private fun SemanticPill(
    label: String,
    icon: ImageVector,
    accent: Color,
    contentDescription: String,
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = accent.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = accent,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private fun categoryIcon(slug: String): ImageVector = when (slug) {
    "buracos-em-vias" -> Icons.Rounded.Construction
    "acumulo-de-lixo" -> Icons.Rounded.DeleteOutline
    "iluminacao-publica" -> Icons.Rounded.Lightbulb
    "vazamentos" -> Icons.Rounded.WaterDrop
    "alagamentos" -> Icons.Rounded.Waves
    "problemas-de-drenagem" -> Icons.Rounded.WaterDrop
    "sinalizacao-danificada" -> Icons.Rounded.Signpost
    "semaforos-quebrados" -> Icons.Rounded.Traffic
    "barreiras-de-acessibilidade" -> Icons.Rounded.AccessibleForward
    else -> Icons.Rounded.Info
}

private fun severityIcon(severity: Severity): ImageVector = when (severity) {
    Severity.LOW -> Icons.Rounded.Info
    Severity.MEDIUM -> Icons.Rounded.WarningAmber
    Severity.HIGH -> Icons.Rounded.ReportProblem
    Severity.CRITICAL -> Icons.Rounded.ErrorOutline
}

private fun severityColor(severity: Severity): Color = when (severity) {
    Severity.LOW -> SeverityLowColor
    Severity.MEDIUM -> SeverityMediumColor
    Severity.HIGH -> SeverityHighColor
    Severity.CRITICAL -> SeverityCriticalColor
}

private fun statusIcon(status: IncidentStatus): ImageVector = when (status) {
    IncidentStatus.OPEN -> Icons.Rounded.RadioButtonChecked
    IncidentStatus.UNDER_REVIEW -> Icons.Rounded.FactCheck
    IncidentStatus.IN_PROGRESS -> Icons.Rounded.HourglassTop
    IncidentStatus.RESOLVED -> Icons.Rounded.CheckCircle
    IncidentStatus.REJECTED -> Icons.Rounded.HighlightOff
}

private fun statusColor(status: IncidentStatus): Color = when (status) {
    IncidentStatus.OPEN -> StatusOpenColor
    IncidentStatus.UNDER_REVIEW -> StatusUnderReviewColor
    IncidentStatus.IN_PROGRESS -> StatusInProgressColor
    IncidentStatus.RESOLVED -> StatusResolvedColor
    IncidentStatus.REJECTED -> StatusRejectedColor
}
