package com.voks.social.navigation

sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Verification : Screen("verification")
    data object ForgotPassword : Screen("forgot_password")
    data object Home : Screen("home")

    // NUEVA RUTA PARA LA FASE 7: Pantalla para redactar una publicación
    data object CreatePost : Screen("create_post")
    data object Profile : Screen("profile_screen") // NUEVO FASE 10
}