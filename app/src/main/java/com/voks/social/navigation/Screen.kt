package com.voks.social.navigation

sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Verification : Screen("verification")
    data object ForgotPassword : Screen("forgot_password")
    data object Home : Screen("home")
    data object CreatePost : Screen("create_post")

    // NUEVO FASE 11: Ruta dinámica para perfiles de otros usuarios
    data object Profile : Screen("profile_screen?userId={userId}") {
        fun createRoute(userId: String? = null): String {
            return if (userId != null) "profile_screen?userId=$userId" else "profile_screen"
        }
    }
}