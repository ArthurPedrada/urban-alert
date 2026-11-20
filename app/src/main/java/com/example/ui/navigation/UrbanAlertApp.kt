package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.data.admin.AdminRepository
import com.example.data.agencies.AgencyRepository
import com.example.data.auth.AuthRepository
import com.example.data.auth.CitizenSessionStore
import com.example.data.community.CommunityRepository
import com.example.data.incidents.IncidentRepository
import com.example.data.local.LocalOfflineRepository
import com.example.data.local.UrbanAlertDatabase
import com.example.data.profile.ProfileRepository
import com.example.data.sync.ConnectivityMonitor
import com.example.data.sync.SyncEngine
import com.example.feature.admin.AdminDashboardScreen
import com.example.feature.admin.AdminViewModel
import com.example.feature.auth.DirectCitizenAccountScreen
import com.example.feature.auth.ProfileScreen
import com.example.feature.auth.ProfileViewModel
import com.example.feature.foundation.FoundationShellScreen
import com.example.feature.incidents.IncidentDetailScreen
import com.example.feature.map.MapViewModel
import com.example.feature.map.PublicMapScreen
import com.example.feature.offline.OfflineQueueScreen
import com.example.feature.privacy.PrivacyAndMediaSafetyScreen
import com.example.feature.report.ReportFlowScreen
import com.example.feature.report.ReportViewModel
import com.google.firebase.firestore.FirebaseFirestore

private enum class HomeTab {
    MAP,
    PROJECT,
    PRIVACY,
    ACCOUNT,
}

