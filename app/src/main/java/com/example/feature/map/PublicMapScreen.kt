package com.example.feature.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.LocationSearching
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ThumbUpOffAlt
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.config.PilotCityConfig
import com.example.core.model.GeoBoundingBox
import com.example.core.model.IncidentStatus
import com.example.core.model.PublicAgency
import com.example.core.model.Severity
import com.example.data.incidents.IncidentDocument
import com.example.feature.incidents.DetailBadge
import com.example.feature.incidents.severityAccent
import com.example.feature.incidents.severityVector
import com.example.feature.incidents.statusAccent
import com.example.feature.incidents.statusVector
import com.example.ui.theme.BaiaTeal
import com.example.ui.theme.CartographicGridLineLight
import com.example.ui.theme.CartographicSage
import com.example.ui.theme.SeverityCriticalColor
import com.example.ui.theme.SeverityHighColor
import com.example.ui.theme.SeverityLowColor
import com.example.ui.theme.SeverityMediumColor
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.UrbanEmerald
import com.example.ui.theme.UrbanEmeraldDark
import com.example.ui.theme.UrbanGraphite
import com.example.ui.theme.categoryAccentColor
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PublicMapScreen(
    viewModel: MapViewModel,
    onOpenIncidentDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    onOpenReport: () -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val focusManager = LocalFocusManager.current
    val uriHandler = LocalUriHandler.current

    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val contentState by viewModel.mapContentState.collectAsStateWithLifecycle()
    val areaInsight by viewModel.localAreaInsight.collectAsStateWithLifecycle()
    val selectedIncident by viewModel.selectedIncident.collectAsStateWithLifecycle()
    val isBusyLocationOrSearch by viewModel.isBusyLocationOrSearch.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showFiltersPanel by rememberSaveable { mutableStateOf(false) }
    var showAreaListSheet by rememberSaveable { mutableStateOf(false) }
    var showLocalityPanoramaSheet by rememberSaveable { mutableStateOf(false) }
    var permissionDeniedNote by rememberSaveable { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            permissionDeniedNote = false
            viewModel.moveToCurrentDeviceLocation(context)
        } else {
            permissionDeniedNote = true
        }
    }

    // Configura cache privado do OSMDroid sem permissões externas
    val mapView = remember(context) {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            osmdroidBasePath = File(context.cacheDir, "osmdroid").apply { mkdirs() }
            osmdroidTileCache = File(context.cacheDir, "osmdroid/tiles").apply { mkdirs() }
        }
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            minZoomLevel = 4.0
            maxZoomLevel = 19.5
            overlayManager.tilesOverlay.loadingBackgroundColor = CartographicSage.toArgb()
            overlayManager.tilesOverlay.loadingLineColor = CartographicGridLineLight.toArgb()
            controller.setZoom(viewModel.lastMapZoom)
            controller.setCenter(
                GeoPoint(
                    viewModel.lastMapCenter.latitude,
                    viewModel.lastMapCenter.longitude,
                ),
            )
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        val listener = object : MapListener {
            private fun handleViewportChange() {
                if (mapView.width <= 0 || mapView.height <= 0) return
                val box = mapView.boundingBox
                val latSpan = kotlin.math.abs(box.latNorth - box.latSouth)
                val lonSpan = kotlin.math.abs(box.lonEast - box.lonWest)
                if (latSpan < 0.0005 || lonSpan < 0.0005) return
                val bounds = GeoBoundingBox(
                    north = box.latNorth,
                    south = box.latSouth,
                    east = box.lonEast,
                    west = box.lonWest,
                )
                viewModel.updateViewport(
                    bounds = bounds,
                    zoomLevel = mapView.zoomLevelDouble,
                )
                viewModel.refreshLocalityForBounds(context, bounds)
            }

            override fun onScroll(event: ScrollEvent?): Boolean {
                handleViewportChange()
                return false
            }

            override fun onZoom(event: ZoomEvent?): Boolean {
                handleViewportChange()
                return false
            }
        }
        mapView.addMapListener(listener)
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.removeMapListener(listener)
            mapView.onDetach()
        }
    }

    val pendingCameraTarget by viewModel.cameraTarget.collectAsStateWithLifecycle()
    LaunchedEffect(pendingCameraTarget) {
        val target = pendingCameraTarget ?: return@LaunchedEffect
        mapView.controller.animateTo(
            GeoPoint(target.first.latitude, target.first.longitude),
            target.second,
            450L,
        )
        viewModel.consumeCameraTarget()
    }

    // Atualiza marcadores e clusters no MapView sempre que os nós renderizáveis mudarem
    LaunchedEffect(contentState) {
        val nodes = (contentState as? MapContentState.Ready)?.renderNodes ?: emptyList()
        mapView.overlays.clear()
        nodes.forEach { node ->
            when (node) {
                is MapRenderNode.SinglePin -> {
                    val marker = Marker(mapView).apply {
                        position = GeoPoint(node.incident.latitude, node.incident.longitude)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = node.incident.title
                        icon = createSinglePinDrawable(
                            context = context,
                            severity = node.incident.resolvedSeverity(),
                        )
                        setOnMarkerClickListener { _, _ ->
                            viewModel.selectIncident(node.incident)
                            mapView.controller.animateTo(position)
                            true
                        }
                    }
                    mapView.overlays.add(marker)
                }
                is MapRenderNode.ClusterPin -> {
                    val marker = Marker(mapView).apply {
                        position = GeoPoint(node.center.latitude, node.center.longitude)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        title = "${node.count} ocorrências"
                        icon = createClusterDrawable(
                            context = context,
                            count = node.count,
                            highestSeverity = node.highestSeverity,
                        )
                        setOnMarkerClickListener { _, _ ->
                            val nextZoom = (mapView.zoomLevelDouble + 2.0).coerceAtMost(17.0)
                            mapView.controller.animateTo(position, nextZoom, 450L)
                            true
                        }
                    }
                    mapView.overlays.add(marker)
                }
            }
        }
        mapView.invalidate()
    }

    val visibleIncidents = (contentState as? MapContentState.Ready)?.incidents.orEmpty()

    if (showLocalityPanoramaSheet) {
        LocalityPanoramaSheet(
            insight = areaInsight,
            onDismiss = { showLocalityPanoramaSheet = false },
            onSelectIncident = { incident ->
                showLocalityPanoramaSheet = false
                viewModel.selectIncident(incident)
                onOpenIncidentDetail(incident.id)
            },
            onOpenPortal = { url -> uriHandler.openUri(url) },
            onCallPhone = { phone ->
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                context.startActivity(dialIntent)
            },
        )
    }

    if (showAreaListSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAreaListSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.map_area_list_title, visibleIncidents.size),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (visibleIncidents.isEmpty()) {
                    Text(
                        text = stringResource(R.string.map_empty_area_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 24.dp),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(340.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                    ) {
                        items(visibleIncidents, key = { it.id }) { incident ->
                            IncidentPreviewRow(
                                incident = incident,
                                onClick = {
                                    showAreaListSheet = false
                                    viewModel.selectIncident(incident)
                                    onOpenIncidentDetail(incident.id)
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Mapa nativo em tela cheia
        AndroidView(
            factory = { mapView },
            modifier = Modifier
                .fillMaxSize()
                .testTag("salvador_public_map"),
        )

        // 2. Painel flutuante superior (busca + GPS + filtros)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.statusBars)
                .widthIn(max = 640.dp)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
                tonalElevation = 6.dp,
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 6.dp, top = 2.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.map_search_placeholder),
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = stringResource(R.string.cd_search_location),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = stringResource(R.string.cd_clear_search),
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                focusManager.clearFocus()
                                viewModel.searchLocationInSalvador(context, searchQuery)
                            },
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("map_search_input"),
                    )

                    val activeFilterCount = listOfNotNull(
                        filterState.categorySlug,
                        filterState.statusFilter,
                    ).size

                    FilledTonalIconButton(
                        onClick = { showFiltersPanel = !showFiltersPanel },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("map_toggle_filters_button"),
                    ) {
                        BadgedBox(
                            badge = {
                                if (activeFilterCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary,
                                    ) {
                                        Text("$activeFilterCount")
                                    }
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FilterList,
                                contentDescription = stringResource(R.string.cd_toggle_filters),
                                tint = if (activeFilterCount > 0) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                        }
                    }

                    FilledTonalIconButton(
                        onClick = {
                            val hasFine = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                            ) == PackageManager.PERMISSION_GRANTED
                            val hasCoarse = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasFine || hasCoarse) {
                                viewModel.moveToCurrentDeviceLocation(context)
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                    ),
                                )
                            }
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("map_my_location_button"),
                    ) {
                        if (isBusyLocationOrSearch) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.MyLocation,
                                contentDescription = stringResource(R.string.cd_my_location),
                                tint = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }
            }

            LiveLocalityPulseCard(
                insight = areaInsight,
                isLoading = contentState is MapContentState.Loading,
                onClick = { showLocalityPanoramaSheet = true },
            )

            AnimatedVisibility(visible = showFiltersPanel) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    tonalElevation = 4.dp,
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.map_filters_title),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            if (filterState.categorySlug != null || filterState.statusFilter != null) {
                                TextButton(onClick = { viewModel.clearAllFilters() }) {
                                    Text(stringResource(R.string.map_clear_filters))
                                }
                            }
                        }

                        Text(
                            text = "Ir rápido para bairro",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(PilotCityConfig.salvadorNeighborhoods, key = { it.id }) { region ->
                                AssistChip(
                                    onClick = {
                                        viewModel.focusNeighborhood(
                                            name = region.name,
                                            coordinate = region.coordinate,
                                            zoom = region.zoom,
                                        )
                                        showFiltersPanel = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    },
                                    label = { Text(region.name) },
                                )
                            }
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(PilotCityConfig.initialCategories, key = { it.slug }) { category ->
                                val selected = filterState.categorySlug == category.slug
                                val catColor = categoryAccentColor(category.slug)
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.selectCategoryFilter(category.slug) },
                                    label = { Text(category.title) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = catColor.copy(alpha = 0.16f),
                                        selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selected,
                                        selectedBorderColor = catColor,
                                    ),
                                )
                            }
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(
                                listOf(
                                    IncidentStatus.OPEN,
                                    IncidentStatus.UNDER_REVIEW,
                                    IncidentStatus.IN_PROGRESS,
                                    IncidentStatus.RESOLVED,
                                ),
                            ) { status ->
                                val selected = filterState.statusFilter == status
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.selectStatusFilter(status) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = statusVector(status),
                                            contentDescription = stringResource(R.string.cd_status_icon),
                                            modifier = Modifier.size(16.dp),
                                        )
                                    },
                                    label = { Text(stringResource(status.labelRes)) },
                                )
                            }
                        }
                    }
                }
            }

            if (permissionDeniedNote) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.map_location_permission_denied),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = { permissionDeniedNote = false }) {
                            Text("OK")
                        }
                    }
                }
            }

            val banner = statusBanner
            if (banner != null) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    border = BorderStroke(
                        1.dp,
                        if (banner.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = banner.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (banner.isError) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { viewModel.clearBanner() }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.cd_clear_search),
                            )
                        }
                    }
                }
            }
        }

        // 3. Controles laterais direitos de zoom e retorno a Salvador
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 4.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                IconButton(
                    onClick = { mapView.controller.zoomIn() },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("map_zoom_in_button"),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.cd_zoom_in),
                    )
                }
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 4.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                IconButton(
                    onClick = { mapView.controller.zoomOut() },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("map_zoom_out_button"),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Remove,
                        contentDescription = stringResource(R.string.cd_zoom_out),
                    )
                }
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.95f),
                shadowElevation = 4.dp,
            ) {
                IconButton(
                    onClick = { viewModel.recenterOnSalvador() },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("map_recenter_salvador_button"),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationSearching,
                        contentDescription = stringResource(R.string.cd_recenter_salvador),
                        tint = MaterialTheme.colorScheme.onSecondary,
                    )
                }
            }
        }

        // 4. Painel flutuante inferior (FAB Reportar + resumo da área ou ocorrência selecionada)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .widthIn(max = 640.dp)
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val activeIncident = selectedIncident
            if (activeIncident == null) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    ExtendedFloatingActionButton(
                        onClick = onOpenReport,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.AddLocationAlt,
                                contentDescription = null,
                            )
                        },
                        text = {
                            Text(
                                text = stringResource(R.string.map_fab_report),
                                fontWeight = FontWeight.SemiBold,
                            )
                        },
                        modifier = Modifier.testTag("map_fab_report"),
                    )
                }
            }
            if (activeIncident != null) {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("selected_incident_card"),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = activeIncident.categoryTitle(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            IconButton(
                                onClick = { viewModel.selectIncident(null) },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = stringResource(R.string.cd_clear_search),
                                )
                            }
                        }

                        Text(
                            text = activeIncident.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            val severity = activeIncident.resolvedSeverity()
                            val status = activeIncident.resolvedStatus()
                            DetailBadge(
                                label = stringResource(severity.labelRes),
                                icon = severityVector(severity),
                                accent = severityAccent(severity),
                                contentDescription = stringResource(R.string.cd_severity_icon),
                            )
                            DetailBadge(
                                label = stringResource(status.labelRes),
                                icon = statusVector(status),
                                accent = statusAccent(status),
                                contentDescription = stringResource(R.string.cd_status_icon),
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ThumbUpOffAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = "${activeIncident.stillExistsCount} confirmações · ${activeIncident.elapsedLabel()}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            Button(
                                onClick = { onOpenIncidentDetail(activeIncident.id) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("open_incident_detail_button"),
                            ) {
                                Text(stringResource(R.string.map_view_details_button))
                            }
                        }
                    }
                }
            } else {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("map_area_summary_bar"),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                when (val current = contentState) {
                                    MapContentState.Loading -> {
                                        Text(
                                            text = stringResource(R.string.map_loading_area),
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                    }
                                    is MapContentState.Error -> {
                                        Text(
                                            text = current.message,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                    is MapContentState.Ready -> {
                                        Text(
                                            text = if (current.incidents.isEmpty()) {
                                                stringResource(R.string.map_empty_area_title)
                                            } else {
                                                stringResource(
                                                    R.string.map_area_count_title,
                                                    current.incidents.size,
                                                )
                                            },
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            text = if (current.incidents.isEmpty()) {
                                                stringResource(R.string.map_empty_area_subtitle)
                                            } else {
                                                stringResource(R.string.map_tap_pin_hint)
                                            },
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilledTonalButton(
                                    onClick = { showLocalityPanoramaSheet = true },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("map_open_panorama_button"),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Analytics,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Panorama")
                                }

                                Button(
                                    onClick = { showAreaListSheet = true },
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("map_area_list_button"),
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.FormatListBulleted,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.map_list_button))
                                }
                            }
                        }

                        val latest = areaInsight.latestIncident
                        if (latest != null) {
                            val catColor = categoryAccentColor(latest.categorySlug)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = catColor.copy(alpha = 0.10f),
                                border = BorderStroke(1.dp, catColor.copy(alpha = 0.30f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectIncident(latest)
                                        onOpenIncidentDetail(latest.id)
                                    },
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Schedule,
                                        contentDescription = null,
                                        tint = catColor,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        text = "Mais recente: ${latest.title} · ${latest.elapsedLabel()}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text(
                                        text = latest.categoryTitle(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = catColor,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveLocalityPulseCard(
    insight: LocalAreaInsight,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (insight.isUrgentSituation) {
        SeverityHighColor.copy(alpha = 0.50f)
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("map_live_locality_card"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (insight.isUrgentSituation) SeverityHighColor else UrbanEmerald,
                        ),
                )
                Text(
                    text = insight.localityTitle,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (isLoading) {
                    Text(
                        text = "Atualizando...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    LiveMetricChip(
                        label = "${insight.totalActive} ativas",
                        accent = BaiaTeal,
                    )
                    if (insight.urgentCount > 0) {
                        LiveMetricChip(
                            label = "${insight.urgentCount} urg.",
                            accent = SeverityHighColor,
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "Abrir panorama da localidade",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun LiveMetricChip(
    label: String,
    accent: Color,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = accent.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.30f)),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = accent,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun LocalityPanoramaSheet(
    insight: LocalAreaInsight,
    onDismiss: () -> Unit,
    onSelectIncident: (IncidentDocument) -> Unit,
    onOpenPortal: (String) -> Unit,
    onCallPhone: (String) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .background(
                            brush = Brush.linearGradient(
                                colors = if (insight.isUrgentSituation) {
                                    listOf(SeverityCriticalColor, SeverityHighColor, SolarAmber)
                                } else {
                                    listOf(UrbanEmeraldDark, UrbanEmerald, BaiaTeal)
                                },
                            ),
                        )
                        .padding(18.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Panorama ao vivo · ${insight.localityTitle}",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = insight.situationHeadline,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.92f),
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            PanoramaWhitePill("${insight.totalActive} ativas")
                            PanoramaWhitePill("${insight.urgentCount} urgentes")
                            PanoramaWhitePill("${insight.recent24hCount} nas últimas 24h")
                            PanoramaWhitePill("${insight.resolvedCount} resolvidas")
                        }
                    }
                }
            }

            if (insight.topCategories.isNotEmpty()) {
                item {
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                text = "O que mais acontece aqui agora",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            insight.topCategories.forEach { cat ->
                                val barColor = categoryAccentColor(cat.slug)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            text = cat.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            text = "${cat.count} ocorrência(s)",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = barColor,
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { cat.share },
                                        color = barColor,
                                        trackColor = barColor.copy(alpha = 0.16f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "Canais úteis para esta região",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        insight.relevantAgencies.forEach { agency ->
                            PanoramaAgencyRow(
                                agency = agency,
                                onOpenPortal = onOpenPortal,
                                onCallPhone = onCallPhone,
                            )
                        }
                    }
                }
            }

            if (insight.sortedRecent.isNotEmpty()) {
                item {
                    Text(
                        text = "Registros recentes na área (${insight.sortedRecent.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                items(insight.sortedRecent.take(8), key = { it.id }) { incident ->
                    IncidentPreviewRow(
                        incident = incident,
                        onClick = { onSelectIncident(incident) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PanoramaWhitePill(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.22f),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun PanoramaAgencyRow(
    agency: PublicAgency,
    onOpenPortal: (String) -> Unit,
    onCallPhone: (String) -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
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
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
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
                            containerColor = MaterialTheme.colorScheme.secondary,
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

@Composable
private fun IncidentPreviewRow(
    incident: IncidentDocument,
    onClick: () -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = incident.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${incident.categoryTitle()} · ${incident.elapsedLabel()}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val severity = incident.resolvedSeverity()
                val status = incident.resolvedStatus()
                DetailBadge(
                    label = stringResource(severity.labelRes),
                    icon = severityVector(severity),
                    accent = severityAccent(severity),
                    contentDescription = stringResource(R.string.cd_severity_icon),
                )
                DetailBadge(
                    label = stringResource(status.labelRes),
                    icon = statusVector(status),
                    accent = statusAccent(status),
                    contentDescription = stringResource(R.string.cd_status_icon),
                )
            }
        }
    }
}

private fun createSinglePinDrawable(
    context: Context,
    severity: Severity,
): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val widthPx = (38 * density).toInt().coerceAtLeast(52)
    val heightPx = (46 * density).toInt().coerceAtLeast(62)
    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val fillColor = when (severity) {
        Severity.LOW -> SeverityLowColor.toArgb()
        Severity.MEDIUM -> SeverityMediumColor.toArgb()
        Severity.HIGH -> SeverityHighColor.toArgb()
        Severity.CRITICAL -> SeverityCriticalColor.toArgb()
    }
    val cx = widthPx / 2f
    val cy = widthPx * 0.46f
    val radius = widthPx * 0.36f
    val tipY = heightPx - 3f

    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(65, 16, 28, 32)
        style = Paint.Style.FILL
    }
    canvas.drawOval(
        cx - radius * 0.55f,
        tipY - 4f,
        cx + radius * 0.55f,
        tipY + 2f,
        shadowPaint,
    )

    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = fillColor
        style = Paint.Style.FILL
    }
    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }

    val outerPath = Path().apply {
        addCircle(cx, cy, radius + 3f, Path.Direction.CW)
        moveTo(cx - (radius * 0.72f), cy + (radius * 0.55f))
        lineTo(cx, tipY)
        lineTo(cx + (radius * 0.72f), cy + (radius * 0.55f))
        close()
    }
    canvas.drawPath(outerPath, borderPaint)

    val innerPath = Path().apply {
        addCircle(cx, cy, radius, Path.Direction.CW)
        moveTo(cx - (radius * 0.62f), cy + (radius * 0.48f))
        lineTo(cx, tipY - 3f)
        lineTo(cx + (radius * 0.62f), cy + (radius * 0.48f))
        close()
    }
    canvas.drawPath(innerPath, pinPaint)
    canvas.drawCircle(cx, cy, radius * 0.34f, dotPaint)
    return BitmapDrawable(context.resources, bitmap)
}

private fun createClusterDrawable(
    context: Context,
    count: Int,
    highestSeverity: Severity,
): BitmapDrawable {
    val sizePx = (48 * context.resources.displayMetrics.density).toInt().coerceAtLeast(68)
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val ringColor = when (highestSeverity) {
        Severity.CRITICAL -> SeverityCriticalColor.toArgb()
        Severity.HIGH -> SeverityHighColor.toArgb()
        Severity.MEDIUM -> SeverityMediumColor.toArgb()
        Severity.LOW -> BaiaTeal.toArgb()
    }
    val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(65, android.graphics.Color.red(ringColor), android.graphics.Color.green(ringColor), android.graphics.Color.blue(ringColor))
        style = Paint.Style.FILL
    }
    val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ringColor
        style = Paint.Style.FILL
    }
    val whiteRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = UrbanGraphite.toArgb()
        style = Paint.Style.FILL
    }
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = sizePx * 0.32f
        typeface = Typeface.DEFAULT_BOLD
    }
    val center = sizePx / 2f
    canvas.drawCircle(center, center, center - 1f, haloPaint)
    canvas.drawCircle(center, center, center * 0.85f, whiteRingPaint)
    canvas.drawCircle(center, center, center * 0.78f, outerPaint)
    canvas.drawCircle(center, center, center * 0.62f, innerPaint)
    val textY = center - (textPaint.descent() + textPaint.ascent()) / 2f
    canvas.drawText(count.toString(), center, textY, textPaint)
    return BitmapDrawable(context.resources, bitmap)
}
