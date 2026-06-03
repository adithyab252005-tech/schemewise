package com.schemewise.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import android.graphics.Rect
import android.view.ViewTreeObserver
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.schemewise.app.ui.components.LocalDrawerAction
import com.schemewise.app.ui.screens.auth.ForgotPasswordScreen
import com.schemewise.app.ui.screens.auth.ResetPasswordScreen
import com.schemewise.app.ui.screens.auth.LoginScreen
import com.schemewise.app.ui.screens.auth.RegisterScreen
import com.schemewise.app.ui.screens.bot.BotScreen
import com.schemewise.app.ui.screens.civic.CivicReadinessScreen
import com.schemewise.app.ui.screens.explore.ExploreScreen
import com.schemewise.app.ui.screens.home.HomeScreen
import com.schemewise.app.ui.screens.onboarding.DetailsFillingScreen
import com.schemewise.app.ui.screens.onboarding.EligibilityResultsScreen
import com.schemewise.app.ui.screens.onboarding.JourneyIntroScreen
import com.schemewise.app.ui.screens.onboarding.WelcomeScreen
import com.schemewise.app.ui.screens.profile.ProfileScreen
import com.schemewise.app.ui.screens.profile.EditProfileScreen
import com.schemewise.app.ui.screens.household.ComboBenefitsScreen
import com.schemewise.app.ui.screens.profile.card.YojanaCardScreen
import com.schemewise.app.ui.screens.settings.SettingsScreen
import com.schemewise.app.ui.screens.centers.CentersMapScreen
import com.schemewise.app.ui.theme.*
import com.schemewise.app.ui.screens.saved.SavedScreen
import com.schemewise.app.ui.screens.scheme.CompareScreen
import com.schemewise.app.ui.screens.scheme.SchemeAIDetailsScreen
import com.schemewise.app.ui.screens.scheme.SchemeDetailScreen
import com.schemewise.app.ui.screens.scheme.SchemeResultsScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.schemewise.app.ui.screens.simulator.SimulatorScreen
import com.schemewise.app.ui.screens.simulator.SimulatorViewModel
import com.schemewise.app.ui.screens.splash.SplashScreen
import com.schemewise.app.ui.screens.updates.UpdatesScreen