@Composable
fun UrbanAlertApp(
    authRepository: AuthRepository = remember { AuthRepository() },
) {
    val context = LocalContext.current
    val citizenSessionStore = remember(context) { CitizenSessionStore(context) }
    val citizenSession by citizenSessionStore.session.collectAsStateWithLifecycle()
    val currentUser by authRepository.authStateFlow()
        .collectAsStateWithLifecycle(initialValue = authRepository.currentUser())
    val effectiveAuthorId = currentUser?.uid ?: citizenSession.guestId

    var selectedTab by rememberSaveable { mutableStateOf(HomeTab.MAP) }
    var selectedDetailIncidentId by rememberSaveable { mutableStateOf<String?>(null) }
    var isReportFlowOpen by rememberSaveable { mutableStateOf(false) }
    var isOfflineQueueOpen by rememberSaveable { mutableStateOf(false) }
    var isAdminDashboardOpen by rememberSaveable { mutableStateOf(false) }

    val incidentRepository = remember(context) { IncidentRepository(context) }
    val communityRepository = remember(context) { CommunityRepository(context) }
    val agencyRepository = remember(context) { AgencyRepository(context) }
    val adminRepository = remember(context) { AdminRepository(context) }
    val adminInfoFlow = remember(currentUser?.uid) {
        adminRepository.observeAdminAccess(currentUser?.uid)
    }
    val adminInfo by adminInfoFlow.collectAsStateWithLifecycle(initialValue = null)
    val reportViewModel: ReportViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY ausente em CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                val remoteRepo = IncidentRepository(db)
                val offlineRepo = LocalOfflineRepository(UrbanAlertDatabase.getInstance(app))
                val syncEngine = SyncEngine(offlineRepo, remoteRepo)
                val connectivityMonitor = ConnectivityMonitor(app)
                ReportViewModel(
                    incidentRepository = remoteRepo,
                    offlineRepository = offlineRepo,
                    syncEngine = syncEngine,
                    connectivityMonitor = connectivityMonitor,
                )
            }
        },
    )

    val isOnline by reportViewModel.isOnline.collectAsStateWithLifecycle()
    val pendingSyncCount by reportViewModel.pendingSyncCount.collectAsStateWithLifecycle()
    val mapViewModel: MapViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY ausente em CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                MapViewModel(IncidentRepository(db))
            }
        },
    )

    LaunchedEffect(isOnline, effectiveAuthorId) {
        if (isOnline && effectiveAuthorId.isNotBlank()) {
            reportViewModel.syncPendingNow(authorUid = effectiveAuthorId, forceRetryAll = true)
        }
    }

    val currentAdmin = adminInfo
    if (isAdminDashboardOpen && currentAdmin != null) {
        val adminViewModel: AdminViewModel = viewModel(
            key = "admin_${currentAdmin.uid}",
            factory = viewModelFactory {
                initializer {
                    AdminViewModel(
                        adminRepository = adminRepository,
                        adminUid = currentAdmin.uid,
                    )
                }
            },
        )
        AdminDashboardScreen(
            adminInfo = currentAdmin,
            viewModel = adminViewModel,
            onBack = { isAdminDashboardOpen = false },
        )
        return
    }

    val detailId = selectedDetailIncidentId
    if (detailId != null) {
        IncidentDetailScreen(
            incidentId = detailId,
            incidentRepository = incidentRepository,
            communityRepository = communityRepository,
            agencyRepository = agencyRepository,
            currentUserId = effectiveAuthorId,
            onRequestSignIn = {
                selectedDetailIncidentId = null
                selectedTab = HomeTab.ACCOUNT
            },
            onBack = { selectedDetailIncidentId = null },
            authRepository = authRepository,
        )
        return
    }

    if (isOfflineQueueOpen) {
        OfflineQueueScreen(
            viewModel = reportViewModel,
            currentUserId = effectiveAuthorId,
            onBack = { isOfflineQueueOpen = false },
        )
        return
    }

    if (isReportFlowOpen) {
        ReportFlowScreen(
            viewModel = reportViewModel,
            isSignedIn = true,
            currentUserId = effectiveAuthorId,
            onRequestSignIn = {
                isReportFlowOpen = false
                selectedTab = HomeTab.ACCOUNT
            },
            onOpenOfflineQueue = { isOfflineQueueOpen = true },
            onExitToMap = {
                reportViewModel.consumeLastPublishedLocation()?.let { (coord, label) ->
                    mapViewModel.focusCoordinate(coordinate = coord, localityLabel = label)
                }
                isReportFlowOpen = false
                selectedTab = HomeTab.MAP
            },
            authRepository = authRepository,
        )
        return
    }

    if (selectedTab != HomeTab.MAP) {
        BackHandler { selectedTab = HomeTab.MAP }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            val navColors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
            ) {
                NavigationBarItem(
                    selected = selectedTab == HomeTab.MAP,
                    onClick = { selectedTab = HomeTab.MAP },
                    colors = navColors,
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == HomeTab.MAP) {
                                Icons.Rounded.Map
                            } else {
                                Icons.Outlined.Map
                            },
                            contentDescription = stringResource(R.string.cd_nav_map),
                        )
                    },
                    label = { Text(stringResource(R.string.nav_map)) },
                    modifier = Modifier.testTag("nav_tab_map"),
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { isReportFlowOpen = true },
                    colors = navColors,
                    icon = {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(34.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.AddCircle,
                                    contentDescription = stringResource(R.string.cd_nav_report),
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.nav_report),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                    modifier = Modifier.testTag("nav_tab_report"),
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.PROJECT,
                    onClick = { selectedTab = HomeTab.PROJECT },
                    colors = navColors,
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == HomeTab.PROJECT) {
                                Icons.Rounded.Explore
                            } else {
                                Icons.Outlined.Explore
                            },
                            contentDescription = stringResource(R.string.cd_nav_explore),
                        )
                    },
                    label = { Text(stringResource(R.string.nav_explore)) },
                    modifier = Modifier.testTag("nav_tab_explore"),
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.PRIVACY,
                    onClick = { selectedTab = HomeTab.PRIVACY },
                    colors = navColors,
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == HomeTab.PRIVACY) {
                                Icons.Rounded.Shield
                            } else {
                                Icons.Outlined.Shield
                            },
                            contentDescription = stringResource(R.string.cd_nav_privacy),
                        )
                    },
                    label = { Text(stringResource(R.string.nav_privacy)) },
                    modifier = Modifier.testTag("nav_tab_privacy"),
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.ACCOUNT,
                    onClick = { selectedTab = HomeTab.ACCOUNT },
                    colors = navColors,
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == HomeTab.ACCOUNT) {
                                Icons.Rounded.Person
                            } else {
                                Icons.Outlined.PersonOutline
                            },
                            contentDescription = stringResource(R.string.cd_nav_account),
                        )
                    },
                    label = { Text(stringResource(R.string.nav_account)) },
                    modifier = Modifier.testTag("nav_tab_account"),
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            when (selectedTab) {
                HomeTab.MAP -> {
                    PublicMapScreen(
                        viewModel = mapViewModel,
                        onOpenIncidentDetail = { id -> selectedDetailIncidentId = id },
                        onOpenReport = { isReportFlowOpen = true },
                    )
                }
                HomeTab.PROJECT -> {
                    FoundationShellScreen(
                        pendingOfflineCount = pendingSyncCount,
                        onOpenMap = { selectedTab = HomeTab.MAP },
                        onOpenReport = { isReportFlowOpen = true },
                        onOpenOfflineQueue = { isOfflineQueueOpen = true },
                        onOpenPrivacy = { selectedTab = HomeTab.PRIVACY },
                        onJumpToNeighborhood = { preset ->
                            mapViewModel.focusNeighborhood(
                                name = preset.name,
                                coordinate = preset.coordinate,
                                zoom = preset.zoom,
                            )
                            selectedTab = HomeTab.MAP
                        },
                    )
                }
                HomeTab.PRIVACY -> {
                    PrivacyAndMediaSafetyScreen()
                }
                HomeTab.ACCOUNT -> {
                    val user = currentUser
                    if (user == null) {
                        DirectCitizenAccountScreen(
                            citizenSessionStore = citizenSessionStore,
                            incidentRepository = incidentRepository,
                            pendingOfflineCount = pendingSyncCount,
                            onOpenOfflineQueue = { isOfflineQueueOpen = true },
                            onOpenIncidentDetail = { id -> selectedDetailIncidentId = id },
                        )
                    } else {
                        val uid = user.uid
                        key(uid) {
                            val profileViewModel: ProfileViewModel = viewModel(
                                key = uid,
                                factory = viewModelFactory {
                                    initializer {
                                        val app = checkNotNull(this[APPLICATION_KEY]) {
                                            "APPLICATION_KEY ausente em CreationExtras"
                                        }
                                        val databaseId = app.getString(R.string.firestore_database_id)
                                        val db = FirebaseFirestore.getInstance(databaseId)
                                        ProfileViewModel(
                                            profileRepository = ProfileRepository(db),
                                            authRepository = authRepository,
                                            currentUserId = uid,
                                            incidentRepository = IncidentRepository(db),
                                        )
                                    }
                                },
                            )
                            ProfileScreen(
                                viewModel = profileViewModel,
                                fallbackEmail = user.email,
                                fallbackPhotoUrl = user.photoUrl?.toString(),
                                adminInfo = adminInfo,
                                onOpenAdminPanel = { isAdminDashboardOpen = true },
                                pendingOfflineCount = pendingSyncCount,
                                onOpenOfflineQueue = { isOfflineQueueOpen = true },
                                onOpenIncidentDetail = { id -> selectedDetailIncidentId = id },
                            )
                        }
                    }
                }
            }
        }
    }
}
