package com.schemewise.app.ui.navigation

/**
 * All route strings in one place.
 * Mirrors the web app's React Router paths.
 *
 * Web route  →  Android route constant
 * /           →  Screen.Splash
 * /login      →  Screen.Login
 * /register   →  Screen.Register
 * ...etc
 */
sealed class Screen(val route: String) {

    // ── Auth / Onboarding ─────────────────────────────────────────────
    data object Splash          : Screen("splash")
    data object Login           : Screen("login")
    data object Register        : Screen("register")
    data object ForgotPassword  : Screen("forgot_password")
    data object ResetPassword   : Screen("reset_password/{token}") {
        fun withToken(token: String) = "reset_password/$token"
    }

    // ── Onboarding Flow ───────────────────────────────────────────────
    data object Welcome             : Screen("welcome")
    data object DetailsFilling      : Screen("onboarding/details")
    data object EligibilityResults  : Screen("onboarding/results")
    data object JourneyIntro        : Screen("onboarding/journey")

    // ── Main App (Bottom Nav) ─────────────────────────────────────────
    data object Home     : Screen("home")
    data object Explore  : Screen("explore")
    data object Simulator: Screen("simulator")
    data object EligibleSchemes: Screen("eligible")
    data object Saved    : Screen("saved")
    data object Bot      : Screen("bot")

    // ── Secondary Screens (pushed onto backstack) ──────────────────────
    data object SchemeDetail   : Screen("scheme/{schemeId}") {
        fun withId(id: String) = "scheme/$id"
    }
    data object SchemeAIDetails : Screen("scheme/{schemeId}/ai-details") {
        fun withId(id: String) = "scheme/$id/ai-details"
    }
    data object SchemeResults  : Screen("results")
    data object Compare        : Screen("compare")
    data object Updates        : Screen("updates")
    data object Profile        : Screen("profile")
    data object Settings       : Screen("settings")
    data object EditProfile    : Screen("profile/edit")
    data object CivicReadiness      : Screen("civic")
    data object ComboBenefits       : Screen("combo")
    data object CentersMap          : Screen("centers")
    data object BotWithContext  : Screen("bot_context?query={query}") {
        fun withQuery(q: String) = "bot_context?query=${java.net.URLEncoder.encode(q, "UTF-8")}"
    }
    data object YojanaCard      : Screen("yojana_card")
}