/** Screens that show the bottom navigation bar */
private val bottomNavScreens = setOf(
    Screen.Home.route,
    Screen.Explore.route,
    Screen.Simulator.route,
    Screen.Bot.route,
    Screen.Saved.route,
)

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute   = backStackEntry?.destination?.route

    val view = LocalView.current
    var isKeyboardOpen by remember { mutableStateOf(false) }

    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            isKeyboardOpen = keypadHeight > screenHeight * 0.15
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    val showBottomBar = currentRoute in bottomNavScreens && !isKeyboardOpen

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    CompositionLocalProvider(
        LocalDrawerAction provides { scope.launch { drawerState.open() } }
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = showBottomBar, // Only enable swipe-to-open on main tabs
            drawerContent = {
                AppDrawerContent(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        scope.launch { drawerState.close() }
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    },
                    onLogout = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        ) {
            Scaffold(
                bottomBar = {
                    if (showBottomBar) {
                        BottomNavBar(
                            currentRoute = currentRoute,
                            onNavigate   = { screen ->
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            }
                        )
                    }
                }
            ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Splash.route,
            modifier         = Modifier.padding(innerPadding),
            enterTransition  = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(300)) + androidx.compose.animation.scaleIn(
                    initialScale = 0.95f,
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                )
            },
            exitTransition   = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeOut(tween(300)) + androidx.compose.animation.scaleOut(
                    targetScale = 1.05f,
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                )
            },
            popEnterTransition  = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(300)) + androidx.compose.animation.scaleIn(
                    initialScale = 1.05f,
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                )
            },
            popExitTransition   = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeOut(tween(300)) + androidx.compose.animation.scaleOut(
                    targetScale = 0.95f,
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                )
            },
        ) {
            // ── Auth / Splash ──────────────────────────────────────────────
            composable(Screen.Splash.route) {
                SplashScreen(onNavigateToLogin = { navController.navigate(Screen.Login.route) })
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess  = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Splash.route) { inclusive = true } } },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onForgotPassword     = { navController.navigate(Screen.ForgotPassword.route) }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = { navController.navigate(Screen.Welcome.route) },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }
            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onBack = { navController.popBackStack() },
                    onResetTokenReceived = { token ->
                        navController.navigate(Screen.ResetPassword.withToken(token))
                    }
                )
            }
            composable(
                Screen.ResetPassword.route,
                deepLinks = listOf(
                    navDeepLink { uriPattern = "http://180.235.121.245:5173/reset-password?token={token}" },
                    navDeepLink { uriPattern = "http://localhost:5173/reset-password?token={token}" }
                )
            ) { backStack ->
                val token = backStack.arguments?.getString("token") ?: ""
                ResetPasswordScreen(
                    token = token,
                    onLoginSuccess = { _ ->
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Onboarding Flow ────────────────────────────────────────────
            composable(Screen.Welcome.route) {
                WelcomeScreen(onContinue = { navController.navigate(Screen.DetailsFilling.route) })
            }
            composable(Screen.DetailsFilling.route) {
                DetailsFillingScreen(onSubmit = { navController.navigate(Screen.EligibilityResults.route) })
            }
            composable(Screen.EligibilityResults.route) {
                EligibilityResultsScreen(onContinue = { navController.navigate(Screen.JourneyIntro.route) }, onSchemeClick = { id -> navController.navigate(Screen.SchemeDetail.withId(id)) })
            }
            composable(Screen.JourneyIntro.route) {
                JourneyIntroScreen(onEnterApp = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                })
            }

            // ── Main App Tabs ──────────────────────────────────────────────
            composable(Screen.Home.route) {
                HomeScreen(
                    onSchemeClick    = { id -> navController.navigate(Screen.SchemeDetail.withId(id)) },
                    onExploreClick   = { navController.navigate(Screen.Explore.route) },
                    onSavedClick     = { navController.navigate(Screen.Saved.route) },
                    onUpdatesClick   = { navController.navigate(Screen.Updates.route) },
                    onSimulatorClick = { navController.navigate(Screen.Simulator.route) },
                    onRecheckClick   = { navController.navigate(Screen.EligibilityResults.route) },
                    onSettingsClick  = { navController.navigate(Screen.Settings.route) },
                    onProfileClick   = { navController.navigate(Screen.Profile.route) },
                    onCentersMapClick = { navController.navigate(Screen.CentersMap.route) },
                    onCompareClick    = { navController.navigate(Screen.Compare.route) }
                )
            }
            composable(Screen.Explore.route) {
                ExploreScreen(
                    onSchemeClick   = { id -> navController.navigate(Screen.SchemeDetail.withId(id)) },
                    onSettingsClick = { navController.navigate(Screen.Settings.route) },
                    onProfileClick  = { navController.navigate(Screen.Profile.route) },
                )
            }
            composable(Screen.EligibleSchemes.route) {
                EligibilityResultsScreen(onContinue = {}, showContinueButton = false, onSchemeClick = { id -> navController.navigate(Screen.SchemeDetail.withId(id)) })
            }
            // ── Simulator Graph (nested so SimulatorViewModel is shared) ────
            navigation(startDestination = Screen.Simulator.route, route = "simulator_graph") {
                composable(Screen.Simulator.route) { entry ->
                    val parentEntry = remember(entry) {
                        navController.getBackStackEntry("simulator_graph")
                    }
                    SimulatorScreen(
                        viewModel       = hiltViewModel(parentEntry),
                        onResultsReady  = { navController.navigate(Screen.SchemeResults.route) },
                        onSettingsClick = { navController.navigate(Screen.Settings.route) },
                        onProfileClick  = { navController.navigate(Screen.Profile.route) }
                    )
                }
                composable(Screen.SchemeResults.route) { entry ->
                    val parentEntry = remember(entry) {
                        navController.getBackStackEntry("simulator_graph")
                    }
                    SchemeResultsScreen(
                        viewModel     = hiltViewModel(parentEntry),
                        onSchemeClick = { id -> navController.navigate(Screen.SchemeDetail.withId(id)) },
                        onBack        = { navController.popBackStack() }
                    )
                }
            }
            composable(Screen.Saved.route) {
                SavedScreen(
                    onSchemeClick   = { id -> navController.navigate(Screen.SchemeDetail.withId(id)) },
                    onSettingsClick = { navController.navigate(Screen.Settings.route) },
                    onProfileClick  = { navController.navigate(Screen.Profile.route) },
                )
            }
            composable(Screen.Bot.route) {
                BotScreen(
                    onSettingsClick = { navController.navigate(Screen.Settings.route) },
                    onProfileClick  = { navController.navigate(Screen.Profile.route) }
                )
            }

            // ── Secondary Screens ──────────────────────────────────────────
            composable(Screen.SchemeDetail.route) { backStack ->
                val schemeId = backStack.arguments?.getString("schemeId") ?: ""
                SchemeDetailScreen(
                    schemeId  = schemeId,
                    onBack    = { navController.popBackStack() },
                    onAskAi   = { prompt ->
                        navController.navigate(Screen.BotWithContext.withQuery(prompt))
                    },
                )
            }
            // SchemeResults is now part of the simulator_graph nested nav above
            composable(Screen.Compare.route) {
                CompareScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.BotWithContext.route) { backStack ->
                val rawQuery = backStack.arguments?.getString("query") ?: ""
                val query = try { java.net.URLDecoder.decode(rawQuery, "UTF-8") } catch (e: Exception) { rawQuery }
                BotScreen(
                    initialQuery = query,
                    onSettingsClick = { navController.navigate(Screen.Settings.route) },
                    onProfileClick  = { navController.navigate(Screen.Profile.route) }
                )
            }
            composable(Screen.Updates.route) {
                UpdatesScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onBack              = { navController.popBackStack() },
                    onEditClick         = { navController.navigate(Screen.EditProfile.route) },
                    onComboBenefits     = { navController.navigate(Screen.ComboBenefits.route) },
                    onCivicReadiness    = { navController.navigate(Screen.CivicReadiness.route) },
                )
            }
            composable(Screen.EditProfile.route) {
                EditProfileScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack       = { navController.popBackStack() },
                    onViewProfile = { navController.navigate(Screen.Profile.route) },
                    onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // ── New Feature Screens ────────────────────────────────────────
            composable(Screen.CivicReadiness.route) {
                CivicReadinessScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.ComboBenefits.route) {
                ComboBenefitsScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Screen.SchemeAIDetails.route) { backStack ->
                val schemeId = backStack.arguments?.getString("schemeId") ?: ""
                SchemeAIDetailsScreen(
                    schemeId = schemeId,
                    onBack   = { navController.popBackStack() }
                )
            }
            composable(Screen.CentersMap.route) {
                CentersMapScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.YojanaCard.route) {
                YojanaCardScreen(onBack = { navController.popBackStack() })
            }
        }
        }
    }
    }
}

@Composable
private fun AppDrawerContent(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = Color.Transparent,
        modifier = Modifier.width(288.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(NavyDark, NavyMid, Color(0xFF0D1B35)))
                )
        ) {
            // Ambient orange orb — top-left decoration
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .offset((-60).dp, (-40).dp)
                    .background(
                        Brush.radialGradient(
                            listOf(Brand500.copy(0.15f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 0.dp)
            ) {
                // ── Header: avatar + user info ────────────────────────────
                // Tricolor accent strip
                Row(modifier = Modifier.fillMaxWidth().height(3.dp)) {
                    Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFFF97316)))
                    Box(Modifier.weight(1f).fillMaxHeight().background(Color.White.copy(0.25f)))
                    Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFF22C55E)))
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    // App logo row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(Brush.linearGradient(GradOrangeGold)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("SW", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                        Column {
                            Text(
                                "SchemeWise",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                            )
                            Text(
                                "NIC PORTAL",
                                color = Color.White.copy(0.4f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                letterSpacing = 1.5.sp,
                            )
                        }
                    }
                }

                Divider(
                    color = Color.White.copy(0.08f),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(8.dp))

                // ── Navigation Sections ───────────────────────────────────
                @Composable
                fun SectionLabel(text: String) {
                    Text(
                        text = text,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(0.3f),
                        letterSpacing = 1.8.sp,
                        modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 4.dp)
                    )
                }

                @Composable
                fun DrawerItem(
                    label: String,
                    icon: ImageVector,
                    route: String,
                    badge: String? = null
                ) {
                    val selected = currentRoute == route
                    val bgColor  = if (selected) Brand500.copy(0.15f) else Color.Transparent
                    val iconTint = if (selected) Brand500 else Color.White.copy(0.55f)
                    val textCol  = if (selected) Color.White else Color.White.copy(0.65f)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(bgColor)
                            .clickable { onNavigate(route) }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Icon with optional glow box
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(
                                    if (selected)
                                        Brush.linearGradient(listOf(Brand500.copy(0.3f), Brand600.copy(0.15f)))
                                    else
                                        Brush.linearGradient(listOf(Color.White.copy(0.06f), Color.White.copy(0.03f)))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, null, tint = iconTint, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            label,
                            color      = textCol,
                            fontSize   = 14.sp,
                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                            modifier   = Modifier.weight(1f)
                        )
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(20.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Brush.verticalGradient(GradOrangeGold))
                            )
                        }
                        if (badge != null) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Brand500,
                            ) {
                                Text(badge, fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }

                SectionLabel("MAIN")
                DrawerItem("Dashboard",     Icons.Filled.Home,         Screen.Home.route)
                DrawerItem("Schemes",       Icons.Filled.Search,       Screen.Explore.route)
                DrawerItem("My Eligibility",Icons.Filled.CheckCircle,  Screen.EligibleSchemes.route)
                DrawerItem("Saved Schemes", Icons.Filled.Bookmark,     Screen.Saved.route)
                DrawerItem("Civic Readiness",Icons.Filled.VerifiedUser,Screen.CivicReadiness.route)

                SectionLabel("TOOLS")
                DrawerItem("Simulator",     Icons.Filled.Analytics,    Screen.Simulator.route)
                DrawerItem("Centers Map",   Icons.Filled.LocationOn,   Screen.CentersMap.route)
                DrawerItem("Yojana Card",   Icons.Filled.QrCode,       Screen.YojanaCard.route)
                DrawerItem("Updates",       Icons.Filled.Notifications, Screen.Updates.route)
                DrawerItem("Settings",      Icons.Filled.Settings,     Screen.Settings.route)

                Spacer(modifier = Modifier.height(32.dp))

                Divider(color = Color.White.copy(0.08f), modifier = Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(4.dp))

                DrawerItem("Profile",       Icons.Filled.Person,       Screen.Profile.route)

                // Logout row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onLogout() }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(Color(0xFFEF4444).copy(0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.ExitToApp, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    }
                    Text("Sign Out", color = Color(0xFFEF4444), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
